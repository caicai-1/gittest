package com.leyou.search.scheduled;

import com.leyou.client.auth.AuthClient;
import com.leyou.search.config.JwtProperties;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

@Component
@Slf4j
public class AppTokenScheduled {
    //定义token
    private String token;

    @Autowired
    private AuthClient authClient;

    @Autowired
    private JwtProperties jwtProperties;
    /**
     * token刷新间隔
     */
    private static final long TOKEN_REFRESH_INTERVAL = 86400000L; //24小时

    /**
     * token获取失败后重试的间隔
     */
    private static final long TOKEN_RETRY_INTERVAL = 10000L;

    /**
     * 需求：每隔24小时定时调用授权中心，获取应用token
     */
    @Scheduled(fixedRate = TOKEN_REFRESH_INTERVAL)
    public void autoGetToken(){

        while (true) {
            try {
                String token = authClient.authorization(jwtProperties.getApp().getServiceName(),
                        jwtProperties.getApp().getSecret());
                this.token = token;
                log.info("【服务鉴权】{ " + jwtProperties.getApp().getServiceName() + " }服务上线");
                break;
            } catch (Exception e) {
                e.printStackTrace();
                log.error("【服务鉴权】{获取token失败，请稍后再试!}");
                try {
                    Thread.sleep(TOKEN_RETRY_INTERVAL);
                } catch (InterruptedException e1) {
                    e1.printStackTrace();
                }
            }
        }
    }

    public String getToken() {
        return token;
    }
}
