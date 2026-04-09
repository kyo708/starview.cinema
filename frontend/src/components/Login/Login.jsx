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

        // Lưu token vào sessionStorage cho mọi user đăng nhập thành công
        sessionStorage.setItem('token', token);

        // Đưa về chữ hoa và dùng includes để bắt được cả "admin", "ADMIN" hoặc "ROLE_ADMIN"
        const userRole = decodedToken.role ? decodedToken.role.toUpperCase() : '';
        if (userRole.includes('STAFF') || userRole.includes('ADMIN')) {
          navigate('/admin');
        } else {
          // Mặc định MEMBER hoặc các role khác trả về trang chủ
          navigate('/');
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
        <h2 className="login-title">Đăng Nhập</h2>
        
        {error && <p className="error-msg">{error}</p>}

        <div className="login-input-group">
          <label>Email</label>
          <input 
            type="email" 
            value={email}
            onChange={(e) => setEmail(e.target.value)}
            required 
            placeholder="Nhập email của thành viên..."
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
          <Link to="/register">Đăng ký thành viên mới</Link>
        </p>
      </form>
    </div>
  );
}

export default Login;
