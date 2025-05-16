package com.example.crowdfund;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.autoconfigure.jdbc.DataSourceAutoConfiguration;

//@SpringBootApplication(exclude = {DataSourceAutoConfiguration.class})
@SpringBootApplication
public class CrowdfundApplication {

	public static void main(String[] args) {
		SpringApplication.run(CrowdfundApplication.class, args);
	}

}
