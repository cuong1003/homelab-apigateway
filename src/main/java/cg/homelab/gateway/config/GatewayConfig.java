package cg.homelab.gateway.config;

import org.springframework.cloud.gateway.route.RouteLocator;
import org.springframework.cloud.gateway.route.builder.RouteLocatorBuilder;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class GatewayConfig {

    @Bean
    public RouteLocator customeRouteLocator(RouteLocatorBuilder builder) {
        return builder.routes()
                .route("jellyseerr", r -> r
                        .path("/jellyseerr/**")
                        .filters(f -> f.stripPrefix(1))
                        .uri("http://192.168.1.109:5055"))
                .route("authservice", r -> r
                        .path("/auth/**")
                        .filters(f -> f.stripPrefix(1))
                        .uri("http://localhost:6969"))
                .build();
    }
}
