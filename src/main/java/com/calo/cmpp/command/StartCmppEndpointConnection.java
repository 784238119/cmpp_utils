package com.calo.cmpp.command;

import cn.hutool.core.convert.Convert;
import com.calo.cmpp.domain.CmppSendAccountChannel;
import com.calo.cmpp.service.CmppAccountManage;
import com.calo.cmpp.service.CmppSessionHandler;
import com.zx.sms.connect.manager.EndpointEntity;
import com.zx.sms.connect.manager.EndpointManager;
import com.zx.sms.connect.manager.cmpp.CMPPClientEndpointEntity;
import com.zx.sms.handler.api.BusinessHandlerInterface;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.CommandLineRunner;
import org.springframework.context.annotation.Lazy;
import org.springframework.stereotype.Component;

import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.List;

/**
 * 程序启动的时候，加载通道信息
 */

@Component
public class StartCmppEndpointConnection implements CommandLineRunner {

    private final EndpointManager manager = EndpointManager.INS;
    private CmppAccountManage cmppAccountManage;
    private CmppSessionHandler cmppSessionHandler;
    private CommandController commandController;

    //加载cmpp初始化
    @Override
    public void run(String... args) {
        Thread thread;
        try {
            thread = new Thread(() -> commandController.bootMenuFunction());
            thread.setDaemon(true);
            thread.start();
        } catch (Exception e) {
            System.out.println("启动菜单异常：" + e.getMessage());
        }
        List<CmppSendAccountChannel> cmppAccounts = cmppAccountManage.getAccountAll();
        if (cmppAccounts == null || cmppAccounts.isEmpty()) return;
        for (CmppSendAccountChannel cmppAccount : cmppAccounts) {
            EndpointEntity cmppClient = this.getCmppClient(cmppAccount);
            manager.openEndpoint(cmppClient);
        }
        manager.startConnectionCheckTask();
    }

    public EndpointEntity openEndpoint(CmppSendAccountChannel cmppAccount) {
        EndpointEntity cmppClient = this.getCmppClient(cmppAccount);
        manager.openEndpoint(cmppClient);
        return cmppClient;
    }

    public EndpointEntity getCmppClient(CmppSendAccountChannel cmppAccount) {
        CMPPClientEndpointEntity client = new CMPPClientEndpointEntity();
        client.setId(cmppAccount.getChannelId());
        client.setHost(cmppAccount.getChannelHost());
        client.setPort(cmppAccount.getChannelPort());
        client.setChartset(StandardCharsets.UTF_8);
        client.setGroupName(cmppAccount.getChannelId());
        client.setUserName(cmppAccount.getLoginName());
        client.setPassword(cmppAccount.getPassword());
        client.setSpCode(cmppAccount.getSrcId());
        client.setServiceId(cmppAccount.getSrcId());
        client.setMsgSrc(cmppAccount.getLoginName());
        client.setMaxChannels(Convert.toShort(cmppAccount.getMaxConnect()));
        client.setCloseWhenRetryFailed(false);
        client.setVersion(cmppAccount.getProtocol().getValue());
        client.setRetryWaitTimeSec((short) 30);
        client.setUseSSL(false);
        client.setWriteLimit(50000);
        client.setReadLimit(50000);
        client.setReSendFailMsg(false);
        client.setSupportLongmsg(EndpointEntity.SupportLongMessage.BOTH);
        List<BusinessHandlerInterface> clienthandlers = new ArrayList<>();
        clienthandlers.add(cmppSessionHandler);
        client.setBusinessHandlerSet(clienthandlers);
        return client;
    }

    @Autowired
    public void setCmppAccountManage(CmppAccountManage cmppAccountManage) {
        this.cmppAccountManage = cmppAccountManage;
    }

    @Autowired
    public void setCmppSessionConnectedHandler(CmppSessionHandler cmppSessionHandler) {
        this.cmppSessionHandler = cmppSessionHandler;
    }

    @Lazy
    @Autowired
    public void setCommandController(CommandController commandController) {
        this.commandController = commandController;
    }
}
