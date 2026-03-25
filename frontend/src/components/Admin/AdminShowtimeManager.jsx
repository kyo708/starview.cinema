import React, { useState, useEffect } from 'react';
import './AdminShowtimeManager.css';

const baseUrl = import.meta.env.VITE_BASE_URL || 'http://localhost:8080';

function AdminShowtimeManager() {
  const [movies, setMovies] = useState([]);
  const [showtimes, setShowtimes] = useState([]);
  const [rooms, setRooms] = useState([]);
  
  // State dành cho bộ lọc (Filtering)
  const [filterName, setFilterName] = useState('');
  const [filterRoomId, setFilterRoomId] = useState('');
  const [filterDate, setFilterDate] = useState('');
  
  
  const [formData, setFormData] = useState({
    phimId: '',
    phongChieuId: '',
    thoiGianChieu: '',
    heSoGia: 1.0
  });

  const API_URL = `${baseUrl}/api/v1/suat-chieu/staff`;
  const token = sessionStorage.getItem('token');
  const headers = {
    'Content-Type': 'application/json',
    'Authorization': `Bearer ${token}`
  };

  const fetchShowtimes = () => {
    fetch(API_URL, { headers })
      .then(res => {
        if (!res.ok) throw new Error("Server trả về lỗi " + res.status);
        return res.json();
      })
      .then(data => {
        // Chỉ set state nếu data thực sự là một mảng
        if (Array.isArray(data)) {
          setShowtimes(data);
        } else {
          console.error("Dữ liệu không hợp lệ:", data);
          setShowtimes([]);
        }
      })
      .catch(err => console.error("Lỗi load suất chiếu:", err));
  };

  useEffect(() => {
    // Load danh sách phim để đổ vào dropdown
    fetch(`${baseUrl}/api/v1/phim`)
      .then(res => res.json())
      .then(data => setMovies(data));

    // Load danh sách phòng từ API để dùng cho Form và Filter
    fetch(`${baseUrl}/api/v1/phong-chieu/staff`, { headers })
      .then(res => res.json())
      .then(data => setRooms(data))
      .catch(err => console.error("Lỗi load phòng:", err));

    fetchShowtimes();
  }, []);

  const handleSubmit = (e) => {
    e.preventDefault();
    
    fetch(API_URL, {
      method: 'POST',
      headers: headers,
      body: JSON.stringify({
        ...formData,
        phimId: parseInt(formData.phimId),
        phongChieuId: parseInt(formData.phongChieuId),
        heSoGia: parseFloat(formData.heSoGia)
      })
    })
    .then(async res => {
      if (res.ok) {
        alert("Thêm suất chiếu thành công!");
        setFormData({ phimId: '', phongChieuId: '', thoiGianChieu: '', heSoGia: 1.0 });
        fetchShowtimes(); // Refresh danh sách
      } else {
        const err = await res.json();
        alert("Lỗi: " + (err.message || "Không thể thêm suất chiếu (Có thể bị trùng lịch)"));
      }
    })
    .catch(err => console.error(err));
  };

  // Logic lọc suất chiếu (Client-side filtering)
  const filteredShowtimes = showtimes.filter(sc => {
    // Lưu ý: DTO trả về thường có tenPhim, tenPhong trực tiếp ở root object
    const matchName = sc.tenPhim?.toLowerCase().includes(filterName.toLowerCase());
    const matchRoom = filterRoomId === '' || sc.phongChieuId === parseInt(filterRoomId);
    
    // So khớp ngày (YYYY-MM-DD)
    const showtimeDate = sc.thoiGianChieu?.split('T')[0];
    const matchDate = filterDate === '' || showtimeDate === filterDate;
    
    return matchName && matchRoom && matchDate;
  });

  return (
    <div className="showtime-manager">
      <div className="admin-card">
        <h3>Tạo Suất Chiếu Mới</h3>
        <form onSubmit={handleSubmit} className="admin-form-grid">
          <div className="form-group">
            <label>Chọn Phim</label>
            <select 
              value={formData.phimId} 
              onChange={(e) => setFormData({...formData, phimId: e.target.value})}
              required
            >
              <option value="">-- Chọn phim --</option>
              {movies.map(m => <option key={m.id} value={m.id}>{m.tenPhim}</option>)}
            </select>
          </div>

          <div className="form-group">
            <label>Phòng Chiếu</label>
            <select 
              value={formData.phongChieuId} 
              onChange={(e) => setFormData({...formData, phongChieuId: e.target.value})}
              required
            >
              <option value="">-- Chọn phòng --</option>
              {rooms.map(r => <option key={r.id} value={r.id}>{r.tenPhong}</option>)}
            </select>
          </div>

          <div className="form-group">
            <label>Thời Gian Bắt Đầu</label>
            <input 
              type="datetime-local" 
              value={formData.thoiGianChieu}
              onChange={(e) => setFormData({...formData, thoiGianChieu: e.target.value})}
              required
            />
          </div>

          <div className="form-group">
            <label>Hệ Số Giá (VD: 1.2 cho cuối tuần)</label>
            <input 
              type="number" step="0.1"
              value={formData.heSoGia}
              onChange={(e) => setFormData({...formData, heSoGia: e.target.value})}
              required
            />
          </div>

          <div className="btn-primary-admin-showtime">
            <button type="submit" >
            Xác Nhận Tạo Suất Chiếu
          </button>
          </div>
        </form>
      </div>

      {/* BỘ LỌC TÌM KIẾM (FILTER BAR) */}
      <div className="admin-card filter-section-admin">
        <h4><span className="icon">🔍</span> Bộ lọc danh sách</h4>
        <div className="admin-form-grid">
          <div className="form-group">
            <label>Tên phim</label>
            <input 
              type="text" placeholder="Tìm tên phim..." 
              value={filterName} onChange={(e) => setFilterName(e.target.value)}
            />
          </div>
          <div className="form-group">
            <label>Phòng chiếu</label>
            <select value={filterRoomId} onChange={(e) => setFilterRoomId(e.target.value)}>
              <option value="">Tất cả phòng</option>
              {rooms.map(r => <option key={r.id} value={r.id}>{r.tenPhong}</option>)}
            </select>
          </div>
          <div className="form-group">
            <label>Ngày chiếu</label>
            <input 
              type="date" 
              value={filterDate} onChange={(e) => setFilterDate(e.target.value)}
            />
          </div>
        </div>
      </div>

      <div className="showtime-table-container">
        <table className="showtime-table">
          <thead>
            <tr>
              <th>ID</th>
              <th>Phim</th>
              <th>Phòng</th>
              <th>Thời Gian Chiếu</th>
              <th>Hệ Số Giá</th>
            </tr>
          </thead>
          <tbody>
            {filteredShowtimes.map((sc) => (
              <tr key={sc.id}>
                <td>#{sc.id}</td>
                <td><strong>{sc.tenPhim}</strong></td>
                <td><span className="room-badge">{sc.tenPhong}</span></td>
                <td>{new Date(sc.thoiGianChieu).toLocaleString('vi-VN')}</td>
                <td>x{sc.heSoGia}</td>
              </tr>
            ))}
          </tbody>
        </table>
      </div>
    </div>
  );
}

export default AdminShowtimeManager;