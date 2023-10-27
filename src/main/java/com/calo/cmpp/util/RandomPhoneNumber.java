package com.calo.cmpp.util;

import java.util.Random;

public class RandomPhoneNumber {

    private static Random random = new Random();

    //中国移动
    public static final String[] CHINA_MOBILE = {
            "134", "135", "136", "137", "138", "139", "150", "151", "152", "157", "158", "159",
            "182", "183", "184", "187", "188", "178", "147", "172", "198"
    };
    //中国联通
    public static final String[] CHINA_UNICOM = {
            "130", "131", "132", "145", "155", "156", "166", "171", "175", "176", "185", "186", "166"
    };
    //中国电信
    public static final String[] CHINA_TELECOME = {
            "133", "149", "153", "173", "177", "180", "181", "189", "199"
    };

    public static String createMobile(String op) {
        StringBuilder sb = new StringBuilder();
        Random random = new Random();
        String mobile01;//手机号前三位
        int temp;
        mobile01 = switch (op) {
            case "1" -> CHINA_MOBILE[random.nextInt(CHINA_MOBILE.length)];
            case "2" -> CHINA_UNICOM[random.nextInt(CHINA_UNICOM.length)];
            case "3" -> CHINA_TELECOME[random.nextInt(CHINA_TELECOME.length)];
            default -> "op标志位有误！";
        };
        if (mobile01.length() > 3) {
            return mobile01;
        }
        sb.append(mobile01);
        //生成手机号后8位
        for (int i = 0; i < 8; i++) {
            temp = random.nextInt(10);
            sb.append(temp);
        }
        return sb.toString();
    }

    public static String generateRandomPhoneNumber(String operator) {
        String op;
        if ("0".equals(operator)) {
            op = random.nextInt(1, 4) + "";
        } else {
            op = operator;
        }
        return createMobile(op);
    }


}
