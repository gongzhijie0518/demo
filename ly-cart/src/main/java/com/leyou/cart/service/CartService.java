package com.leyou.cart.service;

import com.leyou.auth.pojo.UserInfo;
import com.leyou.cart.interceptors.UserInterceptor;
import com.leyou.cart.pojo.Cart;
import com.leyou.common.enums.ExceptionEnum;
import com.leyou.common.exception.LyException;
import com.leyou.common.utils.JsonUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.BoundHashOperations;
import org.springframework.data.redis.core.HashOperations;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Service;
import org.springframework.util.CollectionUtils;

import java.util.Collection;
import java.util.Collections;
import java.util.List;
import java.util.stream.Collectors;

@Service
public class CartService {
    private static final String KEY_PREFIX = "ly:cart:uid:";
    @Autowired
    private StringRedisTemplate redisTemplate;

    public void addCart(Cart cart) {
        //获取用户
        UserInfo user = UserInterceptor.getUser();
        //根据userId查询购物车数据
        //准备key
        String key = KEY_PREFIX + user.getId();
        //准备hashkey
        String hashKey = cart.getSkuId().toString();
        //判断该商品在购物车中是否已经存在
//        HashOperations<String, Object, Object> hashOperations = redisTemplate.opsForHash();
        BoundHashOperations<String, String, String> bhashops = redisTemplate.boundHashOps(key);
        if (bhashops.hasKey(hashKey)) {
            //购物车中已存在
            String json = bhashops.get(hashKey);
            Cart cachCart = JsonUtils.toBean(json, Cart.class);
            cart.setNum(cachCart.getNum() + cart.getNum());
            //写会redis
        }
        //第一次加入购物车
        //已存在改数量后存，不存在 直接存
        bhashops.put(hashKey, JsonUtils.toString(cart));
    }

    public List<Cart> queryCartList() {
        //获取用户
        UserInfo user = UserInterceptor.getUser();
        //准备key
        String key = KEY_PREFIX + user.getId();
        if (!redisTemplate.hasKey(key)) {
            throw new LyException(ExceptionEnum.CART_IS_EMPTY);
        }
        BoundHashOperations<String, String, String> bhashops = redisTemplate.boundHashOps(key);
      //获取购物车信息
        List<String> values = bhashops.values();
        if (CollectionUtils.isEmpty(values)) {
            throw new LyException(ExceptionEnum.CART_IS_EMPTY);
        }
        //处理拿到的json数据
        List<Cart> cartList = values.stream().map(json -> JsonUtils.toBean(json, Cart.class))
                .collect(Collectors.toList());
        return cartList;
    }

    public void deleteCart(Long skuId) {


    }
}
