package com.leyou.client.item;

import com.leyou.common.pojo.PageResult;
import com.leyou.item.dto.SpecGroupDTO;
import com.leyou.item.dto.SpuDTO;
import com.leyou.item.entity.*;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestParam;

import java.util.List;
import java.util.Map;

@FeignClient("item-service")
public interface ItemClient {

    @GetMapping("/spu/page")
    public PageResult<SpuDTO> spuPageQuery(
            @RequestParam(value = "page", defaultValue = "1") Integer page,
            @RequestParam(value = "rows", defaultValue = "5") Integer rows,
            @RequestParam(value = "key", required = false) String key,
            @RequestParam(value = "saleable", required = false) Boolean saleable
    );

    /**
     * 根据spuId查询sku
     *
     * @param id
     * @return
     */
    @GetMapping("/sku/of/spu")
    public List<Sku> findSkuBySpuId(@RequestParam("id") Long id);

    /**
     * 查找分组规格
     *
     * @param gid
     * @param cid
     * @param searching
     * @return
     */
    @GetMapping("/spec/params")
    public List<SpecParam> findParams(
            @RequestParam(value = "gid", required = false) Long gid,
            @RequestParam(value = "cid", required = false) Long cid,
            @RequestParam(value = "searching", required = false) Boolean searching
    );

    @GetMapping("/spu/detail")
    public SpuDetail findSpuDetailBySpuId(@RequestParam("id") Long id);

    /**
     * 根据多个id查找多个分类
     *
     * @param ids
     * @return
     */
    @GetMapping("/category/list")
    public List<Category> findCategoryByIds(@RequestParam("ids") List<Long> ids);

    /**
     * 根据多个id查找多个品牌
     *
     * @param ids
     * @return
     */
    @GetMapping("/brand/list")
    public List<Brand> findBrandByIds(@RequestParam("ids") List<Long> ids);

    /**
     * 根据spuId查询SpuDto
     *
     * @param spuId
     * @return
     */
    @GetMapping("/spu/{id}")
    public SpuDTO findSpuById(@PathVariable("id") Long spuId);

    /**
     * 根据id查询品牌
     *
     * @param id
     * @return
     */
    @GetMapping("/brand/{id}")
    public Brand findById(@PathVariable("id") Long id);

    /**
     * 根据cid查询SpecGroupDTO结果集
     *
     * @param cid
     * @return
     */
    @GetMapping("/spec/of/category")
    public List<SpecGroupDTO> findSpecsByCid(@RequestParam("id") Long cid);

    /**
     * 根据多个skuId查询sku集合
     */
    @GetMapping("/sku/list")
    public List<Sku> findSkusBySkuIds(@RequestParam("ids") List<Long> ids);
    /**
     * 减库存接口
     */
    @PutMapping("/stock/minus")
    public Void stockMinus(Map<Long, Integer> map);

}
