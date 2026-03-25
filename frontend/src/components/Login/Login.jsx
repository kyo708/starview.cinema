import React, { useState } from 'react';
import { useNavigate, Link } from 'react-router-dom';
import { jwtDecode } from 'jwt-decode';
import './Login.css';

const baseUrl = import.meta.env.VITE_BASE_URL || 'http://localhost:8080';

function Login() {
  const [email, setEmail] = useState('');
  const [password, setPassword] = useState('');
  const [error, setError] = useState('');
  const navigate = useNavigate();

  const handleLogin = async (e) => {
    e.preventDefault();
    setError('');

    try {
      const response = await fetch(`${baseUrl}/api/v1/auth/login`, {
        method: 'POST',
        headers: {
          'Content-Type': 'application/json',
        },
        // Backend AuthController đang nhận key là "email" và "matKhau"
        body: JSON.stringify({ email: email, matKhau: password }),
      });

      if (response.ok) {
        const data = await response.json();
        const token = data.token;

        // Giải mã token để lấy thông tin vai trò
        const decodedToken = jwtDecode(token);

        // Chỉ cho phép STAFF đăng nhập vào trang admin
        if (decodedToken.role === 'STAFF') {
          // Lưu token vào sessionStorage để tự xóa khi đóng trình duyệt
          sessionStorage.setItem('token', token);
          navigate('/admin');
        } else {
          setError('Bạn không có quyền truy cập vào trang này!');
        }
      } else {
        // Phân loại lỗi để hiển thị thông báo phù hợp
        if (response.status === 401 || response.status === 403) {
          setError('Sai email hoặc mật khẩu. Vui lòng kiểm tra lại!');
        } else {
          const errorData = await response.text();
          setError(`Lỗi hệ thống (${response.status}): ${errorData}`);
        }
      }
    } catch (err) {
      console.error('Lỗi kết nối:', err);
      setError('Không thể kết nối đến máy chủ. Vui lòng thử lại sau.');
    }
  };

  return (
    <div className="login-container">
      <form className="login-form" onSubmit={handleLogin}>
        <h2 className="login-title">Staff Login</h2>
        
        {error && <p className="error-msg">{error}</p>}

        <div className="login-input-group">
          <label>Email</label>
          <input 
            type="email" 
            value={email}
            onChange={(e) => setEmail(e.target.value)}
            required 
            placeholder="Nhập email của staff..."
          />
        </div>

        <div className="login-input-group">
          <label>Mật khẩu</label>
          <input 
            type="password" 
            value={password}
            onChange={(e) => setPassword(e.target.value)}
            required 
            placeholder="Nhập password..."
          />
        </div>

        <button type="submit" className="btn-login">ĐĂNG NHẬP</button>

        <p style={{marginTop: '20px', fontSize: '0.9rem'}}>
          <Link to="/register">Đăng ký tài khoản cho nhân viên mới</Link>
        </p>
      </form>
    </div>
  );
}

export default Login;
