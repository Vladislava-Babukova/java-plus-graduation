package ru.practicum.explorewithme;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.cloud.client.discovery.EnableDiscoveryClient;


@SpringBootApplication
@EnableDiscoveryClient
public class ExploreWithMeStatsServer {
    public static void main(String[] args) {
        SpringApplication.run(ExploreWithMeStatsServer.class, args);
    }
}
