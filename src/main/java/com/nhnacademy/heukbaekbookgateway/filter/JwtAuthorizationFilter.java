package com.nhnacademy.heukbaekbookgateway.filter;

import com.nhnacademy.heukbaekbookgateway.util.JwtUtil;
import lombok.Getter;
import lombok.Setter;
import org.springframework.cloud.gateway.filter.GatewayFilter;
import org.springframework.cloud.gateway.filter.factory.AbstractGatewayFilterFactory;
import org.springframework.http.HttpStatus;
import org.springframework.http.server.reactive.ServerHttpRequest;
import org.springframework.stereotype.Component;
import org.springframework.web.server.ResponseStatusException;

import java.util.List;

@Component
public class JwtAuthorizationFilter extends AbstractGatewayFilterFactory<JwtAuthorizationFilter.Config> {
    private static final String ROLE_ADMIN = "ROLE_ADMIN";
    private static final String ROLE_MEMBER = "ROLE_MEMBER";
    private static final String X_USER_ROLE = "X-USER-ROLE";

    private final JwtUtil jwtUtil;

    public JwtAuthorizationFilter(JwtUtil jwtUtil) {
        super(Config.class);
        this.jwtUtil = jwtUtil;
    }

    @Override
    public GatewayFilter apply(Config config) {
        return (exchange, chain) -> {
            String path = exchange.getRequest().getPath().toString();
            String method = exchange.getRequest().getMethod().toString();

            if (config.getExcludedPaths() != null &&
                    config.getExcludedPaths().stream().anyMatch(p -> matches(p, path, method))) {
                return chain.filter(exchange);
            }

            String token = jwtUtil.resolveToken(exchange.getRequest());
            if (token == null || !jwtUtil.validateToken(token)) {
                throw new ResponseStatusException(HttpStatus.UNAUTHORIZED, "JWT Token is missing or invalid");
            }

            String userRole = getValueFromRequest(exchange.getRequest(), X_USER_ROLE);

            // Admin paths
            checkAccess(config.getAdminPaths(), ROLE_ADMIN, userRole, path, method);

            // Member paths
            checkAccess(config.getMemberPaths(), ROLE_MEMBER, userRole, path, method);


            return chain.filter(exchange);
        };
    }

    private boolean matches(PathMethodConfig configPath, String requestPath, String requestMethod) {
        if (configPath == null || requestPath == null || requestMethod == null) {
            return false;
        }
        boolean pathMatches = requestPath.startsWith(configPath.getPath());
        boolean methodMatches = configPath.getMethods() == null ||
                configPath.getMethods().stream().anyMatch(m -> m.equalsIgnoreCase(requestMethod));
        return pathMatches && methodMatches;
    }

    private void verifyRole(String userRole, String requiredRole) {
        if (!userRole.equals(requiredRole)) {
            throw new ResponseStatusException(HttpStatus.FORBIDDEN, requiredRole + " access only");
        }
    }

    private void checkAccess(List<PathMethodConfig> paths, String requiredRole, String userRole, String path, String method) {
        if (paths != null) {
            for (PathMethodConfig p : paths) {
                if (matches(p, path, method)) {
                    verifyRole(userRole, requiredRole);
                    break;
                }
            }
        }
    }

    private String getValueFromRequest(ServerHttpRequest request, String name) {
        return request.getHeaders().getFirst(name);
    }

    @Setter
    @Getter
    public static class Config {
        private List<PathMethodConfig> excludedPaths;
        private List<PathMethodConfig> adminPaths;
        private List<PathMethodConfig> memberPaths;
    }

    @Setter
    @Getter
    public static class PathMethodConfig {
        private String path;
        private List<String> methods;
    }
}
