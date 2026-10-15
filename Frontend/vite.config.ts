import { defineConfig } from "vite"
import react from "@vitejs/plugin-react"
import tailwindcss from "@tailwindcss/vite"
import fs from "node:fs"
import path from "node:path"
import { fileURLToPath } from "node:url"

const frontendDirectory = path.dirname(fileURLToPath(import.meta.url))
const certificatePath = process.env.HTTPS_CERT_FILE
  ? path.resolve(process.env.HTTPS_CERT_FILE)
  : path.join(frontendDirectory, ".cert", "localhost.pem")
const keyPath = process.env.HTTPS_KEY_FILE
  ? path.resolve(process.env.HTTPS_KEY_FILE)
  : path.join(frontendDirectory, ".cert", "localhost-key.pem")

const apiProxyTarget = process.env.IDENTITY_API_PROXY_TARGET || "http://localhost:8081"

export default defineConfig(({ mode, command }) => {
  const emitSourcemaps = mode === "development"
  const https = command === "build"
    ? undefined
    : {
        cert: fs.readFileSync(certificatePath),
        key: fs.readFileSync(keyPath),
      }

  return {
    base: "/",
    build: {
      sourcemap: emitSourcemaps ? "inline" : false,
      minify: !emitSourcemaps,
    },
    plugins: [react(), tailwindcss()],
    resolve: {
      alias: {
        "@": path.resolve(frontendDirectory, "./src"),
      },
    },
    server: {
      host: process.env.HOST || "0.0.0.0",
      port: parseInt(process.env.PORT || "8443", 10),
      strictPort: true,
      https,
      proxy: {
        "/api/account": { target: apiProxyTarget, changeOrigin: true },
        "/api/authorization": { target: apiProxyTarget, changeOrigin: true },
        "/api/profile": { target: apiProxyTarget, changeOrigin: true },
      },
    },
    preview: {
      host: process.env.HOST || "0.0.0.0",
      port: parseInt(process.env.PORT || "8443", 10),
      https,
    },
  }
})
