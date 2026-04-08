package com.starview.cinemabooking.config;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import org.springframework.boot.CommandLineRunner;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.crypto.password.PasswordEncoder;

import com.starview.cinemabooking.model.Phim;
import com.starview.cinemabooking.model.PhongChieu;
import com.starview.cinemabooking.model.SuatChieu;
import com.starview.cinemabooking.model.DichVuBanKem;
import com.starview.cinemabooking.model.GheSuatChieu;
import com.starview.cinemabooking.model.KhuyenMai;
import com.starview.cinemabooking.model.NguoiDung;
import com.starview.cinemabooking.repository.PhimRepository;
import com.starview.cinemabooking.repository.PhongChieuRepository;
import com.starview.cinemabooking.repository.SuatChieuRepository;
import com.starview.cinemabooking.repository.DichVuBanKemRepository;
import com.starview.cinemabooking.repository.DonHangRepository;
import com.starview.cinemabooking.repository.GheSuatChieuRepository;
import com.starview.cinemabooking.repository.KhuyenMaiRepository;
import com.starview.cinemabooking.repository.NguoiDungRepository;

@Configuration
public class DataSeeder {
    @Bean
    CommandLineRunner initDatabase(
            PhimRepository phimRepository,
            NguoiDungRepository nguoiDungRepository,
            PhongChieuRepository phongChieuRepository,
            SuatChieuRepository suatChieuRepository,
            GheSuatChieuRepository gheSuatChieuRepository,
            KhuyenMaiRepository khuyenMaiRepository,
            DonHangRepository donHangRepository,
            DichVuBanKemRepository dichVuBanKemRepository,
            PasswordEncoder passwordEncoder) {
        return args -> {
            // --- RESET DỮ LIỆU TOÀN BỘ (Theo yêu cầu) ---
            // Lưu ý: Phải xóa theo thứ tự từ bảng con đến bảng cha để tránh lỗi khóa ngoại
            System.out.println("🔄 Đang thực hiện reset toàn bộ dữ liệu...");
            
            // THE FIX: Use InBatch to bypass Optimistic Locking checks
            gheSuatChieuRepository.deleteAllInBatch();
            donHangRepository.deleteAllInBatch();      // <-- 2. THÊM DÒNG NÀY ĐỂ XÓA ĐƠN HÀNG
            suatChieuRepository.deleteAllInBatch();
            phongChieuRepository.deleteAllInBatch();
            phimRepository.deleteAllInBatch();
            khuyenMaiRepository.deleteAllInBatch();
            dichVuBanKemRepository.deleteAllInBatch();

            // --- TẠO TÀI KHOẢN STAFF MẪU ---
            // Chỉ tạo nếu chưa có tài khoản nào trong DB
            if (nguoiDungRepository.count() == 0) {
                NguoiDung staffUser = new NguoiDung();
                staffUser.setHoTen("Staff Account");
                staffUser.setEmail("staff@starview.com");
                staffUser.setMatKhau(passwordEncoder.encode("123456"));
                staffUser.setVaiTro("STAFF");

                NguoiDung adminUser = new NguoiDung();
                adminUser.setHoTen("Admin Account");
                adminUser.setEmail("admin@starview.com");
                adminUser.setMatKhau(passwordEncoder.encode("123456"));
                adminUser.setVaiTro("ADMIN");
                
                // Thêm một tài khoản MEMBER có sẵn điểm để frontend dev test giỏ hàng
                NguoiDung memberUser = new NguoiDung();
                memberUser.setHoTen("VIP Member");
                memberUser.setEmail("member@starview.com");
                memberUser.setMatKhau(passwordEncoder.encode("123456"));
                memberUser.setVaiTro("MEMBER");
                memberUser.setDiemTichLuy(1500); // Tặng 1500 điểm test

                nguoiDungRepository.saveAll(Arrays.asList(adminUser, staffUser, memberUser));
                System.out.println("✅ Admin account created: admin@starview.com / 123456");
                System.out.println("✅ Staff account created: staff@starview.com / 123456");
                System.out.println("✅ Member account created: member@starview.com / 123456");
            }

            // --- TẠO DỮ LIỆU PHIM MẪU ---
            // Chỉ tạo nếu chưa có phim nào trong DB
            if (phimRepository.count() == 0) {
                Phim phim1 = new Phim();
                phim1.setTenPhim("Dune: Hành Tinh Cát - Phần 2");
                phim1.setGiaGoc(120000.0f);
                phim1.setThoiLuongPhut(166);
                phim1.setTrailerUrl("https://www.youtube.com/watch?v=Way9Dexny3w");
                phim1.setPosterUrl("https://image.tmdb.org/t/p/w500/8QdnKQyZDlN6rBSrfU1V5PctfUu.jpg");
                phim1.setTheLoai("Viễn tưởng, Hành động");
                phim1.setDanhGia(8.8f);
                phim1.setMoTa("Mô tả Placeholder");
                phim1.setActive(true);

                Phim phim2 = new Phim();
                phim2.setTenPhim("Kung Fu Panda 4");
                phim2.setGiaGoc(100000.0f);
                phim2.setThoiLuongPhut(94);
                phim2.setTrailerUrl("https://www.youtube.com/watch?v=_inKs4eeHiI");
                phim2.setPosterUrl("https://image.tmdb.org/t/p/w500/kDp1vUBnMpe8ak4rjgl3cLELqjU.jpg");
                phim2.setTheLoai("Hoạt hình, Hài hước");
                phim2.setDanhGia(7.5f);
                phim2.setMoTa("Mô tả Placeholder");
                phim2.setActive(true);

                Phim phim3 = new Phim();
                phim3.setTenPhim("Godzilla x Kong: Đế Chế Mới");
                phim3.setGiaGoc(110000.0f);
                phim3.setThoiLuongPhut(115);
                phim3.setTrailerUrl("https://www.youtube.com/watch?v=qqrpMRDuPfc");
                phim3.setPosterUrl("https://image.tmdb.org/t/p/w500/lTpnAtn1hWXDLxEmkD28l6UyPlF.jpg");
                phim3.setTheLoai("Hành động, Phiêu lưu");
                phim3.setDanhGia(6.9f);
                phim3.setMoTa("Mô tả Placeholder");
                phim3.setActive(true);

                Phim phim4 = new Phim();
                phim4.setTenPhim("The Batman");
                phim4.setGiaGoc(120000.0f);
                phim4.setThoiLuongPhut(176);
                phim4.setTrailerUrl("https://www.youtube.com/watch?v=mqqft2x_Aa4");
                phim4.setPosterUrl("https://image.tmdb.org/t/p/w500/nMp4tu8XuVG3CSWdXTFiHLdngnc.jpg");
                phim4.setTheLoai("Hành động, Hình sự");
                phim4.setDanhGia(6.9f);
                phim4.setMoTa("Mô tả Placeholder");
                phim4.setActive(true);

                Phim phim5 = new Phim();
                phim5.setTenPhim("Oppenheimer");
                phim5.setGiaGoc(120000.0f);
                phim5.setThoiLuongPhut(180);
                phim5.setTrailerUrl("https://www.youtube.com/watch?v=uYPbbksJxIg");
                phim5.setPosterUrl("https://image.tmdb.org/t/p/w500/8Gxv8gSFCU0XGDykEGv7zR1n2ua.jpg");
                phim5.setTheLoai("Lịch sử, Chính kịch");
                phim5.setDanhGia(8.1f);
                phim5.setMoTa("Mô tả Placeholder");
                phim5.setActive(true);

                Phim phim6 = new Phim();
                phim6.setTenPhim("Exhuma: Quật Mộ Trùng Ma");
                phim6.setGiaGoc(11000.0f);
                phim6.setThoiLuongPhut(134);
                phim6.setTrailerUrl("https://www.youtube.com/watch?v=7LH-TIcPqks");
                phim6.setPosterUrl("https://image.tmdb.org/t/p/w500/enAODUiL6eMpKaYPH2BEStVafQ2.jpg");
                phim6.setTheLoai("Hành động, Hình sự");
                phim6.setDanhGia(7.6f);
                phim6.setMoTa("Mô tả Placeholder");
                phim6.setActive(true);

                Phim phim7 = new Phim();
                phim7.setTenPhim("Civil War: Ngày Tàn Của Đế Quốc");
                phim7.setGiaGoc(110000.0f);
                phim7.setThoiLuongPhut(109);
                phim7.setTrailerUrl("https://www.youtube.com/watch?v=aDyQxtg0V2w");
                phim7.setPosterUrl("https://image.tmdb.org/t/p/w500/sh7Rg8Er3tFcN9BpKIPOMvALgZd.jpg");
                phim7.setTheLoai("Hành động, Chiến tranh");
                phim7.setDanhGia(7.4f);
                phim7.setMoTa("Mô tả Placeholder");
                phim7.setActive(true);

                Phim phim8 = new Phim();
                phim8.setTenPhim("Inside Out 2: Những Mảnh Ghép Cảm Xúc");
                phim8.setGiaGoc(100000.0f);
                phim8.setThoiLuongPhut(100);
                phim8.setTrailerUrl("https://www.youtube.com/watch?v=LEjhY15eCx0");
                phim8.setPosterUrl("https://image.tmdb.org/t/p/w500/vpnVM9B6NMmQpWeZvzLvDESb2QY.jpg");
                phim8.setTheLoai("Hoạt hình, Gia đình");
                phim8.setDanhGia(8.0f);
                phim8.setMoTa("Mô tả Placeholder");
                phim8.setActive(true);

                Phim phim9 = new Phim();
                phim9.setTenPhim("Hidden Movie");
                phim9.setGiaGoc(50000.0f);
                phim9.setThoiLuongPhut(10);
                phim9.setTrailerUrl("https://www.youtube.com/");
                phim9.setPosterUrl("https://image.tmdb.org/t/p/w500/vpnVM9B6NMmQpWeZvzLvDESb2QY.jpg");
                phim9.setTheLoai("Hidden");
                phim9.setDanhGia(0.0f);
                phim9.setMoTa("Mô tả Placeholder");
                phim9.setActive(true);

                phimRepository.saveAll(Arrays.asList(phim1, phim2, phim3, phim4, phim5, phim6, phim7, phim8, phim9));
                System.out.println("✅ Mock movie data successfully seeded!");
            }

            // --- TẠO PHÒNG CHIẾU MẪU ---
            if (phongChieuRepository.count() == 0) {
                PhongChieu room1 = new PhongChieu();
                room1.setTenPhong("Phòng 1");
                room1.setTongSoGhe(100);
                room1.setLoaiPhong("2D");
                room1.setPhuThu(0.0f); // Không phụ thu

                PhongChieu room2 = new PhongChieu();
                room2.setTenPhong("Phòng 2");
                room2.setTongSoGhe(64);
                room2.setLoaiPhong("IMAX");
                room2.setPhuThu(30000.0f); // Phụ thu 30k cho phim IMAX

                PhongChieu room3 = new PhongChieu();
                room3.setTenPhong("Phòng VIP");
                room3.setTongSoGhe(48);
                room3.setLoaiPhong("VIP");
                room3.setPhuThu(50000.0f); // Phụ thu 50k cho phòng siêu sang

                phongChieuRepository.saveAll(Arrays.asList(room1, room2, room3));
                System.out.println("✅ Mock room data successfully seeded!");
            }

            // --- TẠO MÃ GIẢM GIÁ MẪU ---
            if (khuyenMaiRepository.count() == 0) {
                LocalDateTime expiry = LocalDateTime.of(2026, 12, 31, 23, 59, 59);

                KhuyenMai welcome = new KhuyenMai();
                welcome.setMaKhuyenMai("WELCOME");
                welcome.setLoai("FLAT");
                welcome.setGiaTri(50000f);
                welcome.setMaxGiamGia(null);
                welcome.setNgayHetHan(expiry);
                welcome.setGioiHanSuDung(10000000); // Không giới hạn lượt sử dụng
                welcome.setDaSuDung(0);
                welcome.setDanhChoThanhVienMoi(true);

                khuyenMaiRepository.save(welcome);
                System.out.println("✅ Welcome voucher WELCOME seeded (new members only)!");
            }

            // --- TẠO SUẤT CHIẾU MẪU ---
            if (suatChieuRepository.count() == 0) {
                List<Phim> phims = phimRepository.findAll();
                List<PhongChieu> phongs = phongChieuRepository.findAll();

                // Đảm bảo đã có Phim và Phòng chiếu thì mới tạo Suất chiếu
                if (!phims.isEmpty() && !phongs.isEmpty()) {
                    LocalDateTime now = LocalDateTime.now();

                    SuatChieu sc1 = new SuatChieu();
                    sc1.setPhim(phims.get(0)); // Phim: Dune 2
                    sc1.setPhongChieu(phongs.get(0)); // Phòng 1
                    sc1.setThoiGianChieu(now.withHour(18).withMinute(30).withSecond(0));
                    sc1.setHeSoGia(1.0f);

                    SuatChieu sc2 = new SuatChieu();
                    sc2.setPhim(phims.get(0)); // Phim: Dune 2
                    sc2.setPhongChieu(phongs.get(1)); // Phòng 2
                    sc2.setThoiGianChieu(now.plusDays(1).withHour(20).withMinute(0).withSecond(0));
                    sc2.setHeSoGia(1.2f); // Suất chiếu giờ vàng cuối tuần đắt hơn x1.2

                    SuatChieu sc3 = new SuatChieu();
                    sc3.setPhim(phims.get(1)); // Phim: Kung Fu Panda 4
                    sc3.setPhongChieu(phongs.get(2)); // Phòng VIP
                    sc3.setThoiGianChieu(now.withHour(19).withMinute(15).withSecond(0));
                    sc3.setHeSoGia(1.0f);

                    List<SuatChieu> savedShowtimes = suatChieuRepository.saveAll(Arrays.asList(sc1, sc2, sc3));
                    System.out.println("✅ Mock showtime data successfully seeded!");

                    List<GheSuatChieu> allSeats = new ArrayList<>();
                    for (SuatChieu showtime : savedShowtimes) {
                        int totalCapacity = showtime.getPhongChieu().getTongSoGhe();
                        int seatsGenerated = 0;
                        while (seatsGenerated < totalCapacity) {
                            GheSuatChieu ghe = new GheSuatChieu();
                            ghe.setSuatChieu(showtime);

                            String seatType = determineSeatType(seatsGenerated + 1, totalCapacity, showtime.getPhongChieu().getLoaiPhong());
                            ghe.setLoaiGhe(seatType);

                            ghe.setTrangThai("TRONG");
                            ghe.setThoiGianHetHanGiuCho(showtime.getThoiGianChieu());
                            ghe.setPhienBan(1);
                            allSeats.add(ghe);

                            if ("SWEETBOX".equals(seatType)) {
                                seatsGenerated += 2; // Ghế đôi chiếm 2 sức chứa
                            } else {
                                seatsGenerated += 1; // Ghế thường/VIP chiếm 1 sức chứa
                            }
                        }
                    }
                    gheSuatChieuRepository.saveAll(allSeats);
                    System.out.println("✅ Mock seats successfully generated for all showtimes!");
                }
            }
            // --- TẠO PERKS / ĐỒ ĂN KÈM MẪU (NEW LOGIC) ---
            if (dichVuBanKemRepository.count() == 0) {
                DichVuBanKem b1 = new DichVuBanKem(null, "Bắp Thường", 300, "https://upload.wikimedia.org/wikipedia/commons/0/09/Popcorn02.jpg");
                DichVuBanKem b2 = new DichVuBanKem(null, "Bắp Phô Mai", 350, "https://static.wixstatic.com/media/35f3d9_5b91c3b320af4d96b9ee85b67332965b~mv2.jpg/v1/fill/w_560,h_352,al_c,q_80,usm_0.66_1.00_0.01,enc_avif,quality_auto/Image-empty-state.jpg");
                DichVuBanKem b3 = new DichVuBanKem(null, "Bắp Caramel", 350, "https://cdn.eva.vn//upload/3-2013/images/2013-09-30/1380527354-8.jpg");
                
                DichVuBanKem d1 = new DichVuBanKem(null, "Coca Cola", 200, "https://img.asmedia.epimg.net/resizer/v2/DSWLWJ7BVJD25JQT7YTRI63ES4.jpg?auth=e5ecd62e9d7c277d574a06940a3e8a964507e07307948442af52764f398bbdcd&width=1200&height=1200&smart=true");
                DichVuBanKem d2 = new DichVuBanKem(null, "Pepsi", 200, "https://ktmt.vnmediacdn.com/images/2022/05/09/13-1652060192-anh-1.jpg");
                DichVuBanKem d3 = new DichVuBanKem(null, "Milo", 250, "https://lh5.googleusercontent.com/proxy/-apuGHWEhvIvc8ZMzVzzIg6-1FChCdgW2Co60VNUmobF2KbUcMirFO_9MwLcAVH2j2E7yoZJ5UA0Zbigztvv3BnXRwghJvXOyCTn6QrE_Q-nQci9lkFD5lyS");
                DichVuBanKem d4 = new DichVuBanKem(null, "Sprite", 200, "https://as2.ftcdn.net/v2/jpg/18/67/70/11/1000_F_1867701190_YKrzqXvOQFFTBzdHcjc3LgszTF7vK3XA.jpg");
                DichVuBanKem d5 = new DichVuBanKem(null, "7Up", 200, "https://c8.alamy.com/comp/2PPH09C/can-of-7-up-drink-in-crashed-ice-2PPH09C.jpg");
                
                DichVuBanKem c1 = new DichVuBanKem(null, "Combo Solo (1 Bắp + 1 Nước)", 450, "https://media.istockphoto.com/id/681903568/vi/anh/b%E1%BB%8Fng-ng%C3%B4-trong-h%E1%BB%99p-v%E1%BB%9Bi-cola.jpg?s=612x612&w=0&k=20&c=K3GZEiD0fB31ufVuEkJZsxOt8ZTm5YaMMr7Eh2-rKjI=");
                DichVuBanKem c2 = new DichVuBanKem(null, "Combo Couple (1 Bắp Phô Mai + 2 Nước)", 750, "https://static.wixstatic.com/media/45c4d7_e4d4d8f5f94d41e38007ca35573dd15a~mv2.jpg/v1/fill/w_480,h_480,al_c,q_80,usm_0.66_1.00_0.01,enc_avif,quality_auto/45c4d7_e4d4d8f5f94d41e38007ca35573dd15a~mv2.jpg");

                dichVuBanKemRepository.saveAll(Arrays.asList(b1, b2, b3, d1, d2, d3, d4, d5, c1, c2));
                System.out.println("✅ Mock Perks (Concessions) successfully seeded!");
            }
        };
    }

    private String determineSeatType(int index, int totalCapacity, String loaiPhong) {
        String type = (loaiPhong != null) ? loaiPhong.trim().toUpperCase() : "2D";
        
        switch (type) {
            case "VIP":

                if (index <= 12) return "THUONG";
                else if (index <= 42) return "VIP";
                else return "SWEETBOX";
            case "IMAX":
                // Ví dụ phòng IMAX (Tổng 80 sức chứa): 20 Thường, 40 VIP, 20 sức chứa cho Sweetbox (10 ghế đôi)
                if (index <= 16) return "THUONG";
                else if (index <= 56) return "VIP";
                else return "SWEETBOX";
                
            case "2D":
            default:
                // Ví dụ phòng 2D (Tổng 100 sức chứa): 40 Thường, 50 VIP, 10 sức chứa cho Sweetbox (5 ghế đôi)
                if (index <= 30) return "THUONG";
                else if (index <= 90) return "VIP";
                else return "SWEETBOX";
        }
    }
}
