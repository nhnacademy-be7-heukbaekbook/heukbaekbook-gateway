package com.nhnacademy.heukbaekbookgateway.filter;

import com.nhnacademy.heukbaekbookgateway.util.JwtUtil;
import lombok.Getter;
import lombok.Setter;
import org.springframework.cloud.gateway.filter.GatewayFilter;
import org.springframework.cloud.gateway.filter.factory.AbstractGatewayFilterFactory;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Component;
import org.springframework.web.server.ResponseStatusException;

import java.util.List;

@Component
public class JwtAuthorizationFilter extends AbstractGatewayFilterFactory<JwtAuthorizationFilter.Config> {
    private static final String ROLE_ADMIN = "ROLE_ADMIN";
    private static final String ROLE_MEMBER = "ROLE_MEMBER";

    private final JwtUtil jwtUtil;

    public JwtAuthorizationFilter(JwtUtil jwtUtil) {
        super(Config.class);
        this.jwtUtil = jwtUtil;
    }

    @Override
    public GatewayFilter apply(Config config) {
        return (exchange, chain) -> {
            String path = exchange.getRequest().getPath().toString();

            if (config.getExcludedPaths() != null &&
                    config.getExcludedPaths().stream().anyMatch(path::startsWith)) {
                return chain.filter(exchange);
            }

            String token = jwtUtil.resolveToken(exchange.getRequest());
            if (token == null || !jwtUtil.validateToken(token)) {
                throw new ResponseStatusException(HttpStatus.UNAUTHORIZED, "JWT Token is missing or invalid");
            }

            String userRole = jwtUtil.getRoleFromToken(token);

            if (config.getAdminPaths().stream().anyMatch(path::startsWith) && !userRole.equals(ROLE_ADMIN)) {
                throw new ResponseStatusException(HttpStatus.FORBIDDEN, "Admin access only");
            }

            if (config.getMemberPaths().stream().anyMatch(path::startsWith) && !userRole.equals(ROLE_MEMBER)) {
                throw new ResponseStatusException(HttpStatus.FORBIDDEN, "Member access only");
            }

            return chain.filter(exchange);
        };
    }

    @Setter
    @Getter
    public static class Config {
        private List<String> excludedPaths;
        private List<String> adminPaths;
        private List<String> memberPaths;
    }
}
