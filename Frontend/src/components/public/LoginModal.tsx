import { useCallback, useEffect, useRef, useState } from "react"
import { identityApi } from "@/api/identity.ts"
import type { LoginModalProps as Props } from "./types"

export function LoginModal({ onClose, onLogin }: Props) {
  const googleButtonRef = useRef<HTMLDivElement>(null)
  const [email, setEmail] = useState("")
  const [password, setPassword] = useState("")
  const [error, setError] = useState<string | null>(null)
  const [isSubmitting, setIsSubmitting] = useState(false)
  const googleClientId = import.meta.env.VITE_GOOGLE_CLIENT_ID?.trim()

  const handleGoogleLogin = useCallback(async (credential: string) => {
    setError(null)
    setIsSubmitting(true)
    try {
      const email = readGoogleEmail(credential)
      if (!email) throw new Error("Google không trả về địa chỉ email. Vui lòng thử lại.")
      const result = await identityApi.loginWithGoogle(email, credential)
      const roleCodes = result.account.roleCodes ?? []
      onLogin({ accountId: result.account.accountId, email: result.account.email, roleCodes })
    } catch (cause) {
      setError(cause instanceof Error ? cause.message : "Không thể đăng nhập bằng Google. Vui lòng thử lại.")
    } finally {
      setIsSubmitting(false)
    }
  }, [onLogin])

  useEffect(() => {
    if (!googleClientId || !googleButtonRef.current) return

    let isActive = true
    const renderButton = () => {
      const buttonContainer = googleButtonRef.current
      if (!isActive || !window.google || !buttonContainer) return
      window.google.accounts.id.initialize({
        client_id: googleClientId,
        callback: (response) => {
          void handleGoogleLogin(response.credential)
        },
      })
      buttonContainer.replaceChildren()
      window.google.accounts.id.renderButton(buttonContainer, {
        theme: "outline",
        size: "large",
        shape: "pill",
        text: "continue_with",
        width: Math.floor(buttonContainer.getBoundingClientRect().width),
      })
    }

    const scriptId = "google-identity-services"
    let script = document.getElementById(scriptId) as HTMLScriptElement | null
    if (!script) {
      script = document.createElement("script")
      script.id = scriptId
      script.src = "https://accounts.google.com/gsi/client"
      script.async = true
      script.defer = true
      script.onload = renderButton
      document.head.appendChild(script)
    } else if (window.google) {
      renderButton()
    } else {
      script.addEventListener("load", renderButton)
    }

    return () => {
      isActive = false
      script?.removeEventListener("load", renderButton)
    }
  }, [googleClientId, handleGoogleLogin])

  const handleSubmit = async (event: React.FormEvent<HTMLFormElement>) => {
    event.preventDefault()
    setError(null)
    setIsSubmitting(true)

    try {
      const result = await identityApi.login(email.trim(), password)
      const roleCodes = result.account.roleCodes ?? []
      onLogin({ accountId: result.account.accountId, email: result.account.email, roleCodes })
    } catch (cause) {
      setError(cause instanceof Error ? cause.message : "Không thể đăng nhập. Vui lòng thử lại.")
    } finally {
      setIsSubmitting(false)
    }
  }

  return (
    <div
      className="fixed inset-0 z-[100] flex items-center justify-center bg-slate-950/65 p-4 backdrop-blur-sm"
      onMouseDown={(event) => {
        if (event.target === event.currentTarget) onClose()
      }}
    >
      <section
        role="dialog"
        aria-modal="true"
        aria-labelledby="login-title"
        className="relative w-full max-w-md rounded-3xl bg-white p-7 shadow-2xl sm:p-9"
      >
        <button
          type="button"
          onClick={onClose}
          aria-label="Đóng đăng nhập"
          className="absolute right-5 top-5 flex h-9 w-9 items-center justify-center rounded-full text-xl text-slate-400 transition hover:bg-slate-100 hover:text-slate-700"
        >
          ×
        </button>
        <div className="mb-7">
          <div className="mb-5 flex h-12 w-12 items-center justify-center rounded-2xl bg-blue-600 text-lg font-bold text-white">M</div>
          <p className="text-xs font-semibold uppercase tracking-[0.2em] text-blue-600">ManagerSpace</p>
          <h2 id="login-title" className="mt-2 text-2xl font-semibold tracking-tight text-slate-950">Đăng nhập quản lý</h2>
          <p className="mt-2 text-sm leading-6 text-slate-500">Đăng nhập bằng tài khoản đã được cấp để vào không gian làm việc.</p>
        </div>

        {googleClientId ? (
          <>
            <div ref={googleButtonRef} className="mb-4 flex min-h-10 justify-center" />
            <div className="mb-4 flex items-center gap-3 text-xs text-slate-400">
              <span className="h-px flex-1 bg-slate-200" />
              hoặc đăng nhập bằng email
              <span className="h-px flex-1 bg-slate-200" />
            </div>
          </>
        ) : (
          <p className="mb-4 rounded-xl bg-amber-50 px-4 py-3 text-xs leading-5 text-amber-800">
            Đăng nhập Google chưa được cấu hình. Hãy thiết lập VITE_GOOGLE_CLIENT_ID.
          </p>
        )}

        <form onSubmit={(event) => void handleSubmit(event)} className="space-y-4">
          <label className="block">
            <span className="mb-1.5 block text-sm font-medium text-slate-700">Email</span>
            <input
              autoFocus
              required
              type="email"
              autoComplete="username"
              value={email}
              onChange={(event) => setEmail(event.target.value)}
              placeholder="ten@congty.vn"
              className="w-full rounded-xl border border-slate-200 px-4 py-3 text-sm outline-none transition placeholder:text-slate-400 focus:border-blue-500 focus:ring-4 focus:ring-blue-500/10"
            />
          </label>
          <label className="block">
            <span className="mb-1.5 block text-sm font-medium text-slate-700">Mật khẩu</span>
            <input
              required
              type="password"
              autoComplete="current-password"
              value={password}
              onChange={(event) => setPassword(event.target.value)}
              placeholder="Nhập mật khẩu"
              className="w-full rounded-xl border border-slate-200 px-4 py-3 text-sm outline-none transition placeholder:text-slate-400 focus:border-blue-500 focus:ring-4 focus:ring-blue-500/10"
            />
          </label>
          {error && <p role="alert" className="rounded-xl bg-red-50 px-4 py-3 text-sm leading-5 text-red-700">{error}</p>}
          <button
            type="submit"
            disabled={isSubmitting}
            className="w-full rounded-xl bg-blue-600 px-4 py-3 text-sm font-semibold text-white shadow-lg shadow-blue-600/20 transition hover:bg-blue-700 disabled:cursor-wait disabled:opacity-60"
          >
            {isSubmitting ? "Đang xác thực..." : "Đăng nhập"}
          </button>
        </form>
        <p className="mt-5 text-center text-xs leading-5 text-slate-400">Tài khoản khách thuê được ban quản lý cấp sau khi ký hợp đồng.</p>
      </section>
    </div>
  )
}

function readGoogleEmail(credential: string): string | null {
  try {
    const payload = credential.split(".")[1]
    if (!payload) return null
    const normalized = payload.replace(/-/g, "+").replace(/_/g, "/")
    const decoded = JSON.parse(atob(normalized)) as { email?: unknown }
    return typeof decoded.email === "string" ? decoded.email.trim() : null
  } catch {
    return null
  }
}
