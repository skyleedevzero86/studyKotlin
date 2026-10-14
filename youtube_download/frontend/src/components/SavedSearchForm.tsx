"use client";

type SavedSearchFormProps = {
  onSubmit: (query: string) => void;
  busy: boolean;
};

export function SavedSearchForm({ onSubmit, busy }: SavedSearchFormProps) {
  return (
    <form
      className="paste"
      onSubmit={(event) => {
        event.preventDefault();
        const data = new FormData(event.currentTarget);
        onSubmit(String(data.get("q") ?? ""));
      }}
    >
      <label htmlFor="saved-query">저장된 영상</label>
      <input id="saved-query" name="q" placeholder="제목, 파일명, 영상 ID" autoComplete="off" />
      <button type="submit" disabled={busy}>
        검색
      </button>
    </form>
  );
}
