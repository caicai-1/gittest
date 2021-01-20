package com.leyou.item.controller;

import com.leyou.common.constants.LyConstants;
import com.leyou.common.pojo.PageResult;
import com.leyou.item.entity.Brand;
import com.leyou.item.service.BrandService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import javax.servlet.http.HttpServletRequest;
import java.util.Date;
import java.util.List;

@RestController
public class BrandController {

    @Autowired
    private BrandService brandService;

    /**
     * 分页查询品牌表
     * @param key  查询条件
     * @param page 当前页
     * @param rows 当前页大小
     * @param sortBy 排序字段
     * @param desc 是否降序
     * @return
     */
    @RequestMapping("brand/page")
    private ResponseEntity<PageResult<Brand>> page(
            @RequestParam(value = "key", required = false) String key,
            @RequestParam(value = "page", defaultValue = "1") Integer page,
            @RequestParam(value = "rows", defaultValue = "5") Integer rows,
            @RequestParam(value = "sortBy", required = false) String sortBy,
            @RequestParam(value = "desc", required = false) Boolean desc
    ) {
        PageResult<Brand> pageResult = brandService.brandsPageQuery(key, page, rows, sortBy, desc);
        return ResponseEntity.ok(pageResult);
    }

    /**
     * 保存品牌
     * @param brand     品牌信息
     * @param cids      对应所有分类的id
     * @return
     */
    @PostMapping("/brand")
    public ResponseEntity<Void> saveBrand(Brand brand, @RequestParam("cids") List<Long> cids) {
        brandService.saveBrand(brand, cids);
        //添加为create，状态码为201
        return ResponseEntity.status(HttpStatus.CREATED).build();
    }

    /**
     * 修改品牌
     * @param brand  修改后的品牌信息
     * @param cids   修改后对应的分类
     * @return
     */
    @PutMapping("/brand")
    public ResponseEntity<Void> updateBrand(Brand brand, @RequestParam("cids") List<Long> cids) {
//    public ResponseEntity<Void> updateBrand(@RequestParam("id")Long id,
//                                            @RequestParam("name")String name,
//                                            @RequestParam("image")String image,
//                                            @RequestParam("letter")String letter,
//                                            @RequestParam("cids")List<Long> cids){
//        brandService.updateBrand(id, name, image, letter, cids);
        brandService.updateBrand(brand, cids);
        //添加为create，状态码为201
        return ResponseEntity.status(HttpStatus.ACCEPTED).build();
    }

    /**
     * 删除品牌
     * @param bid   id品牌
     * @return
     */
    @GetMapping("/brand/delete")
    public ResponseEntity<Void> deleteBrandById(@RequestParam("bid") String bid) {

        brandService.deleteBrandById(Long.parseLong(bid));
        return ResponseEntity.status(HttpStatus.NO_CONTENT).build();
    }

    /**
     * 根据id查询品牌
     * @param id
     * @return
     */
    @GetMapping("/brand/{id}")
    public ResponseEntity<Brand> findById(@PathVariable("id") Long id) {
        Brand brand = brandService.findById(id);
        return ResponseEntity.ok(brand);
    }

    /**
     * 根据分类id查询对应的品牌
     * @param cid
     * @return
     */
    @GetMapping("/brand/of/category")
    public ResponseEntity<List<Brand>> findBrandsByCid(@RequestParam("id") Long cid) {
        List<Brand> brands = brandService.findBrandsByCid(cid);
        return ResponseEntity.ok(brands);
    }

    /**
     * 根据多个id查找多个品牌
     * @param ids
     * @return
     */
    @GetMapping("/brand/list")
    public ResponseEntity<List<Brand>> findBrandByIds(@RequestParam("ids")List<Long> ids, HttpServletRequest request){
        String header = request.getHeader(LyConstants.APP_TOKEN_HEADER);
        System.out.println("header = " + header);
        List<Brand> brands = brandService.findBrandByIds(ids);
        return ResponseEntity.ok(brands);
    }
}