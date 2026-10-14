export type DownloadTicket = {
  id: number;
  videoId: string;
  status: string;
};

export type ListedDownload = {
  id: number;
  videoId: string;
  title: string | null;
  status: string;
  fileName: string | null;
  sizeBytes: number | null;
  errorMessage: string | null;
  updatedAt: string;
  fileUrl: string | null;
  likeCount: number;
  dislikeCount: number;
  viewCount: number;
};

export type ListedPage = {
  page: number;
  size: number;
  total: number;
  items: ListedDownload[];
};

export type SimilarItem = {
  item: ListedDownload;
  esScore: number;
  cosine: number;
};

export type SimilarPage = {
  dims: number;
  similarity: string;
  k: number;
  items: SimilarItem[];
};

export function statusLabel(status: string): string {
  if (status === "REQUESTED") return "요청";
  if (status === "SAVING") return "저장 중";
  if (status === "SAVED") return "저장됨";
  if (status === "FAILED") return "실패";
  return status;
}

export function isPending(status: string): boolean {
  return status === "REQUESTED" || status === "SAVING";
}

export function formatWhen(value: string): string {
  const parsed = new Date(value);
  if (Number.isNaN(parsed.getTime())) return value;
  return parsed.toLocaleString("ko-KR");
}

export type ReactionKind = "likes" | "dislikes" | "views";

export function replaceListed(page: ListedPage | null, next: ListedDownload): ListedPage | null {
  if (!page) return page;
  return { ...page, items: page.items.map((item) => (item.id === next.id ? next : item)) };
}

export function replaceSimilar(page: SimilarPage | null, next: ListedDownload): SimilarPage | null {
  if (!page) return page;
  return {
    ...page,
    items: page.items.map((hit) => (hit.item.id === next.id ? { ...hit, item: next } : hit)),
  };
}
