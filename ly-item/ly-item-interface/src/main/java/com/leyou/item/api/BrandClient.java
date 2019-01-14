package com.leyou.item.api;

import com.leyou.item.pojo.Brand;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;

@FeignClient("item-service")
public interface BrandClient {

    @GetMapping("brand/{id}")
    Brand queryById(@PathVariable("id") Long id);
}