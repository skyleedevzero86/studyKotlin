import { defineConfig } from 'vite'
import { svelte } from '@sveltejs/vite-plugin-svelte'

function rewriteSetCookie(proxyRes: import('http').IncomingMessage) {
  const setCookie = proxyRes.headers['set-cookie']
  if (!setCookie) return
  const arr = Array.isArray(setCookie) ? setCookie : [setCookie]
  proxyRes.headers['set-cookie'] = arr.map((c) =>
    c.replace(/;\s*Domain=[^;]+/gi, '').trim()
  )
}

export default defineConfig({
  plugins: [svelte()],
  server: {
    port: 5173,
    proxy: {
      '/api': {
        target: 'http://localhost:8080',
        changeOrigin: true,
        configure(proxy) {
          proxy.on('proxyRes', (proxyRes) => {
            rewriteSetCookie(proxyRes)
            if (proxyRes.statusCode === 302 && proxyRes.headers['location']?.includes('/login')) {
              proxyRes.statusCode = 401
              delete proxyRes.headers['location']
            }
          })
        },
      },
      '/login': {
        target: 'http://localhost:8080',
        changeOrigin: true,
        bypass(req) {
          if (req.method === 'POST') return undefined
          const u = (req.url ?? '').split('?')[0]
          if (u === '/login/ott') return undefined
          return '/index.html'
        },
        configure(proxy) {
          proxy.on('proxyRes', (proxyRes) => {
            rewriteSetCookie(proxyRes)
            const loc = proxyRes.headers['location']
            if (proxyRes.statusCode === 302 && loc) {
              try {
                const u = new URL(loc, 'http://localhost:8080')
                if (u.origin === 'http://localhost:8080')
                  proxyRes.headers['location'] = `http://localhost:5173${u.pathname}${u.search}`
              } catch (_) {}
            }
          })
        },
      },
      '/logout': {
        target: 'http://localhost:8080',
        changeOrigin: true,
        configure(proxy) {
          proxy.on('proxyRes', (proxyRes) => {
            rewriteSetCookie(proxyRes)
          })
        },
      },
      '/ott': {
        target: 'http://localhost:8080',
        changeOrigin: true,
        configure(proxy) {
          proxy.on('proxyRes', (proxyRes) => {
            rewriteSetCookie(proxyRes)
            const loc = proxyRes.headers['location']
            if (proxyRes.statusCode === 302 && loc?.includes('/login')) {
              try {
                const u = new URL(loc, 'http://localhost:8080')
                proxyRes.headers['location'] = `http://localhost:5173${u.pathname}${u.search}`
              } catch (_) {}
            }
          })
        },
      },
      '/user': {
        target: 'http://localhost:8080',
        changeOrigin: true,
        configure(proxy) {
          proxy.on('proxyRes', (proxyRes) => {
            rewriteSetCookie(proxyRes)
            const loc = proxyRes.headers['location']
            if (proxyRes.statusCode === 302 && loc?.includes('/login')) {
              try {
                const u = new URL(loc, 'http://localhost:8080')
                proxyRes.headers['location'] = `http://localhost:5173${u.pathname}${u.search}`
              } catch (_) {}
            }
          })
        },
      },
      '/admin': {
        target: 'http://localhost:8080',
        changeOrigin: true,
        configure(proxy) {
          proxy.on('proxyRes', (proxyRes) => {
            rewriteSetCookie(proxyRes)
            const loc = proxyRes.headers['location']
            if (proxyRes.statusCode === 302 && loc?.includes('/login')) {
              try {
                const u = new URL(loc, 'http://localhost:8080')
                proxyRes.headers['location'] = `http://localhost:5173${u.pathname}${u.search}`
              } catch (_) {}
            }
          })
        },
      },
    },
  },
})
