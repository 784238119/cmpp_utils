package com.calo.cmpp.service;

import com.calo.cmpp.domain.CmppSendAccountChannel;
import com.calo.cmpp.domain.SendMessageSubmit;
import com.zx.sms.codec.cmpp.msg.CmppSubmitRequestMessage;
import com.zx.sms.common.util.ChannelUtil;
import com.zx.sms.connect.manager.EndpointConnector;
import com.zx.sms.connect.manager.EndpointManager;
import jakarta.annotation.PostConstruct;
import lombok.extern.log4j.Log4j2;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

@Log4j2
@Service
public class CmppSendGateway {

    private final EndpointManager manager = EndpointManager.INS;
    private MonitorSendManage monitorSendManage;
    private GenerateInfoService generateInfoService;
    private CmppAccountManage cmppAccountManage;
    private final static ExecutorService consumerThread = Executors.newFixedThreadPool(64);


    @PostConstruct
    public void init() {
        for (int i = 0; i < 64; i++) {
            consumerThread.execute(this::getMessageToSend);
        }
    }

    private void getMessageToSend() {
        while (true) try {
            SendMessageSubmit messageSubmit = generateInfoService.getQueue().take();
            CmppSendAccountChannel account = cmppAccountManage.getAccount(messageSubmit.getChannelId());
            this.submitMessageSend(account, messageSubmit);
        } catch (Exception e) {
            log.error("取出数据异常：" + e.getMessage());
        }
    }

    private void submitMessageSend(CmppSendAccountChannel sendAccountChannel, SendMessageSubmit sendMessageSubmit) {
        CmppSubmitRequestMessage submitMessage = this.getCmppSubmitRequestMessage(sendAccountChannel, sendMessageSubmit);
        EndpointConnector<?> managerEndpointConnector = manager.getEndpointConnector(sendAccountChannel.getChannelId());
        try {
            List<CmppSubmitRequestMessage> cmppSubmitRequestMessages = ChannelUtil.splitLongSmsMessage(managerEndpointConnector.getEndpointEntity(), submitMessage);
            cmppSubmitRequestMessages.forEach(row -> monitorSendManage.addSequence(row.getSequenceNo(), sendMessageSubmit.getLocalMessageId()));
            managerEndpointConnector.synwriteUncheck(cmppSubmitRequestMessages);
            monitorSendManage.addMessage(sendMessageSubmit);
            System.out.println(sendMessageSubmit);
        } catch (Exception e) {
            System.out.println("短信提交失败:" + submitMessage);
        }
    }

    private CmppSubmitRequestMessage getCmppSubmitRequestMessage(CmppSendAccountChannel sendAccountChannel, SendMessageSubmit sendMessageSubmit) {
        String accessCode = sendAccountChannel.getSrcId() == null ? "" : sendAccountChannel.getSrcId();
        String extend = sendMessageSubmit.getExtend() == null ? "" : sendMessageSubmit.getExtend();
        String srcId = StringUtils.substring(accessCode + extend, 0, 20);
        CmppSubmitRequestMessage message = CmppSubmitRequestMessage.create(sendMessageSubmit.getMobile(), srcId, sendMessageSubmit.getContent());
        message.setRegisteredDelivery((short) 1);
        message.setMsgsrc(sendAccountChannel.getLoginName());
        return message;
    }

    @Autowired
    public void setMonitorSendManage(MonitorSendManage monitorSendManage) {
        this.monitorSendManage = monitorSendManage;
    }

    @Autowired
    public void setCmppAccountManage(CmppAccountManage cmppAccountManage) {
        this.cmppAccountManage = cmppAccountManage;
    }

    @Autowired
    public void setGenerateInfoService(GenerateInfoService generateInfoService) {
        this.generateInfoService = generateInfoService;
    }
}
