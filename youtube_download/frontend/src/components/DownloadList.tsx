"use client";

import { formatWhen, statusLabel, type ListedDownload, type ReactionKind } from "@/lib/downloads";
import { ReactionBar } from "@/components/SavedSearchForm";

type DownloadListProps = {
  items: ListedDownload[];
  onReact?: (id: number, kind: ReactionKind) => void;
};

export function DownloadList({ items, onReact }: DownloadListProps) {
  if (items.length === 0) {
    return <p className="empty">저장된 영상이 없습니다.</p>;
  }
  return (
    <ul className="list">
      {items.map((item) => (
        <li key={item.id}>
          <div>
            <strong>{item.title || item.videoId}</strong>
            <span>{statusLabel(item.status)}</span>
          </div>
          <p>{formatWhen(item.updatedAt)}</p>
          {item.errorMessage ? <p className="error">{item.errorMessage}</p> : null}
          {item.fileUrl ? (
            <a
              href={item.fileUrl}
              target="_blank"
              rel="noreferrer"
              onClick={() => onReact?.(item.id, "views")}
            >
              MinIO에서 받기
            </a>
          ) : null}
          {onReact ? (
            <ReactionBar
              id={item.id}
              status={item.status}
              likeCount={item.likeCount}
              dislikeCount={item.dislikeCount}
              viewCount={item.viewCount}
              onReact={onReact}
            />
          ) : null}
        </li>
      ))}
    </ul>
  );
}
