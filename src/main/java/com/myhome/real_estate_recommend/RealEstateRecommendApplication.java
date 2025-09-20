package com.myhome.real_estate_recommend;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.autoconfigure.domain.EntityScan;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.data.jpa.repository.config.EnableJpaRepositories;

@SpringBootApplication
@ComponentScan(basePackages = {"com.myhome.real_estate_recommend", "controller", "service", "security", "repository", "entity"})
@EntityScan(basePackages = "entity")
@EnableJpaRepositories(basePackages = "repository")
public class RealEstateRecommendApplication {

	public static void main(String[] args) {
		SpringApplication.run(RealEstateRecommendApplication.class, args);
	}

}
