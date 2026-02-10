"use client";

import { useState } from "react";

type SearchFormProps = {
    onSubmit: (query: string) => void;
    loading: boolean;
};

export function SearchForm({ onSubmit, loading }: SearchFormProps) {
    const [query, setQuery] = useState("");

    function handleSubmit(e: React.FormEvent) {
        e.preventDefault();
        onSubmit(query);
    }

    return (
        <form onSubmit={handleSubmit} className="search-form">
            <input
                type="search"
                value={query}
                onChange={(e) => setQuery(e.target.value)}
                placeholder='예: "Spring Security 설정 방법"'
                className="search-input"
                autoFocus
                disabled={loading}
            />
            <button
                type="submit"
                className="search-btn"
                disabled={loading}
            >
                {loading ? "검색 중…" : "검색"}
            </button>
        </form>
    );
}
