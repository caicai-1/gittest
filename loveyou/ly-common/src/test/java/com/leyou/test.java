package com.leyou;

import com.leyou.common.auth.utils.RsaUtils;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;

import java.security.PrivateKey;
import java.security.PublicKey;

public class test {

    private String pubPath = "D:\\Develop\\rsa-key\\rsa-key.pub"; //公钥路径

    private String priPath = "D:\\Develop\\rsa-key\\rsa-key"; //私钥路径

    @Test
    public void test() throws Exception {
        //参数一：给公钥指定路径和名称，参数二：给私钥指定路径和名称，参数三：盐，参数四：密钥大小
        RsaUtils.generateKey(pubPath, priPath, "heima", 2048);
    }

    @Test
    public void getKey() throws Exception {
        PublicKey publicKey = RsaUtils.getPublicKey(pubPath);
        System.out.println(publicKey);
        PrivateKey privateKey = RsaUtils.getPrivateKey(priPath);
        System.out.println(privateKey);
    }
}
