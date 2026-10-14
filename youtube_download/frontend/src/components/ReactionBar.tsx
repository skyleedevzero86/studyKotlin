"use client";

import type { ReactionKind } from "@/lib/reactionKind";

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
