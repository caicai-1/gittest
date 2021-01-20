package com.leyou.common.auth.pojo;

import lombok.Data;

import java.util.Date;

/**
 * @author 黑马程序员
 */
@Data
public class Payload<T> {
    private String id;
    private T info;
    private Date expiration;
}