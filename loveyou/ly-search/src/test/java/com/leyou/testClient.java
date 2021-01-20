package com.leyou;

import com.leyou.client.item.ItemClient;
import com.leyou.item.entity.Sku;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;

import java.util.List;

@RunWith(SpringJUnit4ClassRunner.class)
@SpringBootTest(classes = LySearchApplication.class)
public class testClient {

    @Autowired
    private ItemClient itemClient;

    @Test
    public void test() {
        List<Sku> skus = itemClient.findSkuBySpuId(2L);
        for (Sku sku : skus) {
            System.out.println("sku = " + sku);
        }
    }
}
