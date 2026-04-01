import React, { useState, useEffect, useRef } from 'react';
import { useNavigate } from 'react-router-dom';
import './Navbar.css';

const baseUrl = import.meta.env.VITE_BASE_URL || 'http://localhost:8080';

function Navbar() {
  const [searchTerm, setSearchTerm] = useState('');
  const [searchResults, setSearchResults] = useState([]);
  const [isSearching, setIsSearching] = useState(false);
  const [isDropdownVisible, setIsDropdownVisible] = useState(false);
  const navigate = useNavigate();
  const searchContainerRef = useRef(null);

  // Tối ưu hóa: Thay vì tải tất cả phim về client, ta sẽ gọi API search của backend
  // Sử dụng Debounce để tránh gọi API liên tục mỗi khi gõ phím
  useEffect(() => {
    if (searchTerm.trim() === '') {
      setSearchResults([]);
      setIsDropdownVisible(false);
      setIsSearching(false);
      return;
    }

    setIsSearching(true);
    const delayDebounceFn = setTimeout(() => {
      const searchUrl = `${baseUrl}/api/v1/phim/search?keyword=${encodeURIComponent(searchTerm)}`;
      fetch(searchUrl)
        .then(res => res.json())
        .then(data => {
          setSearchResults(data);
          setIsDropdownVisible(true); // Luôn hiện dropdown để báo "Không tìm thấy" nếu cần
        })
        .catch(err => console.error("Lỗi tìm kiếm phim:", err))
        .finally(() => setIsSearching(false));
    }, 300); // Chờ 300ms sau khi người dùng ngừng gõ

    return () => clearTimeout(delayDebounceFn);
  }, [searchTerm]);

  // Handle clicks outside to close dropdown
  useEffect(() => {
    function handleClickOutside(event) {
      if (searchContainerRef.current && !searchContainerRef.current.contains(event.target)) {
        setIsDropdownVisible(false);
      }
    }
    document.addEventListener("mousedown", handleClickOutside);
    return () => {
      document.removeEventListener("mousedown", handleClickOutside);
    };
  }, [searchContainerRef]);

  const handleMovieSelect = (movieId) => {
    navigate(`/phim/${movieId}`);
    setSearchTerm('');
    setIsDropdownVisible(false);
  };

  const handleSearch = (e) => {
    // This function is just for the button click now.
    // It doesn't need to do much since the dropdown is the primary search UI.
    e.preventDefault();
    if (searchTerm.trim()) {
      console.log("Đang tìm kiếm:", searchTerm);
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
          
          <div className="search-container" ref={searchContainerRef}>
            <div className="search-box">
              <input 
                type="text" 
                placeholder="Tìm phim..." 
                value={searchTerm}
                onChange={(e) => setSearchTerm(e.target.value)}
                onFocus={() => {
                  if (searchTerm.trim() !== '') {
                    setIsDropdownVisible(true);
                  }
                }}
              />
              <button onClick={handleSearch}>
                <svg xmlns="http://www.w3.org/2000/svg" width="16" height="16" viewBox="0 0 24 24" fill="none" stroke="currentColor" strokeWidth="2" strokeLinecap="round" strokeLinejoin="round" >
                  <circle cx="11" cy="11" r="8"></circle>
                  <line x1="21" y1="21" x2="16.65" y2="16.65"></line>
                </svg>
              </button>
            </div>

            {isDropdownVisible && (
              <div className="search-results-dropdown">
                {isSearching ? (
                  <div className="search-result-item">Đang tìm...</div>
                ) : searchResults.length > 0 ? (
                  searchResults.slice(0, 5).map(movie => (
                    <div 
                      key={movie.id} 
                      className="search-result-item"
                      onClick={() => handleMovieSelect(movie.id)}
                    >
                      <img src={movie.posterUrl} alt={movie.tenPhim} className="search-result-poster" />
                      <span className="search-result-title">{movie.tenPhim}</span>
                    </div>
                  ))
                ) : (
                  <div className="search-result-item">Không tìm thấy kết quả.</div>
                )}
              </div>
            )}
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