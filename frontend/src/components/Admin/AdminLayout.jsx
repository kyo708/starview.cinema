import React, { useState, useEffect } from 'react';
import { useNavigate } from 'react-router-dom';
import { jwtDecode } from 'jwt-decode';
import './AdminLayout.css';
import AdminMovieManager from './AdminMovieManager';
import AdminShowtimeManager from './AdminShowtimeManager';
import AdminVoucherManager from './AdminVoucherManager';
import AdminStaffManager from './AdminStaffManager';

function AdminLayout() {
  const [activeTab, setActiveTab] = useState('movies'); // 'movies', 'showtimes', hoặc 'vouchers'
  const navigate = useNavigate();
  const [isAdmin, setIsAdmin] = useState(false);
  const [userRole, setUserRole] = useState('Staff');
  const [userName, setUserName] = useState('');

  useEffect(() => {
    const token = sessionStorage.getItem('token');
    if (token) {
      const decoded = jwtDecode(token);
      const roleStr = decoded.role ? decoded.role.toUpperCase() : '';
      setIsAdmin(roleStr.includes('ADMIN'));
      setUserRole(roleStr.includes('ADMIN') ? 'Admin' : 'Staff');
      
      const extractedName = decoded.hoTen || decoded.fullName || decoded.name || decoded.sub;
      setUserName(extractedName || 'Nhân viên');
    }
  }, []);

  const handleLogout = () => {
    sessionStorage.removeItem('token');
    navigate('/login');
  };

  return (
    <div className="admin-dashboard">
      {/* LEFT SIDEBAR */}
      <aside className="admin-sidebar">
        <div className="sidebar-brand">
          <span className="brand-icon">🎬</span>
          <h2>GSC Admin</h2>
        </div>
        
        <nav className="sidebar-nav">
          <button 
            className={`nav-item ${activeTab === 'movies' ? 'active' : ''}`}
            onClick={() => setActiveTab('movies')}
          >
            <span className="icon">🎥</span> Quản lý Phim
          </button>
          
          <button 
            className={`nav-item ${activeTab === 'showtimes' ? 'active' : ''}`}
            onClick={() => setActiveTab('showtimes')}
          >
            <span className="icon">📅</span> Quản lý Suất Chiếu
          </button>
            
          <button 
            className={`nav-item ${activeTab === 'vouchers' ? 'active' : ''}`}
            onClick={() => setActiveTab('vouchers')}
          >
            <span className="icon">🎟️</span> Quản lý Voucher
          </button>
            
          {isAdmin && (
            <button 
              className={`nav-item ${activeTab === 'staff' ? 'active' : ''}`}
              onClick={() => setActiveTab('staff')}
            >
              <span className="icon">👥</span> Quản lý Nhân Viên
            </button>
          )}
            
        </nav>

        <div className="sidebar-footer">
          <button className="btn-logout-sidebar" onClick={handleLogout}>
            🚪 Đăng Xuất
          </button>
        </div>
      </aside>

      {/* RIGHT CONTENT AREA */}
      <main className="admin-main-content">
        <header className="content-header">
          <h1>{activeTab === 'movies' ? 'Quản Lý Phim' : activeTab === 'showtimes' ? 'Quản Lý Suất Chiếu' : activeTab === 'vouchers' ? 'Quản Lý Voucher' : 'Quản Lý Nhân Viên'}</h1>
          <div className="user-info">Xin chào, <strong style={{color: '#ffb400'}}>{userName}</strong> ({userRole}) 👤</div>
        </header>
        
        <div className="content-body">
          {activeTab === 'movies' && <AdminMovieManager />}
          {activeTab === 'showtimes' && <AdminShowtimeManager />}
          {activeTab === 'vouchers' && <AdminVoucherManager />}
          {activeTab === 'staff' && isAdmin && <AdminStaffManager />}
        </div>
      </main>
    </div>
  );
}

export default AdminLayout;