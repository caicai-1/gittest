package com.leyou.common.constants;

public class LyConstants {

    /*本地上传图片的上传路径*/
    public static final String IMAGE_PATH = "D:\\Develop\\nginx-1.16.0\\html\\brand-logo";

    /*本地图片服务器的访问地址*/
    public static final String IMAGE_URL = "http://localhost/brand-logo/";

    /*注册时短信验证码在redis中的key的前缀*/
    public static final String REDIS_KEY_PRE = "REDIS_KEY_PRE";

    /*服务认证请求头*/
    public static final String APP_TOKEN_HEADER = "APP_TOKEN_HEADER";

    /*用户购物车对象在redis中的key的前缀*/
    public static final String CART_PRE = "CART_PRE";

    /*支付链接在redis中存储的key的前缀*/
    public static final String PAY_URL_PRE = "PAY_URL_PRE";
}