package com.plm.platform.gateway.common.filter;

import com.plm.platform.common.core.entity.constant.PlatformConstant;
import com.plm.platform.gateway.enhance.service.RouteEnhanceService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.cloud.gateway.filter.GatewayFilterChain;
import org.springframework.cloud.gateway.filter.GlobalFilter;
import org.springframework.core.annotation.Order;
import org.springframework.http.server.reactive.ServerHttpRequest;
import org.springframework.stereotype.Component;
import org.springframework.util.AntPathMatcher;
import org.springframework.util.Base64Utils;
import org.springframework.web.server.ServerWebExchange;
import reactor.core.publisher.Mono;

/**
 * @author crystal
 */
@Slf4j
@Component
@Order(0)
@RequiredArgsConstructor
public class PlatformGatewayRequestFilter implements GlobalFilter {

    private final RouteEnhanceService routeEnhanceService;
    private final AntPathMatcher pathMatcher = new AntPathMatcher();
    @Value("${platform.gateway.enhance:false}")
    private Boolean routeEhance;

    @Override
    public Mono<Void> filter(ServerWebExchange exchange, GatewayFilterChain chain) {
        if (routeEhance) {
            Mono<Void> blackListResult = routeEnhanceService.filterBlackList(exchange);
            if (blackListResult != null) {
                routeEnhanceService.saveBlockLogs(exchange);
                return blackListResult;
            }
            Mono<Void> rateLimitResult = routeEnhanceService.filterRateLimit(exchange);
            if (rateLimitResult != null) {
                routeEnhanceService.saveRateLimitLogs(exchange);
                return rateLimitResult;
            }
            routeEnhanceService.saveRequestLogs(exchange);
        }

        byte[] token = Base64Utils.encode((PlatformConstant.GATEWAY_TOKEN_VALUE).getBytes());
        String[] headerValues = {new String(token)};
        ServerHttpRequest build = exchange.getRequest().mutate().header(PlatformConstant.GATEWAY_TOKEN_HEADER, headerValues).build();
        ServerWebExchange newExchange = exchange.mutate().request(build).build();
        return chain.filter(newExchange);
    }
}
