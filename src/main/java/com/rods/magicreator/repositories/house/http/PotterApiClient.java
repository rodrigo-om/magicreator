package com.rods.magicreator.repositories.house.http;

import com.rods.magicreator.repositories.house.http.models.HouseModelRoot;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Component;
import org.springframework.web.reactive.function.client.WebClient;
import reactor.core.publisher.Mono;
import reactor.util.retry.Retry;

import java.time.Duration;

@Component
public class PotterApiClient {

    private final WebClient webClient;

    public PotterApiClient(WebClient webClient) {
        this.webClient = webClient;
    }

    @Autowired(required = true)
    public PotterApiClient(@Value("${potterapi.baseURL}") String baseUrl, @Value("${potterapi.apikey}") String apiKey) {
        this.webClient = WebClient.builder()
                .defaultHeader("apikey", apiKey)
                .baseUrl(baseUrl)
                .build();
    }

    public HouseModelRoot getHouses() throws PotterApiCallException {
        return webClient
                .get()
                .uri("potterApi/houses")
                .retrieve()
                .onStatus(HttpStatus::is5xxServerError,
                        response -> Mono.error(new PotterApiCallException("Potter API returned an error.", response.rawStatusCode())))
                .bodyToMono(HouseModelRoot.class)
                .retryWhen(Retry.backoff(3, Duration.ofSeconds(5))
                        .filter(throwable -> throwable instanceof PotterApiCallException)
                        .onRetryExhaustedThrow((retryBackoffSpec, retrySignal) -> {
                            throw new PotterApiCallException("External Service failed to process after max retries", HttpStatus.SERVICE_UNAVAILABLE.value());
                        }))
                .block();
    }

    //Exists mainly to wrap specific status error to be filtered in potter api retry policy
    static class PotterApiCallException extends RuntimeException {

        private final int statusCode;

        public PotterApiCallException(String message, int statusCode) {
            super(message);
            this.statusCode = statusCode;
        }

        public int getStatusCode() {
            return statusCode;
        }
    }
}

