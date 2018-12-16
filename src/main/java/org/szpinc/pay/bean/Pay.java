package org.szpinc.pay.bean;

import lombok.Data;

import javax.persistence.*;
import java.io.Serializable;
import java.math.BigDecimal;
import java.util.Date;


@Entity
@Table(name = "t_pay")
@Data
public class Pay implements Serializable {

    @Id
    @Column
    private String id;
    @Column(name = "nick_name")
    private String nickName;
    @Column
    private BigDecimal money;
    @Column
    private String info;
    @Column(name = "create_date")
    private Date createDate;
    @Column(name = "update_date")
    private Date updateDate;
    /**
     * 用户通知邮箱
     */
    @Column
    private String email;
    /**
     * 用户测试邮箱
     */
    @Column
    private String testEmail;

    /**
     * 显示状态
     * 0: 待审核
     * 1: 确认显示
     * 2: 驳回
     * 3: 通过不展示
     */
    @Column
    private int status = 0;
    @Column
    private String payType;
    @Column
    private String userName;
    @Transient
    private String time;
    @Transient
    private String passUrl;
    @Transient
    private String backUrl;
    @Transient
    private String passNotShowUrl;
    @Transient
    private String editUrl;
    @Transient
    private String delUrl;
}