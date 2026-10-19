package com.example.systemcookies.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.example.systemcookies.entity.Recipe;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Update;

/**
 * 食谱 Mapper
 */
@Mapper
public interface RecipeMapper extends BaseMapper<Recipe> {

    /**
     * 扣库存并累加销量：条件更新，库存不足时影响行数为 0
     */
    @Update("UPDATE recipe SET stock = stock - #{quantity}, sales = sales + #{quantity} " +
            "WHERE id = #{id} AND deleted = 0 AND stock >= #{quantity}")
    int deductStock(@Param("id") Long id, @Param("quantity") Integer quantity);

    /**
     * 归还库存（取消订单）
     */
    @Update("UPDATE recipe SET stock = stock + #{quantity} WHERE id = #{id} AND deleted = 0")
    int restoreStock(@Param("id") Long id, @Param("quantity") Integer quantity);
}
