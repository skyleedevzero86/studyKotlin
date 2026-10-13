import { useNavigate } from 'react-router-dom'
import { createPassage } from '../api/passages'
import { PassageForm } from '../components/PassageForm'

export function PassageCreatePage() {
  const navigate = useNavigate()

  return (
    <PassageForm
      heading="문단 등록"
      submitLabel="등록"
      initialTitle=""
      initialBody=""
      onSubmit={async (title, body) => {
        const created = await createPassage(title, body)
        navigate(`/passages/${created.id}`)
      }}
    />
  )
}
