import React, { useState, useEffect, useMemo } from 'react';
import { useParams, useNavigate } from 'react-router-dom';
import './MovieShowtime.css';
import TrailerModal from '../TrailerModal/TrailerModal';

// Helper function để tạo danh sách 7 ngày tiếp theo
const getNext7Days = () => {
  const days = [];
  const today = new Date();
  const dayNames = ['Chủ Nhật', 'Thứ 2', 'Thứ 3', 'Thứ 4', 'Thứ 5', 'Thứ 6', 'Thứ 7'];

  for (let i = 0; i < 7; i++) {
    const date = new Date(today);
    date.setDate(today.getDate() + i);
    
    const dayOfWeek = dayNames[date.getDay()];
    const day = String(date.getDate()).padStart(2, '0');
    const month = String(date.getMonth() + 1).padStart(2, '0');
    
    // Dùng chữ "Hôm nay" cho ngày đầu tiên
    const displayLabel = i === 0 ? 'Hôm nay' : `${dayOfWeek}, ${day}/${month}`;
    
    days.push({
        key: date.toISOString().split('T')[0], // Key định danh, ví dụ: "2024-05-26"
        display: displayLabel
    });
  }
  return days;
};

function MovieShowtime() {
  const { id } = useParams();
  const navigate = useNavigate();
  const [movie, setMovie] = useState(null);
  const [isTrailerVisible, setIsTrailerVisible] = useState(false);

  // Tạo danh sách ngày và chỉ tính toán lại khi cần
  const availableDays = useMemo(() => getNext7Days(), []);
  const [selectedDate, setSelectedDate] = useState(availableDays[0].key);

  // Dữ liệu giả định mới cho các rạp và suất chiếu
  const mockCinemas = [
    {
      name: 'StarView GigaMall Thủ Đức',
      showtimes: [
        { format: '2D Phụ Đề', times: ['09:00', '11:30', '14:45', '19:00', '21:30'] },
        { format: 'IMAX 2D', times: ['10:00', '13:15', '16:30', '20:15'] }
      ]
    },
    { name: 'StarView Landmark 81', showtimes: [{ format: '2D Phụ Đề', times: ['09:30', '12:00', '18:00', '21:00'] }] },
    { name: 'StarView Crescent Mall', showtimes: [{ format: '2D Phụ Đề', times: ['10:30', '15:00'] }, { format: '4DX', times: ['11:00', '19:30'] }] }
  ];

  useEffect(() => {
    // Cuộn lên đầu trang mỗi khi vào chi tiết phim
    window.scrollTo(0, 0);

    // Gọi API lấy thông tin phim theo ID
    fetch(`http://localhost:8080/api/v1/phim/${id}`)
      .then(res => {
        if (!res.ok) throw new Error("Không tìm thấy phim");
        return res.json();
      })
      .then(data => setMovie(data))
      .catch(err => console.error("Lỗi tải phim:", err));
  }, [id]);

  if (!movie) return <div className="showtime-container" style={{padding: '100px', textAlign: 'center'}}>Đang tải thông tin phim...</div>;

  return (
    <div className="showtime-container">
      {/* Nút Back về trang Catalog */}
      <div className="back-nav" onClick={() => navigate('/')}>
        <svg width="20" height="20" viewBox="0 0 24 24" fill="none" stroke="currentColor" strokeWidth="2" strokeLinecap="round" strokeLinejoin="round">
          <path d="M19 12H5M12 19l-7-7 7-7"/>
        </svg>
        <span>Quay lại</span>
      </div>

      {/* 1. Phần Header thông tin phim (Sử dụng dữ liệu thật) */}
      <section className="movie-hero" style={{backgroundImage: `linear-gradient(to bottom, rgba(0,0,0,0.6), #0f0f0f), url(${movie.posterUrl})`}}>
        <div className="showtime-hero-content">
          <img src={movie.posterUrl} alt={movie.tenPhim} className="mini-poster" />
          <div className="movie-desc">
            <h1>{movie.tenPhim}</h1>
            <div className="movie-meta-row">
              <span className="badge">PG-13</span> 
              <span className="movie-info-text">{movie.thoiLuongPhut} phút</span>
              <span className="separator">•</span>
              <span className="movie-info-text">{movie.theLoai}</span>
            </div>
            <div className="rating-row">
               <span className="star-icon">⭐</span> <span className="score">{movie.danhGia}</span> <span className="score-label">/ 10</span>
            </div>
          </div>
        </div>
      </section>

      <div className="main-content">
        {/* 2. Sidebar hiển thị Movie Detail */}
        <aside className="movie-detail-sidebar">
          <h3>Nội dung phim</h3>
          <p className="description-text">
            {movie.moTa ? movie.moTa : "Đang cập nhật mô tả..."}
          </p>
          {movie.trailerUrl && (
            <button className="btn-trailer" onClick={() => setIsTrailerVisible(true)}>
              ▶ Xem Trailer
            </button>
          )}
        </aside>

        {/* 3. Khu vực chọn lịch chiếu chính */}
        <main className="showtime-main">
          <div className="filter-section">
            <h4>Ngày chiếu</h4>
            <div className="date-grid">
              {availableDays.map(day => (
                <button 
                  key={day.key}
                  className={`date-btn ${selectedDate === day.key ? 'active' : ''}`}
                  onClick={() => setSelectedDate(day.key)}
                >
                  {day.display}
                </button>
              ))}
            </div>
          </div>

          {/* Danh sách rạp và giờ chiếu */}
          <div className="cinema-list">
            {mockCinemas.map((cinema) => (
              <div key={cinema.name} className="cinema-card">
                <h5 className="cinema-name">{cinema.name}</h5>
                <div className="showtime-formats">
                  {cinema.showtimes.map((show) => (
                    <div key={show.format} className="format-row">
                      <span className="format-label">{show.format}</span>
                      <div className="time-slots">
                        {show.times.map((time, index) => (
                          <button key={index} className="time-btn available" onClick={() => alert(`Bạn chọn suất: ${time}`)}>{time}</button>
                        ))}
                      </div>
                    </div>
                  ))}
                </div>
              </div>
            ))}
          </div>
        </main>
      </div>

      {/* Hiển thị Popup Trailer nếu isTrailerVisible = true */}
      {isTrailerVisible && (
        <TrailerModal
          trailerUrl={movie.trailerUrl}
          onClose={() => setIsTrailerVisible(false)}
        />
      )}
    </div>
  );
}

export default MovieShowtime;