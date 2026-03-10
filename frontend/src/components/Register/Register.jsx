import React, { useState } from 'react';
import { Link, useNavigate } from 'react-router-dom';
import './Register.css';

function Register() {
  const [formData, setFormData] = useState({
    hoTen: '',
    email: '',
    matKhau: '',
    soDienThoai: ''
  });
  const [message, setMessage] = useState({ text: '', type: '' });
  const [isLoading, setIsLoading] = useState(false);
  const navigate = useNavigate();

  const handleInputChange = (e) => {
    const { name, value } = e.target;
    setFormData({ ...formData, [name]: value });
  };

  const handleRegister = async (e) => {
    e.preventDefault();
    setMessage({ text: '', type: '' });
    setIsLoading(true);

    try {
      const response = await fetch('http://localhost:8080/api/v1/auth/register', {
        method: 'POST',
        headers: { 'Content-Type': 'application/json' },
        body: JSON.stringify({ ...formData, vaiTro: null }) 
      });

      const responseText = await response.text();

      if (response.ok) {
        setMessage({ text: 'Đăng ký thành công! Bạn sẽ được chuyển đến trang đăng nhập sau 3 giây.', type: 'success' });
        setTimeout(() => {
          navigate('/login');
        }, 3000);
      } else {
        setMessage({ text: `Đăng ký thất bại: ${responseText}`, type: 'error' });
      }
    } catch (err) {
      console.error('Lỗi kết nối:', err);
      setMessage({ text: 'Không thể kết nối đến máy chủ. Vui lòng thử lại sau.', type: 'error' });
    } finally {
      setIsLoading(false);
    }
  };

  return (
    <div className="register-container">
      <form className="register-form" onSubmit={handleRegister}>
        <h2 className="register-title">Đăng Ký Tài Khoản</h2>
        
        {message.text && <p className={`form-message ${message.type}`}>{message.text}</p>}

        <div className="register-input-group">
          <label>Họ và Tên</label>
          <input type="text" name="hoTen" value={formData.hoTen} onChange={handleInputChange} required disabled={isLoading} />
        </div>

        <div className="register-input-group">
          <label>Email</label>
          <input type="email" name="email" value={formData.email} onChange={handleInputChange} required disabled={isLoading} />
        </div>

        <div className="register-input-group">
          <label>Mật khẩu</label>
          <input type="password" name="matKhau" value={formData.matKhau} onChange={handleInputChange} required disabled={isLoading} />
        </div>

        <div className="register-input-group">
          <label>Số điện thoại</label>
          <input type="tel" name="soDienThoai" value={formData.soDienThoai} onChange={handleInputChange} disabled={isLoading} />
        </div>

        <button type="submit" className="btn-register" disabled={isLoading}>
          {isLoading ? 'Đang xử lý...' : 'ĐĂNG KÝ'}
        </button>

        <p className="login-link">
          Đã có tài khoản? <Link to="/login">Đăng nhập tại đây</Link>
        </p>
      </form>
    </div>
  );
}

export default Register;
