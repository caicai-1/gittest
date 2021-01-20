package com.leyou.search.service;

import com.alibaba.nacos.client.utils.StringUtils;
import com.baomidou.mybatisplus.core.toolkit.CollectionUtils;
import com.fasterxml.jackson.core.type.TypeReference;
import com.leyou.client.item.ItemClient;
import com.leyou.common.pojo.PageResult;
import com.leyou.common.utils.BeanHelper;
import com.leyou.common.utils.HighlightUtils;
import com.leyou.common.utils.JsonUtils;
import com.leyou.item.dto.SpuDTO;
import com.leyou.item.entity.*;
import com.leyou.search.dto.GoodsDTO;
import com.leyou.search.dto.SearchRequest;
import com.leyou.search.dto.SearchResult;
import com.leyou.search.entity.Goods;
import com.leyou.search.repository.SearchRepository;
import org.elasticsearch.index.query.BoolQueryBuilder;
import org.elasticsearch.index.query.QueryBuilders;
import org.elasticsearch.search.aggregations.Aggregation;
import org.elasticsearch.search.aggregations.AggregationBuilders;
import org.elasticsearch.search.aggregations.Aggregations;
import org.elasticsearch.search.aggregations.bucket.terms.Terms;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.elasticsearch.core.ElasticsearchTemplate;
import org.springframework.data.elasticsearch.core.aggregation.AggregatedPage;
import org.springframework.data.elasticsearch.core.query.FetchSourceFilter;
import org.springframework.data.elasticsearch.core.query.NativeSearchQueryBuilder;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;

import java.util.*;
import java.util.stream.Collectors;

@Service
public class SearchService {
    @Autowired
    private SearchRepository searchRepository;

    @Autowired
    private ElasticsearchTemplate elasticsearchTemplate;

    @Autowired
    private ItemClient itemClient;
    private NativeSearchQueryBuilder nativeSearchQueryBuilder;

    //写入索引库
    public void importData(){
        int page = 1;
        int rows = 100;
        Long totalPage = 0L;

        do {
            PageResult<SpuDTO> pageResult = itemClient.spuPageQuery(page, rows, null, true);
            List<SpuDTO> spuDTOS = pageResult.getItems();
            List<Goods> goodsList = spuDTOS.stream().map(this::buildGoods).collect(Collectors.toList());
            searchRepository.saveAll(goodsList);

            page++;
            totalPage=pageResult.getTotalPage();
        } while (page <= totalPage);
    }

    //spuDTO转成goods
    public Goods buildGoods(SpuDTO spuDTO){
        Goods goods = new Goods();
        goods.setId(spuDTO.getId());
        goods.setSubTitle(spuDTO.getSubTitle());
        goods.setSpuName(spuDTO.getName());
        goods.setBrandId(spuDTO.getBrandId());
        goods.setCategoryId(spuDTO.getCid3());
        List<Sku> skus = itemClient.findSkuBySpuId(spuDTO.getId());
        List<Map<String, Object>> skuMapList = new ArrayList<>();
        skus.forEach(sku -> {
            //只需要部分数据直接用map封装，提高查询效率，与PageResult一样
            Map<String, Object> map = new HashMap<>();
            map.put("id", sku.getId());
            map.put("title", sku.getTitle());
            map.put("image", sku.getImages());
            map.put("price", sku.getPrice());
            skuMapList.add(map);
        });
        String jsonSkus = JsonUtils.toString(skuMapList);
        goods.setSkus(jsonSkus);
        //查找索引 = 分类名称+品牌名+副标题+所有sku的title
        String All = spuDTO.getCategoryName().replaceAll("/", "")+
                     spuDTO.getBrandName()+spuDTO.getSubTitle()+
                        skus.stream().map(Sku::getTitle).collect(Collectors.joining());
        goods.setAll(All);
        goods.setCreateTime(new Date().getTime());
        //获取所有sku的价格
        Set<Long> prices = skus.stream().map(Sku::getPrice).collect(Collectors.toSet());
        goods.setPrice(prices);
        //根据分类id查出对应的属性
        List<SpecParam> params = itemClient.findParams(null, spuDTO.getCid3(), true);
        //根据spuId查出对应的spuDetail
        SpuDetail spuDetail = itemClient.findSpuDetailBySpuId(spuDTO.getId());
        //将通用数据和特殊数据查出，将key中的数替换成属性名即可封装到specs
        Map<Long, Object> genericSpec = JsonUtils.toMap(spuDetail.getGenericSpec(), Long.class, Object.class);
        Map<Long, Object> specialSpec = JsonUtils.nativeRead(spuDetail.getSpecialSpec(), new TypeReference<Map<Long, Object>>() {
        });

        Map<String, Object> specs = new HashMap<>();

        params.forEach(specParam -> {
            Object value = null;
            if (specParam.getGeneric()){
                value = genericSpec.get(specParam.getId());
            }else {
                value = specialSpec.get(specParam.getId());
            }

            //如果该字段为数字，就转化成区间
            if (specParam.getNumeric()){
                value = chooseSegment(value, specParam);
            }

            specs.put(specParam.getName(), value);
        });

        goods.setSpecs(specs);
        return goods;
    }
    /**
     * 把一个具体的参数值 转换为 区间值
     * @param value
     * @param p
     * @return
     */
    private String chooseSegment(Object value, SpecParam p) {
        if (value == null || StringUtils.isBlank(value.toString())) {
            return "其它";
        }
        double val = parseDouble(value.toString());
        String result = "其它";
        // 保存数值段
        for (String segment : p.getSegments().split(",")) {
            String[] segs = segment.split("-");
            // 获取数值范围
            double begin = parseDouble(segs[0]);
            double end = Double.MAX_VALUE;
            if (segs.length == 2) {
                end = parseDouble(segs[1]);
            }
            // 判断是否在范围内
            if (val >= begin && val < end) {
                if (segs.length == 1) {
                    result = segs[0] + p.getUnit() + "以上";
                } else if (begin == 0) {
                    result = segs[1] + p.getUnit() + "以下";
                } else {
                    result = segment + p.getUnit();
                }
                break;
            }
        }
        return result;
    }

    private double parseDouble(String str) {
        try {
            return Double.parseDouble(str);
        } catch (Exception e) {
            return 0;
        }
    }


    /**
     * 关键字搜索数据
     * @param searchRequest  封装关键字和当前页
     * @return
     */
    public SearchResult<GoodsDTO> goodsSearch(SearchRequest searchRequest) {
        //1.创建SearchResult对象
        SearchResult<GoodsDTO> searchResult = new SearchResult<>();
        //2.封装PageResult对象
        PageResult<GoodsDTO> pageResult = itemQueryPage(searchRequest);
        //3.封装filterConditions
        Map<String,Object> filterConditions = filterConditionsQuery(searchRequest);
        //4.封装SearchResult对象
        searchResult.setTotal(pageResult.getTotal());
        searchResult.setItems(pageResult.getItems());
        searchResult.setTotalPage(pageResult.getTotalPage());
        searchResult.setFilterConditions(filterConditions);
        return searchResult;
    }

    //3.封装filterConditions
    private Map<String, Object> filterConditionsQuery(SearchRequest searchRequest) {
        //LinkedHashMap为有序的map的集合
        Map<String, Object> filterConditions = new LinkedHashMap<>();

        //过滤查询
        NativeSearchQueryBuilder queryBuilder = new NativeSearchQueryBuilder();
        BoolQueryBuilder boolQueryBuilder = QueryBuilders.boolQuery();
        boolQueryBuilder.must(QueryBuilders.multiMatchQuery(searchRequest.getKey(), "all", "spuName"));
        queryBuilder.withQuery(boolQueryBuilder);
        //要保留的字段
        queryBuilder.withSourceFilter(new FetchSourceFilter(new String[]{}, null));
        //添加过滤条件
        addFilterParams(searchRequest, boolQueryBuilder);

        //2.4 添加 分类和品牌的聚合条件（固定过滤条件）
        String categoryAggName = "categoryAgg";
        String brandAggName = "brandAgg";
        //添加聚合条件
        queryBuilder.addAggregation(AggregationBuilders.terms(categoryAggName).field("categoryId"));
        queryBuilder.addAggregation(AggregationBuilders.terms(brandAggName).field("brandId"));
        //获取第一次聚合结果
        AggregatedPage<Goods> aggPage = elasticsearchTemplate.queryForPage(queryBuilder.build(), Goods.class);
        Aggregations aggregations = aggPage.getAggregations();
        Terms categoryTerms = aggregations.get(categoryAggName);
        //聚合结果为  聚合字段:数值  只需要聚合字段直接 getKey[字段类型] 抽取，
        List<Long> categoryIds = categoryTerms.getBuckets()
                                .stream()
                                .map(Terms.Bucket::getKeyAsNumber)
                                .map(Number::longValue)
                                .collect(Collectors.toList());
        //根据category的ids查询分类
        List<Category> categoryList = itemClient.findCategoryByIds(categoryIds);
        filterConditions.put("分类", categoryList);

        Terms brandTerms = aggregations.get(brandAggName);
        List<Long> brandIds = brandTerms.getBuckets()
                .stream()
                .map(Terms.Bucket::getKeyAsNumber)
                .map(Number::longValue)
                .collect(Collectors.toList());
        //根据brand的ids查询分类
        List<Brand> brandList = itemClient.findBrandByIds(brandIds);
        filterConditions.put("品牌", brandList);

        //第二次聚合
        categoryIds.forEach(categoryId->{
            List<SpecParam> params = itemClient.findParams(null, categoryId, true);
            //遍历所有规格参数，添加到聚合条件中
            params.forEach(param->{
                queryBuilder.addAggregation(AggregationBuilders.terms(param.getName()).field("specs." + param.getName() + ".keyword"));
            });
            //取出每个规格参数的聚合结果
            AggregatedPage<Goods> specsAggPage = elasticsearchTemplate.queryForPage(queryBuilder.build(), Goods.class);
            Aggregations specsAggs = specsAggPage.getAggregations();
            //再次遍历根据名字获取Terms结果集和存入map中
            params.forEach(param -> {
                Terms specTerms = specsAggs.get(param.getName());
                List<String> specAggResult = specTerms.getBuckets()
                                                .stream()
                                                .map(Terms.Bucket::getKeyAsString)
                                                .collect(Collectors.toList());
                //5.4 把每个规格参数的聚合结果存入Map集合中
                filterConditions.put(param.getName(), specAggResult);
            });
        });

        return filterConditions;
    }

    //封装PageResult对象
    private PageResult<GoodsDTO> itemQueryPage(SearchRequest searchRequest) {
        //过滤查询
        NativeSearchQueryBuilder queryBuilder = new NativeSearchQueryBuilder();
        BoolQueryBuilder boolQueryBuilder = QueryBuilders.boolQuery();
        //因为高亮字段所以spuName也要索引
        boolQueryBuilder.must(QueryBuilders.multiMatchQuery(searchRequest.getKey(), "all", "spuName"));
        queryBuilder.withQuery(boolQueryBuilder);
        //过滤剩下四个字段
        queryBuilder.withSourceFilter(new FetchSourceFilter(new String[]{"id", "subTitle", "spuName", "skus"}, null));
        //分页查询  参数一为当前页 0开始， 参数二页大小
        queryBuilder.withPageable(PageRequest.of(searchRequest.getPage()-1, searchRequest.getSize()));
        //添加高亮
        HighlightUtils.highlightField(queryBuilder, "spuName");
        //添加过滤条件
        addFilterParams(searchRequest, boolQueryBuilder);

        //查询结果
        Page<Goods> page = elasticsearchTemplate.queryForPage(queryBuilder.build(), Goods.class, HighlightUtils.highlightBody(Goods.class, "spuName"));

        //封装结果
        List<Goods> goods = page.getContent();
        List<GoodsDTO> goodsDTOS = BeanHelper.copyWithCollection(goods, GoodsDTO.class);
        PageResult<GoodsDTO> pageResult = new PageResult<>(page.getTotalElements(), Long.valueOf(page.getTotalPages()), goodsDTOS);
        return pageResult;
    }

    //添加过滤条件
    private void addFilterParams(SearchRequest searchRequest, BoolQueryBuilder boolQueryBuilder){
        Map<String, Object> filterParams = searchRequest.getFilterParams();
        if (CollectionUtils.isNotEmpty(filterParams)){
            filterParams.entrySet().forEach(entry -> {
                String key = entry.getKey();
                Object value = entry.getValue();

                if (key.equals("分类")){
                    key = "categoryId";
                }else if (key.equals("品牌")){
                    key = "brandId";
                }else {
                    key = "specs." + key + ".keyword";
                }
                boolQueryBuilder.filter(QueryBuilders.termQuery(key, value));
            });
        }

    }
    
    //页数变化查询item
    public List<GoodsDTO> pageChange(SearchRequest searchRequest) {
        PageResult<GoodsDTO> pageResult = itemQueryPage(searchRequest);
        return pageResult.getItems();
    }

    //添加索引库
    public void indexCreate(Long spuId) {
        SpuDTO spuDTO = itemClient.findSpuById(spuId);
        Goods goods = buildGoods(spuDTO);
        searchRepository.save(goods);
    }

    //添加索引库
    public void indexDelete(Long spuId){
        searchRepository.deleteById(spuId);
    }
}
