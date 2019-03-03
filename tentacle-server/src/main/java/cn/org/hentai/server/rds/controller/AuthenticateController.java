package cn.org.hentai.server.rds.controller;

import cn.org.hentai.server.rds.*;
import cn.org.hentai.server.util.MD5;
import cn.org.hentai.server.util.NonceStr;
import cn.org.hentai.tentacle.protocol.Command;
import cn.org.hentai.tentacle.protocol.Message;
import cn.org.hentai.tentacle.protocol.Packet;
import cn.org.hentai.tentacle.util.Configs;
import cn.org.hentai.tentacle.util.Log;

/**
 * Created by matrixy on 2019/1/3.
 * 受控端连接发起时的会话认证处理
 */
public class AuthenticateController extends BaseMessageController
{
    @Override
    public boolean authenticateRequired()
    {
        return false;
    }

    @Override
    public Message service(TentacleDesktopSession session, Message msg) throws Exception
    {
        Packet body = msg.getBody();
        String name = new String(body.nextBytes(body.nextInt()), "UTF-8");

        String nonce = new String(body.nextBytes(32));
        String signature = new String(body.nextBytes(32));

        // TODO: 可在这里通过cn.org.hentai.server.util.BeanUtils.createBean()来创建Spring Bean进行数据库查询认证
        if (signature.equals(MD5.encode(nonce + ":::" + Configs.get("client.key"))) == false)
        {
            this.replyAndDisconnect();
            Log.error(String.format("unauth connection: %s", session.getRemoteAddress()));
            Message resp = new Message().withCommand(Command.AUTHENTICATE_RESPONSE).withBody(Packet.create(1).addByte((byte)0x01));
            return resp;
        }

        System.out.println("Authenticated: " + name);

        Client info = new Client();
        info.setName(name);
        info.setControlling(false);
        info.setAddress(session.getRemoteAddress());
        info.setSecret(NonceStr.generate(32));

        session.setClient(info);
        SessionManager.register(session);

        Message resp = new Message().withCommand(Command.AUTHENTICATE_RESPONSE).withBody(Packet.create(41).addByte((byte)0x00).addLong(info.getId()).addBytes(info.getSecret().getBytes()));
        return resp;
    }
}
