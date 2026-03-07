import { BrowserRouter as Router, Routes, Route } from 'react-router-dom';
import Catalog from './components/Catalog/Catalog.jsx';
import Navbar from './components/Navbar/Navbar.jsx';
import AdminMovieManager from './components/Admin/AdminMovieManager';
import './App.css';

function App() {
  return (
    <Router>
      <div>
        <Routes>
          {/* Route cho khách hàng: Trang chủ hiển thị Navbar và Danh sách phim */}
          <Route path="/" element={
            <>
              <Navbar />
              <Catalog />
            </>
          } />

          {/* Route cho nhân viên: Trang quản lý phim */}
          <Route path="/admin" element={<AdminMovieManager />} />
        </Routes>
      </div>
    </Router>
  )
}

export default App;