package com.leyou.search.entity;

import lombok.Data;
import org.springframework.data.annotation.Id;
import org.springframework.data.elasticsearch.annotations.Document;
import org.springframework.data.elasticsearch.annotations.Field;
import org.springframework.data.elasticsearch.annotations.FieldType;

import java.lang.annotation.Documented;
import java.security.Key;
import java.util.Map;
import java.util.Set;

@Data
@Document(indexName = "goods", type = "docs", shards = 1, replicas = 1)
public class Goods {

    @Id
    @Field(type = FieldType.Keyword)
    private Long id;
    @Field(type = FieldType.Keyword, index = false)
    private String subTitle;
    @Field(type = FieldType.Text, analyzer = "ik_max_word")
    private String spuName;
    @Field(type = FieldType.Keyword, index = false)
    private String skus;

    //搜索字段
    @Field(type = FieldType.Text, analyzer = "ik_max_word")
    private String all;  //所有需要被搜索的信息，
                        // 三个类名称+品牌名+spuName+subTitle+所有Sku的title
    private Long brandId;
    private Long categoryId;

    @Field(type = FieldType.Object)//类型选map中value的类型
    private Map<String, Object> specs;

    @Field(type = FieldType.Long)
    private Long createTime;//创建时间
    private Set<Long> price;//价格

}
