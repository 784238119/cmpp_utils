package com.calo.cmpp.command;

import com.calo.cmpp.domain.CmppSendAccountChannel;
import com.calo.cmpp.domain.PressureTestRequest;
import com.calo.cmpp.enums.CmppType;
import com.calo.cmpp.service.CmppAccountManage;
import com.calo.cmpp.service.GenerateInfoService;
import com.calo.cmpp.service.MonitorSendManage;
import com.calo.cmpp.util.KeyboardCommandUtil;
import com.calo.cmpp.util.MySystemFunction;
import com.zx.sms.connect.manager.EndpointConnector;
import com.zx.sms.connect.manager.EndpointManager;
import lombok.extern.log4j.Log4j2;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.Scanner;
import java.util.concurrent.TimeUnit;

@Component
@Log4j2
public class CommandController {

    private StartCmppEndpointConnection startCmppEndpointConnection;
    private CmppAccountManage cmppAccountManage;
    private final EndpointManager manager = EndpointManager.INS;

    private GenerateInfoService generateInfoService;
    private MonitorSendManage monitorSendManage;

    private static volatile boolean isOut = false;

    private static final Scanner scanner = new Scanner(System.in);

    public void bootMenuFunction() {
        do {
            MySystemFunction.clean();
            this.showMenu();
            System.out.print("** 选择功能：");

            switch (scanner.nextLine()) {
                case "" -> {}
                case "1" -> this.addAccountCmpp();
                case "2" -> this.delAccount();
                case "3" -> this.queSendTask();
                case "4" -> this.executeTask(false, false);
                case "5" -> this.executeTask(true, false);
                case "6" -> this.executeTask(false, true);
                case "7" -> this.queryMsgId();
                case "8" -> this.clearCacheData();
                case "9" -> this.rveSendTask();
                case "0" -> this.quit();
                default -> {
                    System.out.println("\033[1;93m程序没有该功能，你是不是找茬, 给你一次机会，回车！！！！！！！！！\033[m");
                    scanner.nextLine();
                }
            }
        } while (true);
    }

    private void clearCacheData() {
        System.out.print("清除缓存数据,建议不要在发送的时候清除，这样会导致数据错误（yes）：");
        String ok = CommandController.scanner.nextLine();
        if ("yes".equalsIgnoreCase(ok)) {
            monitorSendManage.clearCacheData();
        }
    }

    private void queryMsgId() {
        System.out.print("输入需要查询的MsgId：");
        String nextLine = CommandController.scanner.nextLine();
        monitorSendManage.querySendMessageInfo(nextLine);
        CommandController.scanner.nextLine();
    }

    private void showMenu() {
        this.queAccount();
        System.out.println("\033[1;94m------------------------------------------ 功能菜单列表命令 ---------------------------------------\033[m");
        System.out.println("\033[33m[1] \033[1;94m添加——发送账号配置\033[m                  \033[33m[2]\033[m \033[1;94m删除——发送账号配置\033[m                \033[33m[3]\033[m \033[1;94m查询——发送任务参数\033[m");
        System.out.println("\033[33m[4] \033[1;94m执行——批次任务发送\033[m                  \033[33m[5]\033[m \033[1;94m执行——持续任务发送\033[m                \033[33m[6]\033[m \033[1;94m发送——发送批验证码\033[m");
        System.out.println("\033[33m[7] \033[1;94m查询——数据详细信息\033[m                  \033[33m[8]\033[m \033[1;94m清理——清除缓存数据\033[m                \033[33m[9]\033[m \033[1;94m停止——全部发送执行\033[m");
        System.out.println("\033[1;94m------------------------------------------\033[m\033[31m 退出程序输入：0 \033[m\033[1;94m----------------------------------------\033[m");
    }

    private void quit() {
        System.out.print("是否确认退出程序 (yes) ：");
        String nextLine = CommandController.scanner.nextLine();
        if ("yes".equalsIgnoreCase(nextLine)) {
            System.exit(0);
        }
    }

    private void rveSendTask() {
        generateInfoService.stopSend();
    }

    @Scheduled(fixedDelay = 1L, timeUnit = TimeUnit.SECONDS)
    public void printMonitoringLog() {
        if (isOut) {
            monitorSendManage.printMonitoringData();
            System.out.print("\033[31m  ***回车退出***  \033[m\n");
        }
    }

    private synchronized void queSendTask() {
        isOut = true;
        for (; ; ) if (CommandController.scanner.nextLine().equalsIgnoreCase("")) break;
        isOut = false;
    }

    private void executeTask(boolean isLongTime, boolean verificationCode) {
        PressureTestRequest pressureTestRequest = new PressureTestRequest();

        List<String> channelIds = cmppAccountManage.getAccountAllId();
        String channelId = KeyboardCommandUtil.getKeyboardCommand("\n1、选择你要使用的账号输入：", CommandController.scanner, channelIds, "再给你一次机会！");
        if (channelId == null) return;
        pressureTestRequest.setAccountId(channelId);

        if (isLongTime) {
            Integer time = KeyboardCommandUtil.getKeyDefaultValueNumber("2、发送间隔时间(毫秒)输入：", CommandController.scanner, 1000);
            if (time == null) return;
            pressureTestRequest.setPeriodTime(time);
        }
        pressureTestRequest.setIsLongTime(isLongTime);

        Integer size = KeyboardCommandUtil.getKeyDefaultValueNumber("3、输入你发送批次大小输入：", CommandController.scanner, 1000);
        if (size == null) return;
        pressureTestRequest.setSendSize(size);

        System.out.println("\033[1;93m*** (0全部，1移动，2联通，3电信) ***\033[m");
        String operator = KeyboardCommandUtil.getKeyboardCommand("4、是否指定运营商号码输入：", CommandController.scanner, List.of("0", "1", "2", "3"), "再给你一次机会！");
        if (operator == null) return;
        pressureTestRequest.setOperator(operator);

        System.out.println("\033[1;93m*** (0默认生成内容 1自定义内容，) ***\033[m");
        String templateCode = KeyboardCommandUtil.getKeyboardCommand("5、是否指定自定义内容输入：", CommandController.scanner, List.of("0", "1"), "再给你一次机会！");
        if (templateCode == null) return;
        pressureTestRequest.setTemplateCode(templateCode);

        if ("1".equals(templateCode)) {
            if (verificationCode)
                System.out.println("\033[1;93m*** 自定义内容中的验证码使用\033[m \033[31m{}\033[m \033[1;93m代替 ***\033[m ");
            String templateBody = KeyboardCommandUtil.getKeyboardCommand("6、自定义内容短信文案输入：", CommandController.scanner, null, "再给你一次机会！");
            if (templateBody == null) return;
            pressureTestRequest.setTemplateBody(templateBody);
        }
        pressureTestRequest.setVerificationCode(verificationCode);
        generateInfoService.generateSendSubmitMessage(pressureTestRequest);
    }

    private void queAccount() {
        System.out.println("\033[1;94m--------------------------------------------- 账号列表 --------------------------------------------\033[m");
        List<CmppSendAccountChannel> accountAll = cmppAccountManage.getAccountAll();
        if (accountAll.isEmpty()) {
            System.out.println("没有账号，添加一个呗");
            return;
        }
        System.out.printf("%-4s %-7s %-20s %-8s %-8s %-9s %-9s %-2s\n", "编号", "已连接", "地址端口", "协议", "账号", "密码", "接入号", "连接数");
        for (CmppSendAccountChannel sendAccountChannel : accountAll) {
            EndpointConnector<?> endpointConnector = manager.getEndpointConnector(sendAccountChannel.getChannelId());
            if (endpointConnector != null) {
                sendAccountChannel.setConnectCount(endpointConnector.getConnectionNum());
            } else {
                sendAccountChannel.setConnectCount(0);
            }
            sendAccountChannel.printf();
        }
        System.out.println();
    }

    private void delAccount() {
        System.out.print("\n输入要删除的账号编号：");
        String accountNumber = CommandController.scanner.nextLine();
        if (cmppAccountManage.removeAccount(accountNumber)) {
            manager.remove(accountNumber);
            System.out.println("删除成功，恭喜恭喜恭喜。\n");
        } else {
            System.out.println("删除失败，没有这个账号。\n");
        }
    }


    private void addAccountCmpp() {
        CmppSendAccountChannel cmpp = new CmppSendAccountChannel();
        System.out.println("\033[1;94m----------------------------------------- 添加账号列表 -----------------------------------------\033[m");
        System.out.println("* 输入CMPP账号对应的参数：");

        String protocol = KeyboardCommandUtil.getKeyboardCommand("1、选择账号协议[1、CMPP20  2、CMPP30]:", CommandController.scanner, List.of("1", "2"), "再给你一次机会！！");
        if (protocol == null) return;

        switch (protocol) {
            case "1" -> cmpp.setProtocol(CmppType.CMPP20);
            case "2" -> cmpp.setProtocol(CmppType.CMPP30);
        }

        cmpp.setChannelHost(KeyboardCommandUtil.getKeyboardCommand("2、输入连接地址:", CommandController.scanner, null, null));
        if (cmpp.getChannelHost() == null) return;

        cmpp.setChannelPort(KeyboardCommandUtil.getKeyDefaultValueNumber("3、输入账号端口:", CommandController.scanner, 7890));
        if (cmpp.getChannelPort() == null) return;

        cmpp.setLoginName(KeyboardCommandUtil.getKeyboardCommand("4、输入登录账号:", CommandController.scanner, null, null));
        if (cmpp.getLoginName() == null) return;

        cmpp.setPassword(KeyboardCommandUtil.getKeyboardCommand("5、输入登录密码:", CommandController.scanner, null, null));
        if (cmpp.getPassword() == null) return;

        cmpp.setSrcId(KeyboardCommandUtil.getKeyboardCommand("6、输入通道号码:", CommandController.scanner, null, null));
        if (cmpp.getSrcId() == null) return;

        cmpp.setMaxConnect(KeyboardCommandUtil.getKeyDefaultValueNumber("7、输入最大连接:", CommandController.scanner, 2));
        if (cmpp.getMaxConnect() == null) return;

        String command = KeyboardCommandUtil.getKeyboardCommand("8、看清楚，确认好。没问题就输入 (ok):", CommandController.scanner, List.of("ok", "no"), "再给你一次机会！！");
        if (!"ok".equalsIgnoreCase(command)) {
            return;
        }
        cmppAccountManage.addAccount(cmpp);
        startCmppEndpointConnection.openEndpoint(cmpp);
    }


    @Autowired
    public void setStartCmppEndpointConnection(StartCmppEndpointConnection startCmppEndpointConnection) {
        this.startCmppEndpointConnection = startCmppEndpointConnection;
    }

    @Autowired
    public void setGenerateSubmitMessageService(GenerateInfoService generateInfoService) {
        this.generateInfoService = generateInfoService;
    }

    @Autowired
    public void setSendMessageSubmitManage(MonitorSendManage monitorSendManage) {
        this.monitorSendManage = monitorSendManage;
    }

    @Autowired
    public void setCmppAccountManage(CmppAccountManage cmppAccountManage) {
        this.cmppAccountManage = cmppAccountManage;
    }

}
