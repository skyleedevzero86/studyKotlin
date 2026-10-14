"use client";

type PasteFormProps = {
  onSubmit: (url: string) => void;
  busy: boolean;
};

export function PasteForm({ onSubmit, busy }: PasteFormProps) {
  return (
    <form
      className="paste"
      onSubmit={(event) => {
        event.preventDefault();
        const data = new FormData(event.currentTarget);
        onSubmit(String(data.get("url") ?? ""));
      }}
    >
      <label htmlFor="url">유튜브 주소</label>
      <input id="url" name="url" placeholder="공유 주소를 붙여 넣으세요" autoComplete="off" />
      <button type="submit" disabled={busy}>
        저장 요청
      </button>
    </form>
  );
}
