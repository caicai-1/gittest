package com.leyou.item.controller;

import com.leyou.common.pojo.PageResult;
import com.leyou.item.dto.SpuDTO;
import com.leyou.item.entity.Sku;
import com.leyou.item.entity.SpuDetail;
import com.leyou.item.service.GoodsService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

@RestController
public class GoodsController {

    @Autowired
    private GoodsService goodsService;

    /**
     * 查询商品(包括spu,spu_detail,sku)
     * @param page
     * @param rows
     * @param key
     * @param saleable
     * @return
     */
    @GetMapping("/spu/page")
    public ResponseEntity<PageResult<SpuDTO>> spuPageQuery(
            @RequestParam(value = "page", defaultValue = "1")Integer page,
            @RequestParam(value = "rows", defaultValue = "5")Integer rows,
            @RequestParam(value = "key", required = false)String key,
            @RequestParam(value = "saleable", required = false)Boolean saleable
    ){
        PageResult<SpuDTO> pageResult = goodsService.spuPageQuery(page, rows, key, saleable);
        return ResponseEntity.ok(pageResult);
    }

    /**
     * 增加商品
     * @param spuDTO
     * @return
     */
    @PostMapping("/goods")
    public ResponseEntity<Void> saveGoods(@RequestBody SpuDTO spuDTO){
        goodsService.saveGoods(spuDTO);
        return ResponseEntity.status(HttpStatus.CREATED).build();
    }

    /**
     * 商品上下架
     * @param id  spuId
     * @param saleable  是否上架
     * @return
     */
    @PutMapping("/spu/saleable")
    public ResponseEntity<Void> updateSaleable(
            @RequestParam("id") Long id,
            @RequestParam("saleable") Boolean saleable
    ){
        goodsService.updateSaleable(id, saleable);
        return ResponseEntity.status(HttpStatus.NON_AUTHORITATIVE_INFORMATION).build();
    }

    @GetMapping("/sku/of/spu")
    public ResponseEntity<List<Sku>> findSkuBySpuId(@RequestParam("id")Long id){
        List<Sku> skus = goodsService.findSkuBySpuId(id);
        return ResponseEntity.ok(skus);
    }

    @GetMapping("/spu/detail")
    public ResponseEntity<SpuDetail> findSpuDetailBySpuId(@RequestParam("id")Long id){
        SpuDetail spuDetail = goodsService.findSpuDetailBySpuId(id);
        return ResponseEntity.ok(spuDetail);
    }

    @GetMapping("/spu/{id}")
    public ResponseEntity<SpuDTO> findSpuById(@PathVariable("id")Long spuId){
        SpuDTO spuDTO = goodsService.findSpuById(spuId);
        return ResponseEntity.ok(spuDTO);
    }

    /**
     * 根据多个skuId查询sku集合
     */
    @GetMapping("/sku/list")
    public ResponseEntity<List<Sku>> findSkusBySkuIds(@RequestParam("ids")List<Long> ids){
        List<Sku> skus = goodsService.findSkusBySkuIds(ids);
        return ResponseEntity.ok(skus);
    }

    /**
     * 减库存接口
     */
    @PutMapping("/stock/minus")
    public ResponseEntity<Void> stockMinus(@RequestBody Map<Long, Integer> map){
        goodsService.stockMinus(map);
        return ResponseEntity.status(HttpStatus.NO_CONTENT).build();
    }

}
