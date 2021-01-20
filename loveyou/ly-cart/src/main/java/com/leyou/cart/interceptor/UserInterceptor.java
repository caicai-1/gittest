package com.leyou.cart.interceptor;

import com.leyou.cart.config.JwtProperties;
import com.leyou.common.auth.pojo.AppInfo;
import com.leyou.common.auth.pojo.Payload;
import com.leyou.common.auth.pojo.UserHolder;
import com.leyou.common.auth.pojo.UserInfo;
import com.leyou.common.auth.utils.JwtUtils;
import com.leyou.common.constants.LyConstants;
import com.leyou.common.utils.CookieUtils;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.springframework.web.servlet.HandlerInterceptor;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

@Component
public class UserInterceptor implements HandlerInterceptor {

    @Autowired
    private JwtProperties jwtProperties;

    @Override
    public boolean preHandle(HttpServletRequest request, HttpServletResponse response, Object handler) {
        //1.验证用户是否登陆，cookie验证
        String userToken = CookieUtils.getCookieValue(request, jwtProperties.getCookie().getCookieName());
        if (StringUtils.isEmpty(userToken)){
            return false;
        }
        Payload<UserInfo> userPayload = null;
        try {
            userPayload = JwtUtils.getInfoFromToken(userToken, jwtProperties.getPublicKey(), UserInfo.class);
        } catch (Exception e) {
            return false;
        }
        UserInfo userInfo = userPayload.getInfo();
        //存进ThreadLocal中，在此线程中存储，底层为map，key为‘该线程’
        UserHolder.setUser(userInfo);

        //2.服务鉴权
        String appToken = request.getHeader(LyConstants.APP_TOKEN_HEADER);

        if (StringUtils.isEmpty(appToken)){
            return false;
        }
        Payload<AppInfo> appPayload = null;
        try {
            appPayload = JwtUtils.getInfoFromToken(appToken, jwtProperties.getPublicKey(), AppInfo.class);
        } catch (Exception e) {
            return false;
        }
        return true;
    }
}
