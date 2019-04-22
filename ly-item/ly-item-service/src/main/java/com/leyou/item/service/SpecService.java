package com.leyou.item.service;

import com.leyou.common.enums.ExceptionEnum;
import com.leyou.common.exception.LyException;
import com.leyou.item.mapper.SpecGroupMapper;
import com.leyou.item.mapper.SpecParamMapper;
import com.leyou.item.pojo.SpecGroup;
import com.leyou.item.pojo.SpecParam;
import org.springframework.beans.factory.annotation.Autowired;

import org.springframework.stereotype.Service;
import org.springframework.util.CollectionUtils;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Service
public class SpecService {
    @Autowired
    private SpecGroupMapper specGroupMapper;
    @Autowired
    private SpecParamMapper specParamMapper;

    public List<SpecGroup> queryGroupByCid(Long cid) {
        SpecGroup specGroup = new SpecGroup();
        specGroup.setCid(cid);
        List<SpecGroup> list = specGroupMapper.select(specGroup);
        if (CollectionUtils.isEmpty(list)) {
            throw new LyException(ExceptionEnum.SPEC_GROUP_NOT_FOUND);
        }
        return list;
    }

    public List<SpecParam> queryParam(Long cid, Long gid, Boolean search) {
        SpecParam specParam = new SpecParam();
        specParam.setCid(cid);
        specParam.setGroupId(gid);
        specParam.setSearching(search);
        List<SpecParam> list = specParamMapper.select(specParam);
        if (CollectionUtils.isEmpty(list)) {
            throw new LyException(ExceptionEnum.SPEC_PARAM_NOT_FOUND);
        }
        return list;

    }


    public List<SpecGroup> querySpecsByCid(Long cid) {
        //查询规格组
        List<SpecGroup> SpecGroups = queryGroupByCid(cid);
        //查询组内参数
        List<SpecParam> specParams = queryParam(cid, null, null);
      //对规格参数分组，分组之后得到Map,key是组id，值为组内参数List
        Map<Long, List<SpecParam>> map = specParams.stream().collect(Collectors.groupingBy(SpecParam::getGroupId));
        for (SpecGroup specGroup : SpecGroups) {
            specGroup.setParams(map.get(specGroup.getId()));
        }

        //循环查库性能不好
      /*  for (SpecGroup specGroup : SpecGroups) {
            List<SpecParam> specParams = queryParam(null, specGroup.getId(), null);
            specGroup.setParams(specParams);
        }*/

        return  SpecGroups;
    }

}
