import { defineConfig } from 'vite'
import vue from '@vitejs/plugin-vue'

const backendTarget = 'http://localhost:8080'

const attachProxyErrorHandler = (proxy: {
  on: (event: 'error', listener: (error: Error, req: unknown, res: unknown) => void) => void
}) => {
  proxy.on('error', (_error, _req, res) => {
    if (!res || typeof res !== 'object' || !('writeHead' in res)) return
    const serverResponse = res as {
      writableEnded?: boolean
      writeHead: (status: number, headers: Record<string, string>) => void
      end: (body: string) => void
    }
    if (serverResponse.writableEnded) return
    serverResponse.writeHead(502, { 'Content-Type': 'application/json; charset=utf-8' })
    serverResponse.end(
      JSON.stringify({
        error: '백엔드 서버에 연결할 수 없습니다. backend(8080)를 실행한 뒤 다시 시도해 주세요.',
        code: 'BACKEND_UNAVAILABLE',
      }),
    )
  })
}

export default defineConfig({
  plugins: [vue()],
  server: {
    port: 3000,
    proxy: {
      '/api': {
        target: backendTarget,
        changeOrigin: true,
        ws: true,
        configure: attachProxyErrorHandler,
      },
      '/ws': {
        target: backendTarget,
        changeOrigin: true,
        ws: true,
        configure: attachProxyErrorHandler,
      },
      '/actuator': {
        target: backendTarget,
        changeOrigin: true,
        configure: attachProxyErrorHandler,
      },
      '/srs': {
        target: 'http://localhost:1985',
        changeOrigin: true,
        rewrite: (path) => path.replace(/^\/srs/, ''),
      },
    },
  },
})
