"use client";

import { ReactionBar } from "@/components/ReactionBar";
import { formatWhen, statusLabel } from "@/lib/downloads";
import type { ReactionKind } from "@/lib/reactionKind";
import type { SimilarItem } from "@/lib/similarItem";

type SimilarListProps = {
  items: SimilarItem[];
  searched: boolean;
  empty?: string;
  onReact?: (id: number, kind: ReactionKind) => void;
};

export function SimilarList({ items, searched, empty = "비슷한 저장이 없습니다.", onReact }: SimilarListProps) {
  if (!searched) return null;
  if (items.length === 0) return <p className="empty">{empty}</p>;
  return (
    <ul className="list">
      {items.map((hit) => (
        <li key={hit.item.id}>
          <div>
            <strong>{hit.item.title || hit.item.videoId}</strong>
            <span>{statusLabel(hit.item.status)}</span>
            <span className="score">코사인 {hit.cosine.toFixed(4)}</span>
          </div>
          <p>{formatWhen(hit.item.updatedAt)}</p>
          {hit.item.fileUrl ? (
            <a
              href={hit.item.fileUrl}
              target="_blank"
              rel="noreferrer"
              onClick={() => onReact?.(hit.item.id, "views")}
            >
              MinIO에서 받기
            </a>
          ) : null}
          {onReact ? (
            <ReactionBar
              id={hit.item.id}
              status={hit.item.status}
              likeCount={hit.item.likeCount}
              dislikeCount={hit.item.dislikeCount}
              viewCount={hit.item.viewCount}
              onReact={onReact}
            />
          ) : null}
        </li>
      ))}
    </ul>
  );
}
