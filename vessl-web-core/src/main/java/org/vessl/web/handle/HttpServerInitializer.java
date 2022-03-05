package org.vessl.web.handle;

import io.netty.channel.ChannelInitializer;
import io.netty.channel.ChannelPipeline;
import io.netty.channel.socket.SocketChannel;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.vessl.web.bind.WebPathHandleMapping;

@Component
public class HttpServerInitializer extends ChannelInitializer<SocketChannel> {
    @Autowired
    private WebPathHandleMapping webPathHandleMapping;

    @Override
    protected void initChannel(SocketChannel socketChannel) throws Exception {
        ChannelPipeline pipeline= socketChannel.pipeline();
        //pipeline.addLast(new LineBasedFrameDecoder(10240));
        //pipeline.addLast(new StringDecoder(CharsetUtil.UTF_8));
        pipeline.addLast(new HttpContentHandler());
        pipeline.addLast(new HttpReqHandler(webPathHandleMapping));
    }
}