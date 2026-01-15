package cg.homelab.gateway.filter;

import cg.homelab.gateway.util.JwtUtil;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.cloud.gateway.filter.GatewayFilterChain;
import org.springframework.cloud.gateway.filter.GlobalFilter;
import org.springframework.core.Ordered;
import org.springframework.data.domain.Range;
import org.springframework.data.redis.core.ReactiveRedisTemplate;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Component;
import org.springframework.web.server.ServerWebExchange;
import reactor.core.publisher.Mono;

import java.time.Duration;
import java.time.Instant;

@Component
public class RateLimitFilter implements GlobalFilter, Ordered {
    @Autowired
    private ReactiveRedisTemplate<String, String> redisTemplate;
    @Autowired
    private JwtUtil jwtUtil;
    private static final int MAX_REQUESTS = 10;
    private static final int WINDOW_SIZE_SECONDS = 5;

    @Override
    public Mono<Void> filter(ServerWebExchange exchange, GatewayFilterChain chain) {
        String token = jwtUtil.getToken(exchange);
        if (token == null) {
            return chain.filter(exchange);
        }
        String username;
        try {
            username = jwtUtil.extractUsername(token);
        } catch (Exception e) {
            return chain.filter(exchange);
        }
        String rateLimitKey = "rate_limit:user:" + username;
        return isAllowed(rateLimitKey)
                .flatMap(allowed -> {
                    if (!allowed) {
                        return tooManyRequests(exchange);
                    }
                    return chain.filter(exchange);
                });
    }
    private Mono<Void> tooManyRequests(ServerWebExchange exchange) {
        exchange.getResponse().setStatusCode(HttpStatus.TOO_MANY_REQUESTS);
        exchange.getResponse().getHeaders().add("X-RateLimit-Limit", String.valueOf(MAX_REQUESTS));
        exchange.getResponse().getHeaders().add("Retry-After", String.valueOf(WINDOW_SIZE_SECONDS));
        return exchange.getResponse().setComplete();
    }
    private Mono<Boolean> isAllowed(String key) {
        long now = Instant.now().toEpochMilli();
        long windowStart = now - (WINDOW_SIZE_SECONDS * 1000);
        return redisTemplate.opsForZSet()
                .removeRangeByScore(key, Range.closed(0.0, (double) windowStart))
                .then(redisTemplate.opsForZSet()
                        .count(key, Range.closed((double) windowStart, (double) now)))
                .flatMap(count -> {
                    if (count >= MAX_REQUESTS) {
                        return Mono.just(false);
                    }
                    return redisTemplate.opsForZSet()
                            .add(key, String.valueOf(now), now)
                            .then(redisTemplate.expire(key, Duration.ofSeconds(WINDOW_SIZE_SECONDS + 1)))
                            .thenReturn(true);
                });
    }

    @Override
    public int getOrder() {
        return 0;
    }
}
