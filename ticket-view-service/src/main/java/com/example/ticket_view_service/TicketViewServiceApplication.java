package com.example.ticket_view_service;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.cache.annotation.EnableCaching;

@SpringBootApplication
@EnableCaching
public class TicketViewServiceApplication {

	public static void main(String[] args) {
		SpringApplication.run(TicketViewServiceApplication.class, args);
	}

}
