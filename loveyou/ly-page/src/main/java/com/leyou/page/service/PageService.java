package com.leyou.page.service;

import com.leyou.client.item.ItemClient;
import com.leyou.item.dto.SpecGroupDTO;
import com.leyou.item.dto.SpuDTO;
import com.leyou.item.entity.Brand;
import com.leyou.item.entity.Category;
import com.leyou.item.entity.SpuDetail;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.thymeleaf.context.Context;
import org.thymeleaf.spring5.SpringTemplateEngine;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileWriter;
import java.io.PrintWriter;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Service
public class PageService {

    @Autowired
    private ItemClient itemClient;

    @Value("${ly.static.itemDir}")
    private String itemDir;//存放静态页面目录

    @Value("${ly.static.itemTemplate}")
    private String itemTemplate;//模板名称

    @Autowired
    private SpringTemplateEngine templateEngine;//模板引擎，生成静态页面到目录中

    public Map<String, Object> showDetail(Long spuId) {
        Map<String, Object> result = new HashMap<>();
        SpuDTO spuDTO = itemClient.findSpuById(spuId);

        List<Category> categories = itemClient.findCategoryByIds(Arrays.asList(spuDTO.getCid1(), spuDTO.getCid2(), spuDTO.getCid3()));
        Brand brand = itemClient.findById(spuDTO.getBrandId());
        List<SpecGroupDTO> specs = itemClient.findSpecsByCid(spuDTO.getCid3());

        //3.封装Map集合
        result.put("categories",categories);
        result.put("brand",brand);
        result.put("spuName",spuDTO.getName());
        result.put("subTitle",spuDTO.getSubTitle());
        result.put("detail",spuDTO.getSpuDetail());
        result.put("skus",spuDTO.getSkus());
        result.put("specs",specs);
        return result;
    }

    //生成商品详情静态页
    public void createDetailStaticPage(Long spuId){
       // 1.创建Context对象  动态内容
        Context context = new Context();
        context.setVariables(showDetail(spuId));
       // 2.定义模板名称
        String tempName = itemTemplate + ".html";
       // 3.定义输出流，记得关流否则不会io写进磁盘
        PrintWriter writer = null;

        try {
            writer = new PrintWriter(new File(itemDir + "/" + spuId + ".html"));
            /**
             * 参数一: 模板名称
             * 参数二: context对象存储动态内容
             * 参数三: 输出流
             */
            templateEngine.process(tempName, context, writer);
        } catch (FileNotFoundException e) {
            e.printStackTrace();
        }finally {
            writer.close();
        }

    }

    //删除Nginx静态页
    public void deleteStaticPage(Long spuId) {
        //创建路径
        File path = new File(itemDir);
        //静态目录名称
        String pageName = spuId + ".html";

        File file = new File(path, pageName);

        if (file.exists()){
            file.delete();
        }
    }

    //添加静态页面
    public void createStaticPage(Long spuId) {
        createDetailStaticPage(spuId);
    }
}
