const apiBase = (import.meta.env.VITE_API_BASE_URL || '').replace(/\/+$/, '')

export async function apiRequest(path, { token, ...options } = {}) {
  let response
  try {
    response = await fetch(`${apiBase}${path}`, {
      ...options,
      headers: {
        ...(options.body ? { 'Content-Type': 'application/json' } : {}),
        ...(token ? { Authorization: `Bearer ${token}` } : {}),
        ...options.headers,
      },
    })
  } catch {
    throw new Error('Không kết nối được backend. Hãy kiểm tra backend và MySQL đang chạy.')
  }

  const result = await response.json().catch(() => null)
  if (!response.ok) {
    throw new Error(result?.detail || result?.message || `Yêu cầu thất bại (${response.status}).`)
  }
  return result
}
