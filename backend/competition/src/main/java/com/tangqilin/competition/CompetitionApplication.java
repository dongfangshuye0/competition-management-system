package com.tangqilin.competition;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.mybatis.spring.annotation.MapperScan;

@MapperScan("com.tangqilin.competition.mapper")
@SpringBootApplication
public class CompetitionApplication {

	public static void main(String[] args) {
		SpringApplication.run(CompetitionApplication.class, args);
	}

}
