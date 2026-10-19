package com.example.systemcookies.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.example.systemcookies.entity.CookingStep;
import org.apache.ibatis.annotations.Delete;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

/**
 * 烹饪步骤 Mapper
 */
@Mapper
public interface CookingStepMapper extends BaseMapper<CookingStep> {

    /**
     * 物理删除某食谱的全部步骤：子表随主表全量替换，不需要保留历史
     */
    @Delete("DELETE FROM cooking_step WHERE recipe_id = #{recipeId}")
    int deletePhysicallyByRecipeId(@Param("recipeId") Long recipeId);
}
