package com.lorne.tx.utils;

import com.alibaba.dubbo.remoting.exchange.ExchangeServer;
import com.alibaba.dubbo.rpc.protocol.dubbo.DubboProtocol;

import java.util.Collection;

/**
 * Created by lorne on 2017/6/8.
 */
public class DubboUtils {

    public static String getProviderUrl(){
        Collection<ExchangeServer> servers =  DubboProtocol.getDubboProtocol().getServers();
        String url = "";
         for(ExchangeServer server:servers){
             String address =  server.getUrl().getAddress();
             String path = server.getUrl().getAbsolutePath();
             return "dubbo://"+address+path;
         }
         return url;
    }
}
