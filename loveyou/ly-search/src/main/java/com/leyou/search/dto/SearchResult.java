package com.leyou.search.dto;

import com.leyou.common.pojo.PageResult;
import lombok.Data;

import java.util.Map;

/**
 * 封装所有搜索结果的DTO
 */
@Data
public class SearchResult<T> extends PageResult<T>{
    //封装搜索过滤条件
    private Map<String,Object> filterConditions;
}
