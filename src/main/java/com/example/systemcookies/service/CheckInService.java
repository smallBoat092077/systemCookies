package com.example.systemcookies.service;

import com.baomidou.mybatisplus.spring.service.IService;
import com.example.systemcookies.common.PageResult;
import com.example.systemcookies.dto.CheckInCreateDTO;
import com.example.systemcookies.entity.CheckIn;
import com.example.systemcookies.vo.CheckInVO;

/**
 * 打卡 / 菜谱评论服务
 */
public interface CheckInService extends IService<CheckIn> {

    /**
     * 发布打卡
     */
    Long create(Long userId, CheckInCreateDTO dto);

    /**
     * 删除自己的打卡
     */
    void removeOwn(Long userId, Long id);

    /**
     * 某道菜的评论（公开，按时间倒序）
     */
    PageResult<CheckInVO> pageByRecipe(Long recipeId, Integer pageNum, Integer pageSize, Long currentUserId);

    /**
     * 我的打卡
     */
    PageResult<CheckInVO> pageMine(Long userId, Integer pageNum, Integer pageSize);

    /**
     * 某道菜的评论总数
     */
    Long countByRecipe(Long recipeId);

    /**
     * 今日打卡次数
     */
    Long todayCount(Long userId);
}
