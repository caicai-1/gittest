package com.leyou.cart.service;

import com.leyou.cart.pojo.Cart;
import com.leyou.common.auth.pojo.UserHolder;
import com.leyou.common.auth.pojo.UserInfo;
import com.leyou.common.constants.LyConstants;
import com.leyou.common.exception.pojo.ExceptionEnum;
import com.leyou.common.exception.pojo.LyException;
import com.leyou.common.utils.JsonUtils;
import org.apache.lucene.util.CollectionUtil;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.BoundHashOperations;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Service;
import org.springframework.util.CollectionUtils;

import java.util.Collections;
import java.util.List;
import java.util.stream.Collectors;

@Service
public class CartService {

    @Autowired
    private StringRedisTemplate redisTemplate;

    public void addCart(Cart cart) {
        //hash存储在redis中  格式:  map<key, map<key, value>>

        //从ThreadLocal中取出该user
        UserInfo userInfo = UserHolder.getUser();
        //制作键
        String cartKey = userInfo.getId().toString() + LyConstants.CART_PRE;
        //根据外边的key，得到里面的map
        BoundHashOperations<String, Object, Object> boundHashOperations = redisTemplate.boundHashOps(cartKey);
        //因为是StringRedisTemplate，所以只能使用String来增删改查
        String skuId = cart.getSkuId().toString();

        //如果redis中有该商品，就直接增加数量
        if (boundHashOperations.hasKey(skuId)){
            Object oldCartStr = boundHashOperations.get(skuId);
            Cart oldCartObj = JsonUtils.toBean(oldCartStr.toString(), Cart.class);
            cart.setNum(cart.getNum() + oldCartObj.getNum());
        }
        //没有直接添加该商品
        cart.setUserId(userInfo.getId());
        boundHashOperations.put(skuId,JsonUtils.toString(cart));

    }

    public List<Cart> findCarts() {
        //从ThreadLocal中取出该user
        UserInfo userInfo = UserHolder.getUser();
        //制作键
        String cartKey = userInfo.getId().toString() + LyConstants.CART_PRE;
        //根据外边的key，得到里面的map
        BoundHashOperations<String, Object, Object> boundHashOperations = redisTemplate.boundHashOps(cartKey);

        //获取值，里面obj为String的json
        List<Object> values = boundHashOperations.values();

        if (CollectionUtils.isEmpty(values)){
            return null;
        }
        //流收集
        List<Cart> carts = values.stream()
                                    .map(obj -> JsonUtils.toBean(obj.toString(), Cart.class))
                                    .collect(Collectors.toList());
        return carts;
    }

    public void updateCart(Cart cart) {
        //从ThreadLocal中取出该user
        UserInfo userInfo = UserHolder.getUser();
        //制作键
        String cartKey = userInfo.getId().toString() + LyConstants.CART_PRE;
        //根据外边的key，得到里面的map
        BoundHashOperations<String, Object, Object> boundHashOperations = redisTemplate.boundHashOps(cartKey);
        //把id转换成String
        String skuId = cart.getSkuId().toString();
        //获取cart对象
        Object oldCartStr = boundHashOperations.get(skuId);
        Cart oldCartObj = JsonUtils.toBean(oldCartStr.toString(), Cart.class);
        oldCartObj.setNum(cart.getNum());
        //写出redis
        boundHashOperations.put(skuId,JsonUtils.toString(cart));
    }

    public void deleteCart(Long skuId) {
        try {
            //从ThreadLocal中取出该user
            UserInfo userInfo = UserHolder.getUser();
            //制作键
            String cartKey = userInfo.getId().toString() + LyConstants.CART_PRE;
            //根据外边的key，得到里面的map
            BoundHashOperations<String, Object, Object> boundHashOperations = redisTemplate.boundHashOps(cartKey);

            boundHashOperations.delete(skuId.toString());
        } catch (Exception e) {
            e.printStackTrace();
            throw new LyException(ExceptionEnum.DELETE_OPERATION_FAIL);
        }
    }

    public void addCarts(List<Cart> carts) {
        carts.forEach(cart -> {
            this.addCart(cart);
        });
    }
}
