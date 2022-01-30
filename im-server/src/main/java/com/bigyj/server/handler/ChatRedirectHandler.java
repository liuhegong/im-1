package com.bigyj.server.handler;

import com.bigyj.message.ChatRequestMessage;
import com.bigyj.message.ChatResponseMessage;
import com.bigyj.server.manager.ServerSessionManager;
import com.bigyj.server.session.LocalSession;
import com.bigyj.server.session.ServerSession;
import io.netty.channel.ChannelHandler;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.SimpleChannelInboundHandler;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

@Slf4j
@Component
@ChannelHandler.Sharable
public class ChatRedirectHandler extends SimpleChannelInboundHandler<ChatRequestMessage> {
	@Autowired
	private ServerSessionManager serverSessionManager ;
	@Override
	protected void channelRead0(ChannelHandlerContext ctx, ChatRequestMessage msg) throws Exception {

		//反向导航
		LocalSession session = ctx.channel().attr(LocalSession.SESSION_KEY).get();
		//判断是否登录
		if (null == session || !session.isLogin()) {
			logger.error("用户尚未登录，不能发送消息");
			return;
		}
		this.action(msg,ctx);
	}

	private void action(ChatRequestMessage chatRequestMessage,ChannelHandlerContext context) {
		String toUserId = chatRequestMessage.getTo();
		//判断用户是否在线
		ServerSession serverSession = serverSessionManager.getServerSession(toUserId);
		if(serverSession == null){
			this.sentNotOnlineMsg(chatRequestMessage,toUserId,context);
		}else {
			boolean result = serverSession.writeAndFlush(chatRequestMessage);
			if(!result){
			}
		}

	}

	/**
	 * 告知客户端用户不在线
	 * @param toUserId
	 * @param ctx
	 */
	private void sentNotOnlineMsg(ChatRequestMessage chatRequestMessage,String toUserId,ChannelHandlerContext ctx) {
		logger.error("用户{} 不在线，消息发送失败!",toUserId);
		ctx.writeAndFlush(new ChatResponseMessage(false, chatRequestMessage.getTo()+"用户不存在或者不在线"));
	}
}
