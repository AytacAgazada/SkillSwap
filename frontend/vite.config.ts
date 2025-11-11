import { defineConfig } from 'vite'
import react from '@vitejs/plugin-react-swc'

export default defineConfig({
  plugins: [react()],
  define: {
    global: 'globalThis',
  },
  server: {
    port: 5173,
    proxy: {
      '/api': {
        target: 'http://localhost:1120',
        changeOrigin: true,
      },
      '/ws': {
        target: 'http://localhost:1120',
        ws: true,
        changeOrigin: true,
        secure: false,
      },
    },
  },
})
