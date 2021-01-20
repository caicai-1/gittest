package com.leyou.user.service;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.core.toolkit.Wrappers;
import com.leyou.common.constants.LyConstants;
import com.leyou.common.constants.MQConstants;
import com.leyou.common.exception.pojo.ExceptionEnum;
import com.leyou.common.exception.pojo.LyException;
import com.leyou.user.entity.AddressDTO;
import com.leyou.user.entity.User;
import com.leyou.user.mapper.UserMapper;
import org.apache.commons.lang.RandomStringUtils;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.servlet.HandlerInterceptor;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.util.HashMap;
import java.util.Map;

@Service
@Transactional
public class UserService {

    @Autowired
    private UserMapper userMapper;

    @Autowired
    private RabbitTemplate rabbitTemplate;

    @Autowired
    private StringRedisTemplate redisTemplate;
    @Autowired
    private BCryptPasswordEncoder passwordEncoder;

    public Boolean checkData(String data, Integer type) {
        // type:要校验的数据类型：1，用户名；2，手机
        User user = new User();
        if (type==1){
            user.setUsername(data);
        }else if (type == 2) {
            user.setPhone(data);
        }else {
            throw new LyException(ExceptionEnum.INVALID_PARAM_ERROR);
        }
        QueryWrapper<User> wrapper = Wrappers.query(user);
        Integer count = userMapper.selectCount(wrapper);
        return count == 0;
    }

    public void sendCheckCode(String phone) {
        //spring自带的生成6位随机数
        String code = RandomStringUtils.randomNumeric(6);
        //存到redis中
        redisTemplate.opsForValue().set(LyConstants.REDIS_KEY_PRE, code);

        Map<String, Object> map = new HashMap<>();
        map.put("phone", phone);
        map.put("code", code);
        rabbitTemplate.convertAndSend(MQConstants.Exchange.SMS_EXCHANGE_NAME, MQConstants.RoutingKey.VERIFY_CODE_KEY,map);
    }

    public void register(User user, String code) {
        String redisCode = redisTemplate.opsForValue().get(LyConstants.REDIS_KEY_PRE);
        if (!redisCode.equals(code)){
            throw new LyException(ExceptionEnum.INSERT_OPERATION_FAIL);
        }
        //设置密码加盐加密，配置十次加密，且不同的盐
        user.setPassword(passwordEncoder.encode(user.getPassword()));
        userMapper.insert(user);
    }


    public User login(String username, String password) {
        User user = new User();
        user.setUsername(username);
        QueryWrapper<User> wrapper = Wrappers.query(user);
        //根据名字查出用户
        User loginUser = userMapper.selectOne(wrapper);
        //判断用户名是否正确
        if (loginUser == null){
            throw new LyException(ExceptionEnum.UNAUTHORIZED);
        }
        //判断用户密码加密后是否相等
        if (!passwordEncoder.matches(password, loginUser.getPassword())){
            throw new LyException(ExceptionEnum.UNAUTHORIZED);
        }

        return loginUser;
    }

    public AddressDTO findAddressByUserId(Long userId, Long id) {
        AddressDTO addressDTO = new AddressDTO();
        addressDTO.setId(id);
        addressDTO.setUserId(userId);
        addressDTO.setAddressee("小飞飞");
        addressDTO.setPhone("13800138000");
        addressDTO.setCity("广州");
        addressDTO.setProvince("广东");
        addressDTO.setDistrict("天河区");
        addressDTO.setStreet("珠吉路津安创业园");
        addressDTO.setPostcode("510000");
        addressDTO.setIsDefault(true);
        return addressDTO;
    }
}
