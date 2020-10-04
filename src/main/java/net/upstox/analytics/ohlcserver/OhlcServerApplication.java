package net.upstox.analytics.ohlcserver;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.scheduling.annotation.EnableAsync;

@SpringBootApplication
@EnableAsync
public class OhlcServerApplication {

	public static void main(String[] args) {
		SpringApplication.run(OhlcServerApplication.class, args);
	}

}
