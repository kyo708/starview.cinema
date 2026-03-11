import React, { useState, useEffect, useRef, useMemo } from 'react';
import { useNavigate } from 'react-router-dom';
import './Catalog.css';
import MovieCard from '../MovieCard/MovieCard';

function Catalog() {
  const [movies, setMovies] = useState([]);
  const [loading, setLoading] = useState(true);
  
  // Trạng thái lưu vị trí phim đang được hiển thị trên Hero Banner
  const [activeIndex, setActiveIndex] = useState(0);
  const navigate = useNavigate();
  const carouselRef = useRef(null); // Ref cho carousel để xử lý kéo-thả

  useEffect(() => {
    window.scrollTo(0, 0);
    fetch('http://localhost:8080/api/v1/phim')
      .then(res => res.json())
      .then(data => {
        setMovies(data);
        setLoading(false);
      })
      .catch(err => {
        console.error(err);
        setLoading(false);
      });
  }, []);

  // Lấy ra top 10 phim để làm Trending
  const trendingMovies = useMemo(() => movies.slice(0, 10), [movies]);

  // Logic tự động trượt Banner mỗi 5 giây
  useEffect(() => {
    if (trendingMovies.length === 0) return;
    
    const timer = setInterval(() => {
      setActiveIndex((prevIndex) => (prevIndex + 1) % trendingMovies.length);
    }, 5000); // 5000ms = 5 giây

    // Dọn dẹp timer khi component bị hủy hoặc re-render
    return () => clearInterval(timer);
  }, [trendingMovies.length]);

  // Logic cuộn carousel bằng nút bấm
  const scrollCarousel = (direction) => {
    if (carouselRef.current) {
      const scrollAmount = 300; // Khoảng cách cuộn mỗi lần nhấn
      carouselRef.current.scrollBy({
        left: direction === 'left' ? -scrollAmount : scrollAmount,
        behavior: 'smooth'
      });
    }
  };

  // Xử lý khi click vào phim
  const handleMovieClick = (index) => {
    setActiveIndex(index);
  };

  if (loading) return <div className="loading-spinner"></div>;

  const activeMovie = trendingMovies[activeIndex];

  return (
    <div className="gsc-catalog">
      
      {/* PHẦN 1: HERO BANNER & TRENDING CAROUSEL */}
      {activeMovie && (
        <section className="hero-banner" style={{ backgroundImage: `url(${activeMovie.posterUrl})` }}>
          {/* Lớp phủ gradient để làm mờ ảnh nền và nổi chữ */}
          <div className="hero-overlay"></div>

          {/* Thông tin phim (Đưa ra khỏi wrapper dưới để căn chỉnh tự do) */}
          <div className="catalog-hero-content">
            <p className="hero-meta">{activeMovie.theLoai} • {activeMovie.thoiLuongPhut}m</p>
            <h1 className="hero-title">{activeMovie.tenPhim}</h1>
            <p className="hero-desc">
              Khám phá bom tấn điện ảnh với những pha hành động mãn nhãn và cốt truyện hấp dẫn nhất mùa hè này...
            </p>
            <div className="hero-actions">
              <button className="btn-view-more" onClick={() => navigate(`/phim/${activeMovie.id}`)}>
                Chi tiết
              </button>
              <button className="btn-book-now" onClick={() => navigate(`/phim/${activeMovie.id}`)}>
                Mua Vé Ngay 🎟️
              </button>
            </div>
          </div>

          {/* Wrapper cho toàn bộ nội dung ở dưới */}
          <div className="bottom-content-wrapper">
              {/* Header chứa Tiêu đề và Nút điều hướng */}
              <div className="carousel-header">
                <div className="carousel-title">Thịnh hành</div>
                <div className="carousel-nav-buttons">
  <button className="nav-btn" onClick={() => scrollCarousel('left')}>
    {/* Icon Mũi tên trái */}
    <svg viewBox="0 0 24 24" fill="none" stroke="currentColor" strokeWidth="2" strokeLinecap="round" strokeLinejoin="round">
      <polyline points="15 18 9 12 15 6"></polyline>
    </svg>
  </button>
  
  <button className="nav-btn" onClick={() => scrollCarousel('right')}>
    {/* Icon Mũi tên phải */}
    <svg viewBox="0 0 24 24" fill="none" stroke="currentColor" strokeWidth="2" strokeLinecap="round" strokeLinejoin="round">
      <polyline points="9 18 15 12 9 6"></polyline>
    </svg>
  </button>
</div>
              </div>
              
              <div className="carousel-track" ref={carouselRef}>
                {trendingMovies.map((movie, index) => (
                  <div 
                    key={movie.id} 
                    className={`mini-card ${index === activeIndex ? 'active' : ''}`}
                    onClick={() => handleMovieClick(index)}
                  >
                    <span className="mini-rank">{index + 1}</span>
                    {/* Thêm draggable="false" để tránh xung đột với sự kiện kéo của trình duyệt */}
                    <img src={movie.posterUrl} alt={movie.tenPhim} draggable="false" />
                    <div className="mini-title">{movie.tenPhim}</div>
                  </div>
                ))}
              </div>
          </div>

          {/* Các chấm điều hướng bên phải (Pagination Dots) */}
          <div className="hero-pagination">
            {trendingMovies.map((_, index) => (
              <div 
                key={index}
                className={`pagination-dot ${index === activeIndex ? 'active' : ''}`}
                onClick={() => setActiveIndex(index)}
              ></div>
            ))}
          </div>

        </section>
      )}

      {/* PHẦN 2: SHOWTIMES (Danh sách lưới như cũ) */}
      <section className="section-showtimes">
        <div className="catalog-tabs">
          <button className="catalog-tab active">Now showing</button>
          <button className="catalog-tab">Coming soon</button>
        </div>
        <div className="gsc-grid">
          {movies.slice(0, 100).map(movie => (
            <MovieCard key={movie.id} movie={movie} />
          ))}
        </div>
      </section>
    </div>
  );
}

export default Catalog;
