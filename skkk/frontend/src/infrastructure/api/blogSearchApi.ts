import type { BlogSearchApiPort } from "../../application/port/BlogSearchApiPort";
import type { SearchResult, SearchResponse } from "../../domain/types";
import { ok, err } from "../../domain/Result";
import type { AppError } from "../../domain/errors";

export class BlogSearchApi implements BlogSearchApiPort {
    constructor(private readonly baseUrl: string = "") { }

    async search(query: string, topK: number = 5): Promise<import("../../domain/Result").Result<SearchResult[], AppError>> {
        try {
            const url = `${this.baseUrl}/api/search?q=${encodeURIComponent(query)}&topK=${topK}`;
            const res = await fetch(url);
            if (!res.ok) {
                const text = await res.text();
                return err({
                    kind: "ApiError",
                    status: res.status,
                    message: text || "검색 요청 실패",
                });
            }
            const data: SearchResponse = await res.json();
            return ok(data.results ?? []);
        } catch (e) {
            return err({
                kind: "NetworkError",
                message: e instanceof Error ? e.message : "오류가 발생했습니다.",
            });
        }
    }
}
