package com.splitwise;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.data.jpa.repository.config.EnableJpaRepositories;

// Entry point for the Expense Sharing Backend application.
@SpringBootApplication
@EnableJpaRepositories(basePackages = "com.splitwise.repository")
public class Main {

  public static void main(String[] args) {
		SpringApplication.run(Main.class, args);
	}

}
