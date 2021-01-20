package com.leyou.item.dto;

import com.leyou.item.entity.Sku;
import com.leyou.item.entity.SpuDetail;
import lombok.Data;

import java.util.Date;
import java.util.List;

/**
 * 封装商品展示列表需要的数据
 */
@Data
public class SpuDTO{
    private Long id;
    private Long brandId;
    private Long cid1;// 1级类目
    private Long cid2;// 2级类目
    private Long cid3;// 3级类目
    private String name;// 商品名称
    private String subTitle;// 子标题
    private Boolean saleable;// 是否上架
    private Date createTime;// 创建时间
    private Date updateTime;// 最后修改时间

    private String brandName;//品牌名称
    private String categoryName;//分类名称

    private List<Sku> skus;//
    private SpuDetail spuDetail;//

}
