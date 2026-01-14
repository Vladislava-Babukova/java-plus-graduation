package ru.practicum.client;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
import org.springframework.web.util.UriComponentsBuilder;
import ru.practicum.StatsDto;
import ru.practicum.StatsParams;
import ru.practicum.StatsView;

import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.List;

@Component
public class StatsClientImpl implements StatsClient {
    private static final Logger log = LoggerFactory.getLogger(StatsClientImpl.class);
    private static final DateTimeFormatter FORMATTER = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");

    private final HttpClient httpClient;
    private final ObjectMapper objectMapper;
    private final String serverUrl;

    public StatsClientImpl(@Value("${stats.server.url:http://localhost:9090}") String serverUrl, ObjectMapper objectMapper) {
        this.serverUrl = serverUrl;
        this.httpClient = HttpClient.newHttpClient();
        this.objectMapper = objectMapper;
    }

    @Override
    public void hit(StatsDto statsDto) {
        try {
            String requestBody = objectMapper.writeValueAsString(statsDto);

            log.debug("Sending hit request to stats service. DTO: {}", statsDto);
            log.trace("Request body: {}", requestBody);

            HttpRequest request = HttpRequest.newBuilder()
                    .uri(URI.create(serverUrl + "/hit"))
                    .header("Content-Type", "application/json")
                    .POST(HttpRequest.BodyPublishers.ofString(requestBody))
                    .build();

            HttpResponse<Void> response = httpClient.send(request, HttpResponse.BodyHandlers.discarding());

            log.debug("Hit request completed. Status code: {}, URI: {}",
                    response.statusCode(), request.uri());

            if (response.statusCode() >= 400) {
                log.error("Hit request failed with status code: {}", response.statusCode());
                throw new StatsClientException("Failed to send hit: " + response.statusCode());
            }

            log.info("Hit successfully recorded: app={}, uri={}, ip={}",
                    statsDto.getApp(), statsDto.getUri(), statsDto.getIp());

        } catch (Exception e) {
            log.error("Failed to send hit. DTO: {}", statsDto, e);
            throw new StatsClientException("Failed to send hit", e);
        }
    }

    @Override
    public List<StatsView> getStats(StatsParams statsParams) {
        try {
            UriComponentsBuilder uriBuilder = UriComponentsBuilder.fromHttpUrl(serverUrl + "/stats")
                    .queryParam("start", encodeDateTime(statsParams.getStart()))
                    .queryParam("end", encodeDateTime(statsParams.getEnd()));

            if (statsParams.getUris() != null && !statsParams.getUris().isEmpty()) {
                statsParams.getUris().forEach(uri -> uriBuilder.queryParam("uris", uri));
            }

            if (statsParams.getUnique() != null) {
                uriBuilder.queryParam("unique", statsParams.getUnique());
            }

            String finalUri = uriBuilder.toUriString();

            log.debug("Requesting stats from service. Params: {}", statsParams);
            log.debug("Built URI: {}", finalUri);

            HttpRequest request = HttpRequest.newBuilder()
                    .uri(URI.create(finalUri))
                    .header("Content-Type", "application/json")
                    .GET()
                    .build();

            HttpResponse<String> response = httpClient.send(request, HttpResponse.BodyHandlers.ofString());

            log.debug("Stats request completed. Status code: {}, Response length: {} chars",
                    response.statusCode(), response.body().length());
            log.trace("Stats response body: {}", response.body());

            if (response.statusCode() >= 400) {
                log.error("Stats request failed with status code: {}. URI: {}",
                        response.statusCode(), finalUri);
                throw new StatsClientException("Failed to get stats: " + response.statusCode());
            }

            List<StatsView> result = objectMapper.readValue(
                    response.body(),
                    objectMapper.getTypeFactory().constructCollectionType(List.class, StatsView.class)
            );

            log.info("Retrieved {} stats records for period {} - {}",
                    result.size(), statsParams.getStart(), statsParams.getEnd());
            log.debug("Stats result: {}", result);

            return result;
        } catch (Exception e) {
            log.error("Failed to get stats. Params: {}", statsParams, e);
            throw new StatsClientException("Failed to get stats", e);
        }
    }

    private String encodeDateTime(LocalDateTime dateTime) {
        return dateTime.format(FORMATTER);
    }
}