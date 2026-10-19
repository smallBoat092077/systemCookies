package com.example.systemcookies.service.impl;

import com.baomidou.mybatisplus.spring.service.impl.ServiceImpl;
import com.example.systemcookies.entity.CookingTip;
import com.example.systemcookies.mapper.CookingTipMapper;
import com.example.systemcookies.service.CookingTipService;
import org.springframework.stereotype.Service;
import org.springframework.util.CollectionUtils;

import java.util.List;

/**
 * 烹饪技巧服务实现
 */
@Service
public class CookingTipServiceImpl extends ServiceImpl<CookingTipMapper, CookingTip>
        implements CookingTipService {

    @Override
    public List<CookingTip> listByRecipeId(Long recipeId) {
        return lambdaQuery()
                .eq(CookingTip::getRecipeId, recipeId)
                .orderByAsc(CookingTip::getSort)
                .orderByAsc(CookingTip::getId)
                .list();
    }

    @Override
    public void replaceByRecipe(Long recipeId, List<CookingTip> tips) {
        getBaseMapper().deletePhysicallyByRecipeId(recipeId);
        if (CollectionUtils.isEmpty(tips)) {
            return;
        }
        tips.forEach(item -> {
            item.setId(null);
            item.setRecipeId(recipeId);
        });
        saveBatch(tips);
    }
}
