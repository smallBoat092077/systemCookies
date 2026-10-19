package com.example.systemcookies.service;

import com.baomidou.mybatisplus.spring.service.IService;
import com.example.systemcookies.common.PageResult;
import com.example.systemcookies.dto.CheckInReplyCreateDTO;
import com.example.systemcookies.entity.CheckInReply;
import com.example.systemcookies.vo.CheckInReplyVO;

import java.util.List;
import java.util.Map;

/**
 * 打卡回复服务
 */
public interface CheckInReplyService extends IService<CheckInReply> {

    /**
     * 回复某条打卡
     */
    Long create(Long userId, Long checkInId, CheckInReplyCreateDTO dto);

    /**
     * 删除自己的回复
     */
    void removeOwn(Long userId, Long id);

    /**
     * 某条打卡的回复列表（公开，按时间正序）
     */
    PageResult<CheckInReplyVO> pageByCheckIn(
            Long checkInId, Integer pageNum, Integer pageSize, Long currentUserId);

    /**
     * 我收到的回复：别人回复我发布的打卡
     */
    PageResult<CheckInReplyVO> pageInbox(Long userId, Integer pageNum, Integer pageSize);

    /**
     * 我发出的回复
     */
    PageResult<CheckInReplyVO> pageMine(Long userId, Integer pageNum, Integer pageSize);

    /**
     * 批量统计每条打卡的回复数
     */
    Map<Long, Long> countByCheckInIds(List<Long> checkInIds);
}
