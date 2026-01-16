package cg.homelab.gateway.config;

import org.springframework.cloud.gateway.route.RouteLocator;
import org.springframework.cloud.gateway.route.builder.RouteLocatorBuilder;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class GatewayConfig {

    private static final String HOMELAB_IP = "http://192.168.1.109";

    @Bean
    public RouteLocator customeRouteLocator(RouteLocatorBuilder builder) {
        return builder.routes()
                // Auth Service
                .route("auth-service", r -> r
                        .path("/auth/**")
                        .filters(f -> f
                                .stripPrefix(1)
                                .addRequestHeader("X-Forwarded-Prefix", "/auth"))
                        .uri("http://auth-service:6969"))

                // Jellyseerr - Tất cả users
                .route("jellyseerr", r -> r
                        .path("/jellyseerr/**")
                        .filters(f -> f.stripPrefix(1))
                        .uri(HOMELAB_IP + ":5055"))

                // Radarr - Quản lý phim (Admin)
                .route("radarr", r -> r
                        .path("/radarr/**")
                        .filters(f -> f.stripPrefix(1))
                        .uri(HOMELAB_IP + ":7878"))

                // Sonarr - Quản lý TV series (Admin)
                .route("sonarr", r -> r
                        .path("/sonarr/**")
                        .filters(f -> f.stripPrefix(1))
                        .uri(HOMELAB_IP + ":8989"))

                // Prowlarr - Quản lý indexers (Admin)
                .route("prowlarr", r -> r
                        .path("/prowlarr/**")
                        .filters(f -> f.stripPrefix(1))
                        .uri(HOMELAB_IP + ":9696"))

                // qBittorrent - Download client (Admin)
                .route("qbittorrent", r -> r
                        .path("/qbittorrent/**")
                        .filters(f -> f.stripPrefix(1))
                        .uri(HOMELAB_IP + ":8081"))

                // FlareSolverr - Bypass Cloudflare (Admin)
                .route("flaresolverr", r -> r
                        .path("/flaresolverr/**")
                        .filters(f -> f.stripPrefix(1))
                        .uri(HOMELAB_IP + ":8191"))

                .build();
    }
}
