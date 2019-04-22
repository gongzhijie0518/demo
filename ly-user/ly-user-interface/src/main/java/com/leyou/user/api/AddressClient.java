package com.leyou.user.api;

import com.leyou.user.dto.AddressDTO;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;

@FeignClient("user-service")
public interface AddressClient {

    @GetMapping("addr/{id}")
    AddressDTO queryAddressById(@PathVariable("id") Long id);
}