package com.leyou.order.service;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.core.toolkit.Wrappers;
import com.leyou.client.item.ItemClient;
import com.leyou.client.user.UserClient;
import com.leyou.common.auth.pojo.UserHolder;
import com.leyou.common.auth.pojo.UserInfo;
import com.leyou.common.constants.LyConstants;
import com.leyou.common.exception.pojo.ExceptionEnum;
import com.leyou.common.exception.pojo.LyException;
import com.leyou.common.utils.BeanHelper;
import com.leyou.common.utils.IdWorker;
import com.leyou.item.entity.Sku;
import com.leyou.order.dto.CartDTO;
import com.leyou.order.dto.OrderDTO;
import com.leyou.order.dto.OrderVO;
import com.leyou.order.mapper.OrderDetailMapper;
import com.leyou.order.mapper.OrderLogisticsMapper;
import com.leyou.order.mapper.OrderMapper;
import com.leyou.order.pojo.Order;
import com.leyou.order.pojo.OrderDetail;
import com.leyou.order.pojo.OrderLogistics;
import com.leyou.order.pojo.OrderStatusEnum;
import com.leyou.order.utils.PayHelper;
import com.leyou.user.entity.AddressDTO;
import io.seata.spring.annotation.GlobalTransactional;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Date;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Service
@Transactional
@Slf4j
public class OrderService {

    @Autowired
    private OrderMapper orderMapper;
    @Autowired
    private OrderDetailMapper orderDetailMapper;
    @Autowired
    private OrderLogisticsMapper orderLogisticsMapper;
    @Autowired
    private IdWorker idWorker;
    @Autowired
    private ItemClient itemClient;
    @Autowired
    private UserClient userClient;
    @Autowired
    private StringRedisTemplate redisTemplate;
    @Autowired
    private PayHelper payHelper;

    //添加订单
    @GlobalTransactional
    public Long buildOrder(OrderDTO orderDTO) {

        UserInfo user = UserHolder.getUser();
        Long userId = user.getId();
        //1.添加订单表
        Order order = new Order();
        //1.1添加分布式id
        order.setOrderId(idWorker.nextId());
        //1.2添加总价格
        List<CartDTO> carts = orderDTO.getCarts();

        Map<Long, Integer> skuMap = carts.stream().collect(Collectors.toMap(CartDTO::getSkuId, CartDTO::getNum));
        List<Long> skuIds = carts.stream().map(CartDTO::getSkuId).collect(Collectors.toList());
        List<Sku> skuList = itemClient.findSkusBySkuIds(skuIds);
        //订单总金额
        long totalFee = skuList.stream().mapToLong(sku -> {
            return sku.getPrice() * skuMap.get(sku.getId());
        }).sum();
        order.setTotalFee(totalFee);
        //实付金额
        order.setActualFee(totalFee);
        //支付类型
        order.setPaymentType(orderDTO.getPaymentType());
        //邮费
        order.setPostFee(0L);
        //用户id
        order.setUserId(userId);
        //发票类型
        order.setInvoiceType(0);
        //订单来源
        order.setSourceType(2);
        //订单状态
        order.setStatus(OrderStatusEnum.INIT.value());
        //添加订单
        orderMapper.insert(order);

        //2.添加详情表
        skuList.forEach(sku -> {
            OrderDetail orderDetail = new OrderDetail();
            orderDetail.setId(idWorker.nextId());
            orderDetail.setOrderId(order.getOrderId());
            orderDetail.setSkuId(sku.getId());
            orderDetail.setNum(skuMap.get(sku.getId()));
            orderDetail.setTitle(sku.getTitle());
            orderDetail.setPrice(sku.getPrice());
            orderDetail.setOwnSpec(sku.getOwnSpec());
            orderDetail.setImage(sku.getImages());
            orderDetailMapper.insert(orderDetail);
        });

        //3.添加订单物流表
        AddressDTO addressDTO = userClient.findAddressByUserId(userId, orderDTO.getAddressId());
        OrderLogistics orderLogistics = BeanHelper.copyProperties(addressDTO, OrderLogistics.class);
        orderLogistics.setOrderId(order.getOrderId());
        orderLogistics.setLogisticsCompany("20202022");
        orderLogistics.setLogisticsCompany("顺丰公司");
        orderLogisticsMapper.insert(orderLogistics);

        //4.减掉货物库存
        itemClient.stockMinus(skuMap);
        //5.清空购物车

        return order.getOrderId();
    }

    //查询订单
    public OrderVO findOrderByOrderId(Long id) {
        try {
            Order order = orderMapper.selectById(id);
            OrderVO orderVo = BeanHelper.copyProperties(order, OrderVO.class);

            OrderDetail orderDetail = new OrderDetail();
            orderDetail.setOrderId(id);
            QueryWrapper<OrderDetail> wrapper = Wrappers.query(orderDetail);
            List<OrderDetail> orderDetails = orderDetailMapper.selectList(wrapper);
            orderVo.setDetailList(orderDetails);

            OrderLogistics orderLogistics = orderLogisticsMapper.selectById(order.getOrderId());
            orderVo.setLogistics(orderLogistics);

            return orderVo;
        } catch (Exception e) {
            e.printStackTrace();
            throw new LyException(ExceptionEnum.ORDER_NOT_FOUND);
        }
    }

    public String getPayUrl(Long orderId) {
        //合并成唯一的id键
        String key = orderId + LyConstants.PAY_URL_PRE;
        //查询redis中是否含有该url，有就直接返回redis中的值
        if (redisTemplate.hasKey(key)) {
            return redisTemplate.opsForValue().get(key);
        }
        //查询订单
        Order order = orderMapper.selectById(orderId);
        //发送消息给微信并返回
        String payUrl = payHelper.getPayUrl(orderId.toString(), order.getTotalFee().toString());

        redisTemplate.opsForValue().set(key, payUrl);
        return payUrl;
    }

    public void handlerWxNotify(Map<String, String> map) {
        //1.接收订单号 和 订单金额
        Long orderId = Long.valueOf(map.get("out_trade_no"));
        Long total_fee = Long.valueOf(map.get("total_fee"));
        //根据订单号查出订单
        Order order = orderMapper.selectById(orderId);
        //是否存在该订单
        if (order == null) {
            log.error("【微信支付回调】回调失败，订单不存在");
            throw new LyException(ExceptionEnum.ORDER_DETAIL_NOT_FOUND);
        }
        //订单总价是否一样
        if (order.getActualFee() != total_fee) {
            log.error("【微信支付回调】回调失败，订单金额不一样");
            throw new LyException(501, "订单金额不一致");
        }
        //订单是否已经支付
        if (order.getStatus() != OrderStatusEnum.INIT.value()) {
            log.error("【微信支付回调】回调失败，订单状态不正确！");
            throw new LyException(ExceptionEnum.INVALID_ORDER_STATUS);
        }
        //修改订单状态和支付时间
        order.setStatus(OrderStatusEnum.PAY_UP.value());
        order.setPayTime(new Date());
        //判断是否修改，写进日志
        int count = orderMapper.updateById(order);

        if (count > 0) {
            log.info("【微信支付回调】回调成功，订单更新成功！");
        } else {
            log.info("【微信支付回调】订单更新失败！");
        }
    }

    public Integer findOrderStatus(Long id) {

        Order order = orderMapper.selectById(id);
        if (order == null) {
            throw new LyException(ExceptionEnum.ORDER_NOT_FOUND);
        }
        return order.getStatus();
    }
}
