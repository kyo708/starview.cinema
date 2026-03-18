import React, { useState } from 'react';
import { useNavigate } from 'react-router-dom';
import './AdminLayout.css';
import AdminMovieManager from './AdminMovieManager';
import AdminShowtimeManager from './AdminShowtimeManager';

function AdminLayout() {
  const [activeTab, setActiveTab] = useState('movies'); // 'movies' hoặc 'showtimes'
  const navigate = useNavigate();

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
          <h1>{activeTab === 'movies' ? 'Quản Lý Phim' : 'Quản Lý Suất Chiếu'}</h1>
          <div className="user-info">Staff Account 👤</div>
        </header>
        
        <div className="content-body">
          {activeTab === 'movies' ? <AdminMovieManager /> : <AdminShowtimeManager />}
        </div>
      </main>
    </div>
  );
}

export default AdminLayout;