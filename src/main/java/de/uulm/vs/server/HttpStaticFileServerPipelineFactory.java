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
package de.uulm.vs.server;

import static org.jboss.netty.channel.Channels.*;
import org.jboss.netty.channel.ChannelEvent;
import org.jboss.netty.channel.ChannelHandlerContext;
import com.google.common.util.concurrent.RateLimiter;
import org.jboss.netty.channel.ChannelPipeline;
import org.jboss.netty.channel.ChannelPipelineFactory;
import org.jboss.netty.handler.codec.http.HttpChunkAggregator;
import org.jboss.netty.handler.codec.http.HttpRequestDecoder;
import org.jboss.netty.handler.codec.http.HttpResponseEncoder;
import org.jboss.netty.handler.stream.ChunkedWriteHandler;

// Uncomment the following lines if you want HTTPS
/*
import javax.net.ssl.SSLEngine;
import ha.ha.ha.example.securechat.SecureChatSslContextFactory;
import org.jboss.netty.handler.ssl.SslHandler;
*/
import java.util.Map;
import java.util.Map.Entry;

public class HttpStaticFileServerPipelineFactory implements ChannelPipelineFactory {
	private final Map<ShuffleId, ShuffleInfo> shuffleInfoMap;
	public HttpStaticFileServerPipelineFactory(Map<ShuffleId, ShuffleInfo> shuffleInfoMap) {
		this.shuffleInfoMap = shuffleInfoMap;
	}
    public ChannelPipeline getPipeline() throws Exception {
        // Create a default pipeline implementation.
        ChannelPipeline pipeline = pipeline();

        // Uncomment the following lines if you want HTTPS
/*
        SSLEngine engine = SecureChatSslContextFactory.getServerContext().createSSLEngine();
        engine.setUseClientMode(false);
        pipeline.addLast("ssl", new SslHandler(engine));
	*/

        pipeline.addLast("decoder", new HttpRequestDecoder());
        pipeline.addLast("aggregator", new HttpChunkAggregator(65536));
        pipeline.addLast("encoder", new HttpResponseEncoder());
        pipeline.addLast("chunkedWriter", new ChunkedWriteHandler());

        pipeline.addLast("handler", new HttpStaticFileServerHandler(shuffleInfoMap));
        return pipeline;
    }
/////////////////////
	public class myChunkedWriteHandler extends ChunkedWriteHandler {
    		public static final int CHUNKSIZE = 1024 * 64; // 1kB
     		double permitsPerSecond = 1024 * 1024 * 1024; // rate (byte / second)
      		private final RateLimiter rateLimiter = RateLimiter.create(permitsPerSecond);

		@Override
		 public void handleDownstream(ChannelHandlerContext ctx, ChannelEvent e)
	             throws Exception {
		     rateLimiter.acquire(CHUNKSIZE);
			super.handleDownstream(ctx, e);
		 }	     
		@Override
	    	public void handleUpstream(ChannelHandlerContext ctx, ChannelEvent e)
			throws Exception {
		     rateLimiter.acquire(CHUNKSIZE);
			super.handleUpstream(ctx, e);
		}
	}

}
