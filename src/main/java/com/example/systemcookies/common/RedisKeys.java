package com.example.systemcookies.common;

/**
 * Redis Key 常量
 */
public interface RedisKeys {

    /** 食谱详情：recipe:detail:{recipeId} */
    String RECIPE_DETAIL = "recipe:detail:";

    /** 食谱列表：recipe:list:{查询指纹} */
    String RECIPE_LIST = "recipe:list:";

    /** 食谱烹饪步骤：recipe:steps:{recipeId} */
    String RECIPE_STEPS = "recipe:steps:";

    /** 分类列表：category:list */
    String CATEGORY_LIST = "category:list";

    /** 登录令牌：auth:token:{token} */
    String AUTH_TOKEN = "auth:token:";

    static String authToken(String token) {
        return AUTH_TOKEN + token;
    }

    /** 点菜小助理会话：assistant:session:{userId}:{sessionId} */
    String ASSISTANT_SESSION = "assistant:session:";

    static String assistantSession(Long userId, String sessionId) {
        return ASSISTANT_SESSION + userId + ":" + sessionId;
    }

    static String recipeDetail(Long recipeId) {
        return RECIPE_DETAIL + recipeId;
    }

    static String recipeSteps(Long recipeId) {
        return RECIPE_STEPS + recipeId;
    }
}
