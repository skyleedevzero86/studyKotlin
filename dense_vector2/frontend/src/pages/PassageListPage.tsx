import { useEffect, useState } from 'react'
import { Link } from 'react-router-dom'
import type { PassagePage } from '../api/passagePage'
import { listPassages, searchPassages } from '../api/passages'
import type { SearchResult } from '../api/searchResult'

export function PassageListPage() {
  const [page, setPage] = useState(0)
  const [data, setData] = useState<PassagePage | null>(null)
  const [error, setError] = useState('')
  const [query, setQuery] = useState('')
  const [search, setSearch] = useState<SearchResult | null>(null)

  useEffect(() => {
    let ignore = false
    listPassages(page, 20)
      .then((result) => {
        if (!ignore) setData(result)
      })
      .catch((caught: unknown) => {
        if (!ignore) setError(caught instanceof Error ? caught.message : '목록을 불러오지 못했습니다.')
      })
    return () => {
      ignore = true
    }
  }, [page])

  async function runSearch() {
    setError('')
    try {
      setSearch(await searchPassages(query, 5))
    } catch (caught) {
      setError(caught instanceof Error ? caught.message : '검색에 실패했습니다.')
    }
  }

  return (
    <section>
      <div className="row">
        <h1>문단 목록</h1>
        <Link className="button" to="/passages/new">
          등록
        </Link>
      </div>
      <form
        className="search"
        onSubmit={(event) => {
          event.preventDefault()
          void runSearch()
        }}
      >
        <input value={query} placeholder="코사인 유사도" onChange={(event) => setQuery(event.target.value)} />
        <button type="submit">검색</button>
      </form>
      {search ? (
        <ul className="hits">
          {search.hits.map((hit) => (
            <li key={hit.id}>
              <Link to={`/passages/${hit.id}`}>{hit.title}</Link>
              <span>cosine {hit.cosine.toFixed(4)}</span>
              <span>esScore {hit.esScore.toFixed(4)}</span>
            </li>
          ))}
        </ul>
      ) : null}
      {error ? <p className="error">{error}</p> : null}
      <table>
        <thead>
          <tr>
            <th>id</th>
            <th>제목</th>
            <th>dims</th>
            <th></th>
          </tr>
        </thead>
        <tbody>
          {data?.items.map((item) => (
            <tr key={item.id}>
              <td>{item.id}</td>
              <td>{item.title}</td>
              <td>{item.dims}</td>
              <td>
                <Link to={`/passages/${item.id}`}>상세</Link>
              </td>
            </tr>
          ))}
        </tbody>
      </table>
      <div className="pager">
        <button type="button" disabled={page <= 0} onClick={() => setPage((current) => current - 1)}>
          이전
        </button>
        <span>
          {data ? `${data.page + 1} / ${data.totalPages} · ${data.totalElements}건` : '불러오는 중'}
        </span>
        <button
          type="button"
          disabled={!data || page + 1 >= data.totalPages}
          onClick={() => setPage((current) => current + 1)}
        >
          다음
        </button>
      </div>
    </section>
  )
}
