package com.leyou.page.service;

import com.leyou.item.api.BrandClient;
import com.leyou.item.api.CategoryClient;
import com.leyou.item.api.GoodsClient;
import com.leyou.item.api.SpecClient;

import com.leyou.item.pojo.*;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.thymeleaf.TemplateEngine;
import org.thymeleaf.context.Context;

import java.io.File;
import java.io.IOException;
import java.io.PrintWriter;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

@Slf4j
@Service
public class PageService {
    @Autowired
    private GoodsClient goodsClient;
    @Autowired
    private SpecClient specClient;

    @Autowired
    private BrandClient brandClient;
    @Autowired
    private CategoryClient categoryClient;
    @Autowired
    private TemplateEngine templateEngine;
    @Value("${ly.static.itemPath}")
    private String itemPath;

    private static final ExecutorService pool = Executors.newFixedThreadPool(10);

    public Map<String, Object> loadItemModel(Long id) {
        Map<String, Object> map = new HashMap<>();
        //查询spu、skus、spudetail
        Spu spu = goodsClient.querySpuById(id);
        SpuDetail detail = spu.getSpuDetail();
        List<Sku> skus = spu.getSkus();
        //查询brand
        Brand brand = brandClient.queryById(spu.getBrandId());
        //查询分类
        List<Category> categories = categoryClient.queryListIds(Arrays.asList(spu.getCid1(), spu.getCid2(), spu.getCid3()));
        //查询specs
        List<SpecGroup> specs = specClient.querySpecsByCid(spu.getCid3());
        map.put("categories", categories);
        map.put("brand", brand);
        map.put("title", spu.getTitle());
        map.put("subTitle", spu.getSubTitle());
        map.put("detail", detail);
        map.put("skus", skus);
        map.put("specs", specs);
        return map;
    }

    public void createItemHtml(Long id) {
        Context context = new Context();
        context.setVariables(loadItemModel(id));
        File file = getFilePath(id);
        try (PrintWriter write = new PrintWriter(file, "UTF-8")) {

            templateEngine.process("item", context, write);

        } catch (IOException e) {
            log.error("【静态页服务】创建商品静态页失败，商品id：{}" + id, e);
        }
    }

    private File getFilePath(Long id) {
        File dir = new File(itemPath);
        if (!dir.exists()) {
            dir.mkdirs();
        }
        return new File(dir, id + ".html");
    }

    public void syncCreateItemHtml(Long id) {
        pool.submit(()->createItemHtml(id));
    }


    public void deleteItemHtml(Long spuId) {
        File file = getFilePath(spuId);
         if (file.exists()){
             file.delete();
         }
    }
}
