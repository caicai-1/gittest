package com.leyou.sms.config;

import com.aliyuncs.DefaultAcsClient;
import com.aliyuncs.IAcsClient;
import com.aliyuncs.profile.DefaultProfile;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

/**
 * 短信配置类
 */
@Configuration
public class SmsConfiguration {
    
    @Bean
    public IAcsClient createClient(SmsProperties smsProps){
        DefaultProfile profile = DefaultProfile.getProfile(smsProps.getRegionID(), smsProps.getAccessKeyID(), smsProps.getAccessKeySecret());
        return new DefaultAcsClient(profile);
    }

}
