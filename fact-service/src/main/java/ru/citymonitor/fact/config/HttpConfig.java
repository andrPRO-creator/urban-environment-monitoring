package ru.citymonitor.fact.config;
import org.springframework.context.annotation.*; import org.springframework.web.client.RestTemplate; import org.springframework.scheduling.concurrent.ThreadPoolTaskScheduler;
@Configuration
public class HttpConfig {
 @Bean RestTemplate restTemplate(){return new RestTemplate();}
 @Bean ThreadPoolTaskScheduler taskScheduler(){ThreadPoolTaskScheduler s=new ThreadPoolTaskScheduler();s.setPoolSize(1);s.setThreadNamePrefix("fact-generator-");return s;}
}
