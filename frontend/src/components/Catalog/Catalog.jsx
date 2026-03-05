import { useState, useEffect } from 'react'
import './catalog.css'
import MovieCard from '../MovieCard/MovieCard'

function Catalog() {
  const [movies, setMovies] = useState([])
  const [loading, setLoading] = useState(true) // Thêm trạng thái loading

  useEffect(() => {
    // Fetch active movies from Backend API
    // URL constructed from server.port=8080 and server.servlet.context-path=/api/v1
    fetch('http://localhost:8080/api/v1/phim')
      .then((response) => response.json())
      .then((data) => {
        console.log('Số lượng phim API trả về:', data.length)
        setMovies(data)
        setLoading(false) // Tắt loading khi có dữ liệu
      })
      .catch((error) => {
        console.error('Error fetching movies:', error)
        setLoading(false) // Tắt loading ngay cả khi lỗi
      })
  }, [])

  return (
    <div className="catalog-container">
      <h1>Danh Sách Phim Đang Chiếu</h1>
      
      {loading ? (
        <div className="loading-spinner"></div>
      ) : (
        <div className="movie-grid">
          {movies.map((movie) => (
            <MovieCard key={movie.id} movie={movie} />
          ))}
        </div>
      )}
    </div>
  )
}

export default Catalog
