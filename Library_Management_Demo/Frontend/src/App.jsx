import { useEffect, useState } from 'react'
import { Link, NavLink, Route, Routes, useNavigate, useParams } from 'react-router-dom'
import { useAuth } from './auth/AuthContext.jsx'
import { apiRequest } from './api.js'
import './App.css'

const categories = ['Tất cả', 'Sách', 'Tài liệu mở', 'Tạp chí']
const resourceTypes = { Sách: 'Physical_Book', 'Tài liệu mở': 'Open_Access', 'Tạp chí': 'Journal' }
const copyStatusLabels = { Available: 'Có sẵn', Borrowed: 'Đang mượn', Damaged: 'Hư hỏng', Lost: 'Đã mất' }
const returnStatusConditions = { Available: 'Good', Borrowed: 'Good', Damaged: 'Damaged', Lost: 'Lost' }
const PAGE_SIZE = 12

function Icon({ name, size = 20 }) {
  const common = { width: size, height: size, viewBox: '0 0 24 24', fill: 'none', stroke: 'currentColor', strokeWidth: 1.7, strokeLinecap: 'round', strokeLinejoin: 'round', 'aria-hidden': true }
  if (name === 'search') return <svg {...common}><circle cx="11" cy="11" r="7"/><path d="m20 20-4-4"/></svg>
  if (name === 'arrow') return <svg {...common}><path d="M5 12h14M13 6l6 6-6 6"/></svg>
  if (name === 'book') return <svg {...common}><path d="M4 5.5A2.5 2.5 0 0 1 6.5 3H20v17H6.5A2.5 2.5 0 0 1 4 17.5z"/><path d="M4 17.5A2.5 2.5 0 0 1 6.5 15H20M8 7h8"/></svg>
  if (name === 'globe') return <svg {...common}><circle cx="12" cy="12" r="9"/><path d="M3 12h18M12 3a15 15 0 0 1 0 18M12 3a15 15 0 0 0 0 18"/></svg>
  if (name === 'spark') return <svg {...common}><path d="m12 3 1.8 5.2L19 10l-5.2 1.8L12 17l-1.8-5.2L5 10l5.2-1.8L12 3ZM19 16l.8 2.2L22 19l-2.2.8L19 22l-.8-2.2L16 19l2.2-.8L19 16Z"/></svg>
  return null
}

function Header() {
  const { session, logout } = useAuth()
  return <header className="topbar"><div className="nav-shell">
    <Link className="brand" to="/" aria-label="Mở trang chủ"><span className="brand-mark"><Icon name="book" size={21}/></span><span>Thư viện <b>Mở</b></span></Link>
    <nav className="main-nav" aria-label="Điều hướng chính">
      <NavLink to="/" end>Trang chủ</NavLink><NavLink to="/tai-lieu">Tài liệu</NavLink><NavLink to="/gioi-thieu">Giới thiệu</NavLink>
    </nav>
    {session ? <div className="account-actions">{session.role === 'Member' && <Link className="account-button" to="/tai-khoan">Tài khoản</Link>}{session.role === 'Librarian' && <Link className="account-button" to="/quan-ly-thu-vien">Quầy thủ thư</Link>}<span className="account-identifier">{session.identifier}</span><button className="logout-button" onClick={logout}>Đăng xuất</button></div> : <Link className="login-button" to="/dang-nhap">Đăng nhập <Icon name="arrow" size={16}/></Link>}
  </div></header>
}

function Placeholder({ className = '' }) {
  return <div className={`image-placeholder ${className}`} aria-label="Vị trí ảnh bìa tài liệu"><span className="placeholder-art"><Icon name="book" size={35}/></span><span className="placeholder-label">Ảnh tài liệu</span></div>
}

function useCatalog(category, search = '', memberAccess = false, token = null) {
  const [items, setItems] = useState([])
  const [loading, setLoading] = useState(true)
  const [error, setError] = useState('')
  useEffect(() => {
    let active = true
    const params = new URLSearchParams()
    if (resourceTypes[category]) params.set('type', resourceTypes[category])
    else if (!memberAccess) params.set('excludeType', 'Journal')
    if (search.trim()) params.set('q', search.trim())
    setLoading(true)
    setError('')
    apiRequest(`/api/catalog/resources${params.size ? `?${params}` : ''}`, { token })
      .then(data => { if (active) setItems(data) })
      .catch(apiError => { if (active) setError(apiError.message) })
      .finally(() => { if (active) setLoading(false) })
    return () => { active = false }
  }, [category, search, memberAccess, token])
  return { items, loading, error }
}

function PaginationControls({ page, total, onPageChange }) {
  const pageCount = Math.ceil(total / PAGE_SIZE)
  if (pageCount <= 1) return null
  const firstItem = (page - 1) * PAGE_SIZE + 1
  const lastItem = Math.min(page * PAGE_SIZE, total)
  return <nav className="pagination-controls" aria-label="Phân trang">
    <button type="button" onClick={() => onPageChange(page - 1)} disabled={page <= 1}>Trước</button>
    <span>{firstItem}–{lastItem} / {total} · Trang {page}/{pageCount}</span>
    <button type="button" onClick={() => onPageChange(page + 1)} disabled={page >= pageCount}>Tiếp</button>
  </nav>
}

function DocumentCards({ items, loading, error, page = 1, onPageChange = () => {} }) {
  if (loading) return <div className="catalog-message">Đang tải danh mục tài liệu…</div>
  if (error) return <div className="catalog-message catalog-error" role="alert">{error}</div>
  if (!items.length) return <div className="catalog-message">Chưa có tài liệu phù hợp.</div>
  return <><div className="document-grid">{items.slice((page - 1) * PAGE_SIZE, page * PAGE_SIZE).map(item => <Link className="document-card" to={`/tai-lieu/${item.resourceId}`} key={item.resourceId}>
    <Placeholder className="document-cover"/>
    <div className="document-info"><span className="document-type">{item.resourceType === 'Journal' ? 'TẠP CHÍ SỐ' : item.resourceType === 'Open_Access' ? 'TÀI LIỆU MỞ' : 'SÁCH'}</span>
      <h3>{item.title}</h3><p>Tác giả: {item.authors || 'Đang cập nhật'}</p><p>Ngày xuất bản: {item.publishYear || 'Đang cập nhật'}</p>
    </div>
  </Link>)}</div><PaginationControls page={page} total={items.length} onPageChange={onPageChange}/></>
}

function Home() {
  const [query, setQuery] = useState('')
  const [submittedQuery, setSubmittedQuery] = useState('')
  const [category, setCategory] = useState('Tất cả')
  const [page, setPage] = useState(1)
  const { session } = useAuth()
  const catalog = useCatalog(category, submittedQuery, session?.role === 'Member', session?.accessToken)
  useEffect(() => setPage(1), [category, submittedQuery, session?.role, session?.accessToken])
  return <>
    <section className="hero-section"><div className="hero-copy">
      <div className="eyebrow"><span className="eyebrow-dot"/> TRI THỨC DÀNH CHO MỌI NGƯỜI</div>
      <h1>Khám phá tri thức,<br/><em>mở rộng</em> thế giới.</h1>
      <p className="hero-intro">Không gian học tập và nghiên cứu với nguồn tài liệu mở, dễ dàng tiếp cận cho tất cả mọi người.</p>
      <form className="search-box" onSubmit={(event) => { event.preventDefault(); setSubmittedQuery(query) }} role="search">
        <Icon name="search" size={21}/><input aria-label="Tìm kiếm tài liệu" placeholder="Tìm sách, bài báo, chủ đề..." value={query} onChange={(event) => setQuery(event.target.value)}/><button type="submit">Tìm kiếm</button>
      </form>
      <div className="search-hint">Gợi ý: <button type="button" onClick={() => setQuery('Khoa học')}>Khoa học</button><span>·</span><button type="button" onClick={() => setQuery('Giáo dục')}>Giáo dục</button><span>·</span><button type="button" onClick={() => setQuery('Văn học')}>Văn học</button></div>
    </div><div className="hero-visual"><div className="visual-orbit orbit-one"/><div className="visual-orbit orbit-two"/><div className="visual-note"><Icon name="spark" size={18}/><span>Tri thức mở<br/><b>cho tất cả</b></span></div><Placeholder className="hero-placeholder"/><div className="visual-caption"><span>01 / TÀI LIỆU MỞ</span><span>Học tập không giới hạn</span></div></div></section>
    <section className="benefits" aria-label="Lợi ích thư viện"><div><span className="benefit-icon"><Icon name="globe"/></span><span><b>Truy cập mở</b><small>Tri thức không rào cản</small></span></div><div><span className="benefit-icon"><Icon name="book"/></span><span><b>Nhiều loại tài liệu</b><small>Sách, bài báo và hơn thế</small></span></div><div><span className="benefit-icon"><Icon name="spark"/></span><span><b>Luôn sẵn sàng</b><small>Khám phá theo cách của bạn</small></span></div></section>
    <section className="catalog-section"><div className="section-heading"><div><div className="eyebrow">KHO TRI THỨC</div><h2>Tài nguyên <em>thư viện</em></h2><p>Khám phá sách, tài liệu mở và tạp chí số.</p></div><Link className="text-link" to="/tai-lieu">Xem tất cả <Icon name="arrow" size={17}/></Link></div>
      <div className="catalog-toolbar"><div className="category-list" aria-label="Lọc theo loại tài liệu">{categories.map(item => <button key={item} className={category === item ? 'category active' : 'category'} onClick={() => setCategory(item)}>{item}</button>)}</div><span className="open-label"><span/> {category === 'Tạp chí' ? 'TẠP CHÍ SỐ' : category === 'Tài liệu mở' ? 'TÀI LIỆU MỞ' : category === 'Sách' ? 'SÁCH VẬT LÝ' : 'TÀI NGUYÊN'}</span></div>
      {category === 'Tạp chí' && session?.role !== 'Member' ? <div className="journal-gate"><div className="empty-icon"><Icon name="book" size={25}/></div><h3>{session ? 'Tạp chí số chỉ dành cho thành viên' : 'Bạn phải là thành viên mới được xem tạp chí số'}</h3><p>{session ? 'Tài khoản thủ thư không có quyền xem tạp chí dành cho thành viên.' : 'Hãy đăng nhập để xem các số tạp chí dành cho thành viên.'}</p>{session ? <Link className="primary-button" to="/dang-nhap">Đăng nhập thành viên <Icon name="arrow" size={16}/></Link> : <Link className="primary-button" to="/dang-nhap">Đăng nhập tại đây <Icon name="arrow" size={16}/></Link>}</div> : <DocumentCards {...catalog} page={page} onPageChange={setPage}/>}
    </section>
    <section className="join-banner"><div><div className="eyebrow">BẮT ĐẦU HÀNH TRÌNH</div><h2>Mỗi trang sách,<br/>một chân trời mới.</h2></div><Link className="banner-button" to="/dang-nhap">Đăng nhập để khám phá <Icon name="arrow" size={17}/></Link></section>
  </>
}

function LoginForm({ librarian = false }) {
  const [identifier, setIdentifier] = useState('')
  const [password, setPassword] = useState('')
  const [error, setError] = useState('')
  const [loading, setLoading] = useState(false)
  const { loginMember, loginLibrarian } = useAuth()
  const navigate = useNavigate()

  async function handleSubmit(event) {
    event.preventDefault()
    setError('')
    setLoading(true)
    try {
      if (librarian) await loginLibrarian(identifier, password)
      else await loginMember(identifier, password)
      navigate('/')
    } catch (loginError) {
      setError(loginError.message || 'Đăng nhập thất bại.')
    } finally {
      setLoading(false)
    }
  }

  const idLabel = librarian ? 'Mã thủ thư' : 'Mã thành viên'
  return <section className="simple-page"><div className="eyebrow">{librarian ? 'DÀNH CHO NHÂN VIÊN THƯ VIỆN' : 'CHÀO MỪNG BẠN'}</div><h1>{librarian ? 'Đăng nhập thủ thư' : 'Đăng nhập'}</h1><p>{librarian ? 'Sử dụng tài khoản thủ thư để truy cập khu vực quản lý thư viện.' : 'Đăng nhập bằng mã thành viên để tiếp tục hành trình khám phá tri thức.'}</p>
    <form className="login-form" onSubmit={handleSubmit}><label>{idLabel}<input type="text" autoComplete="username" placeholder={`Nhập ${idLabel.toLowerCase()}`} value={identifier} onChange={event => setIdentifier(event.target.value)} required/></label><label>Mật khẩu<input type="password" autoComplete="current-password" placeholder="Nhập mật khẩu" value={password} onChange={event => setPassword(event.target.value)} required/></label>{error && <p className="login-error" role="alert">{error}</p>}<button className="primary-button" type="submit" disabled={loading}>{loading ? 'Đang đăng nhập…' : librarian ? 'Đăng nhập thủ thư' : 'Đăng nhập'} <Icon name="arrow" size={16}/></button></form>
    <p className="form-note">Xác thực qua tài khoản thư viện.</p>{librarian ? <Link className="staff-login-link" to="/dang-nhap">Quay lại đăng nhập thành viên</Link> : <><Link className="staff-login-link" to="/dang-ky">Chưa có tài khoản? Đăng ký bằng mã thành viên <Icon name="arrow" size={15}/></Link><Link className="staff-login-link" to="/dang-nhap-thu-thu">Bạn là thủ thư? Đăng nhập tại đây <Icon name="arrow" size={15}/></Link></>}</section>
}
function Login() { return <LoginForm/> }
function LibrarianLogin() { return <LoginForm librarian/> }
function MemberRegistration() {
  const [memberId, setMemberId] = useState('')
  const [password, setPassword] = useState('')
  const [confirmPassword, setConfirmPassword] = useState('')
  const [error, setError] = useState('')
  const [success, setSuccess] = useState(false)
  const [loading, setLoading] = useState(false)
  const { registerMember } = useAuth()
  async function handleSubmit(event) {
    event.preventDefault()
    setError('')
    if (password !== confirmPassword) { setError('Hai mật khẩu không khớp.'); return }
    setLoading(true)
    try {
      await registerMember(memberId, password)
      setSuccess(true)
    } catch (registerError) {
      setError(registerError.message || 'Không thể đăng ký tài khoản.')
    } finally { setLoading(false) }
  }
  return <section className="simple-page"><div className="eyebrow">TẠO TÀI KHOẢN THÀNH VIÊN</div><h1>Đăng ký tài khoản</h1><p>Thành viên cần còn hạn thẻ để tạo tài khoản truy cập thư viện số.</p>{success ? <div className="register-success" role="status">Đăng ký thành công. <Link to="/dang-nhap">Đăng nhập tại đây.</Link></div> : <form className="login-form" onSubmit={handleSubmit}><label>Mã thành viên<input value={memberId} onChange={event => setMemberId(event.target.value)} autoComplete="username" placeholder="Nhập mã thành viên" required/></label><label>Mật khẩu<input type="password" value={password} onChange={event => setPassword(event.target.value)} autoComplete="new-password" minLength="8" required/></label><label>Nhập lại mật khẩu<input type="password" value={confirmPassword} onChange={event => setConfirmPassword(event.target.value)} autoComplete="new-password" minLength="8" required/></label>{error && <p className="login-error" role="alert">{error}</p>}<button className="primary-button" type="submit" disabled={loading}>{loading ? 'Đang đăng ký…' : 'Tạo tài khoản'} <Icon name="arrow" size={16}/></button></form>}<Link className="staff-login-link" to="/dang-nhap">Đã có tài khoản? Đăng nhập</Link></section>
}
function LibrarianDashboard() {
  const { session } = useAuth()
  const [activeSection, setActiveSection] = useState('member')
  const [memberId, setMemberId] = useState('')
  const [copyBatch, setCopyBatch] = useState('')
  const [copyStatus, setCopyStatus] = useState('All')
  const [copyResourceId, setCopyResourceId] = useState('')
  const [copyRows, setCopyRows] = useState([])
  const [copyPage, setCopyPage] = useState(1)
  const [copyLoading, setCopyLoading] = useState(true)
  const [copyListError, setCopyListError] = useState('')
  const [inventoryVersion, setInventoryVersion] = useState(0)
  const [memberData, setMemberData] = useState(null)
  const [returnStatuses, setReturnStatuses] = useState({})
  const [copyTarget, setCopyTarget] = useState('')
  const [copyCondition, setCopyCondition] = useState('Damaged')
  const [message, setMessage] = useState('')
  const [error, setError] = useState('')
  const [busy, setBusy] = useState(false)
  const token = session?.accessToken
  const filteredCopyRows = copyRows.filter(copy => !copyResourceId.trim() || Number(copy.resourceId) === Number(copyResourceId.trim()))

  useEffect(() => {
    if (session?.role !== 'Librarian' || !token) return undefined
    let active = true
    setCopyLoading(true)
    setCopyListError('')
    const statusQuery = copyStatus === 'All' ? '' : `?status=${encodeURIComponent(copyStatus)}`
    apiRequest(`/api/librarian/copies${statusQuery}`, { token })
      .then(rows => { if (active) setCopyRows(rows) })
      .catch(apiError => { if (active) setCopyListError(apiError.message) })
      .finally(() => { if (active) setCopyLoading(false) })
    return () => { active = false }
  }, [session?.role, token, copyStatus, inventoryVersion])

  useEffect(() => setCopyPage(1), [copyStatus, inventoryVersion, copyResourceId])

  if (session?.role !== 'Librarian') return <section className="simple-page"><div className="eyebrow">QUẦY THỦ THƯ</div><h1>Đăng nhập thủ thư</h1><p>Khu vực này chỉ dành cho tài khoản thủ thư đang hoạt động.</p><Link className="primary-button" to="/dang-nhap-thu-thu">Đăng nhập <Icon name="arrow" size={16}/></Link></section>

  async function loadMember() {
    if (!memberId.trim()) return
    setBusy(true); setError(''); setMessage('')
    try {
      const [profile, loans, allFines] = await Promise.all([
        apiRequest(`/api/librarian/members/${encodeURIComponent(memberId.trim())}/profile`, { token }),
        apiRequest(`/api/librarian/members/${encodeURIComponent(memberId.trim())}/loans`, { token }),
        apiRequest('/api/librarian/fines', { token }),
      ])
      const fines = (Array.isArray(allFines) ? allFines : []).filter(fine => String(fine.memberId) === memberId.trim())
      setMemberData({ profile, loans, fines })
    } catch (loadError) { setError(loadError.message) }
    finally { setBusy(false) }
  }
  async function submitBorrow(event) {
    event.preventDefault(); setBusy(true); setError(''); setMessage('')
    try {
      if (!memberId.trim()) throw new Error('Nhập mã thành viên cần lập phiếu mượn.')
      const copyParts = copyBatch.split(/[,:]/).map(value => value.trim())
      const [resourceId, copyNo] = copyParts.map(Number)
      if (copyParts.length !== 2 || !Number.isInteger(resourceId) || !Number.isInteger(copyNo)) {
        throw new Error('Nhập một bản sao theo dạng mã tài liệu, số bản sao.')
      }
      const copies = [{ resourceId, copyNo }]
      await apiRequest('/api/librarian/loans', { token, method: 'POST', body: JSON.stringify({ memberId: memberId.trim(), copies }) })
      setCopyBatch('')
      setInventoryVersion(version => version + 1)
      await loadMember()
      setMessage('Đã lập phiếu mượn bằng procedure borrow_book.')
    } catch (borrowError) { setError(borrowError.message) }
    finally { setBusy(false) }
  }
  async function returnCopy(loan, status) {
    setBusy(true); setError(''); setMessage('')
    let conditionSaved = false
    try {
      const condition = returnStatusConditions[status]
      if (!condition) throw new Error('Hãy chọn trạng thái sách hợp lệ khi nhận lại.')
      await apiRequest(`/api/librarian/copies/${loan.resourceId}/${loan.copyNo}/condition`, { token, method: 'PATCH', body: JSON.stringify({ condition }) })
      conditionSaved = true
      if (status !== 'Borrowed') {
        await apiRequest(`/api/librarian/loans/copies/${loan.loanedCopyId}/return`, { token, method: 'POST' })
      }
      setInventoryVersion(version => version + 1)
      await loadMember()
      setMessage(status === 'Borrowed'
        ? 'Đã cập nhật trạng thái “Đang mượn”; lượt mượn vẫn đang mở.'
        : `Đã cập nhật trạng thái “${copyStatusLabels[status]}” và ghi nhận thu sách.`)
    } catch (returnError) { setError(conditionSaved && status !== 'Borrowed' ? `Đã cập nhật tình trạng sách nhưng chưa ghi nhận trả sách: ${returnError.message}` : returnError.message) }
    finally { setBusy(false) }
  }
  async function payFine(fine) {
    setBusy(true); setError(''); setMessage('')
    try {
      await apiRequest(`/api/librarian/fines/${fine.loanedCopyId}/${fine.fineId}/pay`, { token, method: 'POST' })
      await loadMember()
      setMessage(`Đã thu ${Number(fine.amount || 0).toLocaleString('vi-VN')} ₫ cho khoản phạt ${fine.fineId}.`)
    } catch (paymentError) { setError(paymentError.message) }
    finally { setBusy(false) }
  }
  async function updateCopyCondition(event) {
    event.preventDefault(); setBusy(true); setError(''); setMessage('')
    try {
      const [resourceId, copyNo] = copyTarget.split(/[,:]/).map(Number)
      if (!Number.isInteger(resourceId) || !Number.isInteger(copyNo)) throw new Error('Nhập theo dạng mã tài liệu, số bản sao.')
      await apiRequest(`/api/librarian/copies/${resourceId}/${copyNo}/condition`, { token, method: 'PATCH', body: JSON.stringify({ condition: copyCondition }) })
      setInventoryVersion(version => version + 1)
      if (memberData) await loadMember()
      setMessage('Đã cập nhật tình trạng bản sao.')
    } catch (conditionError) { setError(conditionError.message) }
    finally { setBusy(false) }
  }
  return <section className="account-page librarian-page">
    <div className="eyebrow">DÀNH CHO NHÂN VIÊN THƯ VIỆN</div>
    <h1>Quầy thủ thư</h1>
    <p>Chọn một danh mục để tra cứu thành viên, kiểm tra bản sao hoặc lập phiếu mượn.</p>

    <nav className="librarian-section-nav" aria-label="Danh mục thủ thư">
      <button type="button" aria-pressed={activeSection === 'member'} className={activeSection === 'member' ? 'active' : ''} onClick={() => setActiveSection('member')}>
        <span className="librarian-tab-number">01</span><span><b>Tra cứu thành viên</b><small>Hồ sơ, lịch sử mượn và tiền phạt</small></span>
      </button>
      <button type="button" aria-pressed={activeSection === 'inventory'} className={activeSection === 'inventory' ? 'active' : ''} onClick={() => setActiveSection('inventory')}>
        <span className="librarian-tab-number">02</span><span><b>Tình trạng sách</b><small>Lọc bản sao theo trạng thái</small></span>
      </button>
      <button type="button" aria-pressed={activeSection === 'borrow'} className={activeSection === 'borrow' ? 'active' : ''} onClick={() => setActiveSection('borrow')}>
        <span className="librarian-tab-number">03</span><span><b>Lập phiếu mượn</b><small>Ghi nhận thành viên và bản sao</small></span>
      </button>
    </nav>

    {activeSection === 'member' && <div className="librarian-section-panel">
      <div className="desk-card"><h2>Tra cứu thành viên</h2><form className="desk-search" onSubmit={event => { event.preventDefault(); loadMember() }}><input value={memberId} onChange={event => setMemberId(event.target.value)} placeholder="Mã thành viên" required/><button className="primary-button" disabled={busy}>Tra cứu</button></form></div>
      {memberData && <>
        <div className="member-profile-card"><div className="member-avatar">{memberData.profile.fullName?.slice(0, 1) || '?'}</div><div className="member-fields"><div><span>Tên thành viên</span><b>{memberData.profile.fullName}</b></div><div><span>Mã thành viên</span><b>{memberData.profile.memberId}</b></div><div><span>Hạn thẻ</span><b>{memberData.profile.expiryDate}</b></div><div><span>Tổng tiền phạt</span><b>{memberData.loans.reduce((total, loan) => total + Number(loan.fineAmount || 0), 0).toLocaleString('vi-VN')} ₫</b></div><div><span>Tổng nợ chưa thanh toán</span><b>{Number(memberData.profile.totalUnpaidAmount ?? memberData.loans.reduce((total, loan) => total + Number(loan.unpaidFineAmount || 0), 0)).toLocaleString('vi-VN')} ₫</b></div></div></div>
        <div className="desk-card">
          <h2>Lịch sử mượn/trả của thành viên</h2>
          <p>Chọn tình trạng thực tế của từng bản sao trước khi ghi nhận thành viên trả sách.</p>
          <div className="loan-table-wrap"><table className="loan-table member-loan-history-table"><thead><tr><th>Mã Loan_Copy</th><th>Tài liệu</th><th>Bản sao</th><th>Ngày mượn</th><th>Hạn trả</th><th>Ngày trả</th><th>Trạng thái mượn</th><th>Tổng phạt</th><th>Còn nợ</th><th>Trạng thái nợ</th><th>Tình trạng & trả sách</th></tr></thead><tbody>{memberData.loans.length ? memberData.loans.map(loan => {
            const fineAmount = Number(loan.fineAmount || 0)
            const unpaidFineAmount = Number(loan.unpaidFineAmount || 0)
            const status = returnStatuses[loan.loanedCopyId] || ''
            return <tr key={loan.loanedCopyId}><td>{loan.loanedCopyId}</td><td>{loan.bookTitle}</td><td>{loan.copyNo}</td><td>{loan.loanDate || '—'}</td><td>{loan.dueDate || '—'}</td><td>{loan.returnDate || '—'}</td><td><span className={`loan-state ${loan.returnDate ? 'returned' : 'borrowed'}`}>{loan.statusLabel}</span></td><td>{fineAmount > 0 ? `${fineAmount.toLocaleString('vi-VN')} ₫` : 'Không có'}</td><td>{unpaidFineAmount > 0 ? `${unpaidFineAmount.toLocaleString('vi-VN')} ₫` : '0 ₫'}</td><td>{loan.paymentStatus || 'Không có'}</td><td>{!loan.returnDate ? <div className="return-copy-actions"><select value={status} onChange={event => setReturnStatuses(current => ({ ...current, [loan.loanedCopyId]: event.target.value }))} aria-label={`Trạng thái bản sao ${loan.copyNo}`}><option value="">Chọn trạng thái</option><option value="Available">Available — Có sẵn</option><option value="Borrowed">Borrowed — Đang mượn</option><option value="Damaged">Damaged — Hư hỏng</option><option value="Lost">Lost — Đã mất</option></select><button className="table-action" disabled={busy || !status} onClick={() => returnCopy(loan, status)}>{status === 'Borrowed' ? 'Cập nhật trạng thái' : 'Cập nhật & thu sách'}</button></div> : 'Đã trả'}</td></tr>
          }) : <tr><td colSpan="11" className="loan-empty">Chưa có lịch sử mượn/trả cho thành viên này.</td></tr>}</tbody></table></div>
        </div>
        <div className="desk-card"><h2>Các khoản tiền phạt</h2><p>Mỗi khoản phạt được thu riêng. Nhấn “Thu” ở đúng khoản thành viên thanh toán.</p><div className="loan-table-wrap"><table className="loan-table member-fines-table"><thead><tr><th>Mã khoản phạt</th><th>Mã Loan_Copy</th><th>Tài liệu</th><th>Lý do</th><th>Số tiền</th><th>Trạng thái</th><th>Ngày đóng</th><th>Thao tác</th></tr></thead><tbody>{memberData.fines.length ? memberData.fines.map(fine => { const isUnpaid = fine.paymentStatus === 'Unpaid'; return <tr key={`${fine.loanedCopyId}-${fine.fineId}`}><td>{fine.fineId}</td><td>{fine.loanedCopyId}</td><td>{fine.bookTitle || '—'}</td><td>{fine.reason || '—'}</td><td>{Number(fine.amount || 0).toLocaleString('vi-VN')} ₫</td><td>{isUnpaid ? 'Đang nợ' : 'Đã đóng'}</td><td>{fine.paidDate || '—'}</td><td>{isUnpaid ? <button className="table-action" disabled={busy} onClick={() => payFine(fine)}>Thu</button> : 'Đã thu'}</td></tr>}) : <tr><td colSpan="8" className="loan-empty">Thành viên chưa có khoản phạt.</td></tr>}</tbody></table></div></div>
      </>}
    </div>}

    {activeSection === 'inventory' && <div className="librarian-section-panel">
      <div className="desk-card copy-inventory-card"><div className="desk-heading"><div><h2>Kiểm tra sách theo trạng thái</h2><p>Tra cứu từng bản sao, tình trạng và thành viên đang mượn.</p></div><button className="table-action" onClick={() => setInventoryVersion(version => version + 1)}>Tải lại</button></div>
        <div className="copy-inventory-filters"><label className="copy-status-filter" htmlFor="copy-status-filter"><span>Trạng thái bản sao</span><select id="copy-status-filter" value={copyStatus} onChange={event => setCopyStatus(event.target.value)}><option value="All">Tất cả</option><option value="Available">Có sẵn</option><option value="Borrowed">Đang mượn</option><option value="Damaged">Hư hỏng</option><option value="Lost">Đã mất</option></select></label><label className="copy-resource-filter" htmlFor="copy-resource-id-filter"><span>Mã đầu sách (resource_id)</span><input id="copy-resource-id-filter" type="number" min="1" step="1" value={copyResourceId} onChange={event => setCopyResourceId(event.target.value)} placeholder="Nhập mã đầu sách"/></label>{copyResourceId && <button type="button" className="table-action" onClick={() => setCopyResourceId('')}>Xóa lọc mã</button>}</div>
        <div className="loan-table-wrap"><table className="loan-table copy-inventory-table"><thead><tr><th>Mã tài liệu</th><th>Tên tác phẩm</th><th>Bản sao</th><th>Trạng thái</th><th>Mã thành viên</th><th>Hạn trả</th><th>Thao tác</th></tr></thead><tbody>{copyLoading ? <tr><td colSpan="7" className="loan-empty">Đang tải bản sao…</td></tr> : copyListError ? <tr><td colSpan="7" className="loan-empty catalog-error">{copyListError}</td></tr> : filteredCopyRows.length ? filteredCopyRows.slice((copyPage - 1) * PAGE_SIZE, copyPage * PAGE_SIZE).map(copy => <tr key={`${copy.resourceId}-${copy.copyNo}`}><td>{copy.resourceId}</td><td><b>{copy.title}</b><small className="copy-author">{copy.authors || 'Chưa cập nhật tác giả'}</small></td><td>{copy.copyNo}</td><td><span className={`copy-status-badge ${String(copy.copyStatus).toLowerCase()}`}>{copyStatusLabels[copy.copyStatus] || copy.copyStatus}</span></td><td>{copy.memberId || '—'}</td><td>{copy.dueDate || '—'}</td><td>{copy.copyStatus === 'Available' ? <button className="table-action" onClick={() => { setCopyBatch(`${copy.resourceId}, ${copy.copyNo}`); setActiveSection('borrow') }}>Chọn mượn</button> : '—'}</td></tr>) : <tr><td colSpan="7" className="loan-empty">Không có bản sao khớp với bộ lọc.</td></tr>}</tbody></table></div>
        {!copyLoading && !copyListError && <PaginationControls page={copyPage} total={filteredCopyRows.length} onPageChange={setCopyPage}/>}
      </div>
      <div className="desk-card"><h2>Ghi nhận sách mất hoặc hư hỏng nặng</h2><form className="desk-search" onSubmit={updateCopyCondition}><input value={copyTarget} onChange={event => setCopyTarget(event.target.value)} placeholder="Mã tài liệu, số bản sao" required/><select value={copyCondition} onChange={event => setCopyCondition(event.target.value)}><option value="Damaged">Hư hỏng nặng</option><option value="Lost">Bị mất</option></select><button className="primary-button" disabled={busy}>Cập nhật</button></form><p>Hệ thống ghi nhận khoản phạt bằng giá đầu sách nếu bản sao đang gắn với lượt mượn.</p></div>
    </div>}

    {activeSection === 'borrow' && <div className="librarian-section-panel">
      <div className="desk-card"><h2>Lập phiếu mượn</h2><p>Procedure borrow_book lập một phiếu cho một bản sao mỗi lần. Bạn có thể nhập mã bản sao tại đây hoặc chọn bản có sẵn trong mục tình trạng sách.</p><form className="desk-borrow" onSubmit={submitBorrow}><label>Mã thành viên<input value={memberId} onChange={event => setMemberId(event.target.value)} placeholder="Nhập mã thành viên" required/></label><label>Mã tài liệu, số bản sao<input value={copyBatch} onChange={event => setCopyBatch(event.target.value)} placeholder="Ví dụ: 101, 1" required/></label><div className="borrow-form-actions"><button className="primary-button" disabled={busy}>Tạo phiếu mượn</button><button type="button" className="table-action" onClick={() => setActiveSection('inventory')}>Xem tình trạng sách</button></div></form></div>
    </div>}

    {error && <p className="login-error" role="alert">{error}</p>}{message && <p className="desk-message" role="status">{message}</p>}
  </section>
}
function Documents() {
  const { session } = useAuth()
  const [page, setPage] = useState(1)
  const catalog = useCatalog('Tất cả', '', session?.role === 'Member', session?.accessToken)
  useEffect(() => setPage(1), [session?.role, session?.accessToken])
  return <section className="catalog-page"><div className="eyebrow">KHO TRI THỨC</div><h1>Danh mục tài nguyên</h1><p>Chọn một tài liệu để xem thông tin chi tiết.</p><DocumentCards {...catalog} page={page} onPageChange={setPage}/></section>
}
function DocumentDetail() {
  const { documentId } = useParams()
  const { session } = useAuth()
  const [item, setItem] = useState(null)
  const [error, setError] = useState('')
  const [loading, setLoading] = useState(true)
  useEffect(() => {
    let active = true
    setLoading(true)
    apiRequest(`/api/catalog/resources/${documentId}`, { token: session?.accessToken })
      .then(resource => resource)
      .then(resource => { if (active) setItem(resource) })
      .catch(apiError => { if (active) setError(apiError.message) })
      .finally(() => { if (active) setLoading(false) })
    return () => { active = false }
  }, [documentId, session?.accessToken, session?.role])
  const licenseDescription = item?.licenses?.[0]?.licenseDescription
  return <section className="document-detail-page"><Link to="/" className="back-link">← Quay lại trang chủ</Link>{loading ? <p>Đang tải thông tin tài liệu…</p> : error ? <div className="catalog-message catalog-error" role="alert">{error}{error.includes('đăng nhập') && <p><Link className="staff-login-link" to="/dang-nhap">Đăng nhập thành viên <Icon name="arrow" size={15}/></Link></p>}</div> : item && <div className="detail-layout"><Placeholder className="detail-cover"/><div className="detail-copy"><div className="eyebrow">{item.resourceType === 'Journal' ? 'TẠP CHÍ SỐ' : item.resourceType === 'Open_Access' ? 'TÀI LIỆU MỞ' : 'SÁCH VẬT LÝ'}</div><h1>{item.title}</h1><dl className="detail-metadata"><div><dt>Tác giả</dt><dd>{item.authors || 'Chưa cập nhật'}</dd></div><div><dt>Ngày xuất bản</dt><dd>{item.publishYear || 'Chưa cập nhật'}</dd></div><div><dt>Mã tài liệu</dt><dd>{item.resourceId}</dd></div><div><dt>Loại</dt><dd>{item.resourceType}</dd></div>{item.isbn && <div><dt>ISBN</dt><dd>{item.isbn}</dd></div>}{item.doi && <div><dt>DOI</dt><dd>{item.doi}</dd></div>}</dl>{licenseDescription && <div className="detail-description"><h2>Giấy phép</h2><p>{licenseDescription}</p></div>}{item.resourceType === 'Journal' && <div className="detail-description"><h2>Thông tin tạp chí</h2><p>{item.subjectArea || 'Chưa cập nhật'}</p><p className="form-note">Lượt xem đã được ghi vào lịch sử truy cập.</p></div>}</div></div>}</section>
}
function MemberAccount() {
  const { session } = useAuth()
  const [filter, setFilter] = useState('Tất cả')
  const [profile, setProfile] = useState(null)
  const [loans, setLoans] = useState([])
  const [loading, setLoading] = useState(true)
  const [error, setError] = useState('')
  useEffect(() => {
    if (session?.role !== 'Member') return undefined
    let active = true
    setLoading(true)
    Promise.all([
      apiRequest('/api/member/profile', { token: session.accessToken }),
      apiRequest('/api/member/loans', { token: session.accessToken }),
    ]).then(([memberProfile, memberLoans]) => {
      if (!active) return
      setProfile(memberProfile)
      setLoans(memberLoans)
    }).catch(apiError => { if (active) setError(apiError.message) })
      .finally(() => { if (active) setLoading(false) })
    return () => { active = false }
  }, [session?.accessToken, session?.role])
  if (session?.role !== 'Member') return <section className="simple-page"><div className="eyebrow">TÀI KHOẢN THÀNH VIÊN</div><h1>Đăng nhập để xem thông tin</h1><p>Đăng nhập bằng mã thành viên để xem thông tin cá nhân và lịch sử mượn sách.</p><Link className="primary-button" to="/dang-nhap">Đăng nhập thành viên <Icon name="arrow" size={16}/></Link></section>
  const filters = ['Tất cả', 'Đang mượn', 'Đã trả']
  const visibleLoans = loans.filter(loan => filter === 'Tất cả' || loan.statusLabel === filter)
  return <section className="account-page"><div className="eyebrow">KHU VỰC THÀNH VIÊN</div><h1>Thông tin cá nhân</h1><div className="member-profile-card"><div className="member-avatar">{(profile?.fullName || session.identifier).slice(0, 1).toUpperCase()}</div><div className="member-fields"><div><span>Tên thành viên</span><b>{profile?.fullName || 'Đang cập nhật'}</b></div><div><span>Mã thành viên</span><b>{profile?.memberId || session.identifier}</b></div></div></div>
    {profile && <div className="member-fields profile-extra"><div><span>Ngày hết hạn thẻ</span><b>{profile.expiryDate || 'Chưa cập nhật'}</b></div><div><span>Sách đang mượn</span><b>{profile.currentBorrowedCount ?? 0}</b></div><div><span>Tổng tiền phạt chưa thanh toán</span><b>{Number(profile.totalUnpaidAmount || 0).toLocaleString('vi-VN')} ₫</b></div></div>}
    {error && <p className="login-error" role="alert">{error}</p>}
    <div className="loan-heading"><div><div className="eyebrow">THEO DÕI TÀI LIỆU</div><h2>Sách đang mượn &amp; lịch sử</h2></div><div className="loan-filters">{filters.map(item => <button key={item} className={filter === item ? 'category active' : 'category'} onClick={() => setFilter(item)}>{item}</button>)}</div></div>
    <div className="loan-status-legend"><span><i className="status-dot borrowed"/>Đang mượn</span><span><i className="status-dot returned"/>Đã trả</span><span><i className="status-dot debt"/>Đang nợ</span><span><i className="status-dot paid"/>Đã đóng</span></div>
    <div className="loan-table-wrap"><table className="loan-table"><thead><tr><th>Tên sách</th><th>Trạng thái</th><th>Ngày trả</th><th>Tiền phạt</th><th>Còn nợ</th><th>Thanh toán</th></tr></thead><tbody>{loading ? <tr><td colSpan="6" className="loan-empty">Đang tải lịch sử mượn…</td></tr> : visibleLoans.length ? visibleLoans.map(loan => { const fineAmount = Number(loan.fineAmount || 0); const unpaidFineAmount = Number(loan.unpaidFineAmount || 0); return <tr key={loan.loanedCopyId}><td>{loan.bookTitle}</td><td><span className={`loan-state ${loan.statusLabel === 'Đã trả' ? 'returned' : 'borrowed'}`}>{loan.statusLabel}</span></td><td>{loan.returnDate || '—'}</td><td>{fineAmount > 0 ? `${fineAmount.toLocaleString('vi-VN')} ₫` : 'Không có'}</td><td>{unpaidFineAmount > 0 ? `${unpaidFineAmount.toLocaleString('vi-VN')} ₫` : '0 ₫'}</td><td>{loan.paymentStatus || 'Không có'}</td></tr>}) : <tr><td colSpan="6" className="loan-empty">{error ? 'Không tải được lịch sử mượn.' : 'Chưa có dữ liệu sách mượn/trả.'}</td></tr>}</tbody></table></div>
  </section>
}
function About() { return <section className="simple-page"><div className="eyebrow">VỀ CHÚNG TÔI</div><h1>Tri thức dành cho mọi người.</h1><p>Thư viện Mở là không gian kết nối người đọc với các nguồn tài liệu học tập và nghiên cứu truy cập mở.</p></section> }
function NotFound() { const navigate = useNavigate(); return <section className="simple-page"><div className="eyebrow">404 · KHÔNG TÌM THẤY</div><h1>Trang này chưa có trong thư viện.</h1><button className="primary-button" onClick={() => navigate('/')}>Về trang chủ <Icon name="arrow" size={16}/></button></section> }

function Footer() { return <footer className="footer"><Link className="brand" to="/"><span className="brand-mark"><Icon name="book" size={19}/></span><span>Thư viện <b>Mở</b></span></Link><span>Tri thức mở, cơ hội rộng.</span><span>© 2026 Thư viện Mở</span></footer> }

export default function App() {
  return <div className="site-shell"><Header/><main><Routes><Route path="/" element={<Home/>}/><Route path="/tai-lieu" element={<Documents/>}/><Route path="/tai-lieu/:documentId" element={<DocumentDetail/>}/><Route path="/tai-khoan" element={<MemberAccount/>}/><Route path="/quan-ly-thu-vien" element={<LibrarianDashboard/>}/><Route path="/gioi-thieu" element={<About/>}/><Route path="/dang-nhap" element={<Login/>}/><Route path="/dang-ky" element={<MemberRegistration/>}/><Route path="/dang-nhap-thu-thu" element={<LibrarianLogin/>}/><Route path="*" element={<NotFound/>}/></Routes></main><Footer/></div>
}
