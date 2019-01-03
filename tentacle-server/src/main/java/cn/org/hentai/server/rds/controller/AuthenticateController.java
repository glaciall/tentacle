package cn.org.hentai.server.rds.controller;

import cn.org.hentai.server.rds.Client;
import cn.org.hentai.server.rds.SessionManager;
import cn.org.hentai.server.rds.TentacleDesktopHandler;
import cn.org.hentai.server.util.Configs;
import cn.org.hentai.server.util.MD5;
import cn.org.hentai.tentacle.protocol.Command;
import cn.org.hentai.tentacle.protocol.Message;
import cn.org.hentai.tentacle.protocol.Packet;
import cn.org.hentai.tentacle.util.ByteUtils;

/**
 * Created by matrixy on 2019/1/3.
 */
public class AuthenticateController extends BaseMessageController
{
    @Override
    public boolean authenticateRequired()
    {
        return false;
    }

    @Override
    public Message service(TentacleDesktopHandler handler, Message msg) throws Exception
    {
        Packet body = msg.getBody();
        String name = new String(body.nextBytes(body.nextInt()), "UTF-8");

        String nonce = new String(body.nextBytes(32));
        String signature = new String(body.nextBytes(32));
        if (signature.equals(MD5.encode(nonce + ":::" + Configs.get("client.key"))) == false)
        {
            throw new RuntimeException(String.format("unauth connection: %s", handler.getRemoteAddress()));
        }

        System.out.println("Authenticated: " + name);

        Client info = new Client();
        info.setName(name);
        info.setControlling(false);
        info.setAddress(handler.getRemoteAddress());

        handler.setClient(info);
        SessionManager.register(handler);

        Message resp = new Message().withCommand(Command.AUTHENTICATE_RESPONSE).withBody(Packet.create(1).addByte((byte)0x00));
        return resp;
    }
}
