/*
 * Copyright 2012 The Netty Project
 *
 * The Netty Project licenses this file to you under the Apache License,
 * version 2.0 (the "License"); you may not use this file except in compliance
 * with the License. You may obtain a copy of the License at:
 *
 *   http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS, WITHOUT
 * WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied. See the
 * License for the specific language governing permissions and limitations
 * under the License.
 */
//package org.jboss.netty.example.http.file;
package de.uulm.vs.client;

import org.jboss.netty.handler.codec.http.HttpChunk;
import org.jboss.netty.handler.codec.http.HttpMessage;  
import static org.jboss.netty.channel.Channels.*;
 import org.jboss.netty.buffer.ChannelBuffer;
       import org.jboss.netty.channel.ChannelHandlerContext;
	   import org.jboss.netty.channel.MessageEvent;

import org.jboss.netty.channel.ChannelPipeline;
import org.jboss.netty.channel.ChannelPipelineFactory;
import org.jboss.netty.handler.codec.http.HttpChunkAggregator;
import org.jboss.netty.handler.codec.http.HttpHeaders;
import org.jboss.netty.handler.codec.http.HttpRequestDecoder;
import org.jboss.netty.handler.codec.http.HttpResponseEncoder;
import org.jboss.netty.handler.codec.http.HttpRequestEncoder;
import org.jboss.netty.handler.codec.http.HttpResponseDecoder;
import org.jboss.netty.handler.stream.ChunkedWriteHandler;

// Uncomment the following lines if you want HTTPS
/*
import javax.net.ssl.SSLEngine;
import ha.ha.ha.example.securechat.SecureChatSslContextFactory;
import org.jboss.netty.handler.ssl.SslHandler;
*/

public class DiscardClientPipelineFactory implements ChannelPipelineFactory {
    public ChannelPipeline getPipeline() throws Exception {
        // Create a default pipeline implementation.
        ChannelPipeline pipeline = pipeline();

        // Uncomment the following lines if you want HTTPS
	 /*
        SSLEngine engine = SecureChatSslContextFactory.getServerContext().createSSLEngine();
        engine.setUseClientMode(true);
        pipeline.addLast("ssl", new SslHandler(engine));
	*/

//        pipeline.addLast("decoder", new myHttpResponseDecoder());
//        pipeline.addLast("aggregator", new myHttpChunkAggregator(65536));
        pipeline.addLast("encoder", new HttpRequestEncoder());
//        pipeline.addLast("chunkedWriter", new ChunkedWriteHandler());

        pipeline.addLast("handler", new DiscardClientHandler());
        return pipeline;
    }
////////////////////////////////////////////////////////////////////////
	class myHttpResponseDecoder extends HttpResponseDecoder {
		@Override
		public void messageReceived(ChannelHandlerContext ctx, MessageEvent e) throws Exception {
			System.out.println("myHttpResponseDecoder::messageReceived");
			System.out.println(e.getMessage());
			ChannelBuffer buf = (ChannelBuffer) e.getMessage();
			while (buf.readable()) {
				System.out.print((char) buf.readByte());
			}
			System.out.println();
			super.messageReceived(ctx, e);
		}
	}

	class myHttpChunkAggregator extends HttpChunkAggregator {
		public myHttpChunkAggregator(int mL) {
			super(mL);
		}

		@Override
		public void messageReceived(ChannelHandlerContext ctx, MessageEvent e) throws Exception {
			System.out.println("myHttpChunkAggregator::messageReceived");
			Object msg = e.getMessage();
			if (msg instanceof HttpMessage) {
				System.out.println("HttpMessage");
			} else if (msg instanceof HttpChunk) {
				System.out.println("HttpChunk");
			} else {
				System.out.println("other");
			}
			super.messageReceived(ctx, e);
		}
	}
}





