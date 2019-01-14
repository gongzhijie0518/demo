package com.leyou.item.api;

import com.leyou.item.pojo.Category;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;

import java.util.List;

@FeignClient(value = "item-service",path ="category" )
public interface CategoryClient {
@GetMapping("list/ids")
    List<Category> queryListIds(@RequestParam("ids")List<Long>ids);


}
