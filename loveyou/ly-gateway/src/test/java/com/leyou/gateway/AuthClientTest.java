package com.leyou.gateway;


import com.leyou.LyGatewayApplication;
import com.leyou.client.auth.AuthClient;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;

@RunWith(SpringJUnit4ClassRunner.class)
@SpringBootTest(classes = LyGatewayApplication.class)
public class AuthClientTest {

    @Autowired
    private AuthClient authClient;

    //测试
    @Test
    public void test() {
       String token =  authClient.authorization("item-service","item-service");
        System.out.println("token = " + token);
    }
}
