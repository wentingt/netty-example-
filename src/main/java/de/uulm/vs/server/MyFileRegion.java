package de.uulm.vs.server;
import org.jboss.netty.channel.ChannelHandlerContext;
import org.jboss.netty.channel.ChannelStateEvent;
import java.net.InetAddress;
import com.google.common.util.concurrent.RateLimiter;
import java.io.IOException;
import org.jboss.netty.buffer.ChannelBuffer;
import org.jboss.netty.buffer.ChannelBuffers;
import org.jboss.netty.channel.Channel;
import org.jboss.netty.channel.ChannelFuture;
import org.jboss.netty.channel.ChannelFutureListener;
import org.jboss.netty.channel.ChannelFutureProgressListener;
import org.jboss.netty.channel.ChannelHandlerContext;
import org.jboss.netty.channel.DefaultFileRegion;
import org.jboss.netty.channel.ExceptionEvent;
import org.jboss.netty.channel.FileRegion;
import org.jboss.netty.channel.MessageEvent;
import org.jboss.netty.channel.SimpleChannelUpstreamHandler;
import org.jboss.netty.handler.codec.frame.TooLongFrameException;
import org.jboss.netty.handler.codec.http.DefaultHttpResponse;
import org.jboss.netty.handler.codec.http.HttpRequest;
import org.jboss.netty.handler.codec.http.HttpResponse;
import org.jboss.netty.handler.codec.http.HttpResponseStatus;
import org.jboss.netty.handler.ssl.SslHandler;
import org.jboss.netty.handler.stream.ChunkedFile;
import org.jboss.netty.util.CharsetUtil;
import java.nio.channels.FileChannel;
import java.nio.channels.WritableByteChannel;
import java.nio.ByteBuffer;

import java.util.logging.Level;
import java.util.logging.Logger;
import org.jboss.netty.channel.ChannelEvent;
import org.jboss.netty.channel.ChannelHandlerContext;
import org.jboss.netty.channel.ChannelState;
import org.jboss.netty.channel.ChannelStateEvent;
import org.jboss.netty.channel.ExceptionEvent;

import org.jboss.netty.channel.group.ChannelGroup;
import org.jboss.netty.channel.group.DefaultChannelGroup;
import javax.activation.MimetypesFileTypeMap;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.RandomAccessFile;
import java.io.UnsupportedEncodingException;
import java.net.URLDecoder;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.GregorianCalendar;
import java.util.Locale;
import java.util.TimeZone;

import static org.jboss.netty.handler.codec.http.HttpHeaders.Names.*;
import static org.jboss.netty.handler.codec.http.HttpHeaders.*;
import static org.jboss.netty.handler.codec.http.HttpMethod.*;
import static org.jboss.netty.handler.codec.http.HttpResponseStatus.*;
import static org.jboss.netty.handler.codec.http.HttpVersion.*;
import java.lang.Thread;


public class MyFileRegion extends DefaultFileRegion {
    private final long count;
    private final long position;
    private final FileChannel fileChannel;
    private RateLimiter rateLimiter;
    private ShuffleInfo shuffleInfo;

    private int CHUNKSIZE;

    public MyFileRegion(FileChannel file, long position, long count,
                              int CHUNKSIZE,
                              RateLimiter rateLimiter,
                        ShuffleInfo shuffleInfo) {
        super(file, position, count);
        this.fileChannel = file;
        this.position = position;
        this.count = count;

        this.rateLimiter = rateLimiter;
        this.shuffleInfo = shuffleInfo;
        this.CHUNKSIZE = CHUNKSIZE;
    }

    @Override
    public long transferTo(WritableByteChannel target, long position)
            throws IOException {
        //return super.transferTo(target, position);

        //return transferToZero(target, position);
        System.out.println("Custom");
        return transferToNonZ(target, position);
    }
    public long transferToZero(WritableByteChannel target, long position)
            throws IOException {
        long actualCount = this.count - position;
        if (actualCount < 0 || position < 0) {
            throw new IllegalArgumentException(
                    "position out of range: " + position +
                            " (expected: 0 - " + (this.count - 1) + ')');
        }
        if (actualCount == 0) {
            return 0L;
        }
        long trans = actualCount;
        long readSize;
        while(trans > 0L) {
            rateLimiter.acquire();
            readSize = fileChannel.transferTo(this.position + position, CHUNKSIZE, target);
            trans -= readSize;
            position += readSize;
        }
        return actualCount - trans;
    }
    public long transferToNonZ(WritableByteChannel target, long position)
            throws IOException {
        //---------------->
//		return super.transferTo(target, position);
        long actualCount = this.count - position;
        if (actualCount < 0 || position < 0) {
            throw new IllegalArgumentException(
                    "position out of range: " + position +
                            " (expected: 0 - " + (this.count - 1) + ')');
        }
        if (actualCount == 0) {
            return 0L;
        }
        long trans = actualCount;
        int readSize;
        ByteBuffer byteBuffer = ByteBuffer.allocate(CHUNKSIZE);
        while(trans > 0L &&
                (readSize = fileChannel.read(byteBuffer, this.position+position)) > 0) {
            if(readSize < trans) {
                trans -= readSize;
                position += readSize;
                byteBuffer.flip(); //
                //rateLimiter.acquire(readSize);
            } else {
                byteBuffer.limit((int)trans);
                byteBuffer.position(0);
                position += trans;
                trans = 0;
                //rateLimiter.acquire(trans);
            }

            System.err.println(rateLimiter.getRate());

            rateLimiter.acquire();
            rateLimiter.setRate((double) shuffleInfo.shuffleRate / (double) CHUNKSIZE);

            while(byteBuffer.hasRemaining()) {
                target.write(byteBuffer);
            }
            byteBuffer.clear();

            // boolean setProgress(long amount, long current, long total)
            if (shuffleInfo.shuffleSize > CHUNKSIZE)
                shuffleInfo.shuffleSize -= CHUNKSIZE;
            else shuffleInfo.shuffleSize = 0;

        }
        return actualCount - trans;
    }
}
