package com.leyou.order.utils;

import com.github.wxpay.sdk.WXPay;
import com.leyou.common.exception.pojo.ExceptionEnum;
import com.leyou.common.exception.pojo.LyException;
import com.leyou.order.config.PayProperties;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.util.HashMap;
import java.util.Map;

@Component
@Slf4j
public class PayHelper {

    @Autowired
    private WXPay wxPay;
    @Autowired
    private PayProperties payProperties;

    public String getPayUrl(String orderId, String total_fee){
        // 请求参数：
        Map<String, String> data = new HashMap<String, String>();
        data.put("body", "乐优商城");
        data.put("out_trade_no", orderId);
        data.put("total_fee", total_fee);
        data.put("spbill_create_ip", "123.12.12.123");
        data.put("notify_url", payProperties.getNotifyUrl());
        data.put("trade_type", payProperties.getPayType());  // 此处指定为扫码支付

        try {
            Map<String, String> resp = wxPay.unifiedOrder(data);

            log.info("【微信支付统一下单】获取二维码成功");
            return resp.get("code_url"); //code_url 二维码地址
        } catch (Exception e) {
            e.printStackTrace();
            log.error("【微信支付统一下单】获取二维码失败:" + e.getMessage());
            throw new LyException(501,"获取二维码支付链接失败");
        }
    }
}
