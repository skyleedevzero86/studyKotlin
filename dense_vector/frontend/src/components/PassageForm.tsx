import type { FormEvent } from 'react'
import { useState } from 'react'

type PassageFormProps = {
  heading: string
  submitLabel: string
  initialTitle: string
  initialBody: string
  onSubmit: (title: string, body: string) => Promise<void>
}

export function PassageForm({
  heading,
  submitLabel,
  initialTitle,
  initialBody,
  onSubmit,
}: PassageFormProps) {
  const [title, setTitle] = useState(initialTitle)
  const [body, setBody] = useState(initialBody)
  const [error, setError] = useState('')
  const [pending, setPending] = useState(false)

  async function handleSubmit(event: FormEvent) {
    event.preventDefault()
    setPending(true)
    setError('')
    try {
      await onSubmit(title, body)
    } catch (caught) {
      setError(caught instanceof Error ? caught.message : '요청을 처리하지 못했습니다.')
    } finally {
      setPending(false)
    }
  }

  return (
    <form className="panel" onSubmit={handleSubmit}>
      <h1>{heading}</h1>
      <label>
        제목
        <input value={title} maxLength={200} onChange={(event) => setTitle(event.target.value)} />
      </label>
      <label>
        본문
        <textarea value={body} rows={8} onChange={(event) => setBody(event.target.value)} />
      </label>
      {error ? <p className="error">{error}</p> : null}
      <button type="submit" disabled={pending}>
        {pending ? '저장 중' : submitLabel}
      </button>
    </form>
  )
}
