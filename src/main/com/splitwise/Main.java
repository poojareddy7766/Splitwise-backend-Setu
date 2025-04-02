package com.splitwise.backend;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

 // Entry point for the Expense Sharing Backend application.
@SpringBootApplication
public class Main {

  public static void main(String[] args) {
		SpringApplication.run(Main.class, args);
	}

}
// just to test if the server is running
@RestController
@RequestMapping("/api")
class TestController {
    
    @GetMapping("/test")
    public String testEndpoint() {
        return "Splitwise Backend is running!";
    }
}