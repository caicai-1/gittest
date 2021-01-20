package com.leyou.gateway.filter;

import com.leyou.common.auth.pojo.Payload;
import com.leyou.common.auth.pojo.UserInfo;
import com.leyou.common.auth.utils.JwtUtils;
import com.leyou.common.constants.LyConstants;
import com.leyou.gateway.Scheduled.AppTokenScheduled;
import com.leyou.gateway.config.FilterProperties;
import com.leyou.gateway.config.JwtProperties;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.cloud.gateway.filter.GatewayFilterChain;
import org.springframework.cloud.gateway.filter.GlobalFilter;
import org.springframework.core.Ordered;
import org.springframework.http.HttpStatus;
import org.springframework.http.server.reactive.ServerHttpRequest;
import org.springframework.http.server.reactive.ServerHttpResponse;
import org.springframework.stereotype.Component;
import org.springframework.web.server.ServerWebExchange;
import reactor.core.publisher.Mono;

import java.util.List;

@Component
public class AuthFilter implements GlobalFilter, Ordered {
    @Autowired
    private JwtProperties jwtProperties;

    @Autowired
    private FilterProperties filterProperties;

    @Autowired
    private AppTokenScheduled appTokenScheduled;

    @Override
    public Mono<Void> filter(ServerWebExchange exchange, GatewayFilterChain chain) {
        ServerHttpRequest request = exchange.getRequest();
        ServerHttpResponse response = exchange.getResponse();

        //需求：往请求头添加应用token
        ServerHttpRequest newRequest = request.mutate().header(LyConstants.APP_TOKEN_HEADER,
                                appTokenScheduled.getToken()).build();
        //生成新的请求头，需要重新设置进新的请求中,使用原来的ServerWebExchange对象来接就行
        exchange = exchange.mutate().request(newRequest).build();


        //白名单访问放行
        String path = request.getURI().getPath();
        List<String> allowPaths = filterProperties.getAllowPaths();
        for (String allowPath : allowPaths) {
            if (path.contains(allowPath)) {
                return chain.filter(exchange);
            }
        }

        Payload<UserInfo> payload = null;
        try {
            String token = request.getCookies().getFirst(jwtProperties.getCookie().getCookieName()).getValue();
            //验证登陆才允许访问其他微服务
            payload = JwtUtils.getInfoFromToken(token, jwtProperties.getPublicKey(), UserInfo.class);
        } catch (Exception e) {
            e.printStackTrace();
            //2.1 不合法，先中止请求，再返回状态码 401 提示未授权
            response.setStatusCode(HttpStatus.UNAUTHORIZED);
            return response.setComplete();
        }
        return chain.filter(exchange);
    }

    @Override
    public int getOrder() {
        return 1;
    }
}
