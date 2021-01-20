package com.leyou.item.service;

import com.baomidou.mybatisplus.core.conditions.Wrapper;
import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.core.toolkit.Wrappers;
import com.leyou.common.utils.BeanHelper;
import com.leyou.item.dto.SpecGroupDTO;
import com.leyou.item.entity.Sku;
import com.leyou.item.entity.SpecGroup;
import com.leyou.item.entity.SpecParam;
import com.leyou.item.mapper.SpecGroupMapper;
import com.leyou.item.mapper.SpecParamMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@Transactional
public class SpecService {

    @Autowired
    private SpecGroupMapper specGroupMapper;

    @Autowired
    private SpecParamMapper specParamMapper;

    public List<SpecGroup> findGroupByCid(Long cid) {
        SpecGroup specGroup = new SpecGroup();
        specGroup.setCid(cid);
        QueryWrapper<SpecGroup> wrapper = Wrappers.query(specGroup);
        List<SpecGroup> specGroups = specGroupMapper.selectList(wrapper);
        return specGroups;
    }

    public List<SpecParam> findParams(Long gid, Long cid, Boolean searching) {
        SpecParam specParam = new SpecParam();
        specParam.setGroupId(gid);
        specParam.setCid(cid);
        specParam.setSearching(searching);

        QueryWrapper<SpecParam> wrapper = Wrappers.query(specParam);

        List<SpecParam> specParams = specParamMapper.selectList(wrapper);
        return specParams;
    }


    public List<SpecGroupDTO> findSpecsByCid(Long cid) {
        List<SpecGroup> specGroups = findGroupByCid(cid);
        List<SpecGroupDTO> specGroupDTOS = BeanHelper.copyWithCollection(specGroups, SpecGroupDTO.class);

        specGroupDTOS.forEach(specGroupDTO -> {
            List<SpecParam> params = findParams(specGroupDTO.getId(), null, null);
            specGroupDTO.setParams(params);
        });
        return specGroupDTOS;
    }
}
