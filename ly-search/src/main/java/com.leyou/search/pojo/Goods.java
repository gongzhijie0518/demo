package com.leyou.search.pojo;

import lombok.Data;
import org.springframework.data.elasticsearch.annotations.Document;

import javax.persistence.Id;
import java.util.Map;
import java.util.Set;

@Data
@Document(indexName = "goods", type = "docs")
public class Goods {
    @Id
    private Long id; // spuId
    private String subTitle;// 卖点
    private String skus;// sku信息的json结构
    private String all; // 所有需要被搜索的信息，包含标题，分类，甚至品牌
    private Long brandId;// 品牌id
    private Long cid3;// 3级分类id
    private Long createTime;// spu创建时间
    private Set<Long> price;// 价格
    private Map<String, Object> specs;// 可搜索的规格参数，key是参数名，值是参数值
}