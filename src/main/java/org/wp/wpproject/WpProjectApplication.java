package org.wp.wpproject;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.scheduling.annotation.EnableScheduling;

/**
 * Điểm khởi chạy chính của ứng dụng Spring Boot
 */
@SpringBootApplication
@EnableScheduling // Bật tính năng scheduling (chạy các task theo lịch)
public class WpProjectApplication {

	public static void main(String[] args) {
		SpringApplication.run(WpProjectApplication.class, args);
		System.out.println("🚀 WP Project Application is running...");
	}
}
