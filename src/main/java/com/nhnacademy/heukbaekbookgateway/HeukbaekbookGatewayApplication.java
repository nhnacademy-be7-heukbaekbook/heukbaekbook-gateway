package com.nhnacademy.heukbaekbookgateway;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.cloud.client.discovery.EnableDiscoveryClient;

@SpringBootApplication
@EnableDiscoveryClient
public class HeukbaekbookGatewayApplication {

    public static void main(String[] args) {
        SpringApplication.run(HeukbaekbookGatewayApplication.class, args);
    }

}
