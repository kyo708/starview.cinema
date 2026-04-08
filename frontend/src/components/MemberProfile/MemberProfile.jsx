import React, { useState, useEffect } from 'react';
import { useNavigate } from 'react-router-dom';
import './MemberProfile.css';

const baseUrl = import.meta.env.VITE_BASE_URL || 'http://localhost:8080';

function UserProfile() {
  const navigate = useNavigate();
  const [formData, setFormData] = useState({
    hoTen: '',
    email: '',
    soDienThoai: '',
    ngaySinh: '',
    diemTichLuy: 0,
    matKhau: '' // Bỏ trống trừ khi user muốn đổi mk mới
  });
  const [message, setMessage] = useState({ text: '', type: '' });
  const [isLoading, setIsLoading] = useState(true);

  const getAuthHeaders = () => {
    const token = sessionStorage.getItem('token');
    return {
      'Content-Type': 'application/json',
      'Authorization': `Bearer ${token}`
    };
  };

  useEffect(() => {
    // Load dữ liệu khi trang vừa render
    fetch(`${baseUrl}/api/v1/auth/my-profile`, { headers: getAuthHeaders() })
      .then(res => {
        if (res.status === 401 || res.status === 403) {
            navigate('/login');
            throw new Error("Vui lòng đăng nhập lại.");
        }
        if (!res.ok) throw new Error("Không thể tải thông tin hồ sơ");
        return res.json();
      })
      .then(data => {
        setFormData({
          hoTen: data.hoTen || '',
          email: data.email || '',
          soDienThoai: data.soDienThoai || '',
          ngaySinh: data.ngaySinh || '',
          diemTichLuy: data.diemTichLuy || 0,
          matKhau: ''
        });
        setIsLoading(false);
      })
      .catch(err => {
        console.error(err);
        setIsLoading(false);
      });
  }, [navigate]);

  const handleInputChange = (e) => {
    const { name, value } = e.target;
    setFormData({ ...formData, [name]: value });
  };

  const handleSubmit = (e) => {
    e.preventDefault();
    setMessage({ text: '', type: '' });

    fetch(`${baseUrl}/api/v1/auth/my-profile`, {
      method: 'PUT',
      headers: getAuthHeaders(),
      body: JSON.stringify(formData)
    })
      .then(async res => {
        if (res.ok) {
          setMessage({ text: 'Cập nhật thông tin hồ sơ thành công!', type: 'success' });
          setFormData(prev => ({ ...prev, matKhau: '' })); // Reset ô mk sau khi lưu thành công
        } else {
          throw new Error('Cập nhật thất bại. Vui lòng kiểm tra lại dữ liệu.');
        }
      })
      .catch(err => setMessage({ text: err.message, type: 'error' }));
  };

  if (isLoading) return <div className="profile-container" style={{textAlign: 'center', padding: '100px'}}>Đang tải dữ liệu...</div>;

  return (
    <div className="profile-container">
      <div className="profile-card">
        <h2>Hồ Sơ Của Tôi</h2>
        {message.text && <p className={`profile-message ${message.type}`}>{message.text}</p>}
        
        <form className="profile-form" onSubmit={handleSubmit}>
          <div className="points-display-profile">
            <span>🌟 Điểm tích lũy hiện tại:</span>
            <strong>{formData.diemTichLuy.toLocaleString('vi-VN')} điểm</strong>
          </div>

          <div className="form-group">
            <label>Email (Không thể thay đổi)</label>
            <input type="email" name="email" value={formData.email} disabled />
          </div>

          <div className="form-group">
            <label>Họ và tên</label>
            <input type="text" name="hoTen" value={formData.hoTen} onChange={handleInputChange} required />
          </div>

          <div className="form-group">
            <label>Số điện thoại</label>
            <input type="tel" name="soDienThoai" value={formData.soDienThoai} onChange={handleInputChange} />
          </div>

          <div className="form-group">
            <label>Ngày sinh</label>
            <input type="date" name="ngaySinh" value={formData.ngaySinh} onChange={handleInputChange} />
          </div>

          <div className="form-group">
            <label>Đổi mật khẩu mới (Bỏ trống nếu không muốn đổi)</label>
            <input type="password" name="matKhau" value={formData.matKhau} onChange={handleInputChange} placeholder="Nhập mật khẩu mới nếu muốn thay đổi..." />
          </div>

          <button type="submit" className="btn-save-profile">Lưu Thay Đổi</button>
        </form>
      </div>
    </div>
  );
}

export default UserProfile;
