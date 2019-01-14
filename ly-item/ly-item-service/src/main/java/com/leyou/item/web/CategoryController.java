package com.leyou.item.web;

import com.leyou.item.pojo.Category;
import com.leyou.item.service.CategoryService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("category")
public class CategoryController {
    @Autowired
    private CategoryService categoryService;

    /**
     * 根据父类目id查询子类目的集合
     * @param pid
     * @return
     */
    @GetMapping("list")
    public ResponseEntity<List<Category>> queryListByPid(@RequestParam("pid") Long pid) {
        List<Category> list = categoryService.queryListByPid(pid);
        return ResponseEntity.status(HttpStatus.OK).body(list);
    }
    /**
     * 根据id查询分类
     * @param ids
     * @return
     */
    @GetMapping("list/ids")
    public ResponseEntity<List<Category>> queryListByids(@RequestParam("ids") List <Long> ids) {
        List<Category> list = categoryService.queryByIds(ids);
        return ResponseEntity.status(HttpStatus.OK).body(list);
    }


}
