"use client";

import { useState } from "react";
import { SearchBlogUseCase } from "../../application/SearchBlogUseCase";
import { BlogSearchApi } from "../../infrastructure/api/blogSearchApi";
import { fold } from "../../domain/Result";
import { toMessage } from "../../domain/errors";
import type { SearchResult } from "../../domain/types";
import { SearchForm } from "../components/SearchForm";

const api = new BlogSearchApi("");
const searchBlogUseCase = new SearchBlogUseCase(api);

export function SearchPage() {
    const [results, setResults] = useState<SearchResult[]>([]);
    const [loading, setLoading] = useState(false);
    const [error, setError] = useState<string | null>(null);
    const [hasSearched, setHasSearched] = useState(false);

    async function handleSearch(query: string) {
        if (!query.trim()) return;
        setLoading(true);
        setError(null);
        setHasSearched(true);
        const result = await searchBlogUseCase.execute(query, 5);
        fold(
            result,
            (value) => {
                setResults(value);
                setError(null);
            },
            (e) => {
                setResults([]);
                setError(toMessage(e));
            }
        );
        setLoading(false);
    }

    return (
        <main className="container">
            <header className="header">
                <h1>기술 블로그 시맨틱 검색</h1>
                <p className="subtitle">
                    키워드가 아닌 의미 기반으로 관련 포스팅을 찾아요.
                </p>
            </header>

            <SearchForm onSubmit={handleSearch} loading={loading} />

            {error && <p className="error">{error}</p>}

            {results.length > 0 && (
                <section className="results">
                    <h2 className="results-title">관련 포스팅</h2>
                    <ul className="result-list">
                        {results.map((r) => (
                            <li key={r.id} className="result-item">
                                <a
                                    href={r.url}
                                    target="_blank"
                                    rel="noopener noreferrer"
                                    className="result-link"
                                >
                                    {r.title || r.url}
                                </a>
                                {r.summary && (
                                    <p className="result-summary">{r.summary}</p>
                                )}
                            </li>
                        ))}
                    </ul>
                </section>
            )}

            {hasSearched && !loading && results.length === 0 && !error && (
                <p className="muted">
                    검색 결과가 없어요. 블로그 URL을 먼저 수집해 주세요.
                </p>
            )}

            <footer className="footer">
                <p>
                    블로그 수집: <code>POST /api/ingest/url</code> 또는{" "}
                    <code>POST /api/ingest/feed</code>
                </p>
            </footer>
        </main>
    );
}
