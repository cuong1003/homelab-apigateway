package cg.homelab.gateway.filter;

import org.springframework.cloud.gateway.filter.GatewayFilterChain;
import org.springframework.cloud.gateway.filter.GlobalFilter;
import org.springframework.core.Ordered;
import org.springframework.http.server.reactive.ServerHttpRequest;
import org.springframework.stereotype.Component;
import org.springframework.web.server.ServerWebExchange;
import reactor.core.publisher.Mono;

/**
 * GlobalFilter để thêm header X-Forwarded-Prefix vào mọi request.
 */
@Component
public class ForwardedPrefixFilter implements GlobalFilter, Ordered {

    @Override
    public Mono<Void> filter(ServerWebExchange exchange, GatewayFilterChain chain) {
        String path = exchange.getRequest().getPath().toString();
        System.out.println("=== ForwardedPrefixFilter ===");
        System.out.println("Original path: " + path);
        
        String prefix = extractPrefix(path);
        System.out.println("Extracted prefix: " + prefix);
        
        if (prefix != null) {
            // Tạo request mới với header X-Forwarded-Prefix
            ServerHttpRequest modifiedRequest = exchange.getRequest().mutate()
                    .header("X-Forwarded-Prefix", prefix)
                    .build();
            
            System.out.println("Added header X-Forwarded-Prefix: " + prefix);
            
            // Tạo exchange mới với request đã modified
            ServerWebExchange modifiedExchange = exchange.mutate()
                    .request(modifiedRequest)
                    .build();
            
            return chain.filter(modifiedExchange);
        }
        
        System.out.println("No prefix added");
        return chain.filter(exchange);
    }

    private String extractPrefix(String path) {
        if (path.startsWith("/auth")) {
            return "/auth";
        }
        if (path.startsWith("/jellyseerr")) {
            return "/jellyseerr";
        }
        return null;
    }

    @Override
    public int getOrder() {
        return -100;
    }
}
