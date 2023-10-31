package com.calo.cmpp.service;


import cn.hutool.json.JSONUtil;
import com.calo.cmpp.domain.CmppSendAccountChannel;
import jakarta.annotation.PreDestroy;
import lombok.extern.log4j.Log4j2;
import org.mapdb.DB;
import org.mapdb.DBMaker;
import org.mapdb.Serializer;
import org.springframework.stereotype.Service;

import java.io.File;
import java.util.List;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;

@Log4j2
@Service
public class CmppAccountManage {

    public static  File fileDD = new File("account.db");

    private static final DB fileDb = DBMaker.fileDB(fileDD).fileMmapEnable().make();
    private static final ConcurrentMap<String, String> accountDbMap = fileDb.hashMap("account", Serializer.STRING, Serializer.STRING).createOrOpen();
    private static final ConcurrentMap<String, CmppSendAccountChannel> accountCache = new ConcurrentHashMap<>();

    public void addAccount(CmppSendAccountChannel accountChannel) {
        accountChannel.setChannelId(String.valueOf(accountDbMap.size()));
        accountDbMap.put(accountChannel.getChannelId(), JSONUtil.toJsonStr(accountChannel));
    }

    public boolean removeAccount(String channelId) {
        return accountDbMap.remove(channelId) != null;
    }

    public CmppSendAccountChannel getAccount(String channelId) {
        if (accountCache.containsKey(channelId)) {
            return accountCache.get(channelId);
        }
        String accountJson = accountDbMap.get(channelId);
        if (accountJson != null) {
            accountCache.put(channelId, JSONUtil.toBean(accountJson, CmppSendAccountChannel.class));
        }
        return accountJson == null ? null : JSONUtil.toBean(accountJson, CmppSendAccountChannel.class);
    }

    public List<String> getAccountAllId() {
        return accountDbMap.keySet().stream().toList();
    }

    public List<CmppSendAccountChannel> getAccountAll() {
        return accountDbMap.values().stream().map(json -> JSONUtil.toBean(json, CmppSendAccountChannel.class)).toList();
    }

    @PreDestroy
    public void close() {
        fileDb.close();
    }


}
