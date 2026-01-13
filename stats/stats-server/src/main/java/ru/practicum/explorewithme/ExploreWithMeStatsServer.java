package ru.practicum.explorewithme;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.cloud.netflix.eureka.server.EnableEurekaServer;

@SpringBootApplication
@EnableEurekaServer
public class ExploreWithMeStatsServer {
    public static void main(String[] args) {
        SpringApplication.run(ExploreWithMeStatsServer.class, args);
    }
}
