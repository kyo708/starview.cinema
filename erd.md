```mermaid
erDiagram
    %% Định nghĩa các mối quan hệ
    NGUOI_MUA ||--o{ DON_HANG : "tạo"
    PHIM ||--o{ SUAT_CHIEU : "có"
    PHONG_CHIEU ||--o{ SUAT_CHIEU : "chứa"
    SUAT_CHIEU ||--o{ GHE_SUAT_CHIEU : "bao gồm"
    DON_HANG ||--o{ GHE_SUAT_CHIEU : "chứa"

    %% Định nghĩa chi tiết các bảng
    NGUOI_MUA {
        int id PK
        string email
        string mat_khau
        string so_dien_thoai
    }

    PHIM {
        int id PK
        string ten_phim
        float gia_goc
        int thoi_luong_phut
    }
    
    PHONG_CHIEU {
        int id PK
        string ten_phong
        int tong_so_ghe
    }
    
    SUAT_CHIEU {
        int id PK
        int phim_id FK
        int phong_chieu_id FK
        datetime thoi_gian_chieu
        float he_so_gia 
    }
    
    GHE_SUAT_CHIEU {
        int id PK
        int suat_chieu_id FK
        int don_hang_id FK
        string loai_ghe "Ví dụ: Thường, VIP, Đôi"
        string trang_thai "Trống, Đang chờ, Đã bán"
        datetime thoi_gian_het_han_giu_cho
        int lock "Dùng cho Khóa lạc quan chống trùng ghế"
    }
    
    DON_HANG {
        int id PK
        int nguoi_mua_id FK
        float tong_tien
        string trang_thai_thanh_toan "Chờ thanh toán, Thành công, Thất bại"
        datetime thoi_gian_tao
    }