package org.recap;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.Bean;

import java.io.File;
import java.util.SimpleTimeZone;
import java.util.TimeZone;

@SpringBootApplication
public class Main {

    @Value("${recap.timezone}")
    String timeZone;

    @Bean
    public TimeZone setTime() {
        TimeZone.setDefault(TimeZone.getTimeZone(timeZone));
        return TimeZone.getDefault();
    }

    public static void main(String[] args) {
        SpringApplication.run(Main.class, args);
    }
}
