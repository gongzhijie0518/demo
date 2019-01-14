package com.leyou.search.service;

import com.fasterxml.jackson.core.type.TypeReference;
import com.leyou.common.utils.JsonUtils;
import com.leyou.common.utils.NumberUtils;
import com.leyou.item.api.BrandClient;
import com.leyou.item.api.CategoryClient;
import com.leyou.item.api.GoodsClient;
import com.leyou.item.api.SpecClient;
import com.leyou.item.pojo.*;
import com.leyou.search.pojo.Goods;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.*;
import java.util.stream.Collectors;

@Service
public class SearchService {
    @Autowired
    private BrandClient brandClient;
    @Autowired
    private CategoryClient categoryClient;
    @Autowired
    private GoodsClient goodsClient;
    @Autowired
    private SpecClient specClient;
    /**
     * 把spu转成goods对象
     *
     * @param spu
     * @return
     */
    public Goods buildGoods(Spu spu) {
        //1、用来全文检索的字段、包含标题、分类、品牌等
        //1.1查询分类
        String categoryName = categoryClient.queryListIds
                (Arrays.asList(spu.getCid1(), spu.getCid3(), spu.getCid3()))
                .stream().map(Category::getName).collect(Collectors.joining());
        //1.2查询品牌
        Brand brand = brandClient.queryById(spu.getBrandId());
        String all = spu.getTitle() + categoryName + brand.getName();
        //2 spu下的所有sku的集合的json格式
        //2、1查询sku
        List<Sku> skuList = goodsClient.querySkuListBySpuId(spu.getId());
        //2.1 取出需要的字段（创建时间什么的不需要）
        List<Map<String, Object>> skus = new ArrayList<>();
        for (Sku sku : skuList) {
            HashMap<String, Object> map = new HashMap<>();
            map.put("id", sku.getId());
            map.put("price", sku.getPrice());
            map.put("image", StringUtils.substringBefore(sku.getImages(), ","));
            map.put("title", sku.getTitle());
            skus.add(map);
        }
        //3、查询spu下skus的价格
        Set<Long> price = skuList.stream().map(Sku::getPrice).collect(Collectors.toSet());
        //4、spu下可搜索的的规格参数的键值对
        HashMap<String, Object> spes = new HashMap<>();
        //4.1查询可用于搜索的规格参数key
        List<SpecParam> specParams = specClient.queryParam(null, spu.getCid3(), true);
        //4.2 查询规格参数值，在specDetial中
        SpuDetail spuDetail = goodsClient.queryDetailBySpuId(spu.getId());
        //4.2.1取出通用规格参数值
        String json = spuDetail.getGenericSpec();
        Map<Long, Object> genericSpec = JsonUtils.toMap(json, Long.class, Object.class);
        //4.2.2取出特有规格参数值
        Map<Long, List<String>> specialSpec = JsonUtils.nativeRead(spuDetail.getSpecialSpec(), new TypeReference<Map<Long, List<String>>>() {
        });
        //4.3填充map
        for (SpecParam specParam : specParams) {
            String key = specParam.getName();
            Object value = null;
            //判断是否是通用属性
            if (specParam.getGeneric()) {
                //获取通用属性（通过上面的map，map的值是一张表对应的有sid）
                value = genericSpec.get(specParam.getId());
            } else {
                //特有属性属性
                value = specialSpec.get(specParam.getId());
            }
            //处理vlue，判断是不是数值，如果是，需要分段处理
            if (specParam.getNumeric()) {
                value = chooseSegment(value, specParam);
            }
            spes.put(key, value);
        }
        Goods goods = new Goods();
        //拷贝属性名一致的属性
        BeanUtils.copyProperties(spu, goods);
        //其他属性，自己填写
        //由于是long类型无法自动拷贝
        goods.setCreateTime(spu.getCreateTime().getTime());
        goods.setAll(all);// 用来全文检索的字段
        goods.setSkus(JsonUtils.toString(skus));// spu下的所有sku的集合的json格式
        goods.setPrice(price);// spu下所有的sku的价格
        goods.setSpecs(spes);// spu下可搜索的的规格参数的键值对
        return goods;
    }
    private String chooseSegment(Object value, SpecParam p) {
        if (value == null || StringUtils.isBlank(value.toString())) {
            return "其它";
        }
        double val = NumberUtils.toDouble(value.toString());
        String result = "其它";
        // 保存数值段
        for (String segment : p.getSegments().split(",")) {
            String[] segs = segment.split("-");
            // 获取数值范围
            double begin = NumberUtils.toDouble(segs[0]);
            double end = Double.MAX_VALUE;
            if (segs.length == 2) {
                end = NumberUtils.toDouble(segs[1]);
            }
            // 判断是否在范围内
            if (val >= begin && val < end) {
                if (segs.length == 1) {
                    result = segs[0] + p.getUnit() + "以上";
                } else if (begin == 0) {
                    result = segs[1] + p.getUnit() + "以下";
                } else {
                    result = segment + p.getUnit();
                }
                break;
            }
        }
        return result;
    }
}
