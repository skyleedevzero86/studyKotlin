import type { ListedDownload } from "@/lib/listedDownload";

export type ListedPage = {
  page: number;
  size: number;
  total: number;
  items: ListedDownload[];
};
