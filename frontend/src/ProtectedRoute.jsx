import React from 'react';
import { Navigate } from 'react-router-dom';
import { jwtDecode } from 'jwt-decode';

const ProtectedRoute = ({ children }) => {
  const token = sessionStorage.getItem('token');

  if (!token) {
    // 1. Nếu không có token, chuyển hướng về trang đăng nhập
    return <Navigate to="/login" replace />;
  }

  try {
    const decodedToken = jwtDecode(token);
    // 2. Kiểm tra vai trò trong token
    if (decodedToken.role !== 'STAFF') {
      // Nếu không phải STAFF, cũng chuyển hướng về trang đăng nhập
      // Bạn có thể thêm state để hiển thị thông báo "Không có quyền" ở trang login
      return <Navigate to="/login" replace />;
    }
    // 3. Nếu là STAFF, cho phép truy cập
    return children;
  } catch (error) {
    // Nếu token không hợp lệ (không giải mã được), cũng đẩy về trang login
    return <Navigate to="/login" replace />;
  }
};

export default ProtectedRoute;