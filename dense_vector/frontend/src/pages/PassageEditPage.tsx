import { useEffect, useState } from 'react'
import { useNavigate, useParams } from 'react-router-dom'
import { getPassage, updatePassage } from '../api/passages'
import type { PassageDetail } from '../api/passageDetail'
import { PassageForm } from '../components/PassageForm'

export function PassageEditPage() {
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

  if (error) return <p className="error">{error}</p>
  if (!passage) return <p>불러오는 중</p>

  return (
    <PassageForm
      key={passage.id}
      heading="문단 수정"
      submitLabel="수정"
      initialTitle={passage.title}
      initialBody={passage.body}
      onSubmit={async (title, body) => {
        const updated = await updatePassage(id, title, body)
        navigate(`/passages/${updated.id}`)
      }}
    />
  )
}
