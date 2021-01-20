package com.leyou.item.controller;

import com.leyou.item.dto.SpecGroupDTO;
import com.leyou.item.entity.Sku;
import com.leyou.item.entity.SpecGroup;
import com.leyou.item.entity.SpecParam;
import com.leyou.item.service.SpecService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
public class SpecController {

    @Autowired
    private SpecService specService;

    /**
     * 根据分类id找分组
     * @param id
     * @return
     */
    @GetMapping("/spec/groups/of/category")
    public ResponseEntity<List<SpecGroup>> findGroupByCid(@RequestParam("id") Long id){
        List<SpecGroup> specGroups = specService.findGroupByCid(id);
        return ResponseEntity.ok(specGroups);
    }

    /**
     * 根据组id(gid)、分类id(cid)、查询条件(searching) 查分组属性
     * @param gid  分组id
     * @param cid  分类id
     * @param searching  是否查询
     * @return
     */
    @GetMapping("/spec/params")
    public ResponseEntity<List<SpecParam>> findParams(
            @RequestParam(value = "gid", required = false) Long gid,
            @RequestParam(value = "cid", required = false) Long cid,
            @RequestParam(value = "searching", required = false) Boolean searching
    ){
        List<SpecParam> specParams = specService.findParams(gid, cid, searching);
        return ResponseEntity.ok(specParams);
    }

    /**
     * 根据cid查询SpecGroupDTO结果集
     * @param cid
     * @return
     */
    @GetMapping("/spec/of/category")
    public ResponseEntity<List<SpecGroupDTO>> findSpecsByCid(@RequestParam("id")Long cid){
        List<SpecGroupDTO> specGroupDTOS = specService.findSpecsByCid(cid);
        return ResponseEntity.ok(specGroupDTOS);
    }

}
