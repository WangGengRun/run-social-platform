import { defineConfig } from 'vite'
import vue from '@vitejs/plugin-vue'

// https://vite.dev/config/
export default defineConfig({
  plugins: [vue()],
  server: {
    port: 3000,
    proxy: {
      '/api': {
        // 避免 Windows 上 localhost 解析到 IPv6 ::1 导致 ECONNREFUSED
        target: 'http://127.0.0.1:8120',
        changeOrigin: true
      }
    }
  }
})