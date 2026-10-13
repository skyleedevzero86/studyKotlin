import { Link, NavLink, Outlet } from 'react-router-dom'

export function AppLayout() {
  return (
    <div className="app">
      <header>
        <Link to="/" className="brand">
          dense_vector
        </Link>
        <nav>
          <NavLink to="/" end>목록</NavLink>
          <NavLink to="/passages/new">등록</NavLink>
          <a href="http://127.0.0.1:8088/swagger-ui/index.html" target="_blank" rel="noreferrer">
            API 문서
          </a>
        </nav>
      </header>
      <main>
        <Outlet />
      </main>
    </div>
  )
}
