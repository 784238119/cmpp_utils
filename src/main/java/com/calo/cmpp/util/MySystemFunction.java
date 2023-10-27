package com.calo.cmpp.util;

public class MySystemFunction {

    public static void clean() {
        try {
            String os = System.getProperty("os.name");
            if (os.contains("Windows")) {
                new ProcessBuilder("cmd", "/c", "cls").inheritIO().start().waitFor();
            } else {
                System.out.print("\033[H\033[2J");
                System.out.flush();
            }
        } catch (Exception exception) {
            System.out.println(exception);
        }
    }
}
