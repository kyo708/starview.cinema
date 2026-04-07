import React, { useState, useEffect } from 'react';
import './AdminVoucherManager.css';

const baseUrl = import.meta.env.VITE_BASE_URL || 'http://localhost:8080';

function AdminVoucherManager() {
  const [vouchers, setVouchers] = useState([]);
  const [formData, setFormData] = useState({
    maCode: '',
    mucGiam: '',
    soLuong: '',
    ngayHetHan: ''
  });

  // Thay đổi URL theo cấu trúc API Backend của bạn
  const API_URL = `${baseUrl}/api/v1/vouchers/staff`;

  const getAuthHeaders = () => {
    const token = sessionStorage.getItem('token'); 
    return {
      'Content-Type': 'application/json',
      'Authorization': `Bearer ${token}`
    };
  };

  // Hàm gọi API lấy danh sách voucher
  const fetchVouchers = () => {
    fetch(API_URL, { headers: getAuthHeaders() })
      .then(res => {
        if (!res.ok) throw new Error("Lỗi tải danh sách voucher");
        return res.json();
      })
      .then(data => setVouchers(data))
      .catch(err => console.error("Lỗi khi fetch vouchers:", err));
  };

  useEffect(() => {
    fetchVouchers();
  }, []);

  const handleInputChange = (e) => {
    const { name, value } = e.target;
    setFormData({ ...formData, [name]: value });
  };

  // Xử lý Submit form tạo voucher
  const handleSubmit = (e) => {
    e.preventDefault();
    
    // Parse dữ liệu trước khi gửi
    const payload = {
      maKhuyenMai: formData.maCode.toUpperCase(), 
      loai: 'FLAT',                               
      giaTri: parseFloat(formData.mucGiam) || 0,       
      gioiHanSuDung: parseInt(formData.soLuong)||0,  
      // Đảm bảo có giây (:00) để Spring Boot có thể parse thành LocalDateTime
      ngayHetHan: formData.ngayHetHan.length === 16 ? formData.ngayHetHan + ':00' : formData.ngayHetHan,
      daSuDung: 0,
      danhChoThanhVienMoi: false
    };

    fetch(API_URL, {
      method: 'POST',
      headers: getAuthHeaders(),
      body: JSON.stringify(payload)
    })
    .then(async res => {
      if (res.ok) {
        alert('Thêm mới Voucher thành công!');
        setFormData({ maCode: '', mucGiam: '', soLuong: '', ngayHetHan: '' });
        fetchVouchers(); // Refresh bảng
      } else {
        const errData = await res.text();
        alert('Lỗi: ' + (errData || 'Mã code có thể đã tồn tại!'));
      }
    })
    .catch(err => console.error(err));
  };

  // Vô hiệu hóa (Ẩn / Xóa mềm) voucher
  const handleDelete = (id) => {
    if (window.confirm('Bạn có chắc muốn vô hiệu hóa voucher này không?')) {
      fetch(`${API_URL}/${id}`, {
        method: 'DELETE',
        headers: getAuthHeaders()
      })
      .then(res => {
        if (res.ok) fetchVouchers();
        else alert('Không thể vô hiệu hóa voucher này.');
      });
    }
  };

  return (
    <div className="showtime-manager">
      {/* FORM NHẬP LIỆU */}
      <div className="admin-card">
        <h3>Thêm Voucher Mới</h3>
        <form className="admin-form-grid" onSubmit={handleSubmit}>
          <div className="form-group">
            <label>Mã Code (VD: TET2024)</label>
            <input type="text" name="maCode" required value={formData.maCode} onChange={handleInputChange} placeholder="Nhập mã voucher..." />
          </div>

          <div className="form-group">
            <label>Mức Giảm (VNĐ)</label>
            <input type="number" name="mucGiam" required value={formData.mucGiam} onChange={handleInputChange} placeholder="Ví dụ: 20000" />
          </div>

          <div className="form-group">
            <label>Số Lượng</label>
            <input type="number" name="soLuong" required value={formData.soLuong} onChange={handleInputChange} placeholder="Số lượt sử dụng..." />
          </div>

          <div className="form-group">
            <label>Ngày Hết Hạn</label>
            <input type="datetime-local" name="ngayHetHan" required value={formData.ngayHetHan} onChange={handleInputChange} />
          </div>

          <div className="form-actions" style={{ gridColumn: 'span 2', marginTop: '10px' }}>
            <button type="submit" className="btn-primary-admin" style={{ width: '100%' }}>Tạo Voucher Mới</button>
          </div>
        </form>
      </div>

      {/* DANH SÁCH VOUCHER */}
      <div className="showtime-table-container" style={{ marginTop: '20px' }}>
        <table className="showtime-table">
          <thead>
            <tr>
              <th>ID</th>
              <th>Mã Voucher</th>
              <th>Mức Giảm</th>
              <th>Số Lượng Còn Lại</th>
              <th>Ngày Hết Hạn</th>
              <th>Trạng Thái</th>
              <th>Hành Động</th>
            </tr>
          </thead>
          <tbody>
          {vouchers.map((v) => {
            // Kiểm tra trạng thái hoạt động dựa theo DB thật: hết hạn hoặc đã dùng hết số lượng
            const isExpired = new Date(v.ngayHetHan) < new Date() || v.daSuDung >= v.gioiHanSuDung;
            return (
            <tr key={v.id} style={{ opacity: isExpired ? 0.5 : 1 }}>
                <td>{v.id}</td>
              <td><strong style={{ color: '#ffb400' }}>{v.maKhuyenMai}</strong></td>
              <td>{v.giaTri?.toLocaleString()} đ</td>
              <td>{v.gioiHanSuDung - v.daSuDung} / {v.gioiHanSuDung}</td>
                <td>{new Date(v.ngayHetHan).toLocaleString('vi-VN')}</td>
                <td>
                {isExpired ? 
                    <span style={{color: '#f44336'}}>○ Ngừng h.động</span> : 
                    <span style={{color: '#4caf50', fontWeight: 'bold'}}>● Đang h.động</span>
                  }
                </td>
                <td>
                {!isExpired && (
                  <button className="btn-logout-sidebar" style={{ padding: '6px 12px', fontSize: '0.8rem', width: 'auto' }} onClick={() => handleDelete(v.id)}>Xóa</button>
                  )}
                </td>
              </tr>
          )})}
          </tbody>
        </table>
      </div>
    </div>
  );
}

export default AdminVoucherManager;