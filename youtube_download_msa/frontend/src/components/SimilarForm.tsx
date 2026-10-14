"use client";

type SimilarFormProps = {
  onSubmit: (query: string, k: number) => void;
  busy: boolean;
};

export function SimilarForm({ onSubmit, busy }: SimilarFormProps) {
  return (
    <form
      className="paste"
      onSubmit={(event) => {
        event.preventDefault();
        const data = new FormData(event.currentTarget);
        const k = Number(data.get("k") ?? 5);
        onSubmit(String(data.get("query") ?? ""), Number.isFinite(k) ? k : 5);
      }}
    >
      <label htmlFor="query">비슷한 문장</label>
      <input id="query" name="query" placeholder="제목이나 영상 ID" autoComplete="off" />
      <input id="k" name="k" type="number" min={1} max={20} defaultValue={5} aria-label="개수" />
      <button type="submit" disabled={busy}>
        비슷한 저장 찾기
      </button>
    </form>
  );
}
