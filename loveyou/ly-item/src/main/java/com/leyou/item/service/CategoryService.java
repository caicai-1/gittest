package com.leyou.item.service;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.core.toolkit.CollectionUtils;
import com.baomidou.mybatisplus.core.toolkit.Wrappers;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.leyou.common.exception.pojo.ExceptionEnum;
import com.leyou.common.exception.pojo.LyException;
import com.leyou.item.entity.Category;

import com.leyou.item.mapper.CategoryMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@Transactional
public class CategoryService extends ServiceImpl<CategoryMapper, Category> {

    @Autowired
    private CategoryMapper categoryMapper;


    public List<Category> findCategoryByPid(Long pid) {

        Category category = new Category();

        category.setParentId(pid);
        //根据Category对象创建wrapper
        QueryWrapper<Category> wrapper = Wrappers.query(category);
        //数据库查询
        List<Category> list = categoryMapper.selectList(wrapper);

        if (CollectionUtils.isEmpty(list)){
            //商品分类找不到异常404
            throw new LyException(ExceptionEnum.CATEGORY_NOT_FOUND);
        }

        return list;
    }

    public List<Category> selectCategoryByBrandId(Long id) {
        try {

            List<Long> CategoryIds = categoryMapper.selectCategoryIdsByBrandId(id);
            List<Category> list = categoryMapper.selectBatchIds(CategoryIds);
            return list;
        } catch (Exception e) {
            e.printStackTrace();
            throw new LyException(ExceptionEnum.UPDATE_OPERATION_FAIL);
        }
    }

    public List<Category> findCategoryByIds(List<Long> ids) {
        List<Category> categories = categoryMapper.selectBatchIds(ids);
        return categories;
    }
}
