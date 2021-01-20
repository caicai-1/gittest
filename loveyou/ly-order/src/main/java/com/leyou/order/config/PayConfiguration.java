package com.leyou.order.config;

import com.github.wxpay.sdk.PayConfig;
import com.github.wxpay.sdk.WXPay;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class PayConfiguration {
    @Autowired
    private PayProperties payProperties;

    @Bean
    public WXPay getWXPay() throws Exception {
        PayConfig payConfig = new PayConfig();
        payConfig.setAppID(payProperties.getAppId());
        payConfig.setMchID(payProperties.getMchId());
        payConfig.setKey(payProperties.getKey());
        return new WXPay(payConfig);
    }

}
