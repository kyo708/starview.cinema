package com.starview.cinemabooking.config;

import java.util.Arrays;

import org.springframework.boot.CommandLineRunner;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import com.starview.cinemabooking.model.Phim;
import com.starview.cinemabooking.repository.PhimRepository;

@Configuration
public class DataSeeder {
	@Bean
    CommandLineRunner initDatabase(PhimRepository phimRepository) {
        return args -> {
            // Only insert if the table is completely empty
            if (phimRepository.count() == 0) {
                
                Phim phim1 = new Phim();
                phim1.setTenPhim("Dune: Hành Tinh Cát - Phần 2");
                phim1.setGiaGoc(120000.0f);
                phim1.setThoiLuongPhut(166);
                phim1.setTrailerUrl("https://www.youtube.com/watch?v=Way9Dexny3w");
                phim1.setPosterUrl("https://image.tmdb.org/t/p/w500/8b8R8l88ILwM7t3zyZOG1TaROi3.jpg");
                phim1.setTheLoai("Viễn tưởng, Hành động");
                phim1.setDanhGia(8.8f);
                phim1.setActive(true);

                Phim phim2 = new Phim();
                phim2.setTenPhim("Kung Fu Panda 4");
                phim2.setGiaGoc(100000.0f);
                phim2.setThoiLuongPhut(94);
                phim2.setTrailerUrl("https://www.youtube.com/watch?v=_inKs4eeHiI");
                phim2.setPosterUrl("https://image.tmdb.org/t/p/w500/kDp1vUBnMpe8ak4rjgl3cLELqjU.jpg");
                phim2.setTheLoai("Hoạt hình, Hài hước");
                phim2.setDanhGia(7.5f);
                phim2.setActive(true);

                Phim phim3 = new Phim();
                phim3.setTenPhim("Godzilla x Kong: Đế Chế Mới");
                phim3.setGiaGoc(110000.0f);
                phim3.setThoiLuongPhut(115);
                phim3.setTrailerUrl("https://www.youtube.com/watch?v=qqrpMRDuPfc");
                phim3.setPosterUrl("https://image.tmdb.org/t/p/w500/tMefBSflR6PGQLvLuPEHZotffMv.jpg");
                phim3.setTheLoai("Hành động, Phiêu lưu");
                phim3.setDanhGia(6.9f);
                phim3.setActive(true);

                Phim phim4 = new Phim();
                phim4.setTenPhim("The Batman");
                phim4.setGiaGoc(120000.0f);
                phim4.setThoiLuongPhut(176);
                phim4.setTrailerUrl("https://www.youtube.com/watch?v=mqqft2x_Aa4");
                phim4.setPosterUrl("https://image.tmdb.org/t/p/w500/8Qxk238379X789V2F5f3c4gJYj.jpg");
                phim4.setTheLoai("Hành động, Hình sự");
                phim4.setDanhGia(6.9f);
                phim4.setActive(true); 
                
                Phim phim5 = new Phim();
                phim4.setTenPhim("Oppenheimer");
                phim4.setGiaGoc(120000.0f);
                phim4.setThoiLuongPhut(180);
                phim4.setTrailerUrl("https://www.youtube.com/watch?v=uYPbbksJxIg");
                phim4.setPosterUrl("https://image.tmdb.org/t/p/w500/8Gxv8gSFCU0XGDykEGv7zR1n2ua.jpg");
                phim4.setTheLoai("Lịch sử, Chính kịch");
                phim4.setDanhGia(8.1f);
                phim4.setActive(true); 
                
                Phim phim6 = new Phim();
                phim4.setTenPhim("Exhuma: Quật Mộ Trùng Ma");
                phim4.setGiaGoc(110000.0f);
                phim4.setThoiLuongPhut(134);
                phim4.setTrailerUrl("https://www.youtube.com/watch?v=7LH-TIcPqks");
                phim4.setPosterUrl("https://image.tmdb.org/t/p/w500/pQYHouPsDf32FhIKYB72laNSMod.jpg");
                phim4.setTheLoai("Hành động, Hình sự");
                phim4.setDanhGia(7.6f);
                phim4.setActive(true); 
                
                Phim phim7 = new Phim();
                phim4.setTenPhim("Civil War: Ngày Tàn Của Đế Quốc");
                phim4.setGiaGoc(110000.0f);
                phim4.setThoiLuongPhut(109);
                phim4.setTrailerUrl("https://www.youtube.com/watch?v=aDyQxtg0V2w");
                phim4.setPosterUrl("https://image.tmdb.org/t/p/w500/sh7Rg8Er3tFcN9BpKIPOMvALgZd.jpg");
                phim4.setTheLoai("Hành động, Chiến tranh");
                phim4.setDanhGia(7.4f);
                phim4.setActive(true); 
                
                Phim phim8 = new Phim();
                phim4.setTenPhim("Inside Out 2: Những Mảnh Ghép Cảm Xúc");
                phim4.setGiaGoc(100000.0f);
                phim4.setThoiLuongPhut(100);
                phim4.setTrailerUrl("https://www.youtube.com/watch?v=LEjhY15eCx0");
                phim4.setPosterUrl("https://image.tmdb.org/t/p/w500/vpnVM9B6NMmQpWeZvzLvDESb2QY.jpg");
                phim4.setTheLoai("Hoạt hình, Gia đình");
                phim4.setDanhGia(8.0f);
                phim4.setActive(true); 
                
                Phim phim9 = new Phim();
                phim4.setTenPhim("Hidden Movie");
                phim4.setGiaGoc(50000.0f);
                phim4.setThoiLuongPhut(10);
                phim4.setTrailerUrl("https://www.youtube.com/");
                phim4.setPosterUrl("https://image.tmdb.org/t/p/w500/vpnVM9B6NMmQpWeZvzLvDESb2QY.jpg");
                phim4.setTheLoai("Hidden");
                phim4.setDanhGia(0.0f);
                phim4.setActive(false); 

                phimRepository.saveAll(Arrays.asList(phim1, phim2, phim3, phim4, phim5, phim6, phim7, phim8, phim9));
                System.out.println("✅ Mock movie data successfully seeded!");
            }
        };
    }
}
