package com.leyou.user.controller;

import com.leyou.common.exception.pojo.LyException;
import com.leyou.user.entity.AddressDTO;
import com.leyou.user.entity.User;
import com.leyou.user.service.UserService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.support.DefaultMessageSourceResolvable;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.BindingResult;
import org.springframework.validation.FieldError;
import org.springframework.web.bind.annotation.*;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.validation.Valid;
import java.util.stream.Collectors;

@RestController
public class UserController {

    @Autowired
    private UserService userService;

    //校验手机和用户名是否重复
    @GetMapping("/check/{data}/{type}")
    public ResponseEntity<Boolean> checkData(@PathVariable("data") String data, @PathVariable("type") Integer type){
        Boolean result = userService.checkData(data, type);
        return ResponseEntity.ok(result);
    }

    //发送验证码
    @PostMapping("/code")
    public ResponseEntity<Void> sendCheckCode(@RequestParam("phone") String phone){
        userService.sendCheckCode(phone);
        return ResponseEntity.status(HttpStatus.NO_CONTENT).build();
    }

    //添加用户
    @PostMapping("/register")
    public ResponseEntity<Void> register(@Valid User user,
                                         BindingResult result,//此对象要紧挨着被校验的对象
                                         @RequestParam("code") String code){
        //@Valid是Hibernate后台校验的插件
        //校验后自定义返回异常信息
        if (result.hasErrors()){
            //收集异常信息
            String errorStr = result.getFieldErrors()
                                    .stream()
                                    .map(FieldError::getDefaultMessage)
                                    .collect(Collectors.joining("|"));
            //抛出异常信息
            throw new LyException(400, errorStr);
        }
        userService.register(user, code);
        return ResponseEntity.status(HttpStatus.NO_CONTENT).build();
    }

    /**
     * 根据用户名和密码查询用户
     */
    @PostMapping("/login")
    public ResponseEntity<User> login(@RequestParam("username") String username,
                                      @RequestParam("password") String password ){
        User user = userService.login(username, password);
        return ResponseEntity.ok(user);
    }

    /**
     * 根据用户id获取用户地址信息
     */
    @GetMapping("/address")
    public ResponseEntity<AddressDTO> findAddressByUserId(@RequestParam("userId")Long userId, @RequestParam("id")Long id){
        AddressDTO addressDTO = userService.findAddressByUserId(userId, id);
        return ResponseEntity.ok(addressDTO);
    }
}
