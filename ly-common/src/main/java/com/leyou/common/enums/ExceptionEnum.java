package com.leyou.common.enums;

import lombok.Getter;

@Getter
public enum ExceptionEnum {
    PRICE_CANNOT_BE_NULL(400, "价格不能为空"),
    CATEGORY_NOT_FOUND(404, "商品分类不存在"),
    SPEC_GROUP_NOT_FOUND(404, "规格组不存在"),
    GOODS_SAVE_ERROR(400, "商品参数有误"),
    GOODS_NOT_FOUND(404, "商品不存在"),
    SPEC_PARAM_NOT_FOUND(404, "规格参数不存在"),
    BRAND_NOT_FOUND(404, "品牌不存在"),
    BRAND_EDIT_ERROR(400,"品牌参数有误"),
    GOODS_EDIT_ERROR(400,"商品参数有误"),
    FILE_UPLOAD_ERROR(500,"文件上传异常"),
    INVALID_FILE_TYPE(400,"无效的文件类型"),
    SEND_MESSAGE_ERROR(500,"短信发送失败"),
    ORDER_INSERT_ERROR(500,"新增订单失败"),
    STOCK_NOT_ENOUGH(500,"库存不足"),
    DATA_TYPE_ERROR(400,"数据类型错误"),
    INVALID_USERNAME_PASSWORD(400,"用户名或密码错误"),
    UNAUTHORIZED(401,"没有访问权限"),
    CART_IS_EMPTY(404,"购物车没有商品"),
    ORDER_NOT_FOUND(404,"订单不存在"),
    ;

    private int status;
    private String message;

    ExceptionEnum(int status, String message) {
        this.status = status;
        this.message = message;
    }
}
