import { createContext, useContext, useEffect, useMemo, useState } from 'react'
import { apiRequest } from '../api.js'

const AuthContext = createContext(null)

export function AuthProvider({ children }) {
  const [session, setSession] = useState(() => {
    try {
      return JSON.parse(sessionStorage.getItem('library-session') || 'null')
    } catch {
      return null
    }
  })

  async function login(endpoint, identifierKey, identifier, password) {
    const result = await apiRequest(`/api/auth/${endpoint}`, {
      method: 'POST',
      body: JSON.stringify({ [identifierKey]: identifier.trim(), password }),
    })

    const nextSession = {
      accessToken: result.accessToken,
      tokenType: result.tokenType,
      role: result.role,
      identifier: result.identifier,
      expiresAt: Date.now() + (result.expiresInSeconds || 3600) * 1000,
    }
    sessionStorage.setItem('library-session', JSON.stringify(nextSession))
    setSession(nextSession)
    return nextSession
  }

  async function registerMember(memberId, password) {
    return apiRequest('/api/auth/member/register', {
      method: 'POST',
      body: JSON.stringify({ memberId: memberId.trim(), password }),
    })
  }

  function logout() {
    sessionStorage.removeItem('library-session')
    setSession(null)
  }

  useEffect(() => {
    if (!session) return undefined
    const remaining = session.expiresAt - Date.now()
    if (remaining <= 0) {
      logout()
      return undefined
    }
    const timer = window.setTimeout(logout, remaining)
    return () => window.clearTimeout(timer)
  }, [session])

  const value = useMemo(() => ({
    session: session?.expiresAt > Date.now() ? session : null,
    loginMember: (memberId, password) => login('member/login', 'memberId', memberId, password),
    loginLibrarian: (librarianId, password) => login('librarian/login', 'librarianId', librarianId, password),
    registerMember,
    logout,
  }), [session])

  return <AuthContext.Provider value={value}>{children}</AuthContext.Provider>
}

export function useAuth() {
  const context = useContext(AuthContext)
  if (!context) throw new Error('useAuth phải được dùng bên trong AuthProvider')
  return context
}
