```mermaid
classDiagram
    %% ==========================================
    %% 1. CÁC LỚP THỰC THỂ (ENTITIES / MODELS)
    %% ==========================================
    class NguoiMua {
        -int id
        -String email
        -String matKhau
        -String sdt
        +dangNhap(String email, String pass) boolean
    }

    class Phim {
        -int id
        -String tenPhim
        -float giaGoc
        -int thoiLuongPhut
    }

    class PhongChieu {
        -int id
        -String tenPhong
        -int tongSoGhe
    }

    class SuatChieu {
        -int id
        -int phimId
        -int phongChieuId
        -DateTime thoiGianChieu
        -float heSoGia
    }

    class GheSuatChieu {
        -int id
        -int suatChieuId
        -int donHangId
        -String loaiGhe
        -String trangThai
        -DateTime thoiGianHetHanGiuCho
        -int phienBan
        +kiemTraHetHan() boolean
    }

    class DonHang {
        -int id
        -int nguoiMuaId
        -float tongTien
        -String trangThaiThanhToan
        -DateTime thoiGianTao
        +capNhatTrangThai(String trangThai) void
    }

    %% ==========================================
    %% 2. LỚP XỬ LÝ NGHIỆP VỤ (SERVICE LAYER)
    %% ==========================================
    class TicketService {
        +giuChoGhe(int suatChieuId, int gheId, int nguoiMuaId) boolean
        +tinhTienVe(int suatChieuId, int gheId) float
        +xacNhanThanhToan(int donHangId) boolean
        +taoVaGuiVeQR(int donHangId) String
        +giaiPhongGheHetHan() void
    }

    %% ==========================================
    %% 3. CÁC MỐI QUAN HỆ (RELATIONSHIPS)
    %% ==========================================
    
    %% Quan hệ Thành phần (Composition) - Gắn bó chặt chẽ
    PhongChieu "1" *-- "*" SuatChieu : chứa
    Phim "1" *-- "*" SuatChieu : chiếu
    SuatChieu "1" *-- "*" GheSuatChieu : bao gồm
    
    %% Quan hệ Tập hợp (Aggregation) - Sở hữu lỏng lẻo hơn
    NguoiMua "1" o-- "*" DonHang : tạo
    DonHang "1" o-- "*" GheSuatChieu : chứa

    %% Quan hệ Phụ thuộc (Dependency) - Service thao tác lên Entity
    TicketService ..> DonHang : xử lý thanh toán
    TicketService ..> GheSuatChieu : đổi trạng thái/khóa ghế
    TicketService ..> SuatChieu : lấy hệ số giá vé