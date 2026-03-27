package com.university.asset.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.university.asset.entity.AuditRecord;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import java.util.List;

@Mapper
public interface AuditRecordMapper extends BaseMapper<AuditRecord> {
    List<AuditRecord> selectByBiz(@Param("bizType") String bizType, @Param("bizId") Long bizId);
}
