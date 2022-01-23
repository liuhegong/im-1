package com.bigyj.client.handler;

import com.alibaba.fastjson.JSONException;
import com.bigyj.client.client.ClientSession;
import com.bigyj.client.manager.CommandManager;
import io.netty.channel.ChannelHandler;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.ChannelInboundHandlerAdapter;
import lombok.extern.slf4j.Slf4j;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

@Slf4j
@Service
@ChannelHandler.Sharable
public class ExceptionHandler extends ChannelInboundHandlerAdapter {
    @Autowired
    private CommandManager commandManager;
    @Override
    public void exceptionCaught(ChannelHandlerContext ctx, Throwable cause) throws Exception {
        if (cause instanceof JSONException) {
            logger.error(cause.getMessage());
            ClientSession.getSession(ctx).close();
        } else {
            //捕捉异常信息
            logger.error(cause.getMessage());
            ctx.close();
            //开始重连
            logger.info("客户端重新连接服务器......");
            commandManager.setConnectFlag(false);
            commandManager.startClient();
        }
    }
}
