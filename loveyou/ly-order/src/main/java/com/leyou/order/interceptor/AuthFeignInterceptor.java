package com.leyou.order.interceptor;

import com.leyou.common.constants.LyConstants;
import com.leyou.order.scheduled.AppTokenScheduled;
import feign.RequestInterceptor;
import feign.RequestTemplate;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

/**
 * Feign拦截器
 */
@Component
public class AuthFeignInterceptor implements RequestInterceptor {

    @Autowired
    private AppTokenScheduled appTokenScheduled;
    /**
     * apply： 会在两个微服务调用的过程被执行
     * @param requestTemplate
     */
    @Override
    public void apply(RequestTemplate requestTemplate) {
        requestTemplate.header(LyConstants.APP_TOKEN_HEADER, appTokenScheduled.getToken());
    }
}
