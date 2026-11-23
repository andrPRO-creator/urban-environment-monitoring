package ru.citymonitor.fact;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.scheduling.annotation.EnableScheduling;

@SpringBootApplication
@EnableScheduling
public class FactServiceApplication {
    public static void main(String[] args) { SpringApplication.run(FactServiceApplication.class, args); }
}
