package cg.homelab.gateway.util;

import io.jsonwebtoken.Claims;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.security.Keys;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
import org.springframework.web.server.ServerWebExchange;
import reactor.core.publisher.Mono;
import org.springframework.data.redis.core.ReactiveRedisTemplate;

import java.security.Key;
import java.util.Base64;

@Component
public class JwtUtil {
    @Value("${jwt.secret}")
    private String secretKey;

    @Autowired
    private ReactiveRedisTemplate<String, String> redisTemplate;

    public Mono<Boolean> validateToken(String token) {
        try {
            Jwts.parserBuilder()
                    .setSigningKey(getSigningKey())
                    .build()
                    .parseClaimsJws(token);
            return redisTemplate.hasKey("blacklist:" + token)
                    .map(exists -> !exists);
        } catch (Exception e) {
            return Mono.just(false);
        }
    }
    public String extractUsername(String token){
        return extractClaimsJws(token).getSubject();
    }
    public String extractRole(String token){
        return extractClaimsJws(token).get("role", String.class);
    }
    public String getToken(ServerWebExchange exchange) {
        String header = exchange.getRequest().getHeaders().getFirst("Authorization");
        if (header != null && header.startsWith("Bearer ")) {
            return header.substring(7);
        }
        if (exchange.getRequest().getCookies().containsKey("AUTH_TOKEN")) {
            return exchange.getRequest().getCookies().getFirst("AUTH_TOKEN").getValue();
        }
        return null;
    }
    private Claims extractClaimsJws(String token){
        return Jwts.parserBuilder()
                .setSigningKey(getSigningKey())
                .build()
                .parseClaimsJws(token)
                .getBody();
    }
    private Key getSigningKey() {
        byte[] keyBytes = Base64.getDecoder().decode(secretKey);
        return Keys.hmacShaKeyFor(keyBytes);
    }
}
