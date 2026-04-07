import React, { useState, useEffect, useRef, useMemo } from 'react';
import { useLocation, useNavigate, Link, useSearchParams } from 'react-router-dom';
import { jwtDecode } from 'jwt-decode';
import Ticket from '../Ticket/Ticket';
import '../SeatSelection/SeatSelection.css';
import './Payment.css';

const baseUrl = import.meta.env.VITE_BASE_URL || 'http://localhost:8080';

function Payment() {
  const location = useLocation();
  const navigate = useNavigate();
  
  // Lấy dữ liệu từ SeatSelection truyền sang
  const { 
    movie, 
    cinemaName, 
    formattedDate, 
    showtime, 
    selectedSeats, // render UI
    selectedSeatIds, // gửi cho backend
    totalPrice,
    sessionId, // Nhận sessionId từ SeatSelection
    currentCountdown, // Nhận countdown từ SeatSelection
    suatChieuId, // Nhận để điều hướng ngược
    showdate // Nhận để điều hướng ngược
  } = location.state || {};

  // State cho Form thông tin khách hàng và thẻ tín dụng
  const [email, setEmail] = useState('');
  const [phone, setPhone] = useState('');
  const [isProcessing, setIsProcessing] = useState(false);
  const [showTicket, setShowTicket] = useState(false);
  const [ticketData, setTicketData] = useState(null);
  const [voucherCode, setVoucherCode] = useState('');
  const [isVoucherApplied, setIsVoucherApplied] = useState(false);
  const [finalPrice, setFinalPrice] = useState(totalPrice); // State cho giá cuối cùng
  const [discountAmount, setDiscountAmount] = useState(0); // State cho số tiền được giảm
  const [paymentMessage, setPaymentMessage] = useState(null); // Thay thế cho hộp thoại alert()
  const [voucherError, setVoucherError] = useState(''); // State cho lỗi voucher

  const [concessions, setConcessions] = useState([]);
  const [selectedConcessions, setSelectedConcessions] = useState({});
  const [userPoints, setUserPoints] = useState(0); // State lưu điểm hiện tại của user

  // Hook đọc tham số URL để bắt sự kiện VNPay trả về
  const [searchParams] = useSearchParams();
  const isVerifyingVNPay = searchParams.has('vnp_ResponseCode');
  const hasVerifiedVNPay = useRef(false); // Ngăn chặn lỗi React Strict Mode gọi API 2 lần

  // State và Ref cho bộ đếm ngược trên trang Payment
  const [paymentCountdown, setPaymentCountdown] = useState(currentCountdown || 0);
  const paymentCountdownIntervalRef = useRef(null);

  const isLoggedIn = !!sessionStorage.getItem('token');

  // Tự động điền email nếu người dùng đã đăng nhập
  useEffect(() => {
    const token = sessionStorage.getItem('token');
    if (token) {
      try {
        const decoded = jwtDecode(token);
        if (decoded.sub) setEmail(decoded.sub); // JWT mặc định lưu email ở trường 'sub'
        
        // Gọi API lấy điểm Real-time từ Database thay vì lấy điểm cũ từ Token
        fetch(`${baseUrl}/api/v1/auth/my-profile`, {
          headers: { 'Authorization': `Bearer ${token}` }
        })
        .then(res => res.json())
        .then(data => {
          if (data.diemTichLuy !== undefined) setUserPoints(data.diemTichLuy);
        })
        .catch(err => {
          console.error("Lỗi lấy điểm real-time:", err);
          // Fallback: nếu rớt mạng, dùng tạm điểm trong token
          if (decoded.diemTichLuy !== undefined) setUserPoints(decoded.diemTichLuy);
        });
      } catch (e) {
        console.error("Lỗi giải mã token:", e);
      }
    }
  }, []);

  // Hàm tạo header kèm Token để Backend có thể định danh người dùng
  const getAuthHeaders = () => {
    const token = sessionStorage.getItem('token');
    const headers = { 'Content-Type': 'application/json' };
    if (token) headers['Authorization'] = `Bearer ${token}`;
    return headers;
  };

  // Tải danh sách đồ ăn kèm từ Backend
  useEffect(() => {
    fetch(`${baseUrl}/api/v1/dich-vu`)
      .then(res => res.json())
      .then(data => setConcessions(data))
      .catch(err => console.error("Lỗi tải danh sách dịch vụ:", err));
  }, []);

  const handleConcessionChange = (id, delta) => {
    setSelectedConcessions(prev => {
      const currentQty = prev[id] || 0;
      const newQty = currentQty + delta;
      if (newQty <= 0) {
        const newState = { ...prev };
        delete newState[id];
        return newState;
      }
      return { ...prev, [id]: newQty };
    });
  };

  const totalPointsUsed = useMemo(() => {
    return Object.entries(selectedConcessions).reduce((total, [id, qty]) => {
      const item = concessions.find(c => c.id === parseInt(id));
      return total + (item ? item.diemDoi * qty : 0);
    }, 0);
  }, [selectedConcessions, concessions]);

  const selectedConcessionsString = useMemo(() => {
    return Object.entries(selectedConcessions).map(([id, qty]) => {
      const item = concessions.find(c => c.id === parseInt(id));
      return item ? `${item.tenDichVu} x${qty}` : '';
    }).join(', ');
  }, [selectedConcessions, concessions]);

  // useEffect 1: XỬ LÝ KHI VNPAY REDIRECT TRỞ LẠI FRONTEND
  useEffect(() => {
    if (isVerifyingVNPay && !hasVerifiedVNPay.current) {
      hasVerifiedVNPay.current = true; // Đánh dấu đã xác thực để lần render sau không gọi lại
      setIsProcessing(true);
      
      // Gọi Backend để xác thực chữ ký VNPay
      fetch(`${baseUrl}/api/v1/payments/vnpay-return?${searchParams.toString()}`)
        .then(res => res.json())
        .then(data => {
          if (data.status === 'success') {
            // Xóa giỏ hàng và đếm ngược
            sessionStorage.removeItem('STARVIEW_CART');
            sessionStorage.removeItem('STARVIEW_COUNTDOWN');

            // Phục hồi lại data vé từ sessionStorage (do khi redirect sang VNPay React bị mất state)
            const savedTicketData = sessionStorage.getItem('TEMP_TICKET_DATA');
            if (savedTicketData) {
              setTicketData(JSON.parse(savedTicketData));
              setShowTicket(true);
              sessionStorage.removeItem('TEMP_TICKET_DATA');
            } else {
              // Thay thế alert() bằng giao diện UI mượt mà
              setPaymentMessage({ isError: false, text: "Thanh toán thành công! Vui lòng kiểm tra email để nhận vé điện tử." });
            }
          } else {
            setPaymentMessage({ isError: true, text: "Giao dịch VNPay thất bại hoặc đã bị hủy bởi người dùng." });
            sessionStorage.removeItem('TEMP_TICKET_DATA');
          }
        })
        .catch(err => {
          console.error("Lỗi xác minh VNPay:", err);
          setPaymentMessage({ isError: true, text: "Đã xảy ra lỗi khi kết nối tới máy chủ để xác minh." });
        })
        .finally(() => {
          setIsProcessing(false);
        });
    }
  }, [searchParams, isVerifyingVNPay]);

  // useEffect để quản lý bộ đếm ngược trên trang Payment
  useEffect(() => {
    // Bỏ qua đếm ngược nếu đang trong trạng thái xác thực màn hình VNPay trả về
    if (isVerifyingVNPay) return;

    const handleTimeout = async () => {
      alert("Thời gian giữ ghế đã hết. Vui lòng chọn lại ghế.");
      
      // Xóa các ghế đã chọn và countdown khỏi sessionStorage khi hết giờ
      sessionStorage.removeItem('STARVIEW_CART');
      sessionStorage.removeItem('STARVIEW_COUNTDOWN');

      // Ép backend nhả toàn bộ ghế dựa vào danh sách ID ghế đã lưu ở bước trước
      if (selectedSeatIds && selectedSeatIds.length > 0 && sessionId) {
        const releasePromises = selectedSeatIds.map(seatId =>
          fetch(`${baseUrl}/api/v1/bookings/release`, {
            method: 'POST',
            headers: { 'Content-Type': 'application/json' },
            body: JSON.stringify({ seatId: seatId, sessionId: sessionId }),
          })
        );
        try {
          await Promise.allSettled(releasePromises);
          console.log("Đã yêu cầu backend nhả ghế do hết giờ ở màn hình thanh toán.");
        } catch (e) {
          console.error("Lỗi khi nhả ghế từ backend:", e);
        }
      }

      // Điều hướng về trang chọn ghế, đảm bảo truyền đủ params
      navigate(`/phim/${movie.id}/seatselection?cinema=${cinemaName}&time=${showtime}&date=${showdate}&suatChieuId=${suatChieuId}`);
    };

    if (paymentCountdown > 0 && paymentCountdownIntervalRef.current === null) {
      paymentCountdownIntervalRef.current = setInterval(() => {
        setPaymentCountdown(prev => {
          if (prev <= 1) {
            clearInterval(paymentCountdownIntervalRef.current);
            paymentCountdownIntervalRef.current = null;
            // THE FIX: Ép Backend nhả ghế ngay lập tức giống trang SeatSelection
            if (location.state?.selectedSeatIds) {
              location.state.selectedSeatIds.forEach(async (seatId) => {
                try {
                  await fetch(`${baseUrl}/api/v1/bookings/release`, {
                    method: 'POST',
                    headers: { 'Content-Type': 'application/json' },
                    body: JSON.stringify({ seatId: seatId, sessionId: sessionId })
                  });
                } catch (e) { console.error(e); }
              });
            }

            alert("Thời gian giữ ghế đã hết. Vui lòng chọn lại ghế.");
            sessionStorage.removeItem('STARVIEW_CART');
            sessionStorage.removeItem('STARVIEW_COUNTDOWN');
            navigate(`/phim/${movie.id}/seatselection?cinema=${cinemaName}&time=${showtime}&date=${showdate}&suatChieuId=${suatChieuId}`);
            return 0;
          }
          return prev - 1;
        });
      }, 1000);
    }

    return () => {
      if (paymentCountdownIntervalRef.current) {
        clearInterval(paymentCountdownIntervalRef.current);
        paymentCountdownIntervalRef.current = null;
      }
    };
  }, [paymentCountdown, navigate, movie, cinemaName, showtime, showdate, suatChieuId, selectedSeatIds, sessionId, isVerifyingVNPay]);

  // 1. ƯU TIÊN SỐ 1: NẾU ĐÃ THANH TOÁN THÀNH CÔNG -> HIỂN THỊ POPUP VÉ
  // (Đặt trên cùng để bypass mọi màn hình loading và tránh crash do mất state)
  if (showTicket && ticketData) {
    return (
      <div className="payment-container">
        <Ticket 
          ticketData={ticketData} 
          onClose={() => navigate('/')} 
        />
      </div>
    );
  }

  // 2. THÔNG BÁO UI (THAY THẾ CHO ALERT BLOCK TRÌNH DUYỆT)
  if (paymentMessage) {
    return (
      <div className="payment-container">
        <div className="payment-status-page">
          <h2 className={`payment-message-title ${paymentMessage.isError ? 'error' : 'success'}`}>
            {paymentMessage.isError ? 'Thanh toán không thành công' : 'Hoàn tất giao dịch'}
          </h2>
          <p className="payment-message-text">{paymentMessage.text}</p>
          <button className="btn-pay" onClick={() => navigate('/')}>Về trang chủ</button>
        </div>
      </div>
    );
  }

  // Màn hình chờ khi đang xác thực VNPay
  if (isVerifyingVNPay) {
    return (
      <div className="payment-container">
        <div className="payment-status-page">
          <h2>Đang xác thực thanh toán VNPay...</h2>
          <div className="loading-spinner"></div>
        </div>
      </div>
    );
  }

  // Chặn người dùng vào thẳng link /payment khi không có data
  if (!movie || !selectedSeats) {
    return (
      <div className="payment-container">
        <div className="payment-status-page">
          <h2>Không tìm thấy thông tin đơn hàng</h2>
          <button className="btn-pay" onClick={() => navigate('/')}>Về trang chủ</button>
        </div>
      </div>
    );
  }

  const handleApplyVoucher = async () => {
    if (!voucherCode.trim()) return;
    setVoucherError(''); // Reset lỗi trước khi gọi API

    try {
      const response = await fetch(`${baseUrl}/api/v1/vouchers/preview`, {
        method: 'POST',
        headers: getAuthHeaders(),
        body: JSON.stringify({
          voucherCode: voucherCode,
          originalPrice: totalPrice // Gửi giá gốc để backend tính toán
        })
      });

      const result = await response.json();

      if (!response.ok) {
        // Nếu backend trả về lỗi (vd: 400 Bad Request), ném lỗi với message từ backend
        throw new Error(result.message || 'Mã voucher không hợp lệ hoặc đã hết hạn.');
      }

      // Thành công
      setFinalPrice(result.discountedPrice);
      setDiscountAmount(result.discountAmount);
      setIsVoucherApplied(true);

    } catch (error) {
      // Hiển thị lỗi ngay dưới ô nhập voucher
      setVoucherError(error.message);
    }
  };

  const handlePayment = async (e) => {
    e.preventDefault();
    setIsProcessing(true);
    
    try {
      // 1. TẠO ĐƠN HÀNG CHỜ THANH TOÁN (PENDING)
      const response = await fetch(`${baseUrl}/api/v1/bookings/checkout`, {
        method: 'POST',
        headers: getAuthHeaders(),
        body: JSON.stringify({
          seatIds: location.state.selectedSeatIds,
          seatNames: selectedSeats, 
          sessionId: sessionId, 
          email: email, 
          phone: phone,
          voucherCode: isVoucherApplied ? voucherCode : null, // Gửi mã voucher nếu đã áp dụng
          danhSachDichVu: selectedConcessionsString || null,
          tongDiemSuDung: totalPointsUsed
        })
      });

      if (!response.ok) {
        const errorMsg = await response.text();
        throw new Error(errorMsg || "Khởi tạo đơn hàng thất bại. Vui lòng thử lại!");
      }

      const data = await response.json(); //{ message: "...", paymentUrl: "https://...", bookingRef: "DH5" }

      // Tạm ngưng bộ đếm trong lúc nhảy qua VNPay
      if (paymentCountdownIntervalRef.current) {
        clearInterval(paymentCountdownIntervalRef.current);
        paymentCountdownIntervalRef.current = null;
      }

      // 2. LƯU DỮ LIỆU VÉ TẠM THỜI VÀO SESSION STORAGE (ĐỂ PHỤC HỒI SAU KHI VNPAY REDIRECT VỀ)
      const tempTicketData = {
        movie, 
        cinemaName, 
        formattedDate, 
        showtime, 
        selectedSeats, // Vẫn truyền label A1, A2... để hiển thị trên vé
        totalPrice: finalPrice, // LƯU Ý: Dùng giá cuối cùng cho vé
        bookingRef: data.bookingRef, 
        customerInfo: { email, phone }
      };
      sessionStorage.setItem('TEMP_TICKET_DATA', JSON.stringify(tempTicketData));

      // 3. REDIRECT THẲNG SANG VNPAY
      if (data.paymentUrl) {
        window.location.href = data.paymentUrl; 
      } else {
        throw new Error("Không nhận được URL thanh toán từ hệ thống.");
      }

    } catch (error){
      console.error("Lỗi thanh toán: ", error);
      alert(error.message); // Hiển thị lỗi từ backend lên màn hình (US #9 - AC #37)
    } finally{
      setIsProcessing(false);
    }
  };

  return (
    <div className="payment-container">
      {/* Nút Back */}
      <div className="back-nav" onClick={() => navigate(-1)}>
        <svg width="20" height="20" viewBox="0 0 24 24" fill="none" stroke="currentColor" strokeWidth="2" strokeLinecap="round" strokeLinejoin="round">
          <path d="M19 12H5M12 19l-7-7 7-7"/>
        </svg>
        <span>Quay lại</span>
      </div>

      <div className="payment-content">
        {/* THÔNG BÁO ĐĂNG KÝ THÀNH VIÊN */}
        {!isLoggedIn && (
          <div className="member-promo-banner">
            <span>🌟 Đăng ký tài khoản thành viên để được tích lũy điểm khi mua vé!</span>
            <Link to="/register" className="promo-link">Đăng ký ngay</Link>
          </div>
        )}

        {/* CỘT TRÁI: FORM THANH TOÁN */}
        <div className="payment-form-section">
          <h2>Thanh Toán</h2>
          <p className="payment-subtitle">Vui lòng cung cấp thông tin liên hệ nhận vé</p>
          
          <form className="payment-form" onSubmit={handlePayment}>
            <h4 className="form-section-title">1. Thông tin liên hệ</h4>
            <div className="form-group">
              <label>Email nhận vé</label>
              <input 
                type="email" placeholder="example@gmail.com" required 
                value={email} onChange={(e) => setEmail(e.target.value)}
              />
            </div>
            <div className="form-group">
              <label>Số điện thoại</label>
              <input 
                type="tel" placeholder="" required maxLength="10"
                value={phone} onChange={(e) => setPhone(e.target.value.replace(/\D/g, ''))}
              />
            </div>

            <h4 className="form-section-title concession-section-title">2. Đổi điểm nhận đồ ăn kèm</h4>
            {!isLoggedIn ? (
              <p className="concession-login-prompt">Vui lòng đăng nhập để sử dụng điểm tích lũy đổi đồ ăn kèm.</p>
            ) : (
              <>
                <div className="current-points-display">
                  ✨ Điểm tích luỹ của bạn: <strong>{userPoints.toLocaleString('vi-VN')}</strong> điểm
                </div>
                <div className="concession-grid">
                  {concessions.map(item => (
                    <div key={item.id} className="concession-item">
                      <img src={item.hinhAnhUrl} alt={item.tenDichVu} className="concession-img" />
                      <div className="concession-info">
                        <h5 className="concession-name">{item.tenDichVu}</h5>
                        <p className="concession-points">{item.diemDoi} điểm</p>
                      </div>
                      <div className="concession-controls">
                        <button type="button" className="btn-qty minus" onClick={() => handleConcessionChange(item.id, -1)} disabled={!selectedConcessions[item.id]}>-</button>
                        <span className="qty-display">{selectedConcessions[item.id] || 0}</span>
                        <button 
                          type="button" className="btn-qty plus" onClick={() => handleConcessionChange(item.id, 1)}
                          disabled={(totalPointsUsed + item.diemDoi) > userPoints} // Chặn nếu cộng thêm món này làm vượt quá tổng điểm đang có
                        >+</button>
                      </div>
                    </div>
                  ))}
                </div>
              </>
            )}

            {/* Nút thanh toán sẽ disable nếu chưa nhập đủ chuẩn form */}
            <button type="submit" className="btn-pay" disabled={isProcessing || !email || phone.length < 10}>
              {isProcessing ? 'Đang kết nối VNPay...' : `Thanh toán VNPay (${finalPrice?.toLocaleString('vi-VN')} ₫)`}
            </button>
          </form>
        </div>

        {/* CỘT PHẢI: TÓM TẮT ĐƠN HÀNG  */}
        <div className="payment-summary-section">
          <div className="summary-card">
            <h3>Order Summary</h3>
            <div className="summary-details">
              <p><span>Movie</span>{movie.tenPhim}</p>
              <p><span>Cinema</span>{cinemaName}</p>
              <p><span>Date & Time</span>{formattedDate}, {showtime}</p>
              <div>
                <span>Seats Selected ({selectedSeats.length})</span>
                <div className="selected-seats-list">
                  {selectedSeats.map(seat => <span key={seat} className="seat-pill">{seat}</span>)}
                </div>
              </div>

              {paymentCountdown > 0 && (
                <p className="countdown-timer">
                  Thời gian giữ ghế: {Math.floor(paymentCountdown / 60).toString().padStart(2, '0')}:{(paymentCountdown % 60).toString().padStart(2, '0')}
                </p>
              )}
            </div>

          {/* NHẬP MÃ VOUCHER */}
          <div className="voucher-section">
            <input 
              type="text" placeholder="Nhập mã voucher..." 
              value={voucherCode} onChange={(e) => setVoucherCode(e.target.value.toUpperCase())}
              disabled={isVoucherApplied}
            />
            <button type="button" onClick={handleApplyVoucher} disabled={isVoucherApplied || !voucherCode.trim()}>
              {isVoucherApplied ? 'Đã áp dụng' : 'Áp dụng'}
            </button>
          </div>
          {/* Hiển thị lỗi voucher nếu có */}
          {voucherError && <p className="voucher-error-text">{voucherError}</p>}

          {/* Hiển thị chi tiết giá nếu đã áp dụng voucher */}
          {isVoucherApplied && (
            <div className="summary-discount-details">
              <p><span>Tạm tính</span>{totalPrice?.toLocaleString('vi-VN')} ₫</p>
              <p className="discount-amount"><span>Giảm giá</span>- {discountAmount?.toLocaleString('vi-VN')} ₫</p>
            </div>
          )}

            {totalPointsUsed > 0 && (
              <div className="summary-discount-details points-summary-section">
                <p className="discount-amount points-discount-amount"><span>Điểm sử dụng</span>- {totalPointsUsed} điểm</p>
                <p className="points-exchanged-items">Đổi: {selectedConcessionsString}</p>
              </div>
            )}

            <div className="total-price">
              <span>Thành tiền</span>
              <span>{finalPrice?.toLocaleString('vi-VN')} ₫</span>
            </div>
          </div>
        </div>
      </div>
    </div>
  );
}

export default Payment;