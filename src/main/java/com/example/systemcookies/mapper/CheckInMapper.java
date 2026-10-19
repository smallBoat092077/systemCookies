package com.example.systemcookies.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.example.systemcookies.entity.CheckIn;
import org.apache.ibatis.annotations.Mapper;

/**
 * 打卡 Mapper
 */
@Mapper
public interface CheckInMapper extends BaseMapper<CheckIn> {
}
