package com.calo.cmpp.util;

import java.io.FileWriter;
import java.io.IOException;

public class SequentialStringWriter {
    public static void main(String[] args) {
        // 文件路径
        String filePath = "output.txt";

        // 要写入文件的字符串
        String content = "这是要写入文件的字符串。\n这是第二行。\n这是第三行。";

        try {
            // 创建一个文件写入器
            FileWriter writer = new FileWriter(filePath);

            // 将字符串写入文件
            writer.write(content);

            // 关闭写入器
            writer.close();

            System.out.println("字符串已成功写入文件：" + filePath);
        } catch (IOException e) {
            System.err.println("写入文件时发生错误：" + e.getMessage());
            e.printStackTrace();
        }
    }
}
