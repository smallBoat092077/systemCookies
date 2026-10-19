package com.example.systemcookies.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.example.systemcookies.entity.RecipeSeasoning;
import org.apache.ibatis.annotations.Delete;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

/**
 * 调料清单 Mapper
 */
@Mapper
public interface RecipeSeasoningMapper extends BaseMapper<RecipeSeasoning> {

    /**
     * 物理删除某食谱的全部调料
     */
    @Delete("DELETE FROM recipe_seasoning WHERE recipe_id = #{recipeId}")
    int deletePhysicallyByRecipeId(@Param("recipeId") Long recipeId);
}
