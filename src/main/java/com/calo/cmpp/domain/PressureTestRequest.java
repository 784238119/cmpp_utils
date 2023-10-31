package com.calo.cmpp.domain;

import lombok.Data;

@Data
public class PressureTestRequest {
    /**
     * 账号ID
     */
    private String accountId;
    /**
     * 本次请求发送总数
     */
    private Integer sendSize;
    /**
     * 间隔时间
     */
    private Integer periodTime;
    /**
     * 持续发送、随机间断发送
     */
    private Boolean isLongTime = false;

    private String operator;

    private String templateCode;

    private String templateBody;

    private Boolean verificationCode;
}
