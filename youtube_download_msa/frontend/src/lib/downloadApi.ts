import type { DownloadTicket, ListedDownload, ListedPage, ReactionKind, SimilarPage } from "@/lib/downloads";

type ErrorBody = { message?: string };

async function readError(response: Response): Promise<string> {
  try {
    const body = (await response.json()) as ErrorBody;
    if (body.message) return body.message;
  } catch {
    return "요청을 처리하지 못했습니다.";
  }
  return "요청을 처리하지 못했습니다.";
}

export async function pasteDownload(url: string): Promise<DownloadTicket> {
  const response = await fetch("/api/downloads", {
    method: "POST",
    headers: { "Content-Type": "application/json" },
    body: JSON.stringify({ url }),
  });
  if (!response.ok) throw new Error(await readError(response));
  return (await response.json()) as DownloadTicket;
}

export async function fetchDownloads(page: number, size: number): Promise<ListedPage> {
  const response = await fetch(`/api/downloads?page=${page}&size=${size}`);
  if (!response.ok) throw new Error(await readError(response));
  return (await response.json()) as ListedPage;
}

export async function searchSimilar(query: string, k: number): Promise<SimilarPage> {
  const response = await fetch("/api/downloads/search", {
    method: "POST",
    headers: { "Content-Type": "application/json" },
    body: JSON.stringify({ query, k }),
  });
  if (!response.ok) throw new Error(await readError(response));
  return (await response.json()) as SimilarPage;
}

export async function searchSaved(q: string, page: number, size: number): Promise<ListedPage> {
  const params = new URLSearchParams({ q, page: String(page), size: String(size) });
  const response = await fetch(`/api/downloads/saved?${params}`);
  if (!response.ok) throw new Error(await readError(response));
  return (await response.json()) as ListedPage;
}

export async function fetchRecommendations(): Promise<SimilarPage> {
  const response = await fetch("/api/downloads/recommendations");
  if (!response.ok) throw new Error(await readError(response));
  return (await response.json()) as SimilarPage;
}

export async function reactToDownload(id: number, kind: ReactionKind): Promise<ListedDownload> {
  const response = await fetch(`/api/downloads/${id}/${kind}`, { method: "POST" });
  if (!response.ok) throw new Error(await readError(response));
  return (await response.json()) as ListedDownload;
}
