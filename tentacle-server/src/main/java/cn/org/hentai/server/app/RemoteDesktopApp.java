package cn.org.hentai.server.app;

import cn.org.hentai.server.rds.TentacleDesktopHandler;
import cn.org.hentai.server.rds.coder.MessageDecoder;
import cn.org.hentai.server.rds.coder.MessageEncoder;
import cn.org.hentai.tentacle.util.Configs;
import cn.org.hentai.tentacle.util.Log;
import io.netty.bootstrap.ServerBootstrap;
import io.netty.channel.*;
import io.netty.channel.nio.NioEventLoopGroup;
import io.netty.channel.socket.SocketChannel;
import io.netty.channel.socket.nio.NioServerSocketChannel;

/**
 * Created by matrixy on 2019/1/3.
 */
public class RemoteDesktopApp
{
    private ServerBootstrap serverBootstrap;

    private EventLoopGroup bossGroup = new NioEventLoopGroup(1);
    private EventLoopGroup workerGroup = new NioEventLoopGroup();

    public void startup() throws Exception
    {
        serverBootstrap = new ServerBootstrap();
        serverBootstrap.option(ChannelOption.SO_BACKLOG, 100);
        serverBootstrap.group(bossGroup, workerGroup)
                .channel(NioServerSocketChannel.class)
                // .handler(new LoggingHandler(LogLevel.INFO))
                .childHandler(new ChannelInitializer<SocketChannel>() {
                    @Override
                    protected void initChannel(final SocketChannel channel) throws Exception {
                        ChannelPipeline p = channel.pipeline();
                        p.addLast(new MessageDecoder());
                        p.addLast(new MessageEncoder());
                        p.addLast(new TentacleDesktopHandler());
                    }
                });

        int port = Configs.getInt("rds.server.port", 1986);
        Channel ch = serverBootstrap.bind(port).sync().channel();
        Log.debug("Server started at: " + port);
        ch.closeFuture().sync();
    }
}
