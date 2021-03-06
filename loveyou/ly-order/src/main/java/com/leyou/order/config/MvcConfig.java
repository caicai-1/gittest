package com.leyou.order.config;

import com.leyou.order.interceptor.UserInterceptor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.springframework.web.servlet.config.annotation.InterceptorRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

@Component
public class MvcConfig implements WebMvcConfigurer {

    @Autowired
    private UserInterceptor userInterceptor;
    /**
     * 往环境添加拦截器
     */
    @Override
    public void addInterceptors(InterceptorRegistry registry) {
        //addInterceptor: 把拦截器添加到环境中
        //addPathPatterns: 添加拦截器的目标资源 默认: /**
        //excludePathPatterns: 把某些目标资源排除出去
        registry.addInterceptor(userInterceptor).addPathPatterns("/**").excludePathPatterns("/pay/wx/notify");
    }
}
