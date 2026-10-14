"use client";

import { useCallback, useEffect, useState } from "react";
import { DownloadList } from "@/components/DownloadList";
import { Pager } from "@/components/Pager";
import { PasteForm } from "@/components/PasteForm";
import { SavedSearchForm } from "@/components/SavedSearchForm";
import { SimilarForm } from "@/components/SimilarForm";
import { SimilarList } from "@/components/SimilarList";
import {
  fetchDownloads,
  fetchRecommendations,
  pasteDownload,
  reactToDownload,
  searchSaved,
  searchSimilar,
} from "@/lib/downloadApi";
import {
  isPending,
  replaceListed,
  replaceSimilar,
  type ListedDownload,
  type ListedPage,
  type ReactionKind,
  type SimilarPage,
} from "@/lib/downloads";

const pageSize = 10;

export function DownloadScreen() {
  const [page, setPage] = useState(0);
  const [listed, setListed] = useState<ListedPage | null>(null);
  const [message, setMessage] = useState("");
  const [busy, setBusy] = useState(false);
  const [similar, setSimilar] = useState<SimilarPage | null>(null);
  const [searched, setSearched] = useState(false);
  const [searchBusy, setSearchBusy] = useState(false);
  const [savedQuery, setSavedQuery] = useState("");
  const [savedPage, setSavedPage] = useState(0);
  const [saved, setSaved] = useState<ListedPage | null>(null);
  const [savedBusy, setSavedBusy] = useState(false);
  const [recommended, setRecommended] = useState<SimilarPage | null>(null);

  const load = useCallback(async (nextPage: number) => {
    const body = await fetchDownloads(nextPage, pageSize);
    setListed(body);
    return body;
  }, []);

  useEffect(() => {
    void load(page).catch((error: unknown) => {
      setMessage(error instanceof Error ? error.message : "목록을 읽지 못했습니다.");
    });
  }, [load, page]);

  useEffect(() => {
    if (!listed?.items.some((item) => isPending(item.status))) return;
    const timer = window.setTimeout(() => {
      void load(page).catch(() => undefined);
    }, 3000);
    return () => window.clearTimeout(timer);
  }, [listed, load, page]);

  const loadRecommendations = useCallback(async () => {
    const body = await fetchRecommendations();
    setRecommended(body);
    return body;
  }, []);

  useEffect(() => {
    void loadRecommendations().catch((error: unknown) => {
      setMessage(error instanceof Error ? error.message : "추천을 읽지 못했습니다.");
    });
  }, [loadRecommendations]);

  async function submit(url: string) {
    setBusy(true);
    setMessage("");
    try {
      const ticket = await pasteDownload(url);
      setMessage(`저장을 요청했습니다. 번호 ${ticket.id}`);
      setPage(0);
      await load(0);
    } catch (error: unknown) {
      setMessage(error instanceof Error ? error.message : "저장 요청에 실패했습니다.");
    } finally {
      setBusy(false);
    }
  }

  async function findSimilar(query: string, k: number) {
    setSearchBusy(true);
    setMessage("");
    try {
      setSimilar(await searchSimilar(query, k));
      setSearched(true);
    } catch (error: unknown) {
      setMessage(error instanceof Error ? error.message : "비슷한 저장을 찾지 못했습니다.");
    } finally {
      setSearchBusy(false);
    }
  }

  async function findSaved(query: string, nextPage = 0) {
    setSavedBusy(true);
    setMessage("");
    try {
      setSavedQuery(query);
      setSavedPage(nextPage);
      setSaved(await searchSaved(query, nextPage, pageSize));
    } catch (error: unknown) {
      setMessage(error instanceof Error ? error.message : "저장본을 검색하지 못했습니다.");
    } finally {
      setSavedBusy(false);
    }
  }

  function remember(next: ListedDownload) {
    setListed((current) => replaceListed(current, next));
    setSaved((current) => replaceListed(current, next));
    setSimilar((current) => replaceSimilar(current, next));
    setRecommended((current) => replaceSimilar(current, next));
  }

  async function react(id: number, kind: ReactionKind) {
    setMessage("");
    try {
      remember(await reactToDownload(id, kind));
      await loadRecommendations();
    } catch (error: unknown) {
      setMessage(error instanceof Error ? error.message : "반응을 남기지 못했습니다.");
    }
  }

  return (
    <main>
      <h1>영상 저장</h1>
      <PasteForm onSubmit={(url) => void submit(url)} busy={busy} />
      {message ? <p className="message">{message}</p> : null}
      <DownloadList items={listed?.items ?? []} onReact={(id, kind) => void react(id, kind)} />
      <Pager page={page} size={pageSize} total={listed?.total ?? 0} onPage={setPage} />
      <h2>저장된 영상 검색</h2>
      <SavedSearchForm onSubmit={(query) => void findSaved(query)} busy={savedBusy} />
      {saved ? <DownloadList items={saved.items} onReact={(id, kind) => void react(id, kind)} /> : null}
      {saved ? (
        <Pager page={savedPage} size={pageSize} total={saved.total} onPage={(next) => void findSaved(savedQuery, next)} />
      ) : null}
      <h2>추천</h2>
      <SimilarList
        items={recommended?.items ?? []}
        searched
        empty="추천할 저장 영상이 없습니다."
        onReact={(id, kind) => void react(id, kind)}
      />
      <h2>비슷한 저장</h2>
      <SimilarForm onSubmit={(query, k) => void findSimilar(query, k)} busy={searchBusy} />
      <SimilarList items={similar?.items ?? []} searched={searched} onReact={(id, kind) => void react(id, kind)} />
    </main>
  );
}
