package com.leyou.sms.utils;

import com.aliyuncs.CommonRequest;
import com.aliyuncs.CommonResponse;
import com.aliyuncs.IAcsClient;
import com.aliyuncs.exceptions.ClientException;
import com.aliyuncs.exceptions.ServerException;
import com.aliyuncs.http.MethodType;
import com.leyou.common.utils.JsonUtils;
import com.leyou.sms.config.SmsConstants;
import com.leyou.sms.config.SmsProperties;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.util.Map;

/**
 * 短信工具类
 */
@Component
@Slf4j  // Lombok日志注解
public class SmsHelper {

    @Autowired
    private SmsProperties smsProps;
    @Autowired
    private IAcsClient client;


    /**
     * 发送短信工具方法
     */
    public void sendMsg(String phone,String code){
        CommonRequest request = new CommonRequest();
        request.setMethod(MethodType.POST);
        request.setDomain(smsProps.getDomain());
        request.setVersion(smsProps.getVersion());
        request.setAction(smsProps.getAction());
        request.putQueryParameter(SmsConstants.SMS_PARAM_REGION_ID, smsProps.getRegionID());
        request.putQueryParameter(SmsConstants.SMS_PARAM_KEY_PHONE, phone);
        request.putQueryParameter(SmsConstants.SMS_PARAM_KEY_SIGN_NAME, smsProps.getSignName());
        request.putQueryParameter(SmsConstants.SMS_PARAM_KEY_TEMPLATE_CODE, smsProps.getVerifyCodeTemplate());
        request.putQueryParameter(SmsConstants.SMS_PARAM_KEY_TEMPLATE_PARAM, "{\""+smsProps.getCode()+"\":\""+code+"\"}");
        try {
            CommonResponse response = client.getCommonResponse(request);

            //获取阿里云的结果
            String reuslt = response.getData();

            //把结果转换为json对象
            Map resultMap = JsonUtils.toMap(reuslt,String.class,String.class);

            //判断Code是否为OK
            if(resultMap.get(SmsConstants.SMS_RESPONSE_KEY_CODE).equals(SmsConstants.OK)){
                log.info("【阿里云短信服务】{短信发送成功}");
            }else{
                log.error("【阿里云短信服务】{短信发送失败},{原因}"+resultMap.get(SmsConstants.SMS_RESPONSE_KEY_MESSAGE));
            }

        } catch (ServerException e) {
            e.printStackTrace();
            log.error("【阿里云短信服务】{短信发送失败},{原因}"+e.getMessage());
        } catch (ClientException e) {
            e.printStackTrace();
            log.error("【阿里云短信服务】{短信发送失败},{原因}"+e.getMessage());
        }
    }
}
