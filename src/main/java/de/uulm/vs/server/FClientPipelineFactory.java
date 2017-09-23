package de.uulm.vs.server;

import org.jboss.netty.bootstrap.ClientBootstrap;
import org.jboss.netty.channel.Channel;
import org.jboss.netty.channel.ChannelFuture;
import org.jboss.netty.channel.ChannelPipeline;
import org.jboss.netty.channel.ChannelPipelineFactory;
import org.jboss.netty.channel.socket.nio.NioClientSocketChannelFactory;
import org.jboss.netty.handler.codec.http.*;

import java.io.IOException;
import java.net.InetSocketAddress;
import java.util.Map;
import java.util.concurrent.Executors;

import static org.jboss.netty.channel.Channels.pipeline;

class FClientPipelineFactory implements ChannelPipelineFactory {
    private final Map<ShuffleId, ShuffleInfo> shuffleInfoMap;
    public FClientPipelineFactory(Map<ShuffleId, ShuffleInfo> shuffleInfoMap) {
        this.shuffleInfoMap = shuffleInfoMap;
    }
    public ChannelPipeline getPipeline() throws Exception {
        ChannelPipeline pipeline = pipeline();
        pipeline.addLast("decoder", new HttpResponseDecoder());
        pipeline.addLast("aggregator", new HttpChunkAggregator(65536));
        pipeline.addLast("encoder", new HttpRequestEncoder());
        pipeline.addLast("handler", new FClientHandler(shuffleInfoMap));
        return pipeline;
    }
}

class Fetch extends Thread {
    private static final long SLEEP_TIME = 1000;
    private volatile boolean stopped = false;
    private final Map<ShuffleId, ShuffleInfo> shuffleInfoMap;

    private final ClientBootstrap bootstrap;
    private Channel channel;
    private ChannelFuture channelFuture;
    private final InetSocketAddress inetsocketAddress =
            new InetSocketAddress("localhost", 9090);
    // constructor
    public Fetch(Map<ShuffleId, ShuffleInfo> shuffleInfoMap) {
        this.shuffleInfoMap = shuffleInfoMap;
        // Configure the client.
        bootstrap = new ClientBootstrap(
                new NioClientSocketChannelFactory(
                        Executors.newCachedThreadPool(),
                        Executors.newCachedThreadPool()));
        // Set up the pipeline factory.
        bootstrap.setPipelineFactory(new FClientPipelineFactory(shuffleInfoMap));
        bootstrap.setOption("tcpNoDelay", true);
        bootstrap.setOption("keepAlive", true);
        channelFuture = bootstrap.connect(inetsocketAddress);
        channel = channelFuture.getChannel();
        // Wait until the connection is closed or the connection attempt fails.
        channel.getCloseFuture().awaitUninterruptibly();
    }

    @Override
    public void run() {
        try {
            while (!stopped && !Thread.currentThread().isInterrupted()) {
                try {
                    // send and receive
                    System.out.println("*** send and receive start");
                    connectGaia(); // add synchronization later
                    System.out.println("*** send and receive end");
                    // sleep
                    if (!Thread.currentThread().isInterrupted()) {
                        Thread.sleep(SLEEP_TIME);
                    }
                } catch (InterruptedException ie) {
                    return;
                }
            }
            //} catch (InterruptedException ie) {
            //  return;
        } catch (Throwable t) {
            return;
        }
    }
    private void connectGaia() throws IOException, InterruptedException {
        //throw new InterruptedIOException("test Interrupted");
        channelFuture = bootstrap.connect(inetsocketAddress);
        channel = channelFuture.getChannel();
        // Wait until the connection is closed or the connection attempt fails.
        channel.getCloseFuture().awaitUninterruptibly();

    }
    // may need
    public void shutdown() {
        bootstrap.releaseExternalResources();
    }
}