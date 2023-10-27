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

    public void bootMenuFunction(String... args) {
        Scanner scanner = new Scanner(System.in);
        do {
            MySystemFunction.clean();
            this.queAccount();
            this.showMenu();
            System.out.print("* 选择功能：");
            String menuIndex = scanner.nextLine();

            switch (menuIndex) {
                case "1" -> this.addAccountCmpp(scanner);
                case "2" -> this.delAccount(scanner);
                case "3" -> this.queSendTask(scanner);
                case "4" -> this.executeTask(scanner, false);
                case "5" -> this.executeTask(scanner, true);
                case "6" -> this.rveSendTask(scanner);
                case "7" -> this.queryMsgId(scanner);
                case "8" -> this.clearCacheData(scanner);
                case "0" -> this.quit(scanner);
                default -> {
                    System.out.println("\033[1;93m程序没有该功能，你是不是找茬, 给你一次机会，回车！！！！！！！！！\033[m");
                    scanner.nextLine();
                }
            }
        } while (true);
    }

    private void clearCacheData(Scanner scanner) {
        System.out.print("清除缓存数据,建议不要在发送的时候清除，这样会导致数据错误（yes）：");
        String ok = scanner.nextLine();
        if ("yes".equalsIgnoreCase(ok)) {
            monitorSendManage.clearCacheData();
        }
    }

    private void queryMsgId(Scanner scanner) {
        System.out.print("输入需要查询的MsgId：");
        String nextLine = scanner.nextLine();
        monitorSendManage.querySendMessageInfo(nextLine);
        scanner.nextLine();
    }

    private void showMenu() {
        System.out.println("\033[1;94m--------------------------------------- 功能菜单列表命令 ---------------------------------------\033[m");
        System.out.println("\033[1;94m[1] 添加——发送账号配置              [2] 删除——发送账号配置               [3] 查询——发送任务参数\033[m");
        System.out.println("\033[1;94m[4] 执行——批次任务发送              [5] 执行——持续任务发送               [6] 停止——全部发送执行\033[m");
        System.out.println("\033[1;94m[7] 查询——数据详细信息              [8] 清理——清除缓存数据\033[m");
        System.out.println("\033[1;94m---------------------------------------\033[m\033[31m 退出程序输入：0 \033[m\033[1;94m-----------------------------------------\033[m");
    }

    private void quit(Scanner scanner) {
        System.out.print("是否确认退出程序 (yes) ：");
        String nextLine = scanner.nextLine();
        if ("yes".equalsIgnoreCase(nextLine)) {
            System.exit(0);
        }
    }

    private void rveSendTask(Scanner scanner) {
        generateInfoService.stopSend();
    }

    @Scheduled(fixedDelay = 1L, timeUnit = TimeUnit.SECONDS)
    public void printMonitoringLog() {
        if (isOut) {
            monitorSendManage.printMonitoringData();
            System.out.print("\033[31m  ***回车退出***  \033[m\n");
        }
    }


    private synchronized void queSendTask(Scanner scanner) {
        isOut = true;
        while (!scanner.nextLine().equalsIgnoreCase("")) {
        }
        isOut = false;
    }

    private void executeTask(Scanner scanner, boolean sendWay) {
        PressureTestRequest pressureTestRequest = new PressureTestRequest();

        List<String> channelIds = cmppAccountManage.getAccountAllId();
        String channelId = KeyboardCommandUtil.getKeyboardCommand("\n选择你要使用的账号输入：", scanner, channelIds, "再给你一次机会！");
        if (channelId == null) return;
        pressureTestRequest.setAccountId(channelId);

        if (sendWay) {
            Integer time = KeyboardCommandUtil.getKeyDefaultValueNumber("发送间隔时间(毫秒)输入：", scanner, 1000);
            if (time == null) return;
            pressureTestRequest.setPeriodTime(time);
        }

        Integer size = KeyboardCommandUtil.getKeyDefaultValueNumber("输入你发送批次大小输入：", scanner, 1000);
        if (size == null) return;
        pressureTestRequest.setSendSize(size);

        System.out.println("(0全部，1移动，2联通，3电信)");
        String operator = KeyboardCommandUtil.getKeyboardCommand("是否指定运营商号码输入：", scanner, List.of("0", "1", "2", "3"), "再给你一次机会！");
        if (operator == null) return;
        pressureTestRequest.setOperator(operator);

        System.out.println("(1自定义内容，2默认生成内容)");
        String templateCode = KeyboardCommandUtil.getKeyboardCommand("是否指定自定义内容输入：", scanner, List.of("1", "2"), "再给你一次机会！");
        if (templateCode == null) return;
        pressureTestRequest.setTemplateCode(templateCode);

        if (!"2".equals(templateCode)){
            String templateBody = KeyboardCommandUtil.getKeyboardCommand("自定义内容输入：", scanner, null, "再给你一次机会！");
            if (templateBody == null) return;
            pressureTestRequest.setTemplateBody(templateBody);
        }
        generateInfoService.generateSendSubmitMessage(pressureTestRequest);
    }

    private void queAccount() {
        System.out.println("\033[1;94m-------------------------------------------- 账号列表 -------------------------------------------\033[m");
        List<CmppSendAccountChannel> accountAll = cmppAccountManage.getAccountAll();
        if (accountAll.isEmpty()) {
            System.out.println("没有账号，添加一个呗");
            return;
        }
        System.out.printf("%-4s%-10s%-20s%-10s%-10s%-10s%-10s%-10s\n", "编号", "已连接数", "地址端口", "协议", "账号", "密码", "接入号", "连接数");
        for (CmppSendAccountChannel sendAccountChannel : accountAll) {
            EndpointConnector<?> endpointConnector = manager.getEndpointConnector(sendAccountChannel.getChannelId());
            if (endpointConnector != null) {
                sendAccountChannel.setConnectCount(endpointConnector.getConnectionNum());
            } else {
                sendAccountChannel.setConnectCount(0);
            }
            sendAccountChannel.printf();
        }
    }

    private void delAccount(Scanner scanner) {
        System.out.print("\n输入要删除的账号编号：");
        String accountNumber = scanner.nextLine();
        if (cmppAccountManage.removeAccount(accountNumber)) {
            manager.remove(accountNumber);
            System.out.println("删除成功，恭喜恭喜！！\n3");
        } else {
            System.out.println("删除失败，没有这个账号。\n");
        }
    }


    private boolean addAccountCmpp(Scanner scanner) {
        CmppSendAccountChannel cmpp = new CmppSendAccountChannel();
        System.out.println("输入CMPP账号对应的参数：(要是输入错了直接出入：reset)");
        System.out.println("\033[1;94m----------------------------------------- 添加账号列表 -----------------------------------------\033[m");
        do {
            System.out.print("选择账号协议[1、CMPP20  2、CMPP30]：");
            String accountNUmber = scanner.nextLine();
            switch (accountNUmber) {
                case "1" -> cmpp.setProtocol(CmppType.CMPP20);
                case "2" -> cmpp.setProtocol(CmppType.CMPP30);
                case "exit" -> {
                    return false;
                }
                default -> System.out.println("再给你一次机会, 想清楚在选");
            }
        } while (cmpp.getProtocol() == null);

        do {
            System.out.print("输入连接地址：");
            String ip = scanner.nextLine();
            if ("reset".equalsIgnoreCase(ip)) {
                continue;
            }
            if ("exit".equalsIgnoreCase(ip)) {
                return false;
            }
            cmpp.setChannelHost(ip);
        } while (cmpp.getChannelHost() == null);

        do {
            System.out.print("输入账号端口：");
            String port = scanner.nextLine();
            if ("reset".equalsIgnoreCase(port)) {
                continue;
            }
            if ("exit".equalsIgnoreCase(port)) {
                return false;
            }
            try {
                cmpp.setChannelPort(Integer.valueOf(port));
            } catch (NumberFormatException e) {
                System.out.println("输入有问题！！！！！！！");
            }
        } while (cmpp.getChannelPort() == null);

        do {
            System.out.print("输入登录账号：");
            String account = scanner.nextLine();
            if ("reset".equalsIgnoreCase(account)) {
                continue;
            }
            if ("exit".equalsIgnoreCase(account)) {
                return false;
            }
            cmpp.setLoginName(account);
        } while (cmpp.getLoginName() == null);

        do {
            System.out.print("输入登录密码：");
            String password = scanner.nextLine();
            if ("reset".equalsIgnoreCase(password)) {
                continue;
            }
            if ("exit".equalsIgnoreCase(password)) {
                return false;
            }
            cmpp.setPassword(password);
        } while (cmpp.getPassword() == null);

        do {
            System.out.print("输入通道号码：");
            String srcId = scanner.nextLine();
            if ("reset".equalsIgnoreCase(srcId)) {
                continue;
            }
            if ("exit".equalsIgnoreCase(srcId)) {
                return false;
            }
            cmpp.setSrcId(srcId);
        } while (cmpp.getSrcId() == null);

        do {
            System.out.print("输入最大连接：");
            String maxConnection = scanner.nextLine();
            if ("reset".equalsIgnoreCase(maxConnection)) {
                continue;
            }
            if ("exit".equalsIgnoreCase(maxConnection)) {
                return false;
            }
            try {
                cmpp.setMaxConnect(Integer.valueOf(maxConnection));
            } catch (NumberFormatException e) {
                System.out.println("输入有问题！！！！！！！");
            }
        } while (cmpp.getMaxConnect() == null);

        System.out.print("看清楚，确认好。没问题就输入 (ok): ");
        String verify = scanner.nextLine();
        if (!"ok".equalsIgnoreCase(verify)) {
            return false;
        }
        cmppAccountManage.addAccount(cmpp);
        startCmppEndpointConnection.openEndpoint(cmpp);
        return true;
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
