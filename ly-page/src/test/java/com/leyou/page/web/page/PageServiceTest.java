package com.leyou.page.web.page;


import com.leyou.page.service.PageService;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.junit4.SpringRunner;

import static org.junit.Assert.*;

@RunWith(SpringRunner.class)
@SpringBootTest
public class PageServiceTest {

    @Autowired
     private PageService pageService;
    @org.junit.Test
    public void createItemHtml() {
        pageService.createItemHtml(88L);

    }
}
