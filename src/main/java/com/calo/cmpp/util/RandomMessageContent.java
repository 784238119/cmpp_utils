package com.calo.cmpp.util;

import org.apache.commons.lang3.StringUtils;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ThreadLocalRandom;

public class RandomMessageContent {

    private final static List<String> CONTENT_LIST = new ArrayList<>();
    private final static List<String> LONG_CONTENT_LIST = new ArrayList<>();

    static {
        CONTENT_LIST.add("【你说啥】对方隔了一条江给你打电话说手机快没电了，江不宽，你仔细听我说，但是你还是听不太清。");
        CONTENT_LIST.add("【听不见】我接到你的电话，你说手机快没电了，你要隔江对话，可是我听不清。");
        CONTENT_LIST.add("【太远啦】听不见你在说什么，于是大声对你喊道：“太远啦！我听不见。”");
        CONTENT_LIST.add("【大点声】继续对你说：“大点声儿！”");
        CONTENT_LIST.add("【我说】我听到你说听不见了，双手比作扩音器对着嘴巴，超大声回答你:“我说......”");
        CONTENT_LIST.add("【我们隔了一条江】“我们隔了一条江，你听不到我的声音很正常。”");
        CONTENT_LIST.add("【你当然听不见】我隐约听懂他在说什么，心里想着“这**不是废话吗”┌( ´_ゝ` )┐（无语颜文字）");
        CONTENT_LIST.add("【或许你能看得懂手语】然后对方好像在用手比划些什么，他在问，你懂手语吗？");
        CONTENT_LIST.add("【yes i can】我懂，然后他说，bushi，他比划道“不小心在隔条江是个意外，因为他的家乡没有江。”");

        LONG_CONTENT_LIST.add("【yes i can】我懂，然后他说，bushi，他比划道“不小心在隔条江是个意外，因为他的家乡没有江。我懂，然后他说，bushi，他比划道“不小心在隔条江是个意外，因为他的家乡没有江。");
    }


    public static String getContentRandom(boolean isLongSms) {
        if (isLongSms){
            int boundedRandomValue = ThreadLocalRandom.current().nextInt(0, LONG_CONTENT_LIST.size());
            return LONG_CONTENT_LIST.get(boundedRandomValue);
        }else {
            int boundedRandomValue = ThreadLocalRandom.current().nextInt(0, CONTENT_LIST.size());
            return CONTENT_LIST.get(boundedRandomValue);
        }

    }

    public static int count(String content) {
        int count;
        if (StringUtils.isBlank(content) || content.length() <= 67) {
            count = 1;
        } else {
            count = (int) Math.ceil((double) content.length() / 70);
        }
        return count;
    }
}
