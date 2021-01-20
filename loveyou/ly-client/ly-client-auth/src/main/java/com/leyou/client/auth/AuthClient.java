package com.leyou.client.auth;

import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;

@FeignClient("auth-service")
public interface AuthClient {

    /**
     * 微服务验证生成token
     */
    @GetMapping("/authorization")
    public String authorization(
            @RequestParam("serviceName") String serviceName,
            @RequestParam("secret") String secret
    );
}
