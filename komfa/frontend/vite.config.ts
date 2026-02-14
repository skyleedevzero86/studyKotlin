import { defineConfig } from 'vite'
import { svelte } from '@sveltejs/vite-plugin-svelte'

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
          if (req.method !== 'POST') return '/index.html'
        },
      },
      '/logout': { target: 'http://localhost:8080', changeOrigin: true },
      '/ott': { target: 'http://localhost:8080', changeOrigin: true },
      '/user': {
        target: 'http://localhost:8080',
        changeOrigin: true,
        configure(proxy) {
          proxy.on('proxyRes', (proxyRes) => {
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
