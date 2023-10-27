package com.calo.cmpp.domain;

import com.calo.cmpp.enums.CmppType;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.io.Serial;
import java.io.Serializable;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class CmppSendAccountChannel implements Serializable {

    @Serial
    private static final long serialVersionUID = 4300214238350805590L;
    @NotNull
    private String channelId;
    @NotNull
    private String channelHost;
    @NotNull
    private Integer channelPort;
    @NotNull
    private CmppType protocol;
    @NotNull
    private String loginName;
    @NotNull
    private String password;
    @NotNull
    private String srcId;
    @NotNull
    private Integer maxConnect;
    @NotNull
    private Integer connectCount;

    public void printf() {
        System.out.printf("\033[32m" + "%-6s%-12s%-23s%-11s%-11s%-12s%-12s%-10s\n", getChannelId(), connectCount, channelHost + ":" + channelPort, protocol, loginName, password, srcId, maxConnect);
    }
}
