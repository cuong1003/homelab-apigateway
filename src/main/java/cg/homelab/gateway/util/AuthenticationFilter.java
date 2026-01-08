package cg.homelab.gateway.util;

import org.springframework.cloud.gateway.filter.GatewayFilterChain;
import org.springframework.cloud.gateway.filter.GlobalFilter;
import org.springframework.core.Ordered;
import org.springframework.http.HttpCookie;
import org.springframework.http.HttpStatus;
import org.springframework.http.server.reactive.ServerHttpRequest;
import org.springframework.stereotype.Component;
import org.springframework.web.reactive.function.client.WebClient;
import org.springframework.web.server.ServerWebExchange;
import reactor.core.publisher.Mono;

import java.util.List;
import java.util.Map;

@Component
public class AuthenticationFilter implements GlobalFilter, Ordered {

    private final WebClient webClient;
    
    // Danh sách các path PUBLIC - không cần token
    private static final List<String> PUBLIC_PATHS = List.of(
            "/auth/login",
            "/auth/signup",
            "/public"
    );

    public AuthenticationFilter(WebClient.Builder webClient) {
        this.webClient = webClient
                .baseUrl("http://localhost:6969")
                .build();
    }

    @Override
    public Mono<Void> filter(ServerWebExchange exchange, GatewayFilterChain chain) {
        ServerHttpRequest request = exchange.getRequest();
        String path = request.getPath().toString();
        
        // Kiểm tra path có public không
        if (isPublicPath(path)) {
            return chain.filter(exchange);
        }
        
        // Lấy token từ Header hoặc Cookie
        String token = extractToken(exchange);
        
        if (token == null) {
            // Nếu không có token, redirect về login thay vì trả 401
            return redirectToLogin(exchange);
        }
        
        // Validate token với auth-service
        return isAuthenticated(token)
                .flatMap(isValid -> {
                    if (isValid) {
                        return chain.filter(exchange);
                    }
                    // Token invalid, redirect về login
                    return redirectToLogin(exchange);
                })
                .onErrorResume(e -> onError(exchange, HttpStatus.SERVICE_UNAVAILABLE));
    }

    /**
     * Kiểm tra path có phải public không
     */
    private boolean isPublicPath(String path) {
        return PUBLIC_PATHS.stream().anyMatch(path::startsWith);
    }

    /**
     * Lấy token từ Authorization header hoặc Cookie
     */
    private String extractToken(ServerWebExchange exchange) {
        ServerHttpRequest request = exchange.getRequest();
        
        // 1. Thử lấy từ Authorization header
        String authHeader = request.getHeaders().getFirst("Authorization");
        if (authHeader != null && authHeader.startsWith("Bearer ")) {
            return authHeader.substring(7);
        }
        
        // 2. Thử lấy từ Cookie
        HttpCookie cookie = request.getCookies().getFirst("AUTH_TOKEN");
        if (cookie != null) {
            return cookie.getValue();
        }
        
        return null;
    }

    /**
     * Redirect về trang login
     */
    private Mono<Void> redirectToLogin(ServerWebExchange exchange) {
        exchange.getResponse().setStatusCode(HttpStatus.FOUND);
        exchange.getResponse().getHeaders().add("Location", "/auth/login");
        return exchange.getResponse().setComplete();
    }

    private Mono<Void> onError(ServerWebExchange exchange, HttpStatus status) {
        exchange.getResponse().setStatusCode(status);
        return exchange.getResponse().setComplete();
    }

    private Mono<Boolean> isAuthenticated(String token) {
        return webClient.post()
                .uri("/api/auth/validate-token")
                .header("Authorization", "Bearer " + token)
                .retrieve()
                .bodyToMono(Map.class)
                .map(response -> Boolean.TRUE.equals(response.get("valid")));
    }

    @Override
    public int getOrder() {
        return -1;
    }
}
