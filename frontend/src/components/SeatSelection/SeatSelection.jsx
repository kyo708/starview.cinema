import React, { useState, useMemo, useEffect, useRef } from 'react';
import { useParams, useSearchParams, useNavigate } from 'react-router-dom';
import './SeatSelection.css';
import Seat from './Seat'; // Import component Seat mới

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
  const [showtimeData, setShowtimeData] = useState(null);
  const [isSeatsLoading, setIsSeatsLoading] = useState(true); // Trạng thái đang tải sơ đồ ghế
  // Trạng thái để vô hiệu hóa nút "Proceed to Payment" khi đang chuyển hướng
  const [countdown, setCountdown] = useState(0); // Thời gian đếm ngược (giây)
  const countdownIntervalRef = useRef(null); // Ref để lưu ID của setInterval
  const [isProcessingPayment, setIsProcessingPayment] = useState(false); 

  // State lưu các object ghế đang được chọn
  const [selectedSeats, setSelectedSeats] = useState(() => {
    const savedCart = sessionStorage.getItem('STARVIEW_CART');
    return savedCart ? JSON.parse(savedCart) : [];
  });

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

  // Generate a unique session ID once when the component mounts
  const sessionIdRef = useRef(
    sessionStorage.getItem('STARVIEW_SESSION_ID') || crypto.randomUUID()
  );

  useEffect(() => {
    sessionStorage.setItem('STARVIEW_SESSION_ID', sessionIdRef.current);
  }, []);

  // useEffect để lưu countdown vào sessionStorage mỗi khi nó thay đổi
  useEffect(() => {
    if (countdown > 0) {
      sessionStorage.setItem('STARVIEW_COUNTDOWN', countdown.toString());
    } else {
      sessionStorage.removeItem('STARVIEW_COUNTDOWN');
    }
  }, [countdown]);

  // Fetch thông tin chi tiết của Suất chiếu (để lấy heSoGia)
  useEffect(() => {
    if (!suatChieuId || suatChieuId === 'undefined') return;

    fetch(`http://localhost:8080/api/v1/suat-chieu/${suatChieuId}`)
      .then(res => {
        if (!res.ok) throw new Error("Không tìm thấy suất chiếu");
        return res.json();
      })
      .then(data => setShowtimeData(data))
      .catch(err => console.error("Lỗi tải suất chiếu:", err));
  }, [suatChieuId]);

  // Function to fetch seats from the backend
  const fetchSeats = () => {
    if (!suatChieuId || suatChieuId === 'undefined') {
      console.error(" LỖI: suatChieuId không tồn tại trên URL!");
      setIsSeatsLoading(false);
      return;
    }
    setIsSeatsLoading(true); // Set loading true while fetching
    const url = `http://localhost:8080/api/v1/suat-chieu/${suatChieuId}/ghe`;
    console.log(" Đang gọi API lấy ghế tại:", url);
    fetch(url)
      .then(res => {
        if (!res.ok) throw new Error("Lỗi tải danh sách ghế");
        return res.json();
      })
      .then(data => {
        setSeats(data);
        // 3. Khôi phục giỏ hàng: Chỉ giữ lại những ghế Backend báo là DANG_CHO
        setSelectedSeats(prevCart => {
            const validSeats = prevCart.map(savedSeat => {
                const backendSeat = data.find(s => Number(s.id) === Number(savedSeat.id));
                if (backendSeat && backendSeat.trangThai === 'DANG_CHO') {
                    return { ...backendSeat, seatLabel: savedSeat.seatLabel };
                }
                return null;
            }).filter(Boolean);
            
            sessionStorage.setItem('STARVIEW_CART', JSON.stringify(validSeats));
            return validSeats;
        });
      })
      .catch(err => {
        console.error(err);
      })
      .finally(() => {
        setIsSeatsLoading(false); // Always set loading to false after fetch attempt
      });
  };
  // Fetch danh sách ghế của suất chiếu khi component mount hoặc suatChieuId thay đổi
  useEffect(() => {
    fetchSeats();
  }, [suatChieuId]);

  // useEffect để quản lý bộ đếm ngược
  useEffect(() => {
    // Nếu có ghế được chọn VÀ bộ đếm chưa chạy
    if (selectedSeats.length > 0 && countdownIntervalRef.current === null) {
      // Lấy giá trị đếm ngược từ sessionStorage, nếu không có thì mặc định 300 giây
      const savedCountdown = parseInt(sessionStorage.getItem('STARVIEW_COUNTDOWN') || '0', 10);
      const startValue = savedCountdown > 0 ? savedCountdown : 300;
      setCountdown(startValue); // Cập nhật state countdown

      countdownIntervalRef.current = setInterval(() => {
        setCountdown(prev => { // Sử dụng functional update để lấy giá trị prev mới nhất
          if (prev <= 1) {
            clearInterval(countdownIntervalRef.current);
            countdownIntervalRef.current = null;
            sessionStorage.removeItem('STARVIEW_COUNTDOWN'); // Xóa khỏi storage khi hết giờ
            handleTimeout();
            return 0;
          }
          return prev - 1;
        });
      }, 1000);
    } else if (selectedSeats.length === 0 && countdownIntervalRef.current !== null) {
      // Nếu không còn ghế nào được chọn, dừng bộ đếm và reset
      clearInterval(countdownIntervalRef.current);
      countdownIntervalRef.current = null;
      setCountdown(0);
      sessionStorage.removeItem('STARVIEW_COUNTDOWN'); // Xóa khỏi storage
    }

    // Cleanup function: Đảm bảo xóa interval khi component unmount hoặc useEffect chạy lại
    return () => {
      if (countdownIntervalRef.current) {
        clearInterval(countdownIntervalRef.current);
        countdownIntervalRef.current = null;
      }
    };
  }, [selectedSeats.length]); // Dependency array: chỉ chạy lại khi số lượng ghế được chọn thay đổi

  const handleTimeout = async () => {
    alert("Thời gian giữ ghế đã hết. Các ghế bạn chọn đã được tự động nhả. Vui lòng chọn lại.");
    // Backend sẽ tự động nhả ghế sau 5 phút, nhưng ta cũng cần cập nhật UI
    setSelectedSeats([]); // Xóa các ghế đã chọn trên UI
    fetchSeats(); // Tải lại sơ đồ ghế để cập nhật trạng thái
  };


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

      const isCurrentlySelected = selectedSeats.some(s => Number(s.id) === Number(seat.id));

      currentRow.push({
        ...seat,
        seatLabel, 
        // 4. LUẬT MỚI: Chỉ khóa NẾU (đã bán thật) HOẶC (đang chờ NHƯNG KHÔNG PHẢI của mình)
        isSold: seat.trangThai === 'DA_BAN' || (seat.trangThai === 'DANG_CHO' && !isCurrentlySelected),
      });

      if (currentRow.length === colsPerRow || index === seats.length - 1) {
        rows.push({ rowLabel: rowLabels[rowIndex], seats: currentRow });
        currentRow = [];
      }
    });
    return rows;
  }, [seats, selectedSeats]);

  const handleSeatClick = async (seatObject) => {
    // Không cho click ghế đã bán
    if (seatObject.isSold) return;

    const currentSessionId = sessionIdRef.current;
    const isCurrentlySelected = selectedSeats.some(s => s.id === seatObject.id);
    const url = isCurrentlySelected ? 'http://localhost:8080/api/v1/bookings/release' : 'http://localhost:8080/api/v1/bookings/hold';

    try {
      const response = await fetch(url, {
        method: 'POST',
        headers: {
          'Content-Type': 'application/json',
        },
        body: JSON.stringify({
          seatId: seatObject.id,
          sessionId: currentSessionId
        })
      });

     if (response.ok) {
        // 5. Cập nhật giỏ hàng và lưu ngay xuống ổ cứng
        setSelectedSeats(currentSelected => {
          let newSelected = isCurrentlySelected 
            ? currentSelected.filter(s => Number(s.id) !== Number(seatObject.id))
            : [...currentSelected, seatObject];
            
          sessionStorage.setItem('STARVIEW_CART', JSON.stringify(newSelected));
          return newSelected;
        });

        // 6. Tự động đổi trạng thái ghế trên sơ đồ để mở khóa màu sắc tức thì
        setSeats(prevSeats => prevSeats.map(s => {
          if (Number(s.id) === Number(seatObject.id)) {
            return { ...s, trangThai: isCurrentlySelected ? 'TRONG' : 'DANG_CHO' };
          }
          return s;
        }));
      } else {
        // If API call fails, get error message and alert user
        const errorMsg = await response.text(); 
        alert(errorMsg || "Có lỗi xảy ra khi cập nhật trạng thái ghế. Vui lòng thử lại.");
        // Re-fetch seats to ensure UI reflects the actual backend state
        fetchSeats(); 
      }
    } catch (error) {
      console.error("Lỗi khi gọi API khóa/mở khóa ghế:", error);
      alert("Không thể kết nối tới hệ thống. Vui lòng thử lại sau.");
      // Re-fetch seats in case of network error to update UI
      fetchSeats(); 
    }
  };

  const handleProceedToPayment = () => {
    if (selectedSeats.length === 0) {
      alert("Vui lòng chọn ít nhất một ghế để tiếp tục.");
      return;
    }

    setIsProcessingPayment(true); // Indicate that navigation is in progress

    // Navigate directly to the payment page, as seats are already held
    navigate('/payment', {
      state: {
        movie,
        cinemaName,
        formattedDate,
        showtime,
        selectedSeats: selectedSeats.map(s => s.seatLabel), // Pass seat labels for display
        selectedSeatIds: selectedSeats.map(s => s.id),      // Pass seat IDs for backend
        totalPrice,
        sessionId: sessionIdRef.current, // Pass the current session ID for final checkout
        currentCountdown: countdown, // Pass the current countdown value
        suatChieuId: suatChieuId, // Pass for navigation back
        showdate: showdate // Pass for navigation back
      }
    });
  };
  
  // Tính tổng tiền động dựa trên loại ghế
const totalPrice = useMemo(() => {
    return selectedSeats.reduce((total, seat) => {
      // Bê nguyên con số giaTien mà Spring Boot đã tính toán sẵn đắp vào
      return total + (seat.giaTien || 0); 
    }, 0);
  }, [selectedSeats]);

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
                      const isAisle = seat.seatLabel.endsWith('2') || seat.seatLabel.endsWith('8');
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

              {selectedSeats.length > 0 && (
                <p className="countdown-timer">
                  Thời gian giữ ghế: {Math.floor(countdown / 60).toString().padStart(2, '0')}:{(countdown % 60).toString().padStart(2, '0')}
                </p>
              )}
            </div>
            
            <div className="total-price">
              <span>Total Price</span>
              <span>{totalPrice.toLocaleString('vi-VN')} ₫</span>
            </div>
            
            <button 
              className="btn-proceed" 
              disabled={selectedSeats.length === 0 || isProcessingPayment}
              onClick={handleProceedToPayment}
            >
              {isProcessingPayment ? 'Đang chuyển hướng...' : 'Proceed to Payment'}
            </button>
          </div>
        </div>

      </div>
    </div>
  );
}

export default SeatSelection;