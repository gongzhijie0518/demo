package com.leyou.item.mapper;

import com.leyou.item.pojo.Brand;
import org.apache.ibatis.annotations.Insert;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;
import tk.mybatis.mapper.common.Mapper;

import java.util.List;

public interface BrandMapper extends Mapper<Brand>{
    @Insert("insert into tb_category_brand values(#{cid},#{bid})")
    int insertCategoryBrand(@Param("cid")Long cid,@Param("bid")Long pid);

    @Select("select b.id,b.name,b.image,b.letter from tb_brand b, tb_category_brand c " +
            "where c.brand_id=b.id and c.category_id=#{cid}")
    List<Brand> queryByCategoryId(@Param("cid")Long cid);
}
