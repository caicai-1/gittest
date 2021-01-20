package com.leyou.auth.config;


import com.leyou.common.auth.utils.RsaUtils;
import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;

import javax.annotation.PostConstruct;
import java.security.PrivateKey;
import java.security.PublicKey;

@ConfigurationProperties(prefix = "ly.jwt")
@Component
@Data
public class JwtProperties {

    private String pubKeyPath;

    private String priKeyPath;

    private CookiePojo cookie = new CookiePojo();

    private AppPojo app = new AppPojo();
    @Data
    public class CookiePojo{
        private Integer expire;
        private Integer refreshTime;
        private String cookieName;
        private String cookieDomain;
    }

    @Data
    public class AppPojo{
        private Integer expire;
    }
    //定义公钥和私有对象
    private PublicKey publicKey;
    private PrivateKey privateKey;

    /**
     * 初始化方法，在IOC创建和DI依赖注入以后的方法
     */
    @PostConstruct
    public void initMethod() throws Exception {
        privateKey = RsaUtils.getPrivateKey(priKeyPath);
        publicKey = RsaUtils.getPublicKey(pubKeyPath);
    }


}
