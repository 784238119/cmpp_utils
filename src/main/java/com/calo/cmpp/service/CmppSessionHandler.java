package com.calo.cmpp.service;

import com.zx.sms.codec.cmpp.msg.CmppDeliverRequestMessage;
import com.zx.sms.codec.cmpp.msg.CmppDeliverResponseMessage;
import com.zx.sms.codec.cmpp.msg.CmppSubmitResponseMessage;
import com.zx.sms.handler.api.AbstractBusinessHandler;
import io.netty.channel.ChannelHandler;
import io.netty.channel.ChannelHandlerContext;
import lombok.extern.log4j.Log4j2;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

@Component
@Log4j2
@ChannelHandler.Sharable
public class CmppSessionHandler extends AbstractBusinessHandler {

    private MonitorSendManage monitorSendManage;

    @Override
    public String name() {
        return "CmppSessionHandler";
    }

    @Override
    public void channelRead(ChannelHandlerContext ctx, Object msg) {

        if (msg instanceof CmppSubmitResponseMessage message) {
            monitorSendManage.addMsgId(message.getSequenceNo(), message.getMsgId().toString());
            return;
        }

        if (msg instanceof CmppDeliverRequestMessage message) {
            CmppDeliverResponseMessage responseMessage = new CmppDeliverResponseMessage(message.getHeader().getSequenceId());
            responseMessage.setResult(0);
            responseMessage.setMsgId(message.getMsgId());
            ctx.channel().writeAndFlush(responseMessage);
            monitorSendManage.addReport(message.getReportRequestMessage().getMsgId().toString(), message.getReportRequestMessage().getStat());
            return;
        }

        ctx.fireChannelRead(msg);
    }

    @Autowired
    public void setSendMessageSubmitManage(MonitorSendManage monitorSendManage) {
        this.monitorSendManage = monitorSendManage;
    }
}
