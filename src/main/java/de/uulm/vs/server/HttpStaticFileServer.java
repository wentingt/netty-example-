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

import java.io.InterruptedIOException;
import java.net.InetSocketAddress;
import java.util.LinkedHashMap;
import java.util.concurrent.Executors;

import java.util.Map;
import java.util.Map.Entry;
import java.io.IOException;
import java.net.*;
import java.io.*;

import org.jboss.netty.buffer.*;
import org.jboss.netty.channel.*;
import org.jboss.netty.handler.codec.http.HttpRequest;
import org.jboss.netty.handler.codec.http.DefaultHttpRequest;
import static org.jboss.netty.handler.codec.http.HttpVersion.HTTP_1_1;
import static org.jboss.netty.handler.codec.http.HttpMethod.*;

import java.net.InetSocketAddress;
import java.util.concurrent.Executors;

import org.jboss.netty.bootstrap.ClientBootstrap;
import org.jboss.netty.channel.socket.nio.NioClientSocketChannelFactory;

import org.jboss.netty.handler.codec.http.HttpChunk;
import org.jboss.netty.handler.codec.http.HttpMessage;
import static org.jboss.netty.channel.Channels.*;
import org.jboss.netty.buffer.ChannelBuffer;

import org.jboss.netty.channel.ChannelPipeline;
import org.jboss.netty.channel.ChannelPipelineFactory;
import org.jboss.netty.handler.codec.http.HttpChunkAggregator;
import org.jboss.netty.handler.codec.http.HttpHeaders;
import org.jboss.netty.handler.codec.http.HttpRequestDecoder;
import org.jboss.netty.handler.codec.http.HttpResponseEncoder;
import org.jboss.netty.handler.codec.http.HttpRequestEncoder;
import org.jboss.netty.handler.codec.http.HttpResponseDecoder;
import org.jboss.netty.handler.stream.ChunkedWriteHandler;

import com.google.common.util.concurrent.RateLimiter;
import org.jboss.netty.bootstrap.ClientBootstrap;
import org.jboss.netty.bootstrap.ServerBootstrap;
import org.jboss.netty.buffer.ChannelBuffer;
import org.jboss.netty.channel.ChannelFutureProgressListener;
import org.jboss.netty.channel.socket.nio.*;

import java.util.Map;
import java.util.Map.Entry;
import java.io.IOException;
//import java.util.concurrent;

public class HttpStaticFileServer {

	public static String getWorld() {
		return "World";
	}

    private final int port;

    public HttpStaticFileServer(int port) {
        this.port = port;
    }

    public void run() {
        // Configure the server.
        ServerBootstrap serverBootstrap = new ServerBootstrap(
                new NioServerSocketChannelFactory(
                        Executors.newCachedThreadPool(),
                        Executors.newCachedThreadPool()));
        // Set up the event pipeline factory.
        serverBootstrap.setPipelineFactory(new HttpStaticFileServerPipelineFactory(shuffleInfoMap));
        // Bind and start to accept incoming connections.
        serverBootstrap.bind(new InetSocketAddress(port));

        ClientBootstrap clientBootstrap = new ClientBootstrap(
                new NioClientSocketChannelFactory(
                        Executors.newCachedThreadPool(),
                        Executors.newCachedThreadPool()));
        // Set up the pipeline factory.
        clientBootstrap.setPipelineFactory(new FClientPipelineFactory(shuffleInfoMap));
        clientBootstrap.setOption("tcpNoDelay", true);
        clientBootstrap.setOption("keepAlive", true);
        InetSocketAddress inetsocketAddress =
                new InetSocketAddress("localhost", 9090);
        ChannelFuture channelFuture = clientBootstrap.connect(inetsocketAddress);
        Channel channel = channelFuture.getChannel();
        // Wait until the connection is closed or the connection attempt fails.
        channel.getCloseFuture().awaitUninterruptibly();
    }

    public static void main(String[] args) {
        //new Fetch(shuffleInfoMap).start();

        int port;
        if (args.length > 0) {
            port = Integer.parseInt(args[0]);
        } else {
            port = 8080;
        }
        new HttpStaticFileServer(port).run();
    }

    private static final Map<ShuffleId, ShuffleInfo> shuffleInfoMap = new LinkedHashMap<ShuffleId, ShuffleInfo>();
    //private final Map<ShuffleId, ShuffleRate> shuffleRateMap = new LinkedHashMap<ShuffleId, ShuffleRate>();
}

class ShuffleId {
    static int Id = 0;
    int id;
    public ShuffleId() {
        this.id = this.Id;
        this.Id += 1;
    }
    public void print() {
        System.out.println("id: " + this.id);
    }
}

class ShuffleInfo{
    int jobId;
    int mapId;
    int reduceId;
    String mapHostName;
    String reduceHostName;
    long shuffleSize;
    long shuffleRate;
    public ShuffleInfo() {
        this.shuffleRate = 1024 * 1024;
    }
    public void print() {
        System.out.println("jobId: " + jobId + " , mapId: " + mapId + " , reduceId: " + reduceId);
        System.out.println("mapHostName: " + mapHostName + " , reduceHostName: " + reduceHostName);
        System.out.println("shuffleSize: " + shuffleSize + " , shuffleRate: " + shuffleRate);
    }
}
