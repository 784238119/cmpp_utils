package com.calo.cmpp.service;


import cn.hutool.core.util.StrUtil;
import com.calo.cmpp.domain.CmppSendAccountChannel;
import com.calo.cmpp.domain.PressureTestRequest;
import com.calo.cmpp.domain.SendMessageSubmit;
import lombok.Getter;
import lombok.extern.log4j.Log4j2;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Lazy;
import org.springframework.stereotype.Service;

import java.util.Timer;
import java.util.TimerTask;
import java.util.concurrent.LinkedBlockingQueue;

@Log4j2
@Service
public class GenerateInfoService {

    private MonitorSendManage monitorSendManage;
    private CmppAccountManage cmppAccountManage;

    // 阻塞队列
    @Getter
    private final LinkedBlockingQueue<SendMessageSubmit> queue = new LinkedBlockingQueue<>();

    private Timer timer = null;

    public synchronized void generateSendSubmitMessage(PressureTestRequest request) {
        CmppSendAccountChannel sendAccountChannel = cmppAccountManage.getAccount(request.getAccountId());

        if (sendAccountChannel == null) {
            System.out.println("没有这个账号！！！！！");
        } else if (request.getSendWay()) {
            this.generateMobilePhoneNumberDataRegularly(sendAccountChannel, request);
        } else {
            this.generateABatchOfMobilePhoneNumberData(sendAccountChannel, request);
        }
    }

    private synchronized void generateABatchOfMobilePhoneNumberData(CmppSendAccountChannel sendAccountChannel, PressureTestRequest request) {
        int sendSize = request.getSendSize();
        while (sendSize > 0) try {
            SendMessageSubmit sendMessageSubmit = monitorSendManage.produceSendMessageSubmit(sendAccountChannel.getChannelId(), request);
            if (sendSize - sendMessageSubmit.getCount() < 0) {
                continue;
            }
            if (request.getVerificationCode()) {
                sendMessageSubmit.setContent(StrUtil.format(sendMessageSubmit.getContent(), sendSize));
            }
            queue.put(sendMessageSubmit);
            monitorSendManage.addMessageCount(sendMessageSubmit.getCount());
            sendSize = sendSize - sendMessageSubmit.getCount();
        } catch (InterruptedException e) {
            System.out.println("加入元素异常");
        }
    }

    private synchronized void generateMobilePhoneNumberDataRegularly(CmppSendAccountChannel sendAccountChannel, PressureTestRequest request) {
        if (timer != null) {
            timer.cancel();
        }
        timer = new Timer();

        TimerTask timerTask = new TimerTask() {
            @Override
            public void run() {
                generateABatchOfMobilePhoneNumberData(sendAccountChannel, request);
            }
        };
        timer.scheduleAtFixedRate(timerTask, request.getPeriodTime(), request.getPeriodTime());

    }

    public synchronized void stopSend() {
        if (timer != null) {
            timer.cancel();
        }
        queue.clear();
    }


    @Lazy
    @Autowired
    public void setMonitorSendManage(MonitorSendManage monitorSendManage) {
        this.monitorSendManage = monitorSendManage;
    }

    @Autowired
    public void setCmppAccountManage(CmppAccountManage cmppAccountManage) {
        this.cmppAccountManage = cmppAccountManage;
    }

}
