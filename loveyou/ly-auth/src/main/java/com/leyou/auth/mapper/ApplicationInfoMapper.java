package com.leyou.auth.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.leyou.auth.entity.ApplicationInfo;
import org.apache.ibatis.annotations.Param;

import java.util.List;

public interface ApplicationInfoMapper extends BaseMapper<ApplicationInfo> {
    List<String> selectTargetsByServiceName(@Param("serviceName") String serviceName);
}
