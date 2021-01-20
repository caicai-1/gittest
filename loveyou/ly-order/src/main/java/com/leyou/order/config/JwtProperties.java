package com.leyou.order.config;

import com.leyou.common.auth.utils.RsaUtils;
import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;

import javax.annotation.PostConstruct;
import java.security.PublicKey;

@Data
@ConfigurationProperties(prefix = "ly.jwt")
@Component
public class JwtProperties {
    private String pubKeyPath;
    private PublicKey publicKey;
    private CookieInfo cookie = new CookieInfo();
    private AppPojo app = new AppPojo();
    @Data
    public class CookieInfo {
        private String cookieName;
    }

    @Data
    public class AppPojo {
        private String serviceName;
        private String secret;
    }
    /**
     * 公钥对象
     */
    @PostConstruct // 等价于init-method
    public void initMethod() throws Exception {
        publicKey = RsaUtils.getPublicKey(pubKeyPath);
    }
}
