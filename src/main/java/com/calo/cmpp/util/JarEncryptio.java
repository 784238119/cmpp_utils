package com.calo.cmpp.util;

import io.xjar.XCryptos;

public class JarEncryptio {
    public static void main(String[] args) throws Exception {
        // Spring-Boot Jar包加密
        XCryptos.encryption()
                .from("./target/CmppUtil-3.1.6.jar")
                .use("fsdasdasdasdf")
                .to("./target/CmppUtil-Security.jar");
        System.out.println("success");
    }
}
