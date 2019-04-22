package com.leyou.item.service;

import com.github.pagehelper.PageHelper;
import com.github.pagehelper.PageInfo;
import com.leyou.common.enums.ExceptionEnum;
import com.leyou.common.exception.LyException;
import com.leyou.common.vo.PageResult;
import com.leyou.item.mapper.BrandMapper;
import com.leyou.item.pojo.Brand;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.CollectionUtils;
import tk.mybatis.mapper.entity.Example;

import java.util.Collection;
import java.util.List;

@Service
public class BrandService {
    @Autowired
    private BrandMapper brandMapper;

    public PageResult<Brand> queryBrandByPage(Integer page, Integer rows, String sortBy, Boolean desc, String key) {
        //1、分页
        PageHelper.startPage(page, rows);
        Example example = new Example(Brand.class);
        Example.Criteria criteria = example.createCriteria();
        //2、关键字过滤
        if (StringUtils.isNotBlank(key)) {
            criteria.andLike("name", "%" + key + "%").orEqualTo("letter", key);
        }
        //3、排序
        if (StringUtils.isNotBlank(sortBy)) {
//            String orderByCase=sortBy + (desc ? " desc" : " asc");
            example.setOrderByClause(sortBy + (desc ? " desc" : " asc"));
        }
        //4、执行，得到结果
        List<Brand> list = brandMapper.selectByExample(example);
        if (CollectionUtils.isEmpty(list)) {
            throw new LyException(ExceptionEnum.BRAND_NOT_FOUND);
        }
        //5处理结果并返回
        PageResult<Brand> pageResult = new PageResult<>();
        PageInfo<Brand> pageInfo = new PageInfo<>(list);
        pageResult.setItems(list);
        pageResult.setTotal(pageInfo.getTotal());
        return pageResult;
    }

    @Transactional
    public void saveBrand(Brand brand, List<Long> cids) {
        //新增品牌
        brand.setId(null);
        int count = brandMapper.insert(brand);
        if (count != 1) {
            throw new LyException(ExceptionEnum.BRAND_EDIT_ERROR);
        }
        //新增中间表
        for (Long cid : cids) {
            count = brandMapper.insertCategoryBrand(cid, brand.getId());
            if (count != 1) {
                throw new LyException(ExceptionEnum.BRAND_EDIT_ERROR);
            }
        }
    }

    public Brand queryById(Long id) {

        Brand brand = brandMapper.selectByPrimaryKey(id);
        if (brand == null) {
            throw new LyException(ExceptionEnum.BRAND_EDIT_ERROR);
        }
        return brand;
    }

    public List<Brand> queryBrandByCid(Long cid) {
        List<Brand> list = brandMapper.queryByCategoryId(cid);
        if (CollectionUtils.isEmpty(list)) {
            throw new LyException(ExceptionEnum.BRAND_EDIT_ERROR);
        }
        return list;
    }

    public List<Brand> queryByIds(List<Long> ids) {
        List<Brand> list = brandMapper.selectByIdList(ids);
        if (CollectionUtils.isEmpty(list)) {
            throw new LyException(ExceptionEnum.BRAND_EDIT_ERROR);
        }
        return list;
    }
}
