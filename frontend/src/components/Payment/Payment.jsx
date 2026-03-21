import React, { useState, useEffect, useRef } from 'react';
import { useLocation, useNavigate } from 'react-router-dom';
import Ticket from '../Ticket/Ticket';
import '../SeatSelection/SeatSelection.css';
import './Payment.css';

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
    selectedSeatsIds, // gửi cho backend
    totalPrice,
    sessionId, // Nhận sessionId từ SeatSelection
    currentCountdown, // Nhận countdown từ SeatSelection
    suatChieuId, // Nhận để điều hướng ngược
    showdate // Nhận để điều hướng ngược
  } = location.state || {};

  // State cho Form thông tin khách hàng và thẻ tín dụng
  const [email, setEmail] = useState('');
  const [phone, setPhone] = useState('');
  const [cardNumber, setCardNumber] = useState('');
  const [expiry, setExpiry] = useState('');
  const [cvv, setCvv] = useState('');
  const [isProcessing, setIsProcessing] = useState(false);
  const [showTicket, setShowTicket] = useState(false);
  const [ticketData, setTicketData] = useState(null);

  // State và Ref cho bộ đếm ngược trên trang Payment
  const [paymentCountdown, setPaymentCountdown] = useState(currentCountdown || 0);
  const paymentCountdownIntervalRef = useRef(null);

  // useEffect để quản lý bộ đếm ngược trên trang Payment
  useEffect(() => {
    if (paymentCountdown > 0 && paymentCountdownIntervalRef.current === null) {
      paymentCountdownIntervalRef.current = setInterval(() => {
        setPaymentCountdown(prev => {
          if (prev <= 1) {
            clearInterval(paymentCountdownIntervalRef.current);
            paymentCountdownIntervalRef.current = null;
            alert("Thời gian giữ ghế đã hết. Vui lòng chọn lại ghế.");
            // Xóa các ghế đã chọn và countdown khỏi sessionStorage khi hết giờ
            sessionStorage.removeItem('STARVIEW_CART');
            sessionStorage.removeItem('STARVIEW_COUNTDOWN');
            // Điều hướng về trang chọn ghế, đảm bảo truyền đủ params
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
  }, [paymentCountdown, navigate, movie, cinemaName, showtime, showdate, suatChieuId]); // Dependencies cho việc khởi tạo và điều hướng

  // Nếu người dùng vào thẳng link /payment mà không qua chọn ghế -> back về trang chủ
  if (!movie || !selectedSeats) {
    return (
      <div className="payment-container" style={{ textAlign: 'center', padding: '100px' }}>
        <h2>Không tìm thấy thông tin đơn hàng</h2>
        <button className="btn-pay" style={{width: '200px'}} onClick={() => navigate('/')}>Về trang chủ</button>
      </div>
    );
  }

  const handlePayment = async (e) => {
    e.preventDefault();
    setIsProcessing(true);
    
    try {
      // 1. GỌI API BACKEND
      const response = await fetch('http://localhost:8080/api/v1/bookings/checkout', {
        method: 'POST',
        headers: {
          'Content-Type': 'application/json'
          // Không có Authorization vì Khách không cần tài khoản
        },
        body: JSON.stringify({
          // Lưu ý: Đảm bảo SeatSelection.jsx đã truyền 'selectedSeatIds' chứa mảng số nguyên [1, 2] qua location.state!
          seatIds: location.state.selectedSeatIds, 
          sessionId: sessionId, // Gửi sessionId lên backend để xác nhận ghế đã được giữ bởi session này
          email: email, 
          phone: phone,
          cardNumber: cardNumber 
        })
      });

      // 2. XỬ LÝ LỖI (Ví dụ: Thẻ sai định dạng, ghế đã bị cướp mất, v.v.)
      if (!response.ok) {
        const errorMsg = await response.text();
        throw new Error(errorMsg || "Giao dịch thất bại. Vui lòng thử lại!");
      }

      const data = await response.json(); // data từ Spring Boot trả về sẽ có dạng: { bookingRef: "DH5", totalPrice: 140000, message: "..." }

      // 4. CHUẨN BỊ DỮ LIỆU QR
      const qrData = `REF:${data.bookingRef}|MOVIE:${movie.tenPhim}|SEATS:${selectedSeats.join(',')}`;
      const qrUrl = `https://api.qrserver.com/v1/create-qr-code/?size=200x200&data=${encodeURIComponent(qrData)}&color=000000&bgcolor=ffffff`;
      
      // 5. Gom dữ liệu vé
      const emailPayload = {
        to_email: email,
        customer_phone: phone,
        movie_name: movie.tenPhim,
        showtime: `${showtime} - ${formattedDate}`,
        seats: selectedSeats.join(', '),
        total_price: `$${data.totalPrice} ₫`, // Lấy giá CHUẨN từ Backend (US #8)
        booking_ref: data.bookingRef, // Lấy mã Đơn hàng từ DB
        qr_image_link: qrUrl // Có thể nhúng trực tiếp link này vào thẻ <img> trong template email
      };
      console.log("Sẵn sàng gửi email vé tới:", email, emailPayload);
      
      //ta lưu dữ liệu và bật Popup
      setTicketData({
        movie, 
        cinemaName, 
        formattedDate, 
        showtime, 
        selectedSeats, // Vẫn truyền label A1, A2... để hiển thị trên vé
        totalPrice: data.totalPrice, 
        bookingRef: data.bookingRef, 
        customerInfo: { email, phone }
      });
      setShowTicket(true);
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
        {/* CỘT TRÁI: FORM THANH TOÁN */}
        <div className="payment-form-section">
          <h2>Thanh Toán</h2>
          <p className="payment-subtitle">Nhập thông tin thẻ tín dụng để hoàn tất đặt vé</p>
          
          <form className="payment-form" onSubmit={handlePayment}>
            <h4 style={{ color: '#fff', fontSize: '1.2rem', marginBottom: '15px', marginTop: '0' }}>1. Thông tin liên hệ</h4>
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

            <h4 style={{ color: '#fff', fontSize: '1.2rem', marginBottom: '15px', marginTop: '30px' }}>2. Thông tin thẻ</h4>
            <div className="form-group">
              <label>Số thẻ tín dụng</label>
              <input 
                type="text" placeholder="0000 0000 0000 0000" maxLength="16" required 
                value={cardNumber} onChange={(e) => setCardNumber(e.target.value.replace(/\D/g, ''))}
              />
            </div>
            
            <div className="form-row">
              <div className="form-group">
                <label>Ngày hết hạn (MM/YY)</label>
                <input 
                  type="text" placeholder="MM/YY" maxLength="5" required 
                  value={expiry}
                  onChange={(e) => {
                    let val = e.target.value.replace(/\D/g, '');
                    if (val.length >= 3) val = val.slice(0, 2) + '/' + val.slice(2);
                    setExpiry(val);
                  }}
                />
              </div>
              <div className="form-group">
                <label>Mã bảo mật (CVV)</label>
                <input 
                  type="password" placeholder="123" maxLength="3" required 
                  value={cvv} onChange={(e) => setCvv(e.target.value.replace(/\D/g, ''))}
                />
              </div>
            </div>

            {/* Nút thanh toán sẽ disable nếu chưa nhập đủ chuẩn form hoặc đang quay loading */}
            <button type="submit" className="btn-pay" disabled={isProcessing || !email || phone.length < 10 || cardNumber.length < 16 || expiry.length < 5 || cvv.length < 3}>
              {isProcessing ? 'Đang xử lý giao dịch...' : `Thanh toán ${totalPrice?.toLocaleString('vi-VN')} ₫`}
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
            <div className="total-price">
              <span>Total Price</span>
              <span>{totalPrice?.toLocaleString('vi-VN')} ₫</span>
            </div>
          </div>
        </div>
      </div>

      {/* Hiển thị Ticket dưới dạng Popup khi showTicket là true */}
      {showTicket && (
        <Ticket 
          ticketData={ticketData} 
          onClose={() => navigate('/')} 
        />
      )}
    </div>
  );
}

export default Payment;