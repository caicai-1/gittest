package com.leyou.item.controller;

import com.leyou.item.entity.Category;
import com.leyou.item.service.CategoryService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
//@CrossOrigin
public class CategoryController {

    @Autowired
    private CategoryService categoryService;

    /**
     * 根据pid查询分类
     * @param pid
     * @return
     */
    @GetMapping("/category/of/parent")
    public ResponseEntity<List<Category>> findById(@RequestParam("pid") Long pid){
        List<Category> categories = categoryService.findCategoryByPid(pid);
        //查询使用ok  ok状态码为200
        return ResponseEntity.ok(categories);
    }

    /**
     * 根据品牌id查询分类
     * @param id
     * @return
     */
    @GetMapping("/category/of/brand")
    public ResponseEntity<List<Category>> selectCategoryByBrandId(@RequestParam("id")Long id){
        List<Category> list = categoryService.selectCategoryByBrandId(id);
        return ResponseEntity.ok(list);
    }

    /**
     * 根据多个id查找多个分类
     * @param ids
     * @return
     */
    @GetMapping("/category/list")
    public ResponseEntity<List<Category>> findCategoryByIds(@RequestParam("ids")List<Long> ids){
        List<Category> categories = categoryService.findCategoryByIds(ids);
        return ResponseEntity.ok(categories);
    }
}
