package com.leyou.page.controller;

import com.leyou.page.service.PageService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;

import java.util.Map;

//这里是跳转不能使用RestController
@Controller
public class PageController {

    @Autowired
    private PageService pageService;

    @GetMapping("/item/{spuId}.html")
    public String showDetail(@PathVariable("spuId")Long spuId, Model model){
        Map<String, Object> map = pageService.showDetail(spuId);
        model.addAllAttributes(map);
        //自动拼接前缀(/templates/)和后缀(.html)
        return "item";
    }
}
