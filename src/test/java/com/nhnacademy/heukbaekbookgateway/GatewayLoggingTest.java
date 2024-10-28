package com.nhnacademy.heukbaekbookgateway;

import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.web.server.LocalServerPort;
import org.springframework.web.reactive.function.client.WebClient;

import static org.assertj.core.api.Assertions.assertThat;

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
public class GatewayLoggingTest {

    @LocalServerPort
    private int port;

    private final WebClient webClient = WebClient.create();

    @Test
    void testGatewayLogging() {

        String url = "http://localhost:" + port + "/test";

        String response = webClient.get()
                .uri(url)
                .retrieve()
                .bodyToMono(String.class)
                .block();

        System.out.println("응답: " + response);

        assertThat(response).isNotNull();
        assertThat(response).isEqualTo("Hello");
    }
}
