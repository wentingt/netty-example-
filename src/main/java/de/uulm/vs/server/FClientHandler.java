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

import java.util.Map;
import java.util.Map.Entry;

import org.jboss.netty.buffer.ChannelBuffer;
import org.jboss.netty.buffer.ChannelBuffers;
import org.jboss.netty.channel.*;
import org.jboss.netty.handler.codec.http.DefaultHttpRequest;
import org.jboss.netty.handler.codec.http.HttpChunkAggregator;
import org.jboss.netty.handler.codec.http.*;
import org.jboss.netty.buffer.*;

import java.util.logging.Level;
import java.util.logging.Logger;

import static org.jboss.netty.handler.codec.http.HttpMethod.*;
import static org.jboss.netty.handler.codec.http.HttpVersion.HTTP_1_1;

/**
 * Handles a client-side channel.
 *
 * @author <a href="http://www.jboss.org/netty/">The Netty Project</a>
 * @author <a href="http://gleamynode.net/">Trustin Lee</a>
 *
 * @version $Rev: 2121 $, $Date: 2010-02-02 09:38:07 +0900 (Tue, 02 Feb 2010) $
 */
public class FClientHandler extends SimpleChannelUpstreamHandler {
    private final Map<ShuffleId, ShuffleInfo> shuffleInfoMap;
    public FClientHandler(Map<ShuffleId, ShuffleInfo> shuffleInfoMap) {
        this.shuffleInfoMap = shuffleInfoMap;
    }
    private static final Logger logger = Logger.getLogger(
            FClientHandler.class.getName());
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
        System.out.println("FClientHandler::channelConnected");
        sendRequest(e.getChannel());
    }
/*
    @Override
    public void channelInterestChanged(ChannelHandlerContext ctx, ChannelStateEvent e) throws Exception {
        // Keep sending messages whenever the current socket buffer has room.
        generateTraffic(e);
    }
*/
    void sendRequest(Channel ch) {
        HttpRequest request = new DefaultHttpRequest(HTTP_1_1, POST, "shuffleRate");
        ch.write(request);
        //ch.write((ChannelBuffer) shuffleInfoMap);
        System.out.println(request);
    }

    @Override
    public void messageReceived(ChannelHandlerContext ctx, MessageEvent e) throws Exception {
        // Server is supposed to send nothing.  Therefore, do nothing.
        System.out.println("FClientHandler::messageReceived");
        Channel ch = e.getChannel();
        ChannelBuffer buf = (ChannelBuffer) e.getMessage();

        while (buf.readable()) {
            System.out.print((char) buf.readByte());
        }
        System.out.println(buf);
        for (Map.Entry<ShuffleId, ShuffleInfo> entry: shuffleInfoMap.entrySet()) {
            ShuffleInfo shuffleInfo = entry.getValue();
            shuffleInfo.shuffleRate += 1024;
        }
        
        sendRequest(e.getChannel());
        //ch.close();
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
