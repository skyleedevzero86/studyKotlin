import type { ListedDownload } from "@/lib/listedDownload";
import type { ListedPage } from "@/lib/listedPage";
import type { SimilarPage } from "@/lib/similarPage";

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
