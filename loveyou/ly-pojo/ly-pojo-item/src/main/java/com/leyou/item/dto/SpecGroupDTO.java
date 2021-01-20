package com.leyou.item.dto;

import com.leyou.item.entity.SpecParam;
import lombok.Data;

import java.util.Date;
import java.util.List;

/**
 * 封装所有规格组
 */
@Data
public class SpecGroupDTO {
    private Long id;

    private Long cid;

    private String name;

    private Date createTime;

    private Date updateTime;

    //封装该组内的所有规格参数
    private List<SpecParam> params;
}
