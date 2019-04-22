package com.leyou.page.web;


import com.leyou.page.service.PageService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;

import java.util.HashMap;
import java.util.Map;

@Controller
public class PageController {
    @Autowired
    private PageService pageService;
    /**
     * 渲染并跳转到商品详情页面
     * @param id
     * @return
     */
    @GetMapping("/item/{id}.html")
    public String toItemPage(@PathVariable("id")Long id, Model model){
         //查询模型数据
        Map<String,Object>itemModel= pageService.loadItemModel(id);
        //向模型数据中添加数据
        model.addAllAttributes(itemModel);
        //走到这里说明静态页面没有，我们要创建一个静态页面
        pageService.syncCreateItemHtml(id);
        return "item";
    }

}
