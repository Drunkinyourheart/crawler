package com.yeepay.bigdata.crawler.crawl.demo;

import org.jboss.netty.buffer.ChannelBuffer;
import org.jboss.netty.buffer.ChannelBuffers;
import org.jboss.netty.buffer.DynamicChannelBuffer;
import org.jboss.netty.channel.*;
import org.jboss.netty.handler.codec.frame.TooLongFrameException;
import org.jboss.netty.handler.codec.http.*;
import org.jboss.netty.util.CharsetUtil;

public class AdminServerHandler extends SimpleChannelUpstreamHandler {

    @Override
    public void messageReceived(ChannelHandlerContext ctx, MessageEvent e)
            throws Exception {
        HttpRequest request = (HttpRequest) e.getMessage();
        String uri = request.getUri();
        System.out.println("uri:" + uri);
        HttpResponse response = new DefaultHttpResponse(HttpVersion.HTTP_1_0, HttpResponseStatus.OK);
        ChannelBuffer buffer = new DynamicChannelBuffer(2048);
        buffer.writeBytes("hello!! 你好".getBytes("UTF-8"));
        response.setContent(buffer);
        response.setHeader("Content-Type", "text/html; charset=UTF-8");
        response.setHeader("Content-Length", response.getContent().writerIndex());
        Channel ch = e.getChannel();
        // Write the initial line and the header.
        ch.write(response);
        ch.disconnect();
        ch.close();

    }

    @Override
    public void exceptionCaught(ChannelHandlerContext ctx, ExceptionEvent e)
            throws Exception {
        Channel ch = e.getChannel();
        Throwable cause = e.getCause();
        if (cause instanceof TooLongFrameException) {
            sendError(ctx, HttpResponseStatus.BAD_REQUEST);
            return;
        }

        cause.printStackTrace();
        if (ch.isConnected()) {
            sendError(ctx, HttpResponseStatus.INTERNAL_SERVER_ERROR);
        }
    }

    private void sendError(ChannelHandlerContext ctx, HttpResponseStatus status) {
        HttpResponse response = new DefaultHttpResponse(HttpVersion.HTTP_1_0, status);
        response.setHeader("CONTENT_TYPE", "text/plain; charset=UTF-8");
//        response.setHeader();
        response.setContent(ChannelBuffers.copiedBuffer("Failure: " + status.toString() + "\r\n", CharsetUtil.UTF_8));

        // Close the connection as soon as the error message is sent.
        ctx.getChannel().write(response).addListener(ChannelFutureListener.CLOSE);
    }
}