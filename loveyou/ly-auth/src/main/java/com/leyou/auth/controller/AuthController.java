package com.leyou.auth.controller;

import com.leyou.auth.service.AuthService;
import com.leyou.common.auth.pojo.UserInfo;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

@RestController
public class AuthController {

    @Autowired
    private AuthService authService;


    /**
     * 用户登陆
     * @param username 用户名
     * @param password 密码
     * @param request 请求
     * @param response 响应
     * @return
     */
    @PostMapping("login")
    public ResponseEntity<Void> login(@RequestParam("username") String username,
                                      @RequestParam("password") String password,
                                      HttpServletRequest request,
                                      HttpServletResponse response) {
        authService.login(username, password, request, response);

        return ResponseEntity.status(HttpStatus.NO_CONTENT).build();
    }

    /**
     * 校验用户是否登陆
     * @param request
     * @param response
     * @return
     */
    @GetMapping("/verify")
    public ResponseEntity<UserInfo> verify(
            HttpServletRequest request,
            HttpServletResponse response
    ){
        UserInfo userInfo = authService.verify(request, response);
        return ResponseEntity.ok(userInfo);
    }

    /**
     * 退出登陆
     */
    @PostMapping("/logout")
    public ResponseEntity<Void> logout(
            HttpServletRequest request,
            HttpServletResponse response
    ){
        authService.logout(request, response);
        return ResponseEntity.status(HttpStatus.NO_CONTENT).build();
    }

    /**
     * 微服务验证生成token
     */
    @GetMapping("/authorization")
    public ResponseEntity<String> authorization(
            @RequestParam("serviceName") String serviceName,
            @RequestParam("secret") String secret
    ){
        String token = authService.authorization(serviceName, secret);
        return ResponseEntity.ok(token);
    }

}
