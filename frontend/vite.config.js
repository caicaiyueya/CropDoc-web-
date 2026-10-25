import { defineConfig } from 'vite'
import vue from '@vitejs/plugin-vue'
import { fileURLToPath, URL } from 'node:url'

export default defineConfig({
  plugins: [vue()],
  resolve: {
    alias: {
      '@': fileURLToPath(new URL('./src', import.meta.url))
    }
  },
  server: {
    port: 5173,
    proxy: {
      '/api': {
        target: 'http://localhost:8080',
        changeOrigin: true
      },
      // 本地开发代理云托管 AI，避免浏览器直连跨域；生产构建仍直连公网地址
      '/ai-proxy': {
        target: 'https://ai-service-267301-4-1440848964.sh.run.tcloudbase.com',
        changeOrigin: true,
        rewrite: (path) => path.replace(/^\/ai-proxy/, '')
      },
      // ImgBB 图床：规避浏览器 CORS
      '/imgbb-api': {
        target: 'https://api.imgbb.com',
        changeOrigin: true,
        rewrite: (path) => path.replace(/^\/imgbb-api/, '')
      }
    }
  }
})
