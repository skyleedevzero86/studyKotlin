import { Route, Routes } from 'react-router-dom'
import { AppLayout } from './components/AppLayout'
import { PassageCreatePage } from './pages/PassageCreatePage'
import { PassageDetailPage } from './pages/PassageDetailPage'
import { PassageEditPage } from './pages/PassageEditPage'
import { PassageListPage } from './pages/PassageListPage'

export default function App() {
  return (
    <Routes>
      <Route element={<AppLayout />}>
        <Route index element={<PassageListPage />} />
        <Route path="passages/new" element={<PassageCreatePage />} />
        <Route path="passages/:id" element={<PassageDetailPage />} />
        <Route path="passages/:id/edit" element={<PassageEditPage />} />
      </Route>
    </Routes>
  )
}
