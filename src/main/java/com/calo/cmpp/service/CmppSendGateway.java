package com.calo.cmpp.service;

import com.calo.cmpp.domain.CmppSendAccountChannel;
import com.calo.cmpp.domain.SendMessageSubmit;
import com.zx.sms.codec.cmpp.msg.CmppSubmitRequestMessage;
import com.zx.sms.common.util.ChannelUtil;
import com.zx.sms.connect.manager.EndpointConnector;
import com.zx.sms.connect.manager.EndpointManager;
import jakarta.annotation.PostConstruct;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.io.UnsupportedEncodingException;
import java.nio.charset.StandardCharsets;
import java.util.List;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

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
            System.out.println("取出数据异常：" + e.getMessage());
        }
    }

    private void submitMessageSend(CmppSendAccountChannel sendAccountChannel, SendMessageSubmit sendMessageSubmit) throws UnsupportedEncodingException {
        CmppSubmitRequestMessage submitMessage = this.getCmppSubmitRequestMessage(sendAccountChannel, sendMessageSubmit);
        EndpointConnector<?> managerEndpointConnector = manager.getEndpointConnector(sendAccountChannel.getChannelId());
        try {
            List<CmppSubmitRequestMessage> cmppSubmitRequestMessages = ChannelUtil.splitLongSmsMessage(managerEndpointConnector.getEndpointEntity(), submitMessage);
            monitorSendManage.addMessage(sendMessageSubmit);
            cmppSubmitRequestMessages.forEach(row -> {
                monitorSendManage.addSequence(row.getSequenceNo(), sendMessageSubmit.getLocalMessageId());
                if (managerEndpointConnector.getConnectionNum() == 0) {
                    monitorSendManage.delMsgId(row.getSequenceNo());
                } else {
                    managerEndpointConnector.synwriteUncheck(row);
                }
            });
        } catch (Exception e) {
            System.out.println("短信提交失败:" + submitMessage);
        }
    }

    private CmppSubmitRequestMessage getCmppSubmitRequestMessage(CmppSendAccountChannel sendAccountChannel, SendMessageSubmit sendMessageSubmit) throws UnsupportedEncodingException {
        String accessCode = sendAccountChannel.getSrcId() == null ? "" : sendAccountChannel.getSrcId();
        String extend = sendMessageSubmit.getExtend() == null ? "" : sendMessageSubmit.getExtend();
        String srcId = StringUtils.substring(accessCode + extend, 0, 20);
        CmppSubmitRequestMessage message = CmppSubmitRequestMessage.create(sendMessageSubmit.getMobile(), srcId, new String(sendMessageSubmit.getContent().getBytes(), StandardCharsets.UTF_8));
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
