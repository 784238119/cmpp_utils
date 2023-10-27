package com.calo.cmpp.util;

import org.apache.commons.lang3.StringUtils;

import java.util.List;
import java.util.Scanner;

public class KeyboardCommandUtil {

    private static final String exit = "exit";

    public static String getKeyboardCommand(String theme, Scanner scanner, List<String> keyList, String remind) {
        System.out.print(theme);
        do {
            String command = scanner.nextLine();
            if (StringUtils.equalsIgnoreCase(command, exit)) {
                return null;
            }
            if (keyList != null && !keyList.contains(command)) {
                System.out.print(remind);
            } else {
                return command;
            }
        } while (true);
    }


    public static Integer getKeyDefaultValueNumber(String theme, Scanner scanner, int defaultValue) {
        System.out.print(theme);
        String command = scanner.nextLine();
        if (StringUtils.equalsIgnoreCase(command, exit)) {
            return null;
        }
        try {
            defaultValue = Integer.parseInt(command);
        } catch (NumberFormatException e) {
        }
        return defaultValue;
    }
}
