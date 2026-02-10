export type SearchResult = {
    id: string;
    title: string;
    url: string;
    summary: string;
    score: number | null;
};

export type SearchResponse = {
    results: SearchResult[];
    error?: string;
};
