import React, { useState } from 'react';
import './Navbar.css';

function Navbar() {
  const [searchTerm, setSearchTerm] = useState('');

  const handleSearch = (e) => {
    if (e.key === 'Enter' || e.type === 'click') {
      if (searchTerm.trim()) {
        console.log("Đang tìm kiếm:", searchTerm);
      }
    }
  };

  return (
    <header className="gsc-header-container">
      <div className="gsc-main-nav">
        
        {/* NỬA TRÁI: Logo và Menu */}
        <div className="navbar-left">
          <div className="logo">
            <h1>StarView<span className="star">*</span></h1>
          </div>
          
          <ul className="nav-links">
            <li className="active">PHIM</li>
            <li>RẠP STARVIEW</li>
            <li>THÀNH VIÊN</li>
            <li>CULTUREPLEX</li>
          </ul>
        </div>

        {/* NỬA PHẢI: Search, Login icon & CTA Button */}
        <div className="navbar-right">
          
          <div className="search-box">
            <input 
              type="text" 
              placeholder="Tìm phim..." 
              value={searchTerm}
              onChange={(e) => setSearchTerm(e.target.value)}
              onKeyDown={handleSearch}
            />
            <button onClick={handleSearch}>
              <svg xmlns="http://www.w3.org/2000/svg" width="16" height="16" viewBox="0 0 24 24" fill="none" stroke="currentColor" strokeWidth="2" strokeLinecap="round" strokeLinejoin="round" >
                <circle cx="11" cy="11" r="8"></circle>
                <line x1="21" y1="21" x2="16.65" y2="16.65"></line>
              </svg>
            </button>
          </div>

          {/* Icon Đăng nhập  */}
          <div className="login-icon">
            <svg viewBox="0 0 24 24" fill="none" stroke="currentColor" strokeWidth="2"><path d="M20 21v-2a4 4 0 0 0-4-4H8a4 4 0 0 0-4 4v2"></path><circle cx="12" cy="7" r="4"></circle></svg>
          </div>
          
          <div className="buy-ticket-btn">
             🎟️ MUA VÉ NGAY
          </div>
        </div>

      </div>
    </header>
  );
}

export default Navbar;