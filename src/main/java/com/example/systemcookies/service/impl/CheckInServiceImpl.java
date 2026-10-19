package com.example.systemcookies.service.impl;

import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.spring.service.impl.ServiceImpl;
import com.example.systemcookies.common.BusinessException;
import com.example.systemcookies.common.PageResult;
import com.example.systemcookies.dto.CheckInCreateDTO;
import com.example.systemcookies.entity.CheckIn;
import com.example.systemcookies.entity.Recipe;
import com.example.systemcookies.entity.User;
import com.example.systemcookies.mapper.CheckInMapper;
import com.example.systemcookies.mapper.RecipeMapper;
import com.example.systemcookies.mapper.UserMapper;
import com.example.systemcookies.service.CheckInReplyService;
import com.example.systemcookies.service.CheckInService;
import com.example.systemcookies.vo.CheckInVO;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.Arrays;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.stream.Collectors;

/**
 * 打卡 / 评论服务实现
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class CheckInServiceImpl extends ServiceImpl<CheckInMapper, CheckIn> implements CheckInService {

    private static final int MAX_IMAGES = 9;

    /** 只允许提交本站上传地址，避免外链与伪造路径 */
    private static final String IMAGE_PREFIX = "/uploads/";

    private static final DateTimeFormatter TIME_FORMAT = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm");

    private final UserMapper userMapper;
    private final RecipeMapper recipeMapper;
    private final CheckInReplyService checkInReplyService;

    @Override
    public Long create(Long userId, CheckInCreateDTO dto) {
        Recipe recipe = recipeMapper.selectById(dto.getRecipeId());
        if (recipe == null || Integer.valueOf(1).equals(recipe.getDeleted())) {
            throw new BusinessException("菜谱不存在或已下架");
        }
        String content = dto.getContent() == null ? "" : dto.getContent().trim();
        List<String> images = normalizeImages(dto.getImages());
        if (!StringUtils.hasText(content) && images.isEmpty()) {
            throw new BusinessException("写点心得或上传图片再打卡吧");
        }

        CheckIn checkIn = new CheckIn();
        checkIn.setUserId(userId);
        checkIn.setRecipeId(dto.getRecipeId());
        checkIn.setContent(content);
        checkIn.setImages(images.isEmpty() ? null : String.join(",", images));
        save(checkIn);
        log.info("用户 {} 打卡菜谱 {}（图片 {} 张）", userId, dto.getRecipeId(), images.size());
        return checkIn.getId();
    }

    @Override
    public void removeOwn(Long userId, Long id) {
        CheckIn checkIn = getById(id);
        if (checkIn == null) {
            throw new BusinessException("打卡不存在");
        }
        if (!Objects.equals(checkIn.getUserId(), userId)) {
            throw new BusinessException("只能删除自己的打卡");
        }
        removeById(id);
    }

    @Override
    public PageResult<CheckInVO> pageByRecipe(
            Long recipeId, Integer pageNum, Integer pageSize, Long currentUserId) {
        Page<CheckIn> page = lambdaQuery()
                .eq(CheckIn::getRecipeId, recipeId)
                .orderByDesc(CheckIn::getCreateTime)
                .page(new Page<>(pageNum, pageSize));
        return buildResult(page, currentUserId);
    }

    @Override
    public PageResult<CheckInVO> pageMine(Long userId, Integer pageNum, Integer pageSize) {
        Page<CheckIn> page = lambdaQuery()
                .eq(CheckIn::getUserId, userId)
                .orderByDesc(CheckIn::getCreateTime)
                .page(new Page<>(pageNum, pageSize));
        return buildResult(page, userId);
    }

    @Override
    public Long countByRecipe(Long recipeId) {
        return lambdaQuery().eq(CheckIn::getRecipeId, recipeId).count();
    }

    @Override
    public Long todayCount(Long userId) {
        LocalDateTime start = LocalDate.now().atStartOfDay();
        return lambdaQuery()
                .eq(CheckIn::getUserId, userId)
                .ge(CheckIn::getCreateTime, start)
                .count();
    }

    /** 分页查完后批量补齐用户与菜谱信息，避免逐条查库 */
    private PageResult<CheckInVO> buildResult(IPage<CheckIn> page, Long currentUserId) {
        List<CheckIn> records = page.getRecords();
        Map<Long, User> userMap = loadUsers(records);
        Map<Long, Recipe> recipeMap = loadRecipes(records);
        Map<Long, Long> replyCounts =
                checkInReplyService.countByCheckInIds(records.stream().map(CheckIn::getId).toList());
        List<CheckInVO> vos = records.stream()
                .map(item -> toVO(item, userMap, recipeMap, replyCounts, currentUserId))
                .toList();

        PageResult<CheckInVO> result = new PageResult<>();
        result.setRecords(vos);
        result.setTotal(page.getTotal());
        result.setPageNum(page.getCurrent());
        result.setPageSize(page.getSize());
        result.setPages(page.getPages());
        return result;
    }

    private Map<Long, User> loadUsers(List<CheckIn> records) {
        List<Long> ids = records.stream().map(CheckIn::getUserId).distinct().toList();
        if (ids.isEmpty()) {
            return Map.of();
        }
        return userMapper.selectBatchIds(ids).stream()
                .collect(Collectors.toMap(User::getId, item -> item, (a, b) -> a));
    }

    private Map<Long, Recipe> loadRecipes(List<CheckIn> records) {
        List<Long> ids = records.stream().map(CheckIn::getRecipeId).distinct().toList();
        if (ids.isEmpty()) {
            return Map.of();
        }
        return recipeMapper.selectBatchIds(ids).stream()
                .collect(Collectors.toMap(Recipe::getId, item -> item, (a, b) -> a));
    }

    private CheckInVO toVO(
            CheckIn item,
            Map<Long, User> userMap,
            Map<Long, Recipe> recipeMap,
            Map<Long, Long> replyCounts,
            Long currentUserId) {
        CheckInVO vo = new CheckInVO();
        vo.setId(item.getId());
        vo.setUserId(item.getUserId());

        User user = userMap.get(item.getUserId());
        vo.setUserNickname(user == null ? "神秘吃货" : resolveNickname(user));
        vo.setUserAvatar(user == null ? null : user.getAvatar());

        vo.setRecipeId(item.getRecipeId());
        Recipe recipe = recipeMap.get(item.getRecipeId());
        vo.setRecipeName(recipe == null ? null : recipe.getName());
        vo.setRecipeCover(recipe == null ? null : recipe.getCoverUrl());

        vo.setContent(item.getContent());
        vo.setImages(splitImages(item.getImages()));
        vo.setCreateTime(item.getCreateTime() == null ? null : item.getCreateTime().format(TIME_FORMAT));
        vo.setReplyCount(replyCounts.getOrDefault(item.getId(), 0L));
        vo.setMine(currentUserId != null && currentUserId.equals(item.getUserId()));
        return vo;
    }

    private String resolveNickname(User user) {
        return StringUtils.hasText(user.getNickname()) ? user.getNickname() : user.getUsername();
    }

    private List<String> splitImages(String images) {
        if (!StringUtils.hasText(images)) {
            return List.of();
        }
        return Arrays.stream(images.split(",")).filter(StringUtils::hasText).toList();
    }

    /** 图片地址校验：只允许本站 /uploads/ 路径，最多 9 张 */
    private List<String> normalizeImages(List<String> images) {
        if (images == null || images.isEmpty()) {
            return List.of();
        }
        List<String> list = images.stream()
                .filter(StringUtils::hasText)
                .map(String::trim)
                .distinct()
                .toList();
        if (list.size() > MAX_IMAGES) {
            throw new BusinessException("最多上传 " + MAX_IMAGES + " 张图片");
        }
        if (list.stream().anyMatch(img -> !img.startsWith(IMAGE_PREFIX))) {
            throw new BusinessException("图片地址无效，请重新上传");
        }
        return list;
    }
}
