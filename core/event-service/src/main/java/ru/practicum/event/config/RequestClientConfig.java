package ru.practicum.event.config;

import com.fasterxml.jackson.databind.ObjectMapper;
import feign.codec.ErrorDecoder;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import ru.practicum.event.client.request.RequestClientErrorDecoder;

@Configuration
public class RequestClientConfig {
    @Bean
    public ErrorDecoder requestErrorDecoder(ObjectMapper objectMapper) {
        return new RequestClientErrorDecoder(objectMapper);
    }
}


