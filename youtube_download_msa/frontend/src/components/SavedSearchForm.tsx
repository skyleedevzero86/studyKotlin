"use client";

import type { ReactionKind } from "@/lib/downloads";

type SavedSearchFormProps = {
  onSubmit: (query: string) => void;
  busy: boolean;
};

export function SavedSearchForm({ onSubmit, busy }: SavedSearchFormProps) {
  return (
    <form
      className="paste"
      onSubmit={(event) => {
        event.preventDefault();
        const data = new FormData(event.currentTarget);
        onSubmit(String(data.get("q") ?? ""));
      }}
    >
      <label htmlFor="saved-query">저장된 영상</label>
      <input id="saved-query" name="q" placeholder="제목, 파일명, 영상 ID" autoComplete="off" />
      <button type="submit" disabled={busy}>
        검색
      </button>
    </form>
  );
}

type ReactionBarProps = {
  id: number;
  status: string;
  likeCount: number;
  dislikeCount: number;
  viewCount: number;
  onReact: (id: number, kind: ReactionKind) => void;
};

export function ReactionBar({ id, status, likeCount, dislikeCount, viewCount, onReact }: ReactionBarProps) {
  if (status !== "SAVED") return null;
  return (
    <div className="reactions">
      <button type="button" onClick={() => onReact(id, "likes")}>
        좋아요 {likeCount}
      </button>
      <button type="button" onClick={() => onReact(id, "dislikes")}>
        싫어요 {dislikeCount}
      </button>
      <button type="button" onClick={() => onReact(id, "views")}>
        조회 {viewCount}
      </button>
    </div>
  );
}
