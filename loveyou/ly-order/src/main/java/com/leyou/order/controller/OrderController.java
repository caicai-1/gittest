package com.leyou.order.controller;

import com.leyou.order.dto.OrderDTO;
import com.leyou.order.dto.OrderVO;
import com.leyou.order.service.OrderService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.scheduling.support.SimpleTriggerContext;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.Map;

@RestController
public class OrderController {

    @Autowired
    private OrderService orderService;

    /**
     * 生成订单
     * @param orderDTO
     * @return
     */
    @PostMapping("/order")
    public ResponseEntity<Long> buildOrder(@RequestBody OrderDTO orderDTO){
        Long orderId = orderService.buildOrder(orderDTO);
        return ResponseEntity.ok(orderId);
    }

    /**
     * 查询订单
     */
    @GetMapping("/order/{id}")
    public ResponseEntity<OrderVO> findOrderByOrderId(@PathVariable("id")Long id){
        OrderVO orderVO = orderService.findOrderByOrderId(id);
        return ResponseEntity.ok(orderVO);
    }

    /**
     * 获取支付链接
     */
    @GetMapping("/order/url/{id}")
    public ResponseEntity<String> getPayUrl(@PathVariable("id") Long id){
        String payUrl = orderService.getPayUrl(id);
        return ResponseEntity.ok(payUrl);
    }

    /**
     * 微信支付后，微信服务端调用服务接口
     */
    @PostMapping(value = "/wx/notify", produces = "application/xml")
    public Map<String, String> handlerWxNotify(@RequestBody Map<String, String> map){
        orderService.handlerWxNotify(map);
        Map<String, String> wxMap = new HashMap<>();
        wxMap.put("return_code", "SUCCESS");
        wxMap.put("return_msg", "OK");
        return wxMap;
    }

    /**
     * 查询订单支付状态
     */
    @GetMapping("order/state/{id}")
    public ResponseEntity<Integer> findOrderStatus(@PathVariable("id") Long id){
        Integer status = orderService.findOrderStatus(id);
        return ResponseEntity.ok(status);
    }
}
