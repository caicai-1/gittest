package com.leyou.client.user;

import com.leyou.user.entity.AddressDTO;
import com.leyou.user.entity.User;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;

@FeignClient("user-service")
public interface UserClient {

    /**
     * 根据用户名和密码查询用户
     */
    @PostMapping("/login")
    public User login(@RequestParam("username") String username,
                                      @RequestParam("password") String password );

    /**
     * 根据用户id获取用户地址信息
     */
    @GetMapping("/address")
    public AddressDTO findAddressByUserId(
            @RequestParam("userId")Long userId,
            @RequestParam("id")Long id);
}
