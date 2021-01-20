package com.leyou.search.controller;

import com.leyou.search.dto.GoodsDTO;
import com.leyou.search.dto.SearchRequest;
import com.leyou.search.dto.SearchResult;
import com.leyou.search.service.SearchService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
public class SearchController {

    @Autowired
    private SearchService searchService;

    @PostMapping("/page")
    public ResponseEntity<SearchResult<GoodsDTO>> goodsSearch(@RequestBody SearchRequest searchRequest){
        SearchResult<GoodsDTO> searchResult = searchService.goodsSearch(searchRequest);
        return ResponseEntity.ok(searchResult);
    }

    //换页
    @PostMapping("/page/change")
    public ResponseEntity<List<GoodsDTO>> pageChange(@RequestBody SearchRequest searchRequest) {
        List<GoodsDTO> goodsDTOS = searchService.pageChange(searchRequest);
        return ResponseEntity.ok(goodsDTOS);
    }
}
