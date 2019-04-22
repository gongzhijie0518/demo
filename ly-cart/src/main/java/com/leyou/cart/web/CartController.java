package com.leyou.cart.web;

import com.leyou.auth.pojo.UserInfo;
import com.leyou.auth.utils.JwtUtils;
import com.leyou.cart.config.JwtProperties;
import com.leyou.cart.pojo.Cart;
import com.leyou.cart.service.CartService;
import com.leyou.common.enums.ExceptionEnum;
import com.leyou.common.exception.LyException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
public class CartController {
    @Autowired
    private JwtProperties prop;
    @Autowired
    private CartService cartService;

    /**
     * 添加购物车
     *
     * @param cart
     * @return
     */
    /*@PostMapping()
    public ResponseEntity<Void> addCart(@RequestBody Cart cart, @CookieValue("LY_TOKEN") String token) {
        //解析token
        try {
            UserInfo user = JwtUtils.getInfoFromToken(token, prop.getPublicKey());
            cartService.addCart(cart, user.getId());
            return ResponseEntity.status(HttpStatus.CREATED).build();
        } catch (Exception e) {
            throw new LyException(ExceptionEnum.UNAUTHORIZED);
        }
    }

    @GetMapping("list")
    public ResponseEntity<List<Cart>> queryCartList(@CookieValue("LY_TOKEN") String token) {
        try {
            UserInfo user = JwtUtils.getInfoFromToken(token, prop.getPublicKey());
        return ResponseEntity.ok(cartService.queryCartList(user.getId()));
        } catch (Exception e) {
            throw new LyException(ExceptionEnum.UNAUTHORIZED);
        }
    }*/

    @PostMapping()
    public ResponseEntity<Void> addCart(@RequestBody Cart cart) {

            cartService.addCart(cart);
            return ResponseEntity.status(HttpStatus.CREATED).build();

    }

    @GetMapping("list")
    public ResponseEntity<List<Cart>> queryCartList(@CookieValue("LY_TOKEN") String token) {

            return ResponseEntity.ok(cartService.queryCartList());

        }
     @DeleteMapping("{skuId}")
    public ResponseEntity<Void>deleteCart(@PathVariable("skuId")Long skuId){
         cartService.deleteCart(skuId);
        return ResponseEntity.status(HttpStatus.NO_CONTENT).build();

        }
    }








