package org.recap;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.Bean;

@SpringBootApplication
public class Main {

	@Bean
	public ReCAPCamelContext getReCAPCamelContext(){
		return ReCAPCamelContext.getInstance();

	}

	public static void main(String[] args) {
		SpringApplication.run(Main.class, args);
	}
}
