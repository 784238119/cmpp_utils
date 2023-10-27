package com.calo.cmpp.enums;

import lombok.Getter;

@Getter
public enum CmppType {

    CMPP20((short) 0x20), CMPP30((short) 0x30);

    CmppType(short value) {
        this.value = value;
    }

    private final short value;

}
