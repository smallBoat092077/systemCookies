package com.example.systemcookies.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.spring.service.impl.ServiceImpl;
import com.example.systemcookies.common.BusinessException;
import com.example.systemcookies.common.PageResult;
import com.example.systemcookies.dto.CheckInReplyCreateDTO;
import com.example.systemcookies.entity.CheckIn;
import com.example.systemcookies.entity.CheckInReply;
import com.example.systemcookies.entity.Recipe;
import com.example.systemcookies.entity.User;
import com.example.systemcookies.mapper.CheckInMapper;
import com.example.systemcookies.mapper.CheckInReplyMapper;
import com.example.systemcookies.mapper.RecipeMapper;
import com.example.systemcookies.mapper.UserMapper;
import com.example.systemcookies.service.CheckInReplyService;
import com.example.systemcookies.vo.CheckInReplyVO;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;

import java.time.format.DateTimeFormatter;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Set;
import java.util.stream.Collectors;

/**
 * 打卡回复服务实现
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class CheckInReplyServiceImpl
        extends ServiceImpl<CheckInReplyMapper, CheckInReply> implements CheckInReplyService {

    private static final DateTimeFormatter TIME_FORMAT = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm");

    private final CheckInMapper checkInMapper;
    private final UserMapper userMapper;
    private final RecipeMapper recipeMapper;

    @Override
    public Long create(Long userId, Long checkInId, CheckInReplyCreateDTO dto) {
        CheckIn checkIn = checkInMapper.selectById(checkInId);
        if (checkIn == null) {
            throw new BusinessException("打卡不存在或已删除");
        }

        CheckInReply reply = new CheckInReply();
        reply.setCheckInId(checkInId);
        reply.setUserId(userId);
        reply.setContent(dto.getContent().trim());

        // 没指定回复对象时默认回复打卡作者；自己回复自己不记录被回复人
        Long replyTo = dto.getReplyToUserId();
        if (replyTo == null && !Objects.equals(checkIn.getUserId(), userId)) {
            replyTo = checkIn.getUserId();
        }
        if (Objects.equals(replyTo, userId)) {
            replyTo = null;
        }
        reply.setReplyToUserId(replyTo);

        save(reply);
        log.info("用户 {} 回复打卡 {}", userId, checkInId);
        return reply.getId();
    }

    @Override
    public void removeOwn(Long userId, Long id) {
        CheckInReply reply = getById(id);
        if (reply == null) {
            throw new BusinessException("回复不存在");
        }
        if (!Objects.equals(reply.getUserId(), userId)) {
            throw new BusinessException("只能删除自己的回复");
        }
        removeById(id);
    }

    @Override
    public PageResult<CheckInReplyVO> pageByCheckIn(
            Long checkInId, Integer pageNum, Integer pageSize, Long currentUserId) {
        Page<CheckInReply> page = lambdaQuery()
                .eq(CheckInReply::getCheckInId, checkInId)
                .orderByAsc(CheckInReply::getCreateTime)
                .page(new Page<>(pageNum, pageSize));
        return buildResult(page, currentUserId, false);
    }

    @Override
    public PageResult<CheckInReplyVO> pageInbox(Long userId, Integer pageNum, Integer pageSize) {
        List<Long> myCheckInIds = checkInMapper.selectList(
                        new LambdaQueryWrapper<CheckIn>()
                                .select(CheckIn::getId)
                                .eq(CheckIn::getUserId, userId))
                .stream()
                .map(CheckIn::getId)
                .toList();
        if (myCheckInIds.isEmpty()) {
            return emptyResult(pageNum, pageSize);
        }
        Page<CheckInReply> page = lambdaQuery()
                .in(CheckInReply::getCheckInId, myCheckInIds)
                .ne(CheckInReply::getUserId, userId)
                .orderByDesc(CheckInReply::getCreateTime)
                .page(new Page<>(pageNum, pageSize));
        return buildResult(page, userId, true);
    }

    @Override
    public PageResult<CheckInReplyVO> pageMine(Long userId, Integer pageNum, Integer pageSize) {
        Page<CheckInReply> page = lambdaQuery()
                .eq(CheckInReply::getUserId, userId)
                .orderByDesc(CheckInReply::getCreateTime)
                .page(new Page<>(pageNum, pageSize));
        return buildResult(page, userId, true);
    }

    @Override
    public Map<Long, Long> countByCheckInIds(List<Long> checkInIds) {
        if (checkInIds == null || checkInIds.isEmpty()) {
            return Map.of();
        }
        List<Map<String, Object>> rows = baseMapper.selectMaps(
                new QueryWrapper<CheckInReply>()
                        .select("check_in_id", "COUNT(*) AS cnt")
                        .in("check_in_id", checkInIds)
                        .groupBy("check_in_id"));
        Map<Long, Long> result = new HashMap<>();
        for (Map<String, Object> row : rows) {
            Object key = row.get("check_in_id");
            Object value = row.get("cnt");
            if (key != null && value != null) {
                result.put(((Number) key).longValue(), ((Number) value).longValue());
            }
        }
        return result;
    }

    private PageResult<CheckInReplyVO> buildResult(
            IPage<CheckInReply> page, Long currentUserId, boolean withContext) {
        List<CheckInReply> records = page.getRecords();
        Map<Long, User> userMap = loadUsers(records);
        Map<Long, CheckIn> checkInMap = withContext ? loadCheckIns(records) : Map.of();
        Map<Long, Recipe> recipeMap = withContext ? loadRecipes(checkInMap.values()) : Map.of();

        List<CheckInReplyVO> vos = records.stream()
                .map(item -> toVO(item, userMap, checkInMap, recipeMap, currentUserId))
                .toList();

        PageResult<CheckInReplyVO> result = new PageResult<>();
        result.setRecords(vos);
        result.setTotal(page.getTotal());
        result.setPageNum(page.getCurrent());
        result.setPageSize(page.getSize());
        result.setPages(page.getPages());
        return result;
    }

    private PageResult<CheckInReplyVO> emptyResult(Integer pageNum, Integer pageSize) {
        PageResult<CheckInReplyVO> result = new PageResult<>();
        result.setRecords(List.of());
        result.setTotal(0L);
        result.setPageNum(Long.valueOf(pageNum));
        result.setPageSize(Long.valueOf(pageSize));
        result.setPages(0L);
        return result;
    }

    private Map<Long, User> loadUsers(List<CheckInReply> records) {
        Set<Long> ids = new HashSet<>();
        for (CheckInReply item : records) {
            ids.add(item.getUserId());
            if (item.getReplyToUserId() != null) {
                ids.add(item.getReplyToUserId());
            }
        }
        if (ids.isEmpty()) {
            return Map.of();
        }
        return userMapper.selectBatchIds(ids).stream()
                .collect(Collectors.toMap(User::getId, item -> item, (a, b) -> a));
    }

    private Map<Long, CheckIn> loadCheckIns(List<CheckInReply> records) {
        List<Long> ids = records.stream().map(CheckInReply::getCheckInId).distinct().toList();
        if (ids.isEmpty()) {
            return Map.of();
        }
        return checkInMapper.selectBatchIds(ids).stream()
                .collect(Collectors.toMap(CheckIn::getId, item -> item, (a, b) -> a));
    }

    private Map<Long, Recipe> loadRecipes(java.util.Collection<CheckIn> checkIns) {
        if (checkIns == null || checkIns.isEmpty()) {
            return Map.of();
        }
        List<Long> ids = checkIns.stream().map(CheckIn::getRecipeId).distinct().toList();
        return recipeMapper.selectBatchIds(ids).stream()
                .collect(Collectors.toMap(Recipe::getId, item -> item, (a, b) -> a));
    }

    private CheckInReplyVO toVO(
            CheckInReply item,
            Map<Long, User> userMap,
            Map<Long, CheckIn> checkInMap,
            Map<Long, Recipe> recipeMap,
            Long currentUserId) {
        CheckInReplyVO vo = new CheckInReplyVO();
        vo.setId(item.getId());
        vo.setCheckInId(item.getCheckInId());
        vo.setUserId(item.getUserId());

        User user = userMap.get(item.getUserId());
        vo.setUserNickname(user == null ? "神秘吃货" : resolveNickname(user));
        vo.setUserAvatar(user == null ? null : user.getAvatar());

        vo.setReplyToUserId(item.getReplyToUserId());
        if (item.getReplyToUserId() != null) {
            User target = userMap.get(item.getReplyToUserId());
            vo.setReplyToNickname(target == null ? null : resolveNickname(target));
        }

        vo.setContent(item.getContent());
        vo.setCreateTime(item.getCreateTime() == null ? null : item.getCreateTime().format(TIME_FORMAT));
        vo.setMine(currentUserId != null && currentUserId.equals(item.getUserId()));

        CheckIn checkIn = checkInMap.get(item.getCheckInId());
        if (checkIn != null) {
            vo.setCheckInContent(checkIn.getContent());
            vo.setCheckInRecipeId(checkIn.getRecipeId());
            Recipe recipe = recipeMap.get(checkIn.getRecipeId());
            vo.setCheckInRecipeName(recipe == null ? null : recipe.getName());
        }
        return vo;
    }

    private String resolveNickname(User user) {
        return StringUtils.hasText(user.getNickname()) ? user.getNickname() : user.getUsername();
    }
}
