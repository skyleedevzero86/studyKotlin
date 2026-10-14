import type { ListedDownload } from "@/lib/listedDownload";

export type SimilarItem = {
  item: ListedDownload;
  esScore: number;
  cosine: number;
};
