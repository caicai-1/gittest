package com.leyou.cart.controller;

import com.leyou.cart.pojo.Cart;
import com.leyou.cart.service.CartService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
public class CartController {

    @Autowired
    private CartService cartService;

    /**
     * 购物车添加商品
     * @param cart
     * @return
     */
    @PostMapping
    public ResponseEntity<Void> addCart(@RequestBody Cart cart){
        cartService.addCart(cart);
        return ResponseEntity.status(HttpStatus.NO_CONTENT).build();
    }

    /**
     * 查询redis中的购物车
     * @return
     */
    @GetMapping("/list")
    public ResponseEntity<List<Cart>> findCarts(){
        List<Cart> carts = cartService.findCarts();
        return ResponseEntity.ok(carts);
    }

    /**
     * 修改购物车中的商品数量
     */
    @PutMapping
    public ResponseEntity<Void> updateCart(@RequestBody Cart cart){
        cartService.updateCart(cart);
        return ResponseEntity.status(HttpStatus.NO_CONTENT).build();
    }

    /**
     * 删除购物车中的商品
     */
    @DeleteMapping
    public ResponseEntity<Void> deleteCart(@RequestParam("skuId") Long skuId){
        cartService.deleteCart(skuId);
        return ResponseEntity.status(HttpStatus.NO_CONTENT).build();
    }

    /**
     * 批量添加
     */
    @PostMapping("/list")
    public ResponseEntity<Void> addCarts(@RequestBody List<Cart> carts){
        cartService.addCarts(carts);
        return ResponseEntity.status(HttpStatus.NO_CONTENT).build();
    }
}
