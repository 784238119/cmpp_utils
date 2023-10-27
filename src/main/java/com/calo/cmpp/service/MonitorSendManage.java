package com.calo.cmpp.service;

import cn.hutool.cache.Cache;
import cn.hutool.cache.CacheUtil;
import cn.hutool.cache.impl.TimedCache;
import cn.hutool.core.lang.Snowflake;
import cn.hutool.core.util.IdUtil;
import com.calo.cmpp.domain.PressureTestRequest;
import com.calo.cmpp.domain.SendMessageSubmit;
import com.calo.cmpp.util.MySystemFunction;
import com.calo.cmpp.util.RandomMessageContent;
import com.calo.cmpp.util.RandomPhoneNumber;
import lombok.Getter;
import lombok.extern.log4j.Log4j2;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import java.text.NumberFormat;
import java.util.ArrayList;
import java.util.Random;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicInteger;

@Log4j2
@Component
public class MonitorSendManage {

    private static final int EXPIRATION = 3 * 24 * 60 * 60 * 1000;

    private static final Snowflake snowflake = IdUtil.getSnowflake(1, 1);

    private static final TimedCache<String, String> msgIdCache = CacheUtil.newTimedCache(EXPIRATION);
    private static final TimedCache<Integer, String> suiteCache = CacheUtil.newTimedCache(EXPIRATION);
    private static final Cache<String, SendMessageSubmit> fifoCache = CacheUtil.newFIFOCache(100000000);


    private static final NumberFormat numberFormat = NumberFormat.getInstance();

    private static final TimedCache<String, SendMessageSubmit> messageCache = CacheUtil.newTimedCache(EXPIRATION);

    static {
        numberFormat.setMaximumFractionDigits(2);
    }

    @Getter
    private volatile static long submitSpeed = 0;
    private volatile static long submitOffset = 0;

    @Getter
    private volatile static long responseSpeed = 0;
    private volatile static long responseOffset = 0;

    @Getter
    private volatile static long reportSpeed = 0;
    private volatile static long reportOffset = 0;

    public static final AtomicInteger messageCount = new AtomicInteger(0);
    public static final AtomicInteger submitsCount = new AtomicInteger(0);
    public static final AtomicInteger failureCount = new AtomicInteger(0);
    public static final AtomicInteger successCount = new AtomicInteger(0);
    public static final AtomicInteger answersCount = new AtomicInteger(0);


    @Scheduled(fixedDelay = 1L, timeUnit = TimeUnit.SECONDS)
    private void calculateNumberRecordSecondSpeed() {
        long submitsNewOffset = submitsCount.get();
        submitSpeed = submitsNewOffset - submitOffset;
        submitOffset = submitsNewOffset;

        long responseNewOffset = answersCount.get();
        responseSpeed = responseNewOffset - responseOffset;
        responseOffset = responseNewOffset;

        long reportNewOffset = failureCount.get() + successCount.get();
        reportSpeed = reportNewOffset - reportOffset;
        reportOffset = reportNewOffset;
    }

    public SendMessageSubmit produceSendMessageSubmit(String channelId, boolean isLongSms, PressureTestRequest request) {
        String content;
        if ("1".equals(request.getTemplateCode())) {
            content = request.getTemplateBody();
        } else {
            content = RandomMessageContent.getContentRandom(isLongSms);
        }
        int count = RandomMessageContent.count(content);
        return SendMessageSubmit
                .builder()
                .channelId(channelId)
                .content(content)
                .mobile(RandomPhoneNumber.generateRandomPhoneNumber(request.getOperator()))
                .localMessageId(snowflake.nextIdStr())
                .extend(String.valueOf(new Random().nextInt(10)))
                .msgId(new ArrayList<>())
                .status(new ArrayList<>())
                .count(count)
                .build();
    }

    public void addMessage(SendMessageSubmit sendMessageSubmit) {
        submitsCount.addAndGet(sendMessageSubmit.getCount());
        messageCache.put(sendMessageSubmit.getLocalMessageId(), sendMessageSubmit);
    }

    public void addSequence(int sequence, String localMessageId) {
        suiteCache.put(sequence, localMessageId);
    }

    public void addMsgId(int sequence, String msgId) {
        String localMessageId = suiteCache.get(sequence);
        if (localMessageId == null) {
            return;
        }
        suiteCache.remove(sequence);
        SendMessageSubmit sendMessageSubmit = messageCache.get(localMessageId);
        if (sendMessageSubmit != null) {
            sendMessageSubmit.getMsgId().add(msgId);
        }
        answersCount.incrementAndGet();
        msgIdCache.put(msgId, localMessageId);
    }

    public void addReport(String msgId, String statusCode) {
        String localMessageId = msgIdCache.get(msgId);
        if (localMessageId == null) {
            return;
        }
        msgIdCache.remove(msgId);
        SendMessageSubmit sendMessageSubmit = messageCache.get(localMessageId);
        if ("DELIVRD".equals(statusCode)) {
            successCount.getAndIncrement();
        } else {
            failureCount.getAndIncrement();
        }
        sendMessageSubmit.getStatus().add(statusCode);
        fifoCache.put(msgId, sendMessageSubmit);
    }

    public void printMonitoringData() {
        MySystemFunction.clean();
        int submitsNum = submitsCount.get();
        int messageNum = messageCount.get();
        int answersNum = answersCount.get();
        int successNum = successCount.get();
        int failureNum = failureCount.get();
        String responseRate = "\033[1;95m" + (answersNum == 0 ? "00.00" : numberFormat.format((double) answersNum / (double) submitsNum * 100.00)) + "%\033[m";
        String reportingRate = "\033[1;95m" + (failureNum + successNum == 0 ? "00.00" : numberFormat.format((double) (successNum + failureNum) / (double) submitsNum * 100.00)) + "%\033[m";
        String successRate = "\033[1;95m" + (successNum == 0 ? "00.00" : numberFormat.format((double) successNum / (double) answersNum * 100.00)) + "%\033[m";
        String failureRate = "\033[1;95m" + (failureNum == 0 ? "00.00" : numberFormat.format((double) failureNum / (double) answersNum * 100.00)) + "%\033[m";
        String unknownRate = "\033[1;95m" + (answersNum - successNum - failureNum == 0 ? "00.00" : numberFormat.format((double) (answersNum - failureNum - successNum) / (double) answersNum * 100.00)) + "%\033[m";
        System.out.println("\033[1;94m----------------------------------------- 监控发送 ---------------------------------------------\033[m");
        System.out.printf("%-5s%-26s%-5s%-25s%-5s%-5s\n", "生成消息数: \033[1;95m", messageNum, "\033[m提交消息数: \033[1;95m", submitsNum, "\033[m提交速度: \033[1;95m", submitSpeed + "/s\033[m");
        System.out.printf("%-5s%-26s%-5s%-25s%-5s%-5s\n", "响应消息数: \033[1;95m", answersNum, "\033[m报告消息数: \033[1;95m", successNum + failureNum, "\033[m响应速度: \033[1;95m", responseSpeed + "/s\033[m");
        System.out.printf("%-5s%-26s%-5s%-25s%-5s%-5s\n", "成功消息数: \033[1;95m", successNum, "\033[m失败消息数: \033[1;95m", failureNum, "\033[m报告速度: \033[1;95m", reportSpeed + "/s\033[m");
        System.out.printf("%-6s%-20s%-6s%-20s%-6s%-20s%-6s%-20s%-6s%-5s\n", "响应率: ", responseRate, "报告率: ", reportingRate, "成功率: ", successRate, "失败率: ", failureRate, "未知率: ", unknownRate);
        System.out.println("\033[1;94m------------------------------------------------------------------------------------------------\033[m");
    }

    public void querySendMessageInfo(String nextLine) {
        SendMessageSubmit sendMessageSubmit = fifoCache.get(nextLine);
        if (sendMessageSubmit == null) {
            System.out.println("没有查询到短信数据！！！");
        } else {
            System.out.println("查询到短信数据MsgId: \033[32m" + sendMessageSubmit + "\033[m");
        }
    }

    public synchronized void clearCacheData() {
        fifoCache.clear();
        messageCount.set(0);
        messageCount.set(0);
        submitsCount.set(0);
        failureCount.set(0);
        successCount.set(0);
        answersCount.set(0);
        submitSpeed = 0;
        submitOffset = 0;
        responseSpeed = 0;
        responseOffset = 0;
        reportSpeed = 0;
        reportOffset = 0;
        fifoCache.clear();
        msgIdCache.clear();
        suiteCache.clear();
    }

    public void addMessageCount(int count) {
        messageCount.addAndGet(count);
    }
}
