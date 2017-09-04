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
package de.uulm.vs.server;

import java.util.concurrent.atomic.AtomicLong;
import java.util.logging.Level;
import java.util.logging.Logger;

import org.jboss.netty.buffer.ChannelBuffer;
import org.jboss.netty.buffer.ChannelBuffers;
import org.jboss.netty.channel.Channel;
import org.jboss.netty.channel.ChannelEvent;
import org.jboss.netty.channel.ChannelHandlerContext;
import org.jboss.netty.channel.ChannelStateEvent;
import org.jboss.netty.channel.ExceptionEvent;
import org.jboss.netty.channel.MessageEvent;
import org.jboss.netty.channel.SimpleChannelUpstreamHandler;

import org.jboss.netty.channel.ChannelFuture;
import org.jboss.netty.channel.ChannelFutureListener;

import com.google.common.util.concurrent.RateLimiter;
/**
 * Handles a server-side channel.
 *
 * @author <a href="http://www.jboss.org/netty/">The Netty Project</a>
 * @author <a href="http://gleamynode.net/">Trustin Lee</a>
 *
 * @version $Rev: 2121 $, $Date: 2010-02-02 09:38:07 +0900 (Tue, 02 Feb 2010) $
 */
public class DiscardServerHandler extends SimpleChannelUpstreamHandler {
    private static final Logger logger = Logger.getLogger(
            DiscardServerHandler.class.getName());
//    private final AtomicLong transferredBytes = new AtomicLong();
/*
    public long getTransferredBytes() {
        return transferredBytes.get();
    }
*/
     double permitsPerSecond = 100;
      private final RateLimiter rateLimiter = RateLimiter.create(permitsPerSecond);

    @Override
    public void handleUpstream(ChannelHandlerContext ctx, ChannelEvent e) throws Exception {
        if (e instanceof ChannelStateEvent) {
            logger.info(e.toString());
        }

        // Let SimpleChannelHandler call actual event handler methods below.
        super.handleUpstream(ctx, e);
    }
    @Override
    public void messageReceived(ChannelHandlerContext ctx, MessageEvent e) throws Exception {
        // Discard received data silently by doing nothing.
	System.out.println("MessageReceived");
//        transferredBytes.addAndGet(((ChannelBuffer) e.getMessage()).readableBytes());
	    Channel ch = e.getChannel();
	    ChannelBuffer buf = (ChannelBuffer) e.getMessage();
//	    System.out.println(buf);

		while (buf.readable()) {
			byte b[] = { buf.readByte() };
			 rateLimiter.acquire();
			 System.out.println("sent");
			ch.write(ChannelBuffers.wrappedBuffer(b)) ;
		} 
	System.out.println("doneWritten");

	    ChannelFuture f = ch.write(buf);
	    f.addListener(new ChannelFutureListener() {
		    public void operationComplete(ChannelFuture future) {
			    System.out.println("MessageSent");
			future.getChannel().close();
		    }
	    });
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
}
