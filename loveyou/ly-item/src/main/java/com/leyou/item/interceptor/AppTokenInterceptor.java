package com.leyou.item.interceptor;

import com.leyou.common.auth.pojo.AppInfo;
import com.leyou.common.auth.pojo.Payload;
import com.leyou.common.auth.utils.JwtUtils;
import com.leyou.common.constants.LyConstants;
import com.leyou.item.config.JwtProperties;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.springframework.web.servlet.HandlerInterceptor;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

/**
 * springMvc拦截器，得有注册拦截器才生效
 */
@Component
@Slf4j
public class AppTokenInterceptor implements HandlerInterceptor {

    @Autowired
    private JwtProperties jwtProps;

    /**
     * 前置方法，进行权限校验
     * true: 放行
     * false： 不放行
     */
    @Override
    public boolean preHandle(HttpServletRequest request, HttpServletResponse response, Object handler) {
        String token = request.getHeader(LyConstants.APP_TOKEN_HEADER);

        if (StringUtils.isEmpty(token)){
            log.error("【服务鉴权】{token不存在}");
            return false;
        }
        Payload<AppInfo> payload = null;
        try {
            payload = JwtUtils.getInfoFromToken(token, jwtProps.getPublicKey(), AppInfo.class);
        } catch (Exception e) {
            e.printStackTrace();
            log.error("【服务鉴权】{token不在有效服务列表}");
            return false;
        }

        log.info("【服务鉴权】{服务成功鉴权}");
        return true;
    }
}
