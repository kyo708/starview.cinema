import React from 'react';

// Sử dụng React.memo để ngăn việc render lại không cần thiết
const Seat = React.memo(function Seat({ seatId, loaiGhe, isSold, isSelected, onClick = () => {} }) {
  const isSweetbox = loaiGhe === 'SWEETBOX';
  // Xác định các lớp CSS dựa trên props
  const seatClasses = [
    'seat',
    isSweetbox ? 'sweetbox' : (loaiGhe === 'VIP' ? 'vip' : 'thuong'),
    isSold ? 'sold' : 'available',
    isSelected ? 'selected' : ''
  ].join(' ');

  // Hàm xử lý click, chỉ gọi lại hàm cha nếu ghế không phải là ghế đã bán
  const handleClick = () => {
    if (!isSold) {
      onClick(); // Để cha tự xử lý object seat đã bind
    }
  };

  return (
    <div className={seatClasses} onClick={handleClick}>
      {isSweetbox ? (
        // Thiết kế SVG đôi dành riêng cho Sweetbox
        <svg className="seat-svg" viewBox="0 0 64 32" xmlns="http://www.w3.org/2000/svg">
          <path d="M6 28 C6 28 10 30 32 30 C54 30 58 28 58 28" stroke="rgba(0,0,0,0.5)" strokeWidth="3" strokeLinecap="round" fill="none" />
          <path className="seat-back" d="M6 8 C6 3 9 2 32 2 C55 2 58 3 58 8 V18 H6 V8 Z" />
          <path className="seat-top" d="M8 7 C8 4 10 4 32 4 C54 4 56 4 56 7 V9 H8 V7 Z" />
          {/* 2 phần nệm ghế ghép liền */}
          <path className="seat-cushion" d="M4 17 C4 16 5 15 6 15 H31 V27 H7 C4 27 4 25 4 22 V17 Z" />
          <path className="seat-cushion" d="M60 17 C60 16 59 15 58 15 H33 V27 H57 C60 27 60 25 60 22 V17 Z" />
          <rect className="seat-arm" x="2" y="10" width="5" height="12" rx="2.5" />
          <rect className="seat-arm" x="57" y="10" width="5" height="12" rx="2.5" />
        </svg>
      ) : (
        // Thiết kế SVG ghế đơn bình thường
        <svg className="seat-svg" viewBox="0 0 32 32" xmlns="http://www.w3.org/2000/svg">
          <path d="M6 28 C6 28 10 30 16 30 C22 30 26 28 26 28" stroke="rgba(0,0,0,0.5)" strokeWidth="3" strokeLinecap="round" fill="none" />
          <path className="seat-back" d="M6 8 C6 3 9 2 16 2 C23 2 26 3 26 8 V18 H6 V8 Z" />
          <path className="seat-top" d="M8 7 C8 4 10 4 16 4 C22 4 24 4 24 7 V9 H8 V7 Z" />
          <path className="seat-cushion" d="M4 17 C4 16 5 15 6 15 H26 C27 15 28 16 28 17 V22 C28 25 25 27 16 27 C7 27 4 25 4 22 V17 Z" />
          <rect className="seat-arm" x="2" y="10" width="5" height="12" rx="2.5" />
          <rect className="seat-arm" x="25" y="10" width="5" height="12" rx="2.5" />
        </svg>
      )}
      <span className="tooltip">{seatId}</span>
    </div>
  );
});

export default Seat;