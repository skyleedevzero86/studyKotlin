import type { SimilarItem } from "@/lib/similarItem";

export type SimilarPage = {
  dims: number;
  similarity: string;
  k: number;
  items: SimilarItem[];
};
