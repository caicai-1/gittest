package com.leyou.auth.service;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.core.toolkit.Wrappers;
import com.leyou.auth.config.JwtProperties;
import com.leyou.auth.entity.ApplicationInfo;
import com.leyou.auth.mapper.ApplicationInfoMapper;
import com.leyou.client.user.UserClient;
import com.leyou.common.auth.pojo.AppInfo;
import com.leyou.common.auth.pojo.Payload;
import com.leyou.common.auth.pojo.UserInfo;
import com.leyou.common.auth.utils.JwtUtils;
import com.leyou.common.exception.pojo.ExceptionEnum;
import com.leyou.common.exception.pojo.LyException;
import com.leyou.common.utils.CookieUtils;
import com.leyou.user.entity.User;
import org.joda.time.DateTime;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.stereotype.Service;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.util.Date;
import java.util.List;
import java.util.concurrent.TimeUnit;

@Service
public class AuthService {

    @Autowired
    private UserClient userClient;

    @Autowired
    private JwtProperties jwtProperties;

    @Autowired
    private ApplicationInfoMapper applicationInfoMapper;

    @Autowired
    private StringRedisTemplate redisTemplate;

    @Autowired
    private BCryptPasswordEncoder passwordEncoder;

    /**
     * 登陆校验
     *
     * @param username
     * @param password
     * @param request
     * @param response
     */
    public void login(String username, String password, HttpServletRequest request, HttpServletResponse response) {
        //校验用户名和密码
        User loginUser = userClient.login(username, password);
        //验证后返回一个token写入到cookie中
        UserInfo userInfo = new UserInfo(loginUser.getId(), loginUser.getUsername(), "admin");

        createTokenWriteToCookie(userInfo, response);
    }

    //封装token，cookie
    private void createTokenWriteToCookie(UserInfo userInfo, HttpServletResponse response) {
        //生成token
        String token = JwtUtils.generateTokenExpireInMinutes(userInfo, jwtProperties.getPrivateKey(), jwtProperties.getCookie().getExpire());
        //封装cookie
        CookieUtils.newCookieBuilder()
                .response(response)
                .name(jwtProperties.getCookie().getCookieName())
                .value(token)
                .domain(jwtProperties.getCookie().getCookieDomain())
                .httpOnly(true)
                .build();

    }

    //判断用户是否登陆
    public UserInfo verify(HttpServletRequest request, HttpServletResponse response) {
        //取出token
        String token = CookieUtils.getCookieValue(request, jwtProperties.getCookie().getCookieName());
        //解析token
        Payload<UserInfo> payload = null;
        try {
            //检验token是否合法
            payload = JwtUtils.getInfoFromToken(token, jwtProperties.getPublicKey(), UserInfo.class);
        } catch (Exception e) {
            throw new LyException(ExceptionEnum.UNAUTHORIZED);
        }
        UserInfo userInfo = payload.getInfo();

        //查看当前token是否在黑名单内
        if (redisTemplate.hasKey(payload.getId())) {
            throw new LyException(ExceptionEnum.UNAUTHORIZED);
        }
        //设置token过期时间刷新
        //获取当前token的过期时间
        Date expiration = payload.getExpiration();
        // 计算刷新时间
        DateTime refreshTime = new DateTime(expiration).minusMinutes(jwtProperties.getCookie().getRefreshTime());

        //判断刷新时间<当前时间，重新生成token
        //isBeforeNow:判断时间是否在当前时间之前
        if (refreshTime.isBeforeNow()) {
            this.createTokenWriteToCookie(userInfo, response);
        }

        //返回给用户
        return userInfo;
    }

    //退出登陆
    public void logout(HttpServletRequest request, HttpServletResponse response) {
        //取出token
        String token = CookieUtils.getCookieValue(request, jwtProperties.getCookie().getCookieName());
        Payload<UserInfo> payload = null;
        try {
            //检验token是否合法
            payload = JwtUtils.getInfoFromToken(token, jwtProperties.getPublicKey(), UserInfo.class);
        } catch (Exception e) {
            throw new LyException(ExceptionEnum.UNAUTHORIZED);
        }
        String tokenId = payload.getId();
        //计算出token剩余时间，设置存进redis的存活时间，减小Redis的压力
        Date expiration = payload.getExpiration();
        Long time = expiration.getTime() - System.currentTimeMillis();

        redisTemplate.opsForValue().set(tokenId, "admin", time, TimeUnit.MILLISECONDS);

        //设置cookie的存活时间为0
        CookieUtils.deleteCookie(jwtProperties.getCookie().getCookieName(),
                jwtProperties.getCookie().getCookieDomain(), response);
    }

    /**
     * 查询服务名和密钥是否合法
     */
    public ApplicationInfo findServiceByServiceNameAndSecret(String serviceName, String secret) {
        ApplicationInfo applicationInfo = new ApplicationInfo();
        applicationInfo.setServiceName(serviceName);
        QueryWrapper<ApplicationInfo> query = Wrappers.query(applicationInfo);
        ApplicationInfo info = applicationInfoMapper.selectOne(query);
        if (info == null) {
            throw new LyException(ExceptionEnum.UNAUTHORIZED);
        }

        if (!passwordEncoder.matches(secret, info.getSecret())){
            throw new LyException(ExceptionEnum.UNAUTHORIZED);
        }
        return info;
    }

    public String authorization(String serviceName, String secret) {
        ApplicationInfo applicationInfo = findServiceByServiceNameAndSecret(serviceName, secret);
        List<String> targetList =  applicationInfoMapper.selectTargetsByServiceName(serviceName);
        AppInfo appInfo = new AppInfo(applicationInfo.getId(), applicationInfo.getServiceName(), targetList);

        String token = JwtUtils.generateTokenExpireInMinutes(appInfo, jwtProperties.getPrivateKey(), jwtProperties.getApp().getExpire());

        return token;
    }
}
