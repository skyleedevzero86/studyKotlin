import type { BlogSearchApiPort } from "./port/BlogSearchApiPort";
import type { Result } from "../domain/Result";
import type { SearchResult } from "../domain/types";
import { err } from "../domain/Result";
import type { AppError } from "../domain/errors";

export class SearchBlogUseCase {
    constructor(private readonly api: BlogSearchApiPort) { }

    async execute(query: string, topK: number = 5): Promise<Result<SearchResult[], AppError>> {
        const q = query.trim();
        if (!q) {
            return err({ kind: "ValidationError", message: "검색어를 입력하세요." });
        }
        const result = await this.api.search(q, topK);
        return result;
    }
}
