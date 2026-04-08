import React, { useState, useEffect } from 'react';
import './AdminStaffManager.css'; // Tách riêng CSS sạch sẽ

const baseUrl = import.meta.env.VITE_BASE_URL || 'http://localhost:8080';

function AdminStaffManager() {
  const [staffList, setStaffList] = useState([]);
  const [isEditing, setIsEditing] = useState(false);
  const [currentId, setCurrentId] = useState(null);

  const [formData, setFormData] = useState({
    hoTen: '',
    email: '',
    soDienThoai: '',
    ngaySinh: '',
    matKhau: ''
  });

  const API_URL = `${baseUrl}/api/v1/admin/staff`;

  const getAuthHeaders = () => {
    const token = sessionStorage.getItem('token');
    return {
      'Content-Type': 'application/json',
      'Authorization': `Bearer ${token}`
    };
  };

  const fetchStaff = () => {
    fetch(API_URL, { headers: getAuthHeaders() })
      .then(res => res.json())
      .then(data => setStaffList(data))
      .catch(err => console.error("Lỗi fetch staff:", err));
  };

  useEffect(() => {
    fetchStaff();
  }, []);

  const handleInputChange = (e) => {
    const { name, value } = e.target;
    setFormData({ ...formData, [name]: value });
  };

  const handleSubmit = (e) => {
    e.preventDefault();
    
    const method = isEditing ? 'PUT' : 'POST';
    const url = isEditing ? `${API_URL}/${currentId}` : API_URL;

    fetch(url, {
      method: method,
      headers: getAuthHeaders(),
      body: JSON.stringify(formData)
    })
    .then(async res => {
      const data = await res.json();
      if (res.ok) {
        alert(data.message || (isEditing ? 'Cập nhật thành công!' : 'Tạo tài khoản nhân viên thành công!'));
        resetForm();
        fetchStaff();
      } else {
        alert('Lỗi: ' + (data.message || 'Không thể tạo nhân viên'));
      }
    })
    .catch(err => console.error(err));
  };

  const handleEdit = (staff) => {
    setIsEditing(true);
    setCurrentId(staff.id);
    setFormData({
      hoTen: staff.hoTen || '',
      email: staff.email || '',
      soDienThoai: staff.soDienThoai || '',
      ngaySinh: staff.ngaySinh || '',
      matKhau: '' // Để trống, nếu người dùng nhập thì backend mới đổi pass
    });
    window.scrollTo(0, 0);
  };

  const handleDelete = (id) => {
    if (window.confirm('Bạn có chắc chắn muốn xóa/khóa tài khoản nhân viên này không?')) {
      fetch(`${API_URL}/${id}`, {
        method: 'DELETE',
        headers: getAuthHeaders()
      })
      .then(res => {
        if (res.ok) fetchStaff();
        else alert('Có lỗi xảy ra khi xóa nhân viên.');
      });
    }
  };

  const resetForm = () => {
    setIsEditing(false);
    setCurrentId(null);
    setFormData({ hoTen: '', email: '', soDienThoai: '', ngaySinh: '', matKhau: '' });
  };

  return (
    <div className="showtime-manager">
      <div className="admin-card">
        <h3>{isEditing ? 'Chỉnh Sửa Nhân Viên' : 'Tạo Tài Khoản Nhân Viên (STAFF)'}</h3>
        <form className="admin-form-grid" onSubmit={handleSubmit}>
          <div className="form-group">
            <label>Họ và Tên</label>
            <input type="text" name="hoTen" required value={formData.hoTen} onChange={handleInputChange} placeholder="Nhập tên nhân viên..." />
          </div>
          <div className="form-group">
            <label>Email</label>
            <input type="email" name="email" required value={formData.email} onChange={handleInputChange} placeholder="example@starview.com" disabled={isEditing} />
          </div>
          <div className="form-group">
            <label>Số điện thoại</label>
            <input type="tel" name="soDienThoai" value={formData.soDienThoai} onChange={(e) => setFormData({...formData, soDienThoai: e.target.value.replace(/\D/g, '')})} maxLength="10" placeholder="09xxxxxxxx" />
          </div>
          <div className="form-group">
            <label>Ngày sinh</label>
            <input type="date" name="ngaySinh" value={formData.ngaySinh} onChange={handleInputChange} />
          </div>
          <div className="form-group full-width">
            <label>Mật khẩu {isEditing && <span style={{fontSize: '0.85rem', color: '#888'}}>(Bỏ trống nếu không muốn đổi mật khẩu)</span>}</label>
            <input type="password" name="matKhau" required={!isEditing} value={formData.matKhau} onChange={handleInputChange} placeholder="Khởi tạo mật khẩu..." />
          </div>
          <div className="form-actions-staff">
            <button type="submit" className="btn-primary-admin">
              {isEditing ? 'Cập Nhật Thay Đổi' : 'Tạo Nhân Viên Mới'}
            </button>
            {isEditing && (
              <button type="button" className="btn-cancel-staff" onClick={resetForm}>Hủy bỏ</button>
            )}
          </div>
        </form>
      </div>

      <div className="showtime-table-container">
        <table className="showtime-table">
          <thead>
            <tr>
              <th>ID</th>
              <th>Họ Tên</th>
              <th>Email</th>
              <th>Số Điện Thoại</th>
              <th>Vai Trò</th>
              <th>Hành Động</th>
            </tr>
          </thead>
          <tbody>
            {staffList.map((staff) => (
              <tr key={staff.id}>
                <td>{staff.id}</td>
                <td><strong style={{ color: '#ffb400' }}>{staff.hoTen}</strong> {staff.id === currentId && <span style={{fontSize:'0.75rem', color:'#4caf50', marginLeft: '5px'}}>(Đang sửa)</span>}</td>
                <td>{staff.email}</td>
                <td>{staff.soDienThoai}</td>
                <td><span style={{color: '#4caf50', fontWeight: 'bold'}}>{staff.vaiTro}</span></td>
                <td>
                  <div className="btn-action-group">
                    <button className="btn-edit-staff" onClick={() => handleEdit(staff)}>Sửa</button>
                    <button className="btn-delete-staff" onClick={() => handleDelete(staff.id)}>Xóa</button>
                  </div>
                </td>
              </tr>
            ))}
          </tbody>
        </table>
      </div>
    </div>
  );
}

export default AdminStaffManager;