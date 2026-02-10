import type { Result } from "../../domain/Result";
import type { SearchResult } from "../../domain/types";

export interface BlogSearchApiPort {
    search(query: string, topK?: number): Promise<Result<SearchResult[]>>;
}
