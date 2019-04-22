package com.leyou.search.service;

import com.fasterxml.jackson.core.type.TypeReference;
import com.leyou.common.enums.ExceptionEnum;
import com.leyou.common.exception.LyException;
import com.leyou.common.utils.JsonUtils;
import com.leyou.common.utils.NumberUtils;
import com.leyou.common.vo.PageResult;
import com.leyou.item.api.BrandClient;
import com.leyou.item.api.CategoryClient;
import com.leyou.item.api.GoodsClient;
import com.leyou.item.api.SpecClient;
import com.leyou.item.pojo.*;
import com.leyou.search.pojo.Goods;
import com.leyou.search.pojo.SearchRequest;
import com.leyou.search.pojo.SearchResult;
import com.leyou.search.repository.GoodsRepository;
import org.apache.commons.lang3.StringUtils;
import org.elasticsearch.index.query.BoolQueryBuilder;
import org.elasticsearch.index.query.MatchQueryBuilder;
import org.elasticsearch.index.query.QueryBuilder;
import org.elasticsearch.index.query.QueryBuilders;
import org.elasticsearch.repositories.Repository;
import org.elasticsearch.search.aggregations.Aggregation;
import org.elasticsearch.search.aggregations.AggregationBuilders;
import org.elasticsearch.search.aggregations.Aggregations;
import org.elasticsearch.search.aggregations.bucket.terms.LongTerms;
import org.elasticsearch.search.aggregations.bucket.terms.StringTerms;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.elasticsearch.core.ElasticsearchTemplate;
import org.springframework.data.elasticsearch.core.aggregation.AggregatedPage;
import org.springframework.data.elasticsearch.core.query.FetchSourceFilter;
import org.springframework.data.elasticsearch.core.query.NativeSearchQueryBuilder;
import org.springframework.stereotype.Service;

import java.util.*;
import java.util.stream.Collectors;
import java.util.stream.Stream;

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
    @Autowired
    private ElasticsearchTemplate template;
    @Autowired
     private GoodsRepository repository;

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

    public PageResult<Goods> search(SearchRequest request) {
        //原生查询构建器
        String key = request.getKey();
        if (StringUtils.isBlank(key)) {
            throw new LyException(ExceptionEnum.GOODS_NOT_FOUND);
        }
        NativeSearchQueryBuilder queryBuilder = new NativeSearchQueryBuilder();
        //0.通过source过滤，控制返回的字段,前端只需要三个
        queryBuilder.withSourceFilter(new FetchSourceFilter(new String[]{"id", "subTitle", "skus"}, null));
        //1、分页
        Integer page = request.getPage() - 1;
        Integer size = request.getSize();
        queryBuilder.withPageable(PageRequest.of(page, size));
        //2、关键字搜索
        QueryBuilder basicQuery = buildBasicQuery(request);
        queryBuilder.withQuery(basicQuery);
        //3聚合
        //3.1对分类聚合
        String categoryAggName = "categoryAgg";
        queryBuilder.addAggregation(AggregationBuilders.terms(categoryAggName).field("cid3"));
        //3.1对品牌聚合
        String brandAggName = "brandAgg";
        queryBuilder.addAggregation(AggregationBuilders.terms(brandAggName).field("brandId"));
        //4、查询结果
        AggregatedPage<Goods> result = template.queryForPage(queryBuilder.build(), Goods.class);
        //5解析结果
        //5.1解析分页结果
        long total = result.getTotalElements();
        int totalPages = result.getTotalPages();
        List<Goods> goodsList = result.getContent();
        //5.2处理过滤条件
        List<Map<String, Object>> filterList = new ArrayList<>();
        //5.3解析聚合结果
        Aggregations aggs = result.getAggregations();
        //5.3.1解析分类结果
        LongTerms terms = aggs.get(categoryAggName);
        List<Long> idList = handleCategoryAgg(terms, filterList);
        //5.3.1解析品牌结果
        handleBrandAgg(aggs.get(brandAggName), filterList);
        //6.对规格参数进行聚合
        //6.1判断分类的数量是否为1
        if (idList != null && idList.size() == 1) {
            //6.2处理规格参数
            handleSpec(idList.get(0), filterList, basicQuery);

        }
        //7封装并返回
        return new SearchResult(total, totalPages, goodsList, filterList);
    }

    private QueryBuilder buildBasicQuery(SearchRequest request) {
        //构建boolean查询
        BoolQueryBuilder queryBuilder = QueryBuilders.boolQuery();
        //搜索条件
        queryBuilder.must(QueryBuilders.matchQuery("all",request.getKey()));
        //过滤条件
        Map<String, String> filters = request.getFilters();
        for (Map.Entry<String, String> entry : filters.entrySet()) {
            String key=entry.getKey();
            String value=entry.getValue();
            //处理规格参数的key
            if (!"cid3".equals(key)&&!"brandId".equals(key)){
                key="specs."+key;
            }
            queryBuilder.filter(QueryBuilders.termQuery(key,value));
        }
        return queryBuilder;
    }

    private void handleSpec(Long cid3, List<Map<String, Object>> filterList, QueryBuilder basicQuery) {
        //1.查询需要聚合的规格参数
        List<SpecParam> specParams = specClient.queryParam(null, cid3, true);
        //2.对规格参数聚合

        NativeSearchQueryBuilder queryBuilder = new NativeSearchQueryBuilder();
        //2.1添加过滤条件
        queryBuilder.withQuery(basicQuery);
        //2.2减少分页size 以减少搜索结果
        queryBuilder.withPageable(PageRequest.of(0, 1));
        //2.3聚合
        for (SpecParam specParam : specParams) {
            String name = specParam.getName();
            queryBuilder.addAggregation(AggregationBuilders.terms(name).field("specs." + name));
        }
        //3.得到聚合结果
        AggregatedPage<Goods> result = template.queryForPage(queryBuilder.build(), Goods.class);
        //4.解析结果，封装到fileterList中
        Aggregations aggs = result.getAggregations();
        for (SpecParam specParam : specParams) {
            String name = specParam.getName();
            //取出terms
            StringTerms terms = aggs.get(name);
            List<String> options = terms.getBuckets().
                    stream().map(bucket -> bucket.getKeyAsString())
                    .filter(StringUtils::isNotBlank).collect(Collectors.toList());
            HashMap<String, Object> map = new HashMap<>();
            map.put("k", name);
            map.put("options", options);
            filterList.add(map);
        }

    }

    private void handleBrandAgg(LongTerms brandTerms, List<Map<String, Object>> filterList) {
        //解析terms,取出品牌的id
        List<Long> idList = brandTerms.getBuckets().stream().map(bucket -> bucket.getKeyAsNumber().longValue()).collect(Collectors.toList());
        //根据ids查询品牌
        List<Brand> brands = brandClient.queryByIds(idList);
        HashMap<String, Object> map = new HashMap<>();
        map.put("k", "brandId");
        map.put("options", brands);
        filterList.add(map);
    }

    private List<Long> handleCategoryAgg(LongTerms categoryTerms, List<Map<String, Object>> filterList) {
        //解析terms,取出分类的id
        List<Long> idList = categoryTerms.getBuckets().stream().map(bucket -> bucket.getKeyAsNumber().longValue()).collect(Collectors.toList());
        //根据ids查询分类
        System.out.println(idList);
        List<Category> categories = categoryClient.queryListIds(idList);
        HashMap<String, Object> map = new HashMap<>();
        map.put("k", "cid3");
        map.put("options", categories);
        filterList.add(map);
        return idList;
    }

    public void insertGoods(Long spuId) {
        Spu spu = goodsClient.querySpuById(spuId);
        Goods goods = buildGoods(spu);
        repository.save(goods);
    }

    public void deleteByID(Long spuId) {
        repository.deleteById(spuId);

    }
}
