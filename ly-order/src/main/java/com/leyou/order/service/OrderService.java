package com.leyou.order.service;


import com.leyou.auth.pojo.UserInfo;
import com.leyou.common.enums.ExceptionEnum;
import com.leyou.common.exception.LyException;
import com.leyou.common.utils.IdWorker;
import com.leyou.item.api.GoodsClient;
import com.leyou.item.pojo.Sku;
import com.leyou.common.dto.CartDTO;
import com.leyou.order.dto.OrderDTO;
import com.leyou.order.enums.OrderStatusEnum;
import com.leyou.order.interceptors.UserInterceptor;
import com.leyou.order.mapper.OrderDetailMapper;
import com.leyou.order.mapper.OrderMapper;
import com.leyou.order.mapper.OrderStatusMapper;
import com.leyou.order.pojo.Order;
import com.leyou.order.pojo.OrderDetail;
import com.leyou.order.pojo.OrderStatus;
import com.leyou.user.api.AddressClient;
import com.leyou.user.dto.AddressDTO;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.CollectionUtils;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Service
public class OrderService {
    @Autowired
    private OrderMapper orderMapper;
    @Autowired
    private OrderDetailMapper orderDetailMapper;
    @Autowired
    private OrderStatusMapper orderStatusMapper;
    @Autowired
    private IdWorker idWorker;
    @Autowired
    private GoodsClient goodsClient;
    @Autowired
    private AddressClient addressClient;

    @Transactional
    public Long createOrder(OrderDTO orderDTO) {
        //1、新增order
        Order order = new Order();
        //1、1订单id
        long orderId = idWorker.nextId();
        order.setOrderId(orderId);
        //1、2用户相关
        UserInfo user = UserInterceptor.getUser();
        order.setUserId(user.getId());
        order.setBuyerNick(user.getUsername());
        //1、3金额相关
        // 支付类型，1、在线支付，2、货到付款
        order.setPaymentType(orderDTO.getPaymentType());
        // 总金额
        List<CartDTO> carts = orderDTO.getCarts();
        //获取sku的Id集合
        List<Long> skuIds = carts.stream().map(CartDTO::getSkuId).collect(Collectors.toList());
        //批量查询sku
        List<Sku> skuList = goodsClient.querySkuByIds(skuIds);
        //将carts集合转换成map  key是skuId  值是数量 num
        Map<Long, Integer> map = carts.stream().collect(Collectors.toMap(CartDTO::getSkuId, CartDTO::getNum));
        long totalPay = 0;
        //定义一个List<OrderDetail> 下面用
        List<OrderDetail> list = new ArrayList<>();
        for (Sku sku : skuList) {
            Integer num = map.get(sku.getId());
            totalPay += sku.getPrice() * num;
            OrderDetail orderDetail = new OrderDetail();
            orderDetail.setOrderId(orderId);
            orderDetail.setImage(StringUtils.substringBefore(sku.getImages(), ","));
            orderDetail.setNum(num);
            orderDetail.setSkuId(sku.getId());
            orderDetail.setOwnSpec(sku.getOwnSpec());
            orderDetail.setPrice(sku.getPrice());
            orderDetail.setTitle(sku.getTitle());
            list.add(orderDetail);

        }
        order.setTotalPay(totalPay);
        // 实付金额
        order.setActualPay(totalPay + order.getPostFee()/**减去优惠金额，需查询优惠微服务**/);

        //1、4收货人信息
        AddressDTO addr = addressClient.queryAddressById(orderDTO.getAddressId());
        order.setReceiver(addr.getName());
        order.setReceiverMobile(addr.getPhone());
        order.setReceiverState(addr.getState());
        order.setReceiverCity(addr.getCity());
        order.setReceiverDistrict(addr.getDistrict());
        order.setReceiverAddress(addr.getAddress());
        order.setReceiverZip(addr.getZipCode());

        //1、5写入数据库
        order.setCreateTime(new Date());
        int count = orderMapper.insertSelective(order);
        if (count != 1) {
            throw new LyException(ExceptionEnum.ORDER_INSERT_ERROR);
        }
        //2、新增orderDetail
        count = orderDetailMapper.insertList(list);
        if (count != list.size()) {
            throw new LyException(ExceptionEnum.ORDER_INSERT_ERROR);
        }
        //2.1查询sku信息

        //2.2把sku填到detail

        //2.3写入数据库

        //3、新增orderStatus
        OrderStatus orderStatus = new OrderStatus();
        orderStatus.setOrderId(orderId);
        orderStatus.setCreateTime(order.getCreateTime());
        orderStatus.setStatus(OrderStatusEnum.INIT.value());
        count = orderStatusMapper.insertSelective(orderStatus);
        if (count != 1) {
            throw new LyException(ExceptionEnum.ORDER_INSERT_ERROR);
        }
        //4、减库存
        goodsClient.decrease(carts);
        //5、TODO 删除购物车中已下单的商品

        return orderId;
    }

    public Order queryOrderById(Long orderId) {
        Order order = orderMapper.selectByPrimaryKey(orderId);
        if (order == null) {
            throw new LyException(ExceptionEnum.ORDER_NOT_FOUND);
        }
          //查询订单详情
        OrderDetail orderDetail = new OrderDetail();
        orderDetail.setOrderId(orderId);
        List<OrderDetail> orderDetails = orderDetailMapper.select(orderDetail);
        if (CollectionUtils.isEmpty(orderDetails)) {
            throw new LyException(ExceptionEnum.ORDER_NOT_FOUND);
        }
        order.setOrderDetails(orderDetails);
       // 查询订单状态
        OrderStatus orderStatus = orderStatusMapper.selectByPrimaryKey(orderId);
        if (orderStatus==null) {
            throw new LyException(ExceptionEnum.ORDER_NOT_FOUND);
        }
        order.setOrderStatus(orderStatus);
        return order;
    }
}
