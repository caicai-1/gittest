package com.leyou.item.service;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.core.toolkit.CollectionUtils;
import com.baomidou.mybatisplus.core.toolkit.StringUtils;
import com.baomidou.mybatisplus.core.toolkit.Wrappers;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.leyou.common.constants.MQConstants;
import com.leyou.common.exception.pojo.ExceptionEnum;
import com.leyou.common.exception.pojo.LyException;
import com.leyou.common.pojo.PageResult;
import com.leyou.common.utils.BeanHelper;
import com.leyou.item.dto.SpuDTO;
import com.leyou.item.entity.*;
import com.leyou.item.mapper.SkuMapper;
import com.leyou.item.mapper.SpuDetailMapper;
import com.leyou.item.mapper.SpuMapper;
import org.springframework.amqp.core.AmqpTemplate;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Arrays;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Service
@Transactional
public class GoodsService extends ServiceImpl<SkuMapper, Sku> {

    @Autowired
    private SpuMapper spuMapper;

    @Autowired
    private SpuDetailMapper spuDetailMapper;

    @Autowired
    private SkuMapper skuMapper;

    @Autowired
    private BrandService brandService;

    @Autowired
    private CategoryService categoryService;

    @Autowired
    private AmqpTemplate amqpTemplate;

    public PageResult<SpuDTO> spuPageQuery(Integer page, Integer rows, String key, Boolean saleable) {
        IPage<Spu> iPage = new Page(page, rows);
        QueryWrapper<Spu> wrapper = Wrappers.query();
        if (StringUtils.isNotEmpty(key)){
            wrapper.and(i -> i.like("name", key).or().like("sub_title", key));
        }
        if (saleable != null){
            wrapper.eq("saleable", saleable);
        }
        iPage = spuMapper.selectPage(iPage, wrapper);

        List<Spu> spus = iPage.getRecords();
        List<SpuDTO> spuDTOS = BeanHelper.copyWithCollection(spus, SpuDTO.class);
        //封装spuDtos中的分类名称categoryName 和 品牌brandName
        getCategoryNameAndBrandName(spuDTOS);
        PageResult<SpuDTO> pageResult = new PageResult<>(iPage.getTotal(), iPage.getPages(), spuDTOS);
        return pageResult;
    }

    //封装封装spuDtos中的分类名称categoryName 和 品牌brandName
    private void getCategoryNameAndBrandName(List<SpuDTO> spuDTOS) {
        spuDTOS.forEach(spuDTO -> {
            //
            Brand brand = brandService.findById(spuDTO.getBrandId());
            spuDTO.setBrandName(brand.getName());
            //利用stream的方法收集集合里的属性拼接
            List<Category> categories = categoryService.findCategoryByIds(Arrays.asList(spuDTO.getCid1(), spuDTO.getCid2(), spuDTO.getCid3()));
            String categotryNames = categories.stream().map(Category::getName).collect(Collectors.joining("/"));
            spuDTO.setCategoryName(categotryNames);
        });
    }

    public void saveGoods(SpuDTO spuDTO) {
        try {
            //添加spu表
            //复制类的同名属性
            Spu spu = BeanHelper.copyProperties(spuDTO, Spu.class);
            spuMapper.insert(spu);

            //添加spuDetail表
            SpuDetail spuDetail = spuDTO.getSpuDetail();
            //spu保存后同时返回id，设置spudetail表的id
            spuDetail.setSpuId(spu.getId());
            spuDetailMapper.insert(spuDetail);

            //添加sku表
            List<Sku> skus = spuDTO.getSkus();
            skus.forEach(sku -> {
                sku.setSpuId(spu.getId());
            });
            //ServiceImpl中的方法，同时保存多个sku
            saveBatch(skus);
        } catch (Exception e) {
            e.printStackTrace();
            throw new LyException(ExceptionEnum.INSERT_OPERATION_FAIL);
        }
    }

    //商品上下架
    public void updateSaleable(Long id, Boolean saleable) {
        try {
            Spu spu = new Spu();
            spu.setId(id);
            spu.setSaleable(saleable);
            spuMapper.updateById(spu);

            //根据上下架发送不同的routingKey
            String routingKey = saleable ? MQConstants.RoutingKey.ITEM_UP_KEY:MQConstants.RoutingKey.ITEM_DOWN_KEY;
            //发送消息队列操作nginx操作静态页和操作索引库
            amqpTemplate.convertAndSend(MQConstants.Exchange.ITEM_EXCHANGE_NAME, routingKey, id);
        } catch (Exception e) {
            e.printStackTrace();
            throw new LyException(ExceptionEnum.UPDATE_OPERATION_FAIL);
        }
    }

    public List<Sku> findSkuBySpuId(Long id) {
        try {
            Sku sku = new Sku();
            sku.setSpuId(id);
            QueryWrapper<Sku> wrapper = Wrappers.query(sku);
            List<Sku> skus = skuMapper.selectList(wrapper);
            return skus;
        } catch (Exception e) {
            e.printStackTrace();
            throw new LyException(ExceptionEnum.GOODS_NOT_FOUND);
        }
    }

    public SpuDetail findSpuDetailBySpuId(Long id) {
        SpuDetail spuDetail = spuDetailMapper.selectById(id);
        if (spuDetail == null){
            throw new LyException(ExceptionEnum.GOODS_NOT_FOUND);
        }
        return spuDetail;

    }

    public SpuDTO findSpuById(Long spuId) {
        try {
            Spu spu = spuMapper.selectById(spuId);
            SpuDTO spuDTO = BeanHelper.copyProperties(spu, SpuDTO.class);

            SpuDetail spuDetail = findSpuDetailBySpuId(spuId);
            spuDTO.setSpuDetail(spuDetail);

            List<Sku> skus = findSkuBySpuId(spuId);
            spuDTO.setSkus(skus);

            Brand brand = brandService.findById(spuDTO.getBrandId());
            spuDTO.setBrandName(brand.getName());

            List<Category> categories = categoryService.findCategoryByIds(Arrays.asList(spuDTO.getCid1(), spuDTO.getCid2(), spuDTO.getCid3()));
            String categoryName = categories.stream().map(Category::getName).collect(Collectors.joining("/"));
            spuDTO.setCategoryName(categoryName);

            return spuDTO;
        } catch (Exception e) {
            e.printStackTrace();
            throw new LyException(ExceptionEnum.GOODS_NOT_FOUND);
        }
    }

    public List<Sku> findSkusBySkuIds(List<Long> ids) {
        List<Sku> skus = skuMapper.selectBatchIds(ids);
        if (CollectionUtils.isEmpty(skus)){
            throw new LyException(ExceptionEnum.GOODS_NOT_FOUND);
        }
        return skus;
    }

    public void stockMinus(Map<Long, Integer> map) {
        map.entrySet().forEach(entry->{
            Long skuId = entry.getKey();
            Integer num = entry.getValue();
            Sku dbSku = skuMapper.selectById(skuId);

            Sku sku = new Sku();
            sku.setId(skuId);
            sku.setStock(dbSku.getStock() - num);
            skuMapper.updateById(sku);
        });
    }
}
