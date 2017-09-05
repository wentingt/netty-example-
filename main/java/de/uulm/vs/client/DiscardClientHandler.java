/*
 * Copyright 2009 Red Hat, Inc.
 *
 * Red Hat licenses this file to you under the Apache License, version 2.0
 * (the "License"); you may not use this file except in compliance with the
 * License.  You may obtain a copy of the License at:
 *
 *    http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS, WITHOUT
 * WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.  See the
 * License for the specific language governing permissions and limitations
 * under the License.
 */
package de.uulm.vs.client;

import org.jboss.netty.handler.codec.http.HttpChunk;
import org.jboss.netty.handler.codec.http.HttpMessage;  
import java.util.concurrent.atomic.AtomicLong;
import java.util.logging.Level;
import java.util.logging.Logger;

import org.jboss.netty.buffer.ChannelBuffer;
import org.jboss.netty.buffer.ChannelBuffers;
import org.jboss.netty.channel.Channel;
import org.jboss.netty.channel.ChannelEvent;
import org.jboss.netty.channel.ChannelHandlerContext;
import org.jboss.netty.channel.ChannelState;
import org.jboss.netty.channel.ChannelStateEvent;
import org.jboss.netty.channel.ExceptionEvent;
import org.jboss.netty.channel.MessageEvent;
import org.jboss.netty.channel.SimpleChannelUpstreamHandler;
import org.jboss.netty.channel.WriteCompletionEvent;
import org.jboss.netty.handler.codec.http.HttpRequest;
import org.jboss.netty.handler.codec.http.DefaultHttpRequest;
import static org.jboss.netty.handler.codec.http.HttpVersion.HTTP_1_1;
import static org.jboss.netty.handler.codec.http.HttpMethod.GET;
import java.io.*;
import java.nio.file.Files; 
import org.jboss.netty.handler.ssl.SslHandler;

 import com.google.common.util.concurrent.RateLimiter;

/**
 * Handles a client-side channel.
 *
 * @author <a href="http://www.jboss.org/netty/">The Netty Project</a>
 * @author <a href="http://gleamynode.net/">Trustin Lee</a>
 *
 * @version $Rev: 2121 $, $Date: 2010-02-02 09:38:07 +0900 (Tue, 02 Feb 2010) $
 */
public class DiscardClientHandler extends SimpleChannelUpstreamHandler {
    private static final Logger logger = Logger.getLogger(
            DiscardClientHandler.class.getName());
//    private final AtomicLong transferredBytes = new AtomicLong();
//    private final byte[] content;
/*
	double permitsPerSecond = 50000;
    private final RateLimiter rateLimiter = RateLimiter.create(permitsPerSecond);
    public long getTransferredBytes() {
        return transferredBytes.get();
    }

*/
    @Override
    public void handleUpstream(ChannelHandlerContext ctx, ChannelEvent e) throws Exception {
        if (e instanceof ChannelStateEvent) {
            if (((ChannelStateEvent) e).getState() != ChannelState.INTEREST_OPS) {
                logger.info(e.toString());
            }
        }

        // Let SimpleChannelHandler call actual event handler methods below.
        super.handleUpstream(ctx, e);
    }
    @Override
    public void channelConnected(ChannelHandlerContext ctx, ChannelStateEvent e) throws Exception {
	    //super.channelConnected(ctx, e);
	    /*
	    SslHandler sslHandler = ctx.getPipeline().get(SslHandler.class);
	     sslHandler.handshake();
	     */ 

        // Send the initial messages.
//       generateTraffic(e);
	String uri = "x.txt";
	RandomAccessFile f = new RandomAccessFile(uri, "rw");
	f.setLength(1024 * 1024 * 1024); // 1GB
	f.close();
	HttpRequest request = new DefaultHttpRequest(HTTP_1_1, GET, uri);
	Channel ch = e.getChannel();
	ch.write(request);
	System.out.println("DiscardClientHandler::channelConnected");
	System.out.println(request);
    }
/*
    @Override
    public void channelInterestChanged(ChannelHandlerContext ctx, ChannelStateEvent e) throws Exception {
        // Keep sending messages whenever the current socket buffer has room.
        generateTraffic(e);
    }
*/
    @Override
    public void messageReceived(ChannelHandlerContext ctx, MessageEvent e) throws Exception {
        // Server is supposed to send nothing.  Therefore, do nothing.
	System.out.println("DiscardClientHandler::messageReceived");
	Channel ch = e.getChannel();
	/*
	Object msg = e.getMessage();
	if (msg instanceof HttpMessage) {
		System.out.println("HttpMessage");
	} else if (msg instanceof HttpChunk) {
		System.out.println("HttpChunk");
	} else {
		System.out.println("other");
	}
	*/
	ChannelBuffer buf = (ChannelBuffer) e.getMessage();
	/*
	while (buf.readable()) {
		System.out.print((char) buf.readByte());
	}
	*/
	System.out.println(buf);
//	ch.close();
    }

    @Override
    public void writeComplete(ChannelHandlerContext ctx, WriteCompletionEvent e) throws Exception {
        //transferredBytes.addAndGet(e.getWrittenAmount());
    }

    @Override
    public void exceptionCaught(ChannelHandlerContext ctx, ExceptionEvent e) {
        // Close the connection when an exception is raised.
        logger.log(
                Level.WARNING,
                "Unexpected exception from downstream.",
                e.getCause());
        e.getChannel().close();
    }
/*
    private void generateTraffic(ChannelStateEvent e) {
        // Keep generating traffic until the channel is unwritable.
        // A channel becomes unwritable when its internal buffer is full.
        // If you keep writing messages ignoring this property,
        // you will end up with an OutOfMemoryError.
        Channel channel = e.getChannel();
        while (channel.isWritable()) {
            ChannelBuffer m = nextMessage();
            if (m == null) {
                break;
            }

//	    rateLimiter.acquire(content.length);
            channel.write(m);
        }
    }

    private ChannelBuffer nextMessage() {
        return ChannelBuffers.wrappedBuffer(content);
    }
*/
}
