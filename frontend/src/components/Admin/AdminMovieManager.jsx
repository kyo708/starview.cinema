import React, { useState, useEffect } from 'react';
import { useNavigate } from 'react-router-dom';
import './AdminMovieManager.css';


const baseUrl = import.meta.env.VITE_BASE_URL || 'http://localhost:8080';


function AdminMovieManager() {
  // State lưu trữ danh sách phim lấy từ API
  const [movies, setMovies] = useState([]);
  // State xác định đang ở chế độ thêm mới (false) hay chỉnh sửa (true)
  const [isEditing, setIsEditing] = useState(false);
  // State lưu ID của phim đang được chỉnh sửa
  const [currentId, setCurrentId] = useState(null);

  // State cho bộ lọc
  const [filterName, setFilterName] = useState('');
  const [filterCategory, setFilterCategory] = useState('');

  const navigate = useNavigate();
  
  // State quản lý dữ liệu của form nhập liệu
  const [formData, setFormData] = useState({
    tenPhim: '',
    theLoai: '',
    thoiLuongPhut: '',
    giaGoc: '',
    danhGia: '',
    posterUrl: '',
    trailerUrl: ''
  });

  // URL API gốc (Lưu ý: Backend chạy port 8080)
  const API_URL = `${baseUrl}/api/v1/phim/staff`; 

  // Hàm lấy token từ localStorage (giả sử bạn đã lưu khi login)
  // Cần thiết để vượt qua Security của Backend (Role STAFF)
  const getAuthHeaders = () => {
    const token = sessionStorage.getItem('token'); 
    return {
      'Content-Type': 'application/json',
      'Authorization': `Bearer ${token}` // Gửi token Bearer chuẩn JWT
    };
  };

  // 1. useEffect: Load danh sách phim ngay khi component được render lần đầu
  useEffect(() => {
    fetchMovies();
  }, []);

  // Hàm gọi API lấy danh sách phim
  const fetchMovies = () => {
    fetch(API_URL, { headers: getAuthHeaders() })
      .then(res => {
        if (res.status === 403 || res.status === 401) {
           alert("Phiên đăng nhập hết hạn hoặc không có quyền!");
           navigate('/login');
           throw new Error("Unauthorized");
        }
        if (!res.ok) throw new Error("Lỗi tải dữ liệu");
        return res.json();
      })
      .then(data => setMovies(data))
      .catch(err => console.error(err));
  };

  // 2. Xử lý sự kiện khi người dùng nhập liệu vào các ô input
  const handleInputChange = (e) => {
    const { name, value } = e.target;
    setFormData({ ...formData, [name]: value });
  };

  // 3. Xử lý Submit Form (Tạo mới hoặc Cập nhật)
  const handleSubmit = (e) => {
    e.preventDefault(); // Ngăn trình duyệt reload trang
    
    // Convert dữ liệu từ string sang number trước khi gửi về Backend
    const payload = {
      ...formData,
      thoiLuongPhut: parseInt(formData.thoiLuongPhut),
      giaGoc: parseFloat(formData.giaGoc),
      danhGia: parseFloat(formData.danhGia)
    };

    // Xác định phương thức (POST cho tạo mới, PUT cho cập nhật) và URL tương ứng
    const method = isEditing ? 'PUT' : 'POST';
    const url = isEditing ? `${API_URL}/${currentId}` : API_URL;

    fetch(url, {
      method: method,
      headers: getAuthHeaders(),
      body: JSON.stringify(payload)
    })
    .then(res => {
      if (res.ok) {
        alert(isEditing ? 'Cập nhật thành công!' : 'Thêm mới thành công!');
        fetchMovies(); // Reload lại bảng danh sách sau khi lưu
        resetForm();   // Reset form về trạng thái ban đầu
      } else {
        alert('Có lỗi xảy ra!');
      }
    })
    .catch(err => console.error(err));
  };

  // 4. Chức năng Sửa: Đổ dữ liệu của phim đã chọn lên form
  const handleEdit = (movie) => {
    setIsEditing(true);
    setCurrentId(movie.id);
    setFormData({
      tenPhim: movie.tenPhim,
      theLoai: movie.theLoai,
      thoiLuongPhut: movie.thoiLuongPhut,
      giaGoc: movie.giaGoc,
      danhGia: movie.danhGia,
      posterUrl: movie.posterUrl,
      trailerUrl: movie.trailerUrl
    });
    // Cuộn màn hình lên đầu trang để người dùng thấy form
    window.scrollTo(0, 0);
  };

  // 5. Chức năng Xóa (Soft Delete - Ẩn phim khỏi trang chủ)
  const handleDelete = (id) => {
    if (window.confirm('Bạn có chắc muốn ẩn phim này không?')) {
      fetch(`${API_URL}/${id}`, {
        method: 'DELETE',
        headers: getAuthHeaders()
      })
      .then(res => {
        if (res.ok) {
          fetchMovies(); // Reload lại danh sách để cập nhật trạng thái
        } else {
          alert('Không thể xóa phim này.');
        }
      });
    }
  };

  // 6. Chức năng Xóa Vĩnh Viễn (Hard Delete) cho phim đã ẩn
  const handleHardDelete = (id) => {
    if (window.confirm('CẢNH BÁO: Bạn có chắc muốn xóa vĩnh viễn phim này khỏi cơ sở dữ liệu? Hành động này không thể hoàn tác!')) {
      fetch(`${API_URL}/hard/${id}`, {
        method: 'DELETE',
        headers: getAuthHeaders()
      })
      .then(res => {
        if (res.ok) {
          fetchMovies(); // Reload lại danh sách
        } else {
          alert('Không thể xóa phim này.');
        }
      });
    }
  };

  // 7. Chức năng Khôi phục (Restore)
  const handleRestore = (id) => {
    fetch(`${API_URL}/restore/${id}`, {
      method: 'PUT',
      headers: getAuthHeaders()
    })
    .then(res => {
      if (res.ok) {
        fetchMovies(); // Reload lại danh sách
      } else {
        alert('Không thể khôi phục phim này.');
      }
    });
  };

  // Hàm reset form về trạng thái thêm mới
  const resetForm = () => {
    setIsEditing(false);
    setCurrentId(null);
    setFormData({
      tenPhim: '', theLoai: '', thoiLuongPhut: '',
      giaGoc: '', danhGia: '', posterUrl: '', trailerUrl: ''
    });
  };

  // Logic lọc phim tại client
  const filteredMovies = movies.filter(movie => {
    const matchName = movie.tenPhim?.toLowerCase().includes(filterName.toLowerCase());
    const matchCategory = movie.theLoai?.toLowerCase().includes(filterCategory.toLowerCase());
    return matchName && matchCategory;
  });

  return (
    <div className="showtime-manager">
      {/* FORM NHẬP LIỆU */}
      <div className="admin-card">
        <h3>{isEditing ? 'Chỉnh Sửa Phim' : 'Thêm Phim Mới'}</h3>
        <form className="admin-form-grid" onSubmit={handleSubmit}>
          <div className="form-group" style={{ gridColumn: 'span 2' }}>
            <label>Tên Phim</label>
            <input 
              type="text" name="tenPhim" required 
              value={formData.tenPhim} onChange={handleInputChange} 
              placeholder="Nhập tên phim..."
            />
          </div>

          <div className="form-group">
            <label>Thể Loại</label>
            <input 
              type="text" name="theLoai" required 
              value={formData.theLoai} onChange={handleInputChange} 
              placeholder="Hành động, Hài..."
            />
          </div>

          <div className="form-group">
            <label>Thời Lượng (phút)</label>
            <input 
              type="number" name="thoiLuongPhut" required 
              value={formData.thoiLuongPhut} onChange={handleInputChange} 
            />
          </div>

          <div className="form-group">
            <label>Giá Gốc (VNĐ)</label>
            <input 
              type="number" name="giaGoc" required 
              value={formData.giaGoc} onChange={handleInputChange} 
            />
          </div>

          <div className="form-group">
            <label>Đánh Giá (0-10)</label>
            <input 
              type="number" step="0.1" max="10" min="0" 
              name="danhGia" 
              value={formData.danhGia} onChange={handleInputChange} 
            />
          </div>

          <div className="form-group" style={{ gridColumn: 'span 2' }}>
            <label>Poster URL (Link ảnh)</label>
            <input 
              type="text" name="posterUrl" 
              value={formData.posterUrl} onChange={handleInputChange} 
            />
          </div>

          <div className="form-group" style={{ gridColumn: 'span 2' }}>
            <label>Trailer URL (Youtube)</label>
            <input 
              type="text" name="trailerUrl" 
              value={formData.trailerUrl} onChange={handleInputChange} 
            />
          </div>

          <div className="form-actions" style={{ gridColumn: 'span 2', display: 'flex', gap: '10px', marginTop: '10px' }}>
            <button type="submit" className="btn-primary-admin" style={{ flex: 1 }}>
              {isEditing ? 'Cập Nhật Thay Đổi' : 'Xác Nhận Thêm Phim'}
            </button>
            {isEditing && (
              <button type="button" className="btn-logout-sidebar" style={{ width: '100px', background: '#444' }} onClick={resetForm}>
                Hủy
              </button>
            )}
          </div>
        </form>
      </div>

      {/* BỘ LỌC TÌM KIẾM */}
      <div className="admin-card filter-section-admin">
        <h4><span className="icon">🔍</span> Tìm kiếm phim</h4>
        <div className="admin-form-grid">
          <div className="form-group">
            <label>Tên phim</label>
            <input 
              type="text" placeholder="Nhập tên phim cần tìm..." 
              value={filterName} onChange={(e) => setFilterName(e.target.value)}
            />
          </div>
          <div className="form-group">
            <label>Thể loại</label>
            <input 
              type="text" placeholder="Lọc theo thể loại..." 
              value={filterCategory} onChange={(e) => setFilterCategory(e.target.value)}
            />
          </div>
        </div>
      </div>

      {/* DANH SÁCH PHIM */}
      <div className="showtime-table-container">
        <table className="showtime-table">
          <thead>
            <tr>
              <th>ID</th>
              <th>Poster</th>
              <th>Tên Phim</th>
              <th>Thể Loại</th>
              <th>Giá Vé</th>
              <th>Trạng Thái</th>
              <th>Hành Động</th>
            </tr>
          </thead>
          <tbody>
            {filteredMovies.map((movie) => (
              <tr key={movie.id} style={{ opacity: movie.isActive ? 1 : 0.5 }}>
                <td>{movie.id}</td>
                <td>
                  <img src={movie.posterUrl} alt="" className="poster-preview" />
                </td>
                <td>
                  <strong>{movie.tenPhim}</strong><br/>
                  <small>{movie.thoiLuongPhut} phút</small>
                </td>
                <td>{movie.theLoai}</td>
                <td>{movie.giaGoc?.toLocaleString()} đ</td>
                <td>
                  {movie.isActive ? 
                    <span style={{color: '#4caf50', fontWeight: 'bold'}}>● Đang chiếu</span> : 
                    <span style={{color: '#f44336'}}>○ Đã ẩn</span>
                  }
                </td>
                <td>
                  <button className="btn-primary-admin" style={{ padding: '6px 12px', fontSize: '0.8rem', marginRight: '5px' }} onClick={() => handleEdit(movie)}>Sửa</button>
                  {movie.isActive ? (
                    <button className="btn-logout-sidebar" style={{ padding: '6px 12px', fontSize: '0.8rem', display: 'inline', width: 'auto' }} onClick={() => handleDelete(movie.id)}>Ẩn</button>
                  ) : (
                    <>
                      <button className="btn-primary-admin" style={{ padding: '6px 12px', fontSize: '0.8rem', backgroundColor: '#28a745', marginRight: '5px' }} onClick={() => handleRestore(movie.id)}>Hiện</button>
                      <button className="btn-logout-sidebar" style={{ padding: '6px 12px', fontSize: '0.8rem', display: 'inline', width: 'auto', background: '#d32f2f' }} onClick={() => handleHardDelete(movie.id)}>Xóa</button>
                    </>
                  )}
                </td>
              </tr>
            ))}
          </tbody>
        </table>
      </div>
    </div>
  );
}

export default AdminMovieManager;
