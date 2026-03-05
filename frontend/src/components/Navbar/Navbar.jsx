import React from 'react';
import './Navbar.css'; // Nhúng file CSS để tạo kiểu cho Navbar

function Navbar() {
  return (
    // Thẻ header bao bọc toàn bộ thanh điều hướng phía trên cùng
    <header className="header-container">
      
      {/* TẦNG 1: Chứa các liên kết phụ (Tin tức, Đăng nhập...) nằm ở góc trên cùng */}
      <div className="top-nav">
        <span>TIN MỚI & ƯU ĐÃI</span> | 
        <span>VÉ CỦA TÔI</span> | 
        <span>ĐĂNG NHẬP / ĐĂNG KÝ</span>
      </div>

      {/* TẦNG 2: Chứa Logo, Menu chính và Nút mua vé */}
      <div className="main-nav">
        
        {/* Phần Logo của rạp phim */}
        <div className="logo">
          <h1>StarView<span className="star">*</span></h1>
        </div>
        
        {/* Danh sách các mục menu chính. Dùng thẻ <ul> (unordered list) là chuẩn HTML ngữ nghĩa */}
        <ul className="nav-links">
          <li className="active">PHIM</li>
          <li>RẠP STARVIEW</li>
          <li>THÀNH VIÊN</li>
          <li>CULTUREPLEX</li>
        </ul>
        
        {/* Nút kêu gọi hành động (Call to Action - CTA) để khách hàng chú ý */}
        <div className="buy-ticket-btn">
           🎟️ MUA VÉ NGAY
        </div>

      </div>
    </header>
  );
}

export default Navbar; // Xuất component này ra để file App.jsx có thể gọi vào