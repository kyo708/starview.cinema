import { BrowserRouter as Router, Routes, Route } from 'react-router-dom';
import Catalog from './components/Catalog/Catalog.jsx';
import Navbar from './components/Navbar/Navbar.jsx';
import AdminLayout from './components/Admin/AdminLayout.jsx';
import Login from './components/Login/Login';
import MovieShowtime from './components/MovieShowtime/MovieShowtime.jsx';
import Register from './components/Register/Register.jsx';
import ProtectedRoute from './ProtectedRoute.jsx';
import SeatSelection from './components/SeatSelection/SeatSelection.jsx';
import Payment from './components/Payment/Payment.jsx';
import UserProfile from './components/MemberProfile/MemberProfile.jsx';

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

          {/* Route chi tiết phim & đặt vé (MovieShowtime) */}
          <Route path="/phim/:id" element={
            <>
              <MovieShowtime />
            </>
          } />
          {/* Route chọn ghế (SeatSelection) */}
          <Route path="/phim/:id/seatselection" element={
            <>
              <SeatSelection />
            </>
          } />
          {/* Route Thanh toán (Payment) */}
          <Route path="/payment" element={
            <>
              <Payment />
            </>
          } />

          {/* Route Hồ sơ người dùng */}
          <Route path="/profile" element={
            <>
              <Navbar />
              <UserProfile />
            </>
          } />


          {/* Route cho nhân viên: Trang quản lý phim */}
          <Route path="/admin" element={
            <ProtectedRoute>
              <AdminLayout />
            </ProtectedRoute>
          } />

          {/* Route đăng nhập */}
          <Route path="/login" element={<Login />} />

          {/* Route đăng ký */}
          <Route path="/register" element={<Register />} />
        </Routes>
      </div>
    </Router>
  )
}

export default App;