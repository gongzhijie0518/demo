package com.leyou.item.service;

import com.github.pagehelper.PageHelper;
import com.github.pagehelper.PageInfo;
import com.leyou.common.dto.CartDTO;
import com.leyou.common.enums.ExceptionEnum;
import com.leyou.common.exception.LyException;
import com.leyou.common.vo.PageResult;
import com.leyou.item.mapper.SkuMapper;
import com.leyou.item.mapper.SpuDetailMapper;
import com.leyou.item.mapper.SpuMapper;
import com.leyou.item.mapper.StockMapper;
import com.leyou.item.pojo.*;
import org.apache.commons.lang3.StringUtils;
import org.springframework.amqp.core.AmqpTemplate;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.CollectionUtils;
import tk.mybatis.mapper.entity.Example;

import java.beans.Transient;
import java.util.*;
import java.util.stream.Collectors;
import java.util.stream.Stream;

@Service
public class GoodsService {
    @Autowired
    private SpuDetailMapper spuDetailMapper;
    @Autowired
    private SpuMapper spuMapper;
    @Autowired
    private BrandService brandService;
    @Autowired
    private CategoryService categoryService;
    @Autowired
    private SkuMapper skuMapper;
    @Autowired
    private StockMapper stockMapper;
    @Autowired
    private AmqpTemplate template;


    public PageResult<Spu> querySpuByPage(Integer page, Integer rows, Boolean saleable, String key) {
        //分页查询
        PageHelper.startPage(page, rows);
        Example example = new Example(Spu.class);
        Example.Criteria criteria = example.createCriteria();
        //关键词过滤
        if (StringUtils.isNotBlank(key)) {
            criteria.andLike("title", "%" + key + "%");
        }
        //上下架过滤
        if (saleable != null) {
            criteria.andEqualTo("saleable", saleable);
        }
        //逻辑删除过滤
        criteria.andEqualTo("valid", true);
        //按时间排序
        example.setOrderByClause("last_update_time desc");
        //list为当前叶的信息，size为5
        List<Spu> list = spuMapper.selectByExample(example);
        if (CollectionUtils.isEmpty(list)) {
            throw new LyException(ExceptionEnum.GOODS_NOT_FOUND);
        }
        //获取商品分类和品牌
        handleCategoryAndBrandName(list);

        PageInfo<Spu> pageInfo = new PageInfo<>(list);
        //获取总数据数
        long total = pageInfo.getTotal();
        return new PageResult<>(pageInfo.getTotal(), list);
    }

    public void handleCategoryAndBrandName(List<Spu> list) {
        for (Spu spu : list) {
          /*  List<Category> list1 = categoryService.queryByIds(Arrays.asList(spu.getCid1(), spu.getCid2(), spu.getCid3()));
            String name = "";
            for (int i = 0; i < list1.size(); i++) {
                if (i < list1.size() - 1) {
                    name += list1.get(i).getName() + "/";
                } else {
                    name += list1.get(i).getName();
                }
            }*/
            //流式编程
            String name = categoryService.queryByIds(Arrays.asList(spu.getCid1(), spu.getCid2(), spu.getCid3()))
                    .stream()// 把category集合变为category的流
                    .map(Category::getName) // 把category的流映射为name 的流
                    .collect(Collectors.joining("/"));// 把name的流逐个拼接
            spu.setCname(name);
            Brand brand = brandService.queryById(spu.getBrandId());
            spu.setBname(brand.getName());
        }

    }

    @Transactional
    public void saveGoods(Spu spu) {
        //写Spu
        spu.setSaleable(true);
        spu.setValid(true);
        spu.setCreateTime(new Date());
        spu.setId(null);
        spu.setLastUpdateTime(spu.getCreateTime());
        int count = spuMapper.insert(spu);
        if (count != 1) {
            throw new LyException(ExceptionEnum.GOODS_SAVE_ERROR);
        }
        //写SpuDetail
        SpuDetail spuDetail = spu.getSpuDetail();
        spuDetail.setSpuId(spu.getId());
        count = spuDetailMapper.insert(spuDetail);
        if (count != 1) {
            throw new LyException(ExceptionEnum.GOODS_SAVE_ERROR);
        }
        saveSkuAndStock(spu);

    }

    private void saveSkuAndStock(Spu spu) {
        int count;//写Sku
        List<Sku> skus = spu.getSkus();
        List<Stock> stocks = new ArrayList<>();

        for (Sku sku : skus) {
            sku.setSpuId(spu.getId());
            sku.setCreateTime(new Date());
            sku.setLastUpdateTime(sku.getCreateTime());
            count = skuMapper.insert(sku);
            if (count != 1) {
                throw new LyException(ExceptionEnum.GOODS_SAVE_ERROR);
            }
            //抽取库存
            Stock stock = new Stock();
            stock.setSkuId(sku.getId());
            stock.setStock(sku.getStock());
            stocks.add(stock);
        }
        //写Stock
        count = stockMapper.insertList(stocks);
        if (count != stocks.size()) {
            throw new LyException(ExceptionEnum.GOODS_SAVE_ERROR);
        }
    }

    public void updaSaleable(Long id, Boolean saleable) {
        Spu spu = new Spu();
        spu.setId(id);
        spu.setLastUpdateTime(new Date());
        spu.setSaleable(saleable);
        int count = spuMapper.updateByPrimaryKeySelective(spu);
        if (count != 1) {
            throw new LyException(ExceptionEnum.GOODS_SAVE_ERROR);
        }
        //发送消息
        String routingKey = "item.";
        if (saleable) {
            //发送上架消息
            routingKey += "insert";
        } else {
            //下架
            routingKey += "delete";
        }
        template.convertAndSend(routingKey, spu.getId());
    }

    public SpuDetail queryDetailBySpuId(Long spuId) {
        SpuDetail spuDetail = spuDetailMapper.selectByPrimaryKey(spuId);
        if (spuDetail == null) {
            throw new LyException(ExceptionEnum.GOODS_NOT_FOUND);
        }
        return spuDetail;
    }

    public List<Sku> querySkuBySpuId(Long spuId) {
        Sku s = new Sku();
        s.setSpuId(spuId);
        List<Sku> skuList = skuMapper.select(s);
        if (CollectionUtils.isEmpty(skuList)) {
            throw new LyException(ExceptionEnum.GOODS_NOT_FOUND);
        }
        //查询sku的库存 并添加到每个sku中
        loadSkuStock(skuList);

        return skuList;
    }

    private void loadSkuStock(List<Sku> skuList) {
        //获取sku的集合
        List<Long> idList = skuList
                .stream()//把sku集合变成sku的流
                .map(Sku::getId)//把sku的流变成id的流
                .collect(Collectors.toList());//把流中的数据手机到集合
        List<Stock> stockList = stockMapper.selectByIdList(idList);
        if (CollectionUtils.isEmpty(stockList)) {
            throw new LyException(ExceptionEnum.GOODS_NOT_FOUND);
        }
        //把stockList变成map  k是skuId，值是stock
        Map<Long, Integer> map = stockList.stream().collect(Collectors.toMap(Stock::getSkuId, Stock::getStock));
        //填充sku库存
        for (Sku sku : skuList) {
            sku.setStock(map.get(sku.getId()));
        }
    }

    @Transactional
    public void updateGoods(Spu spu) {
        //1、获取spuid
        int count;
        Long spuId = spu.getId();
        if (spuId == null) {
            throw new LyException(ExceptionEnum.GOODS_EDIT_ERROR);
        }
        //2、删除sku(先删除库存，不然库存的id没法对应)
        Sku s = new Sku();
        s.setSpuId(spuId);
        //3、删除库存
        //3.1 先根据sku的spuID获取sku的集合
        List<Sku> skuList = skuMapper.select(s);
        if (!CollectionUtils.isEmpty(skuList)) {
            //3.2 删除库存
            //流式编程
            List<Long> idList = skuList.stream().map(Sku::getId).collect(Collectors.toList());
            count = stockMapper.deleteByIdList(idList);
            if (count != skuList.size()) {
                throw new LyException(ExceptionEnum.GOODS_EDIT_ERROR);
            }
    /*        for (Sku sku : skuList) {
                count = stockMapper.deleteByPrimaryKey(sku.getId());
                if (count != 1) {
                    throw new LyException(ExceptionEnum.GOODS_EDIT_ERROR);
                }
            }*/
            //3.3删除sku
            count = skuMapper.delete(s);
            if (count != skuList.size()) {
                throw new LyException(ExceptionEnum.GOODS_EDIT_ERROR);
            }
        }
        //4、修改spu数据
        spu.setCreateTime(null);
        spu.setValid(null);
        spu.setSaleable(null);
        spu.setLastUpdateTime(new Date());
        count = spuMapper.updateByPrimaryKeySelective(spu);
        if (count != 1) {
            throw new LyException(ExceptionEnum.GOODS_NOT_FOUND);
        }
        //5、修改spu_detail

        count = spuDetailMapper.updateByPrimaryKeySelective(spu.getSpuDetail());
        if (count != 1) {
            throw new LyException(ExceptionEnum.GOODS_NOT_FOUND);
        }
        //6、新增stock和sku
        saveSkuAndStock(spu);
    }

    public Spu querySpuById(Long id) {
        //查询spu
        Spu spu = spuMapper.selectByPrimaryKey(id);
        if (spu == null) {
            throw new LyException(ExceptionEnum.GOODS_NOT_FOUND);
        }
        //查询detail
        SpuDetail spuDetail = queryDetailBySpuId(id);
        spu.setSpuDetail(spuDetail);
        //查询skus
        List<Sku> skus = querySkuBySpuId(id);
        spu.setSkus(skus);
        return spu;
    }

    public List<Sku> querySkuByIds(List<Long> ids) {
        List<Sku> skus = skuMapper.selectByIdList(ids);
        if (CollectionUtils.isEmpty(skus)) {
            throw new LyException(ExceptionEnum.GOODS_NOT_FOUND);
        }
        //查询库存 并添加到每个sku中
        loadSkuStock(skus);
        return skus;
    }

    @Transactional
    public void decreaseStock(List<CartDTO> carts) {
        //循环减库存需要加事物
        for (CartDTO cart : carts) {
            try {
                //由于设置了数据库stock字段为无符号，不能为负数，所以可以直接减库存
                int count = stockMapper.decreaseStock(cart.getSkuId(), cart.getNum());
            } catch (Exception e) {
                throw new LyException(ExceptionEnum.STOCK_NOT_ENOUGH);
            }
        }

    }
}
