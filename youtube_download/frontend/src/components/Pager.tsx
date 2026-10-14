"use client";

type PagerProps = {
  page: number;
  size: number;
  total: number;
  onPage: (page: number) => void;
};

export function Pager({ page, size, total, onPage }: PagerProps) {
  const pages = Math.max(1, Math.ceil(total / size));
  return (
    <nav className="pager">
      <button type="button" disabled={page <= 0} onClick={() => onPage(page - 1)}>
        이전
      </button>
      <span>
        {page + 1} / {pages} · {total}건
      </span>
      <button type="button" disabled={page + 1 >= pages} onClick={() => onPage(page + 1)}>
        다음
      </button>
    </nav>
  );
}
