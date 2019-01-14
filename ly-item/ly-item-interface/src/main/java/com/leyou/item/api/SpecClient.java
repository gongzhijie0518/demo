package com.leyou.item.api;

import com.leyou.item.pojo.SpecParam;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;

import java.util.List;

@FeignClient("item-service")

public interface SpecClient {
    /**
     *
     * @param gid 组id
     * @param cid 分类id
     * @param searching 是否用于搜索
     * @return
     */
    @GetMapping("spec/params")
    List<SpecParam> queryParam(
            @RequestParam(value = "gid", required = false) Long gid,
            @RequestParam(value = "cid", required = false) Long cid,
            @RequestParam(value = "searching", required = false) Boolean searching
    );
}