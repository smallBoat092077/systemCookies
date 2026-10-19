package com.example.systemcookies.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.example.systemcookies.entity.CookingTip;
import org.apache.ibatis.annotations.Delete;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

/**
 * 烹饪技巧 Mapper
 */
@Mapper
public interface CookingTipMapper extends BaseMapper<CookingTip> {

    /**
     * 物理删除某食谱的全部技巧
     */
    @Delete("DELETE FROM cooking_tip WHERE recipe_id = #{recipeId}")
    int deletePhysicallyByRecipeId(@Param("recipeId") Long recipeId);
}
