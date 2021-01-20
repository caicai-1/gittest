package com.leyou.item.service;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.core.toolkit.CollectionUtils;
import com.baomidou.mybatisplus.core.toolkit.StringUtils;
import com.baomidou.mybatisplus.core.toolkit.Wrappers;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.leyou.common.exception.pojo.ExceptionEnum;
import com.leyou.common.exception.pojo.LyException;
import com.leyou.common.pojo.PageResult;
import com.leyou.item.entity.Brand;
import com.leyou.item.mapper.BrandMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Date;
import java.util.List;

@Service
@Transactional
public class BrandService extends ServiceImpl<BrandMapper, Brand> {

    @Autowired
    private BrandMapper brandMapper;

    public PageResult<Brand> brandsPageQuery(String key, Integer page, Integer rows, String sortBy, Boolean desc) {
        try {
            //复杂查询，分页查询要提供IPage和QueryWrapper
            //page为IPage接口的实现类
            IPage iPage = new Page(page, rows);
            //复杂查询不带参数
            QueryWrapper<Brand> wrapper = Wrappers.query();
            //判断关键词是否为空
            if (StringUtils.isNotEmpty(key)){
                //根据数据库的《字段名》来模糊查询和精确查询
                wrapper.like("name", key).or().eq("letter", key.toUpperCase());
            }
            //判断是否有排序的字段
            if (StringUtils.isNotEmpty(sortBy)){
                //前端传来的desc判断是否为降序
                if (desc){
                    wrapper.orderByDesc(sortBy);
                }else {
                    wrapper.orderByAsc(sortBy);
                }
            }
            //封装到iPage中
            iPage = brandMapper.selectPage(iPage, wrapper);
            //封装PageResult中
            PageResult<Brand> pageResult = new PageResult<Brand>(iPage.getTotal(), iPage.getPages(), iPage.getRecords());
            return pageResult;
        } catch (Exception e) {
            e.printStackTrace();
            //抛出品牌异常
            throw new LyException(ExceptionEnum.BRAND_NOT_FOUND);
        }
    }

    public void saveBrand(Brand brand, List<Long> cids) {
        try {
            //保存品牌表
            brandMapper.insert(brand);

            //保存分类品牌中间表
            brandMapper.insertCategoryAndBrand(brand.getId(), cids);
        } catch (Exception e) {
            e.printStackTrace();
            throw new LyException(ExceptionEnum.INSERT_OPERATION_FAIL);
        }
    }

    public void deleteBrandById(Long bid) {
        try {
            //根据id删除品牌表
            brandMapper.deleteById(bid);
            //根据brand_id删除商品品牌表
            brandMapper.deleteCategoryAndBrandByBrandId(bid);
        } catch (Exception e) {
            e.printStackTrace();
            throw new LyException(ExceptionEnum.DELETE_OPERATION_FAIL);
        }
    }

//    public void updateBrand(Long id, String name, String image,String letter, List<Long> cids) {
    public void updateBrand(Brand brand, List<Long> cids) {
        try {
//            //修改品牌表
//            Brand brand = new Brand();
//            brand.setId(id);
//            brand.setName(name);
//            brand.setImage(image);
//            brand.setLetter(letter);
            brand.setUpdateTime(new Date());
            brandMapper.updateById(brand);

            //修改商品品牌中间表
            brandMapper.deleteCategoryAndBrandByBrandId(brand.getId());
            brandMapper.insertCategoryAndBrand(brand.getId(), cids);
        } catch (Exception e) {
            e.printStackTrace();
            throw new LyException(ExceptionEnum.UPDATE_OPERATION_FAIL);
        }
    }

    public Brand findById(Long id) {
        try {
            return brandMapper.selectById(id);
        } catch (Exception e) {
            e.printStackTrace();
            throw new LyException(ExceptionEnum.BRAND_NOT_FOUND);
        }
    }

    public List<Brand> findBrandsByCid(Long cid) {
        return brandMapper.findBrandsByCid(cid);
    }

    public List<Brand> findBrandByIds(List<Long> ids) {
        List<Brand> brands = brandMapper.selectBatchIds(ids);
        if (CollectionUtils.isEmpty(brands)){
            throw new LyException(ExceptionEnum.BRAND_NOT_FOUND);
        }
        return brands;
    }
}
