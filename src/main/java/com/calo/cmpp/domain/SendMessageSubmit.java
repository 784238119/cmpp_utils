package com.calo.cmpp.domain;


import lombok.Builder;
import lombok.Data;

import java.util.List;

@Data
@Builder
public class SendMessageSubmit {

    private String channelId;

    private String localMessageId;

    private String mobile;

    private String extend;

    private String content;

    private int count;

    private List<String> msgId;

    private List<String> status;

    @Override
    public String toString() {
        return "[" +
               "channelId='" + channelId + '\'' +
               ", mobile='" + mobile + '\'' +
               ", extend='" + extend + '\'' +
               ", content='" + content + '\'' +
               ", count=" + count +
               ", msgId=" + msgId +
               ", status=" + status +
               ']';
    }
}
