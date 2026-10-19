package com.example.systemcookies.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.example.systemcookies.entity.RecipeIngredient;
import org.apache.ibatis.annotations.Delete;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

/**
 * 食材清单 Mapper
 */
@Mapper
public interface RecipeIngredientMapper extends BaseMapper<RecipeIngredient> {

    /**
     * 物理删除某食谱的全部食材
     */
    @Delete("DELETE FROM recipe_ingredient WHERE recipe_id = #{recipeId}")
    int deletePhysicallyByRecipeId(@Param("recipeId") Long recipeId);
}
