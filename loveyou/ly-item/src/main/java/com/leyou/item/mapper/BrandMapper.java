package com.leyou.item.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.leyou.item.entity.Brand;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;

import java.util.List;

public interface BrandMapper extends BaseMapper<Brand> {
    void insertCategoryAndBrand(@Param("bid") Long bid, @Param("cids") List<Long> cids);

    void deleteCategoryAndBrandByBrandId(Long bid);

    @Select("SELECT b.* FROM tb_brand b, tb_category_brand cb WHERE b.id = cb.brand_id AND cb.category_id=#{cid}")
    List<Brand> findBrandsByCid(Long cid);
}
