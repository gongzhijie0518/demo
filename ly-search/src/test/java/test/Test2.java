package test;

import com.leyou.LySearchApplication;
import com.leyou.common.vo.PageResult;
import com.leyou.item.api.CategoryClient;

import com.leyou.item.api.GoodsClient;
import com.leyou.item.pojo.Category;
import com.leyou.item.pojo.Spu;
import com.leyou.search.pojo.Goods;
import com.leyou.search.repository.GoodsRepository;
import com.leyou.search.service.SearchService;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.junit4.SpringRunner;

import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;
import java.util.stream.Stream;

@RunWith(SpringRunner.class)
@SpringBootTest(classes = LySearchApplication.class)
public class Test2 {
    @Autowired
    private CategoryClient categoryClient;
    @Autowired
    private GoodsClient goodsClient;
    @Autowired
    private SearchService searchService;
    @Autowired
    private GoodsRepository repository;

    @Test
    public void test() {
        List<Category> list = categoryClient.queryListIds(Arrays.asList(1l, 2l, 3l));
        list.forEach(System.out::println);

    }

    @Test
    public void loadData() {
        int page = 1, rows = 100, size = 0;
        do {
            try {
                //查询spu
                PageResult<Spu> result = goodsClient.querySpuByPage(page, rows, true, null);
                List<Spu> spuList = result.getItems();
                //把spu转成goods
                List<Goods> goodsList = spuList.stream()
                        .map(searchService::buildGoods)
                        .collect(Collectors.toList());
                //把goods写入索引库
                repository.saveAll(goodsList);
                page++;
                size = spuList.size();
            } catch (Exception e) {
                e.printStackTrace();
                break;
            }
        } while (size == 100);
    }
}
