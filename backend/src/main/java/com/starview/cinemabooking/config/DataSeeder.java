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
import com.starview.cinemabooking.model.NguoiDung;
import com.starview.cinemabooking.repository.PhimRepository;
import com.starview.cinemabooking.repository.PhongChieuRepository;
import com.starview.cinemabooking.repository.SuatChieuRepository;
import com.starview.cinemabooking.repository.GheSuatChieuRepository;
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
            PasswordEncoder passwordEncoder) {
        return args -> {
            // --- RESET DỮ LIỆU TOÀN BỘ (Theo yêu cầu) ---
            // Lưu ý: Phải xóa theo thứ tự từ bảng con đến bảng cha để tránh lỗi khóa ngoại
            System.out.println("🔄 Đang thực hiện reset toàn bộ dữ liệu...");
            gheSuatChieuRepository.deleteAll();
            suatChieuRepository.deleteAll();
            phongChieuRepository.deleteAll();
            phimRepository.deleteAll();

            // --- TẠO TÀI KHOẢN STAFF MẪU ---
            // Chỉ tạo nếu chưa có tài khoản nào trong DB
            if (nguoiDungRepository.count() == 0) {
                NguoiDung staffUser = new NguoiDung();
                staffUser.setHoTen("Staff Account");
                staffUser.setEmail("staff@starview.com");
                staffUser.setMatKhau(passwordEncoder.encode("123456"));
                staffUser.setVaiTro("STAFF");
                nguoiDungRepository.save(staffUser);
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

                PhongChieu room2 = new PhongChieu();
                room2.setTenPhong("Phòng 2");
                room2.setTongSoGhe(80);

                PhongChieu room3 = new PhongChieu();
                room3.setTenPhong("Phòng VIP");
                room3.setTongSoGhe(50);

                phongChieuRepository.saveAll(Arrays.asList(room1, room2, room3));
                System.out.println("✅ Mock room data successfully seeded!");
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
                    sc2.setHeSoGia(1.0f);

                    SuatChieu sc3 = new SuatChieu();
                    sc3.setPhim(phims.get(1)); // Phim: Kung Fu Panda 4
                    sc3.setPhongChieu(phongs.get(2)); // Phòng VIP
                    sc3.setThoiGianChieu(now.withHour(19).withMinute(15).withSecond(0)); 
                    sc3.setHeSoGia(1.0f);
                    
                    List<SuatChieu> savedShowtimes = suatChieuRepository.saveAll(Arrays.asList(sc1, sc2, sc3));
                    System.out.println("✅ Mock showtime data successfully seeded!");

                    List<GheSuatChieu> allSeats = new ArrayList<>();
                    for (SuatChieu showtime : savedShowtimes) {
                        int totalSeats = showtime.getPhongChieu().getTongSoGhe();
                        for (int i = 1; i <= totalSeats; i++) {
                            GheSuatChieu ghe = new GheSuatChieu();
                            ghe.setSuatChieu(showtime);
                            ghe.setLoaiGhe(determineSeatType(i));
                            ghe.setTrangThai("TRONG");
                            ghe.setThoiGianHetHanGiuCho(showtime.getThoiGianChieu());
                            ghe.setPhienBan(1); // Đổi sang ghe.setPhienBan(1) khi merge PR #54
                            allSeats.add(ghe);
                        }
                    }
                    gheSuatChieuRepository.saveAll(allSeats);
                    System.out.println("✅ Mock seats successfully generated for all showtimes!");
                }
            }
        };
    }
    
    private String determineSeatType(int index) {
     return index <= 30 ? "THUONG" : "VIP";

    }
}
