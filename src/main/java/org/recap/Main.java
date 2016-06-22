package org.recap;

import org.apache.camel.CamelContext;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Scope;

@SpringBootApplication
public class Main {

    @Bean
    public ReCAPCamelContext getReCAPCamelContext(CamelContext camelContext) {
        return ReCAPCamelContext.getInstance(camelContext);

    }

    public static void main(String[] args) {
        SpringApplication.run(Main.class, args);
    }
}
