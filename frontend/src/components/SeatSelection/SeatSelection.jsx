import React, { useState, useMemo, useEffect } from 'react';
import { useParams, useSearchParams, useNavigate } from 'react-router-dom';
import './SeatSelection.css';
import Seat from './Seat'; // Import component Seat mới

// Định nghĩa giá ghế (có thể lấy từ API trong tương lai)
const PRICING = {
  THUONG: 70000,
  VIP: 100000,
};

function SeatSelection() {
  const { id } = useParams(); // Get movie ID from URL
  const [searchParams] = useSearchParams(); // Get query params
  const navigate = useNavigate(); // Hook dùng để điều hướng

  // Extract data from URL
  const cinemaName = searchParams.get('cinema');
  const showtime = searchParams.get('time');
  const showdate = searchParams.get('date'); // e.g., "2024-05-26"
  const suatChieuId = searchParams.get('suatChieuId'); // Lấy ID suất chiếu từ URL

  const [movie, setMovie] = useState(null); // State for movie details
  const [seats, setSeats] = useState([]); // State lưu danh sách ghế 1 chiều từ API
  const [isSeatsLoading, setIsSeatsLoading] = useState(true);
  // State lưu các object ghế đang được chọn
  const [selectedSeats, setSelectedSeats] = useState([]); 

  // Fetch movie details when component mounts
  useEffect(() => {
    // This is the same fetch as in MovieShowtime
    fetch(`http://localhost:8080/api/v1/phim/${id}`)
      .then(res => {
        if (!res.ok) throw new Error("Không tìm thấy phim");
        return res.json();
      })
      .then(data => setMovie(data))
      .catch(err => console.error("Lỗi tải phim:", err));
  }, [id]);

  // Fetch danh sách ghế của suất chiếu
  useEffect(() => {
    if (!suatChieuId || suatChieuId === 'undefined') {
      console.error(" LỖI: suatChieuId không tồn tại trên URL!");
      setIsSeatsLoading(false);
      return;
    }

    const url = `http://localhost:8080/api/v1/suat-chieu/${suatChieuId}/ghe`;
    console.log(" Đang gọi API lấy ghế tại:", url);

    fetch(url)
      .then(res => {
        if (!res.ok) throw new Error("Lỗi tải danh sách ghế");
        return res.json();
      })
      .then(data => {
        console.log(" Dữ liệu ghế nhận được:", data);
        setSeats(data);
        setIsSeatsLoading(false);
      })
      .catch(err => {
        console.error(err);
        setIsSeatsLoading(false);
      });
  }, [suatChieuId]);

  // Thuật toán chuyển đổi mảng ghế 1D từ API thành lưới 2D để render
  const seatGrid = useMemo(() => {
    if (!seats || seats.length === 0) return [];

    const colsPerRow = 10; // Giả định mỗi hàng có 10 ghế
    const rows = [];
    let currentRow = [];
    const rowLabels = 'ABCDEFGHIJKLMNOPQRSTUVWXYZ';

    seats.forEach((seat, index) => {
      const rowIndex = Math.floor(index / colsPerRow);
      const colIndex = (index % colsPerRow) + 1;
      const seatLabel = `${rowLabels[rowIndex]}${colIndex}`;

      currentRow.push({
        ...seat,
        seatLabel, // Gán nhãn ghế (A1, A2...)
        // Chấp nhận cả TRONG và DANG_CHO (vì DANG_CHO cũng không được chọn tiếp)
        isSold: seat.trangThai !== 'TRONG' && seat.trangThai !== 'Trống', 
      });

      if (currentRow.length === colsPerRow || index === seats.length - 1) {
        rows.push({ rowLabel: rowLabels[rowIndex], seats: currentRow });
        currentRow = [];
      }
    });
    return rows;
  }, [seats]);

  const handleSeatClick = (seatObject) => {
    // Không cho click ghế đã bán
    if (seatObject.isSold) return;

    // Kiểm tra xem ghế đã được chọn chưa (dựa vào id)
    setSelectedSeats(currentSelected =>
      currentSelected.some(s => s.id === seatObject.id)
        ? currentSelected.filter(s => s.id !== seatObject.id) // Bỏ chọn
        : [...currentSelected, seatObject] // Thêm vào danh sách chọn
    );
  };
  
  // Tính tổng tiền động dựa trên loại ghế
  const totalPrice = useMemo(() => {
    return selectedSeats.reduce((total, seat) => {
      // Nếu không có giá cho loại ghế này, mặc định là giá THUONG
      const price = PRICING[seat.loaiGhe] || PRICING.THUONG;
      return total + price;
    }, 0);
  }, [selectedSeats, movie]);

  // Format date for display
  const formattedDate = useMemo(() => {
    if (!showdate) return '';
    const date = new Date(showdate);
    return date.toLocaleDateString('en-GB', { day: 'numeric', month: 'long', year: 'numeric' });
  }, [showdate]);

  // Loading state
  if (!movie) {
    return <div className="seat-selection-container" style={{textAlign: 'center', padding: '100px'}}>Đang tải dữ liệu suất chiếu...</div>;
  }
  if (!suatChieuId) {
    return <div className="seat-selection-container" style={{textAlign: 'center', padding: '100px', color: 'red'}}>Lỗi: Không tìm thấy thông tin suất chiếu. Vui lòng quay lại và chọn lại.</div>;
  }

  return (
    <div className="seat-selection-container">
      {/* Nút Back Navigation */}
      <div className="back-nav" onClick={() => navigate(-1)}>
        <svg width="20" height="20" viewBox="0 0 24 24" fill="none" stroke="currentColor" strokeWidth="2" strokeLinecap="round" strokeLinejoin="round">
          <path d="M19 12H5M12 19l-7-7 7-7"/>
        </svg>
        <span>Quay lại</span>
      </div>

      <div className="seat-header">
        <h2>{movie.tenPhim}</h2>
        <p>📍 {cinemaName}</p>
      </div>

      <div className="seat-main-layout">
        
        {/* LEFT PANEL: Show information */}
        <div className="left-panel">
          <div className="filter-group">
            <label>Date</label>
            <div className="date-picker">{formattedDate} 📅</div>
          </div>
          <div className="filter-group">
            <label>Time</label>
            <div className="time-grid">
              {/* Display the selected time as active */}
              <button className={`time-btn active`}>{showtime}</button>
            </div>
          </div>
        </div>

        {/* CENTER PANEL: Seat Map */}
        <div className="center-panel">
          <div className="screen-area">
            <div className="screen-curve"></div>
            <div className="screen-text">SCREEN</div>
          </div>

          {isSeatsLoading ? (
            <div style={{padding: '50px'}}>Đang tải sơ đồ ghế...</div>
          ) : (
            <div className="seat-grid-wrapper">
              {seatGrid.map(({ rowLabel, seats: seatsInRow }) => (
                <div key={rowLabel} className="seat-row">
                  <span className="row-label">{rowLabel}</span>
                  <div className="seats">
                    {seatsInRow.map(seat => {
                      const isSelected = selectedSeats.some(s => s.id === seat.id);
                      // Giả định lối đi sau ghế thứ 3 và 7
                      const isAisle = seat.seatLabel.endsWith('3') || seat.seatLabel.endsWith('7');
                      return (
                        <React.Fragment key={seat.id}>
                          <Seat 
                            seatId={seat.seatLabel}
                            loaiGhe={seat.loaiGhe}
                            isSold={seat.isSold}
                            isSelected={isSelected}
                            onClick={() => handleSeatClick(seat)}
                          />
                          {isAisle && <div className="aisle-gap"></div>}
                        </React.Fragment>
                      );
                    })}
                  </div>
                  <span className="row-label">{rowLabel}</span>
                </div>
              ))}
            </div>
          )}

          <div className="seat-legend">
            <div className="legend-item">
              <div className="legend-seat-wrapper"><Seat loaiGhe="THUONG" /></div> Thường
            </div>
            <div className="legend-item">
              <div className="legend-seat-wrapper"><Seat loaiGhe="VIP" /></div> VIP
            </div>
            <div className="legend-item">
              <div className="legend-seat-wrapper"><Seat isSold={false} isSelected={true} /></div> Selected
            </div>
            <div className="legend-item">
              <div className="legend-seat-wrapper"><Seat isSold={true} isSelected={false} /></div> Sold
            </div>
          </div>
        </div>

        {/* RIGHT PANEL: Order Summary */}
        <div className="right-panel">
          <div className="summary-card">
            <h3>Order Summary</h3>
            
            <div className="summary-details">
              <p><span>Movie</span>{movie.tenPhim}</p>
              <p><span>Date & Time</span>{formattedDate}, {showtime}</p>
              
              <div>
                <span>Seats Selected ({selectedSeats.length})</span>
                <div className="selected-seats-list">
                  {selectedSeats.length > 0 
                    ? selectedSeats.map(seat => <span key={seat.id} className="seat-pill">{seat.seatLabel}</span>) 
                    : <span style={{color: '#888', fontSize: '0.9rem'}}>No seats selected</span>}
                </div>
              </div>
            </div>
            
            <div className="total-price">
              <span>Total Price</span>
              <span>{totalPrice.toLocaleString('vi-VN')} ₫</span>
            </div>
            
            <button 
              className="btn-proceed" 
              disabled={selectedSeats.length === 0}
              onClick={() => navigate('/payment', {
                state: {
                  movie,
                  cinemaName,
                  formattedDate,
                  showtime,
                  selectedSeats,
                  totalPrice,
                }
              })}
            >
              Proceed to Payment
            </button>
          </div>
        </div>

      </div>
    </div>
  );
}

export default SeatSelection;