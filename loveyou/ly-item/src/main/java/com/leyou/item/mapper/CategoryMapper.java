package com.leyou.item.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.leyou.item.entity.Category;
import org.apache.ibatis.annotations.Select;

import java.util.List;

public interface CategoryMapper extends BaseMapper<Category> {

    @Select("SELECT category_id FROM `tb_category_brand` WHERE brand_id=#{bid}")
    List<Long> selectCategoryIdsByBrandId(Long bid);
}
