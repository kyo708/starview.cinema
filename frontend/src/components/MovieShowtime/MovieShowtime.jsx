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
    const year = date.getFullYear(); // Lấy năm theo giờ Local
    

    const displayLabel = i === 0 ? 'Hôm nay' : `${dayOfWeek}, ${day}/${month}`;
    
    days.push({
        key: `${year}-${month}-${day}`, // Ghép chuỗi YYYY-MM-DD chuẩn theo giờ địa phương
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

  // State lưu danh sách rạp và suất chiếu lấy từ API
  const [cinemasData, setCinemasData] = useState([]);

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

  // Gọi API lấy lịch chiếu khi đổi ngày hoặc phim
  useEffect(() => {
    if (!id || !selectedDate) return;

    fetch(`http://localhost:8080/api/v1/suat-chieu/phim/${id}?date=${selectedDate}`)
      .then(res => res.json())
      .then(data => {

        // Backend trả về object 
        const showtimesList = data.showtimes || [];

        // Gom nhóm mảng phẳng từ Backend thành cấu trúc hiển thị UI
        const groupedData = showtimesList.reduce((acc, curr) => {

          const cinemaName = "StarView Cinemas";
          // Trích xuất định dạng chiếu từ tên phòng (VD: "P01 - IMAX" -> "IMAX")
          const format = curr.tenPhong && curr.tenPhong.includes('-') ? curr.tenPhong.split('-')[1].trim() : "2D Phụ Đề"; 
          // Lấy chuỗi giờ (vd: "2024-05-26T19:30:00" -> "19:30")
          const time = curr.thoiGianChieu ? curr.thoiGianChieu.substring(11, 16) : "00:00"; 

          let cinema = acc.find(c => c.name === cinemaName);
          if (!cinema) {
            cinema = { name: cinemaName, showtimes: [] };
            acc.push(cinema);
          }

          let showtimeGroup = cinema.showtimes.find(s => s.format === format);
          if (!showtimeGroup) {
            showtimeGroup = { format: format, times: [] };
            cinema.showtimes.push(showtimeGroup);
          }

          // SỬA Ở ĐÂY: Lưu cả chuỗi giờ và ID của suất chiếu
          showtimeGroup.times.push({
            time: time,
            id: curr.id || curr.suatChieuId 
          });
          return acc;
        }, []);

        setCinemasData(groupedData);
      })
      .catch(err => {
        console.error("Lỗi tải lịch chiếu:", err);
      });
  }, [id, selectedDate]);

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
            {cinemasData.length === 0 ? (
              <p style={{textAlign: 'center', color: '#888', marginTop: '20px', fontSize: '1.1rem'}}>Chưa có suất chiếu nào trong ngày này.</p>
            ) : cinemasData.map((cinema) => (
              <div key={cinema.name} className="cinema-card">
                <h5 className="cinema-name">{cinema.name}</h5>
                <div className="showtime-formats">
                  {cinema.showtimes.map((show) => (
                    <div key={show.format} className="format-row">
                      <span className="format-label">{show.format}</span>
                      <div className="time-slots">
                        {/* SỬA Ở ĐÂY: Sử dụng object `t` (bao gồm t.time và t.id) để truyền vào URL */}
                        {show.times.map((t, index) => (
                          <button 
                            key={index} 
                            className="time-btn available" 
                            onClick={() => navigate(`/phim/${id}/seatselection?cinema=${encodeURIComponent(cinema.name)}&time=${t.time}&date=${selectedDate}&suatChieuId=${t.id}`)}
                          >{t.time}</button>
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