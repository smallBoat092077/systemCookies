package com.example.systemcookies.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.example.systemcookies.entity.CartItem;
import org.apache.ibatis.annotations.Delete;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

/**
 * 购物车 Mapper
 */
@Mapper
public interface CartItemMapper extends BaseMapper<CartItem> {

    /**
     * 物理删除单条：购物车条目不做历史留存
     */
    @Delete("DELETE FROM cart_item WHERE user_id = #{userId} AND recipe_id = #{recipeId}")
    int deletePhysically(@Param("userId") Long userId, @Param("recipeId") Long recipeId);

    /**
     * 物理删除指定条目（结算后清理）
     */
    @Delete("""
            <script>
            DELETE FROM cart_item
            WHERE user_id = #{userId}
              AND id IN
              <foreach collection="ids" item="id" open="(" separator="," close=")">#{id}</foreach>
            </script>
            """)
    int deletePhysicallyByIds(@Param("userId") Long userId, @Param("ids") java.util.List<Long> ids);

    /**
     * 清空购物车
     */
    @Delete("DELETE FROM cart_item WHERE user_id = #{userId}")
    int clearByUser(@Param("userId") Long userId);
}
