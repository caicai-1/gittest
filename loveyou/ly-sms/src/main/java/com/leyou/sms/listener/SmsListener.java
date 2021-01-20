package com.leyou.sms.listener;

import com.leyou.common.constants.MQConstants;
import com.leyou.sms.utils.SmsHelper;
import org.springframework.amqp.core.ExchangeTypes;
import org.springframework.amqp.rabbit.annotation.Exchange;
import org.springframework.amqp.rabbit.annotation.Queue;
import org.springframework.amqp.rabbit.annotation.QueueBinding;
import org.springframework.amqp.rabbit.annotation.RabbitListener;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.util.Map;

@Component
public class SmsListener {

    @Autowired
    private SmsHelper smsHelper;

    @RabbitListener(bindings = @QueueBinding(
            value = @Queue(name = MQConstants.Queue.SMS_VERIFY_CODE_QUEUE),
            exchange = @Exchange(name = MQConstants.Exchange.SMS_EXCHANGE_NAME, type = ExchangeTypes.TOPIC),
            key = MQConstants.RoutingKey.VERIFY_CODE_KEY
    ))
    public void sendCheckCode(Map<String, Object> msgMap){
        //1.接收短信的内容
        String phone = (String) msgMap.get("phone");
        String code = (String) msgMap.get("code");

        //2.调用短信工具类发送信息
        smsHelper.sendMsg(phone, code);
    }

}
