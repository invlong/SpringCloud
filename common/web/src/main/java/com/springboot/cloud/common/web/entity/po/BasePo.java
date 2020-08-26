package com.springboot.cloud.common.web.entity.po;

import com.baomidou.mybatisplus.annotation.FieldFill;
import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableId;
import lombok.Data;

import java.io.Serializable;
import java.util.Date;

@Data
public class BasePo implements Serializable {
    public final static String DEFAULT_USERNAME = "system";
    @TableId(type = IdType.AUTO)
    private String id;

    @TableField(fill = FieldFill.INSERT)
    private String ctUserId;

    @TableField(fill = FieldFill.INSERT)
    private Date ctDate;

    @TableField(fill = FieldFill.INSERT_UPDATE)
    private String utUserId;

    @TableField(fill = FieldFill.INSERT_UPDATE)
    private Date utDate;
}
