package com.hostelhub.hostelservice;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.cloud.openfeign.EnableFeignClients;

@SpringBootApplication
@EnableFeignClients
public class HostelServiceApplication {

	public static void main(String[] args) {
		SpringApplication.run(HostelServiceApplication.class, args);
	}

}
