package com.leyou.item.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import com.sun.tracing.dtrace.ArgsAttributes;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.Date;

@Data
@TableName("tb_brand")
public class Brand {

    @TableId(type = IdType.AUTO)
    private Long id;
    private String name;
    private String image;
    private String letter;
    private Date createTime;
    private Date updateTime;
}
