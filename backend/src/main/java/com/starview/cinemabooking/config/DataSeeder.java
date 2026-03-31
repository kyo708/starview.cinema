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
import com.starview.cinemabooking.model.GheSuatChieu;
import com.starview.cinemabooking.model.KhuyenMai;
import com.starview.cinemabooking.model.NguoiDung;
import com.starview.cinemabooking.repository.PhimRepository;
import com.starview.cinemabooking.repository.PhongChieuRepository;
import com.starview.cinemabooking.repository.SuatChieuRepository;
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
            PasswordEncoder passwordEncoder) {
        return args -> {
            // --- RESET DỮ LIỆU TOÀN BỘ (Theo yêu cầu) ---
            // Lưu ý: Phải xóa theo thứ tự từ bảng con đến bảng cha để tránh lỗi khóa ngoại
            System.out.println("🔄 Đang thực hiện reset toàn bộ dữ liệu...");
            gheSuatChieuRepository.deleteAll();
            suatChieuRepository.deleteAll();
            phongChieuRepository.deleteAll();
            phimRepository.deleteAll();
            khuyenMaiRepository.deleteAll();

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
                nguoiDungRepository.saveAll(Arrays.asList(adminUser, staffUser));
                System.out.println("✅ Admin account created: admin@starview.com / 123456");
                System.out.println("✅ Staff account created: staff@starview.com / 123456");
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
                room2.setTongSoGhe(80);
                room2.setLoaiPhong("IMAX");
                room2.setPhuThu(30000.0f); // Phụ thu 30k cho phim IMAX

                PhongChieu room3 = new PhongChieu();
                room3.setTenPhong("Phòng VIP");
                room3.setTongSoGhe(50);
                room3.setLoaiPhong("VIP");
                room3.setPhuThu(50000.0f); // Phụ thu 50k cho phòng siêu sang

                phongChieuRepository.saveAll(Arrays.asList(room1, room2, room3));
                System.out.println("✅ Mock room data successfully seeded!");
            }

            // --- TẠO MÃ GIẢM GIÁ MẪU ---
            if (khuyenMaiRepository.count() == 0) {
                LocalDateTime expiry = LocalDateTime.of(2026, 12, 31, 23, 59, 59);

                KhuyenMai save10 = new KhuyenMai();
                save10.setMaKhuyenMai("SAVE10");
                save10.setLoai("PERCENT");
                save10.setGiaTri(10f);
                save10.setMaxGiamGia(50000f);
                save10.setNgayHetHan(expiry);
                save10.setGioiHanSuDung(1);
                save10.setDaSuDung(0);

                KhuyenMai flat20000 = new KhuyenMai();
                flat20000.setMaKhuyenMai("FLAT20000");
                flat20000.setLoai("FLAT");
                flat20000.setGiaTri(20000f);
                flat20000.setMaxGiamGia(null);
                flat20000.setNgayHetHan(expiry);
                flat20000.setGioiHanSuDung(1);
                flat20000.setDaSuDung(0);

                khuyenMaiRepository.saveAll(Arrays.asList(save10, flat20000));
                System.out.println("✅ Mock voucher data successfully seeded!");
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

                            String seatType = determineSeatType(seatsGenerated + 1, totalCapacity);
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
        };
    }

    private String determineSeatType(int index, int totalSeats) {
    	// Giả sử 10 ghế cuối cùng luôn là Sweetbox đôi
        if (index > totalSeats - 10) {
            return "SWEETBOX";
        } 
        // Giả sử nửa sau của rạp (trừ sweetbox) là ghế VIP có góc nhìn đẹp nhất
        else if (index > totalSeats / 2) {
            return "VIP";
        } 
        // Nửa trước rạp là ghế Standard
        else {
            return "THUONG"; 
        }

    }
}
