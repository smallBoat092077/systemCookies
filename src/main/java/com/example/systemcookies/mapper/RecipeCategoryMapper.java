package com.example.systemcookies.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.example.systemcookies.entity.RecipeCategory;
import org.apache.ibatis.annotations.Mapper;

/**
 * 食谱分类 Mapper
 */
@Mapper
public interface RecipeCategoryMapper extends BaseMapper<RecipeCategory> {
}
