import { useEffect, useState } from 'react'
import { Link, useNavigate, useParams } from 'react-router-dom'
import { deletePassage, getPassage } from '../api/passages'
import type { PassageDetail } from '../api/passageDetail'
import { EmbeddingView } from '../components/EmbeddingView'

export function PassageDetailPage() {
  const params = useParams()
  const navigate = useNavigate()
  const id = Number(params.id)
  const [passage, setPassage] = useState<PassageDetail | null>(null)
  const [error, setError] = useState('')

  useEffect(() => {
    let ignore = false
    getPassage(id)
      .then((result) => {
        if (!ignore) setPassage(result)
      })
      .catch((caught: unknown) => {
        if (!ignore) setError(caught instanceof Error ? caught.message : '문단을 불러오지 못했습니다.')
      })
    return () => {
      ignore = true
    }
  }, [id])

  async function remove() {
    if (!window.confirm('이 문단을 삭제할까요?')) return
    try {
      await deletePassage(id)
      navigate('/')
    } catch (caught) {
      setError(caught instanceof Error ? caught.message : '삭제에 실패했습니다.')
    }
  }

  if (error) return <p className="error">{error}</p>
  if (!passage) return <p>불러오는 중</p>

  return (
    <article className="panel">
      <h1>{passage.title}</h1>
      <p className="meta">id {passage.id} · dims {passage.dims}</p>
      <p className="body">{passage.body}</p>
      <h2>임베딩</h2>
      <EmbeddingView values={passage.embedding} />
      <div className="row">
        <Link className="button" to={`/passages/${passage.id}/edit`}>
          수정
        </Link>
        <button type="button" className="danger" onClick={() => void remove()}>
          삭제
        </button>
      </div>
    </article>
  )
}
