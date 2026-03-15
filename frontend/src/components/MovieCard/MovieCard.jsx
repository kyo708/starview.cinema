import React, { useState } from 'react';
import { useNavigate } from 'react-router-dom';
import './MovieCard.css';
import TrailerModal from '../TrailerModal/TrailerModal';

function MovieCard({ movie, rank }) {
  const [isTrailerVisible, setIsTrailerVisible] = useState(false);
  const navigate = useNavigate();

  return (
    <>
      <div className="gsc-movie-card">
        <div className="poster-wrapper">
          {/* Rank Number (Chỉ hiện ở phần Trending) */}
          {rank && <span className="rank-number">{rank}</span>}
          
          <img src={movie.posterUrl} alt={movie.tenPhim} className="main-poster" />
          
          {/* Lớp phủ khi hover: Hiện nút Play để xem Trailer */}
          <div className="hover-overlay" onClick={() => setIsTrailerVisible(true)}>
            <div className="play-circle">
              <svg viewBox="0 0 24 24" fill="white"><path d="M8 5v14l11-7z" /></svg>
            </div>
          </div>
        </div>

        <div className="movie-details">
          <h3 className="movie-title">{movie.tenPhim}</h3>
          <p className="movie-meta">{movie.theLoai} • {movie.thoiLuongPhut}m</p>
          <button className="gsc-buy-btn" onClick={() => navigate(`/phim/${movie.id}`)}>
            Mua Vé
          </button>
        </div>
      </div>

      {isTrailerVisible && (
        <TrailerModal
          trailerUrl={movie.trailerUrl}
          onClose={() => setIsTrailerVisible(false)}
        />
      )}
    </>
  );
}
export default MovieCard;