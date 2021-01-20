package com.leyou.order.config;

import com.leyou.common.utils.IdWorker;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

/**
 * 初始化分布式ID配置类
 */
@Configuration
public class IdWorkerConfig {
    
    @Autowired
    private IdWorkProperties idWorkerProps;
    
    @Bean
    public IdWorker createIdWorker(){
        return new IdWorker(
                idWorkerProps.getWorkerId(),
                idWorkerProps.getDataCenterId());
    }
    
}

