package cg.homelab.gateway.filter;

import cg.homelab.gateway.util.JwtUtil;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.cloud.gateway.filter.GatewayFilterChain;
import org.springframework.cloud.gateway.filter.GlobalFilter;
import org.springframework.core.Ordered;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Component;
import org.springframework.web.server.ServerWebExchange;
import reactor.core.publisher.Mono;

import java.net.URI;
import java.util.List;

@Component
public class AuthenticationFilter implements GlobalFilter, Ordered {

    @Autowired
    private JwtUtil jwtUtil;

    private static final List<String> PUBLIC_PATHS = List.of(
            "/auth/login",
            "/auth/register",
            "/auth/css/",
            "/auth/js/"
    );

    @Override
    public Mono<Void> filter(ServerWebExchange exchange, GatewayFilterChain chain) {
        String token = jwtUtil.getToken(exchange);
        String path = exchange.getRequest().getURI().getPath();

        if (path.equals("/auth/login") && token != null) {
            return jwtUtil.validateToken(token)
                    .flatMap(isValid -> {
                        if (isValid) {
                            // Token hợp lệ → redirect đến dashboard
                            exchange.getResponse().setStatusCode(HttpStatus.FOUND);  // 302 redirect
                            exchange.getResponse().getHeaders().setLocation(URI.create("/auth/dashboard"));
                            return exchange.getResponse().setComplete();
                        }
                        return chain.filter(exchange);
                    });
        }

        if (PUBLIC_PATHS.stream().anyMatch(path::startsWith)) {
            return chain.filter(exchange);
        }

        if (token == null) {
            exchange.getResponse().getHeaders().setLocation(URI.create("/auth/login"));
            exchange.getResponse().setStatusCode(HttpStatus.FOUND);
            return exchange.getResponse().setComplete();
        }

        return jwtUtil.validateToken(token).flatMap(validate -> {
            if (!validate) {
                exchange.getResponse().getHeaders().setLocation(URI.create("/auth/login"));
                exchange.getResponse().setStatusCode(HttpStatus.FOUND);
                return exchange.getResponse().setComplete();
            }
            return chain.filter(exchange);
        });
    }

    @Override
    public int getOrder() {
        return -1;
    }
}
