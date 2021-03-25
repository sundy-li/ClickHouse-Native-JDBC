/*
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.github.housepower.io;

import com.github.housepower.misc.NettyUtil;
import com.github.housepower.settings.ClickHouseDefines;
import io.netty.buffer.ByteBuf;
import io.netty.util.ReferenceCountUtil;

import java.io.EOFException;
import java.io.IOException;
import java.io.InputStream;
import java.io.UncheckedIOException;
import java.net.Socket;
import java.nio.charset.Charset;
import java.nio.charset.StandardCharsets;

public class SocketSource implements ISource, ByteBufHelper {

    private final InputStream in;
    private final ByteBuf buf;

    public SocketSource(Socket socket) throws IOException {
        this(socket.getInputStream(), ClickHouseDefines.SOCKET_RECV_BUFFER_BYTES);
    }

    SocketSource(InputStream in, int capacity) {
        this.in = in;
        this.buf = NettyUtil.alloc().buffer(capacity, capacity);
    }

    @Override
    public void skipBytes(int len) {
        maybeRefill(len);
        buf.skipBytes(len);
    }

    @Override
    public boolean readBoolean() {
        maybeRefill(1);
        return buf.readBoolean();
    }

    @Override
    public byte readByte() {
        maybeRefill(1);
        return buf.readByte();
    }

    @Override
    public short readShortLE() {
        maybeRefill(2);
        return buf.readShortLE();
    }

    @Override
    public int readIntLE() {
        maybeRefill(4);
        return buf.readIntLE();
    }

    @Override
    public long readLongLE() {
        maybeRefill(8);
        return buf.readLongLE();
    }

    @Override
    public long readVarInt() {
        maybeRefill(1);
        return readVarInt(buf);
    }

    @Override
    public float readFloatLE() {
        maybeRefill(4);
        return buf.readFloatLE();
    }

    @Override
    public double readDoubleLE() {
        maybeRefill(8);
        return buf.readDoubleLE();
    }

    @Override
    public ByteBuf readSlice(int len) {
        maybeRefill(len);
        return buf.readSlice(len);
    }

    @Override
    public ByteBuf readRetainedSlice(int len) {
        ByteBuf ret = NettyUtil.alloc().buffer(len);
        int p = 0;
        do {
            maybeRefill(len);
            int writableBytes = len - p;
            int l = Math.min(writableBytes, buf.readableBytes());
            if (l > 0) {
                ret.writeBytes(buf, l);
                p += l;
            }
        } while (p < len);
        return ret;
    }

    @Override
    public CharSequence readCharSequence(int len, Charset charset) {
        if (len == 0)
            return "";
        maybeRefill(len);
        return buf.readCharSequence(len, charset);
    }

    @Override
    public ByteBuf readSliceBinary() {
        int len = (int) readVarInt();
        maybeRefill(len);
        return buf.readSlice(len);
    }

    @Override
    public CharSequence readCharSequenceBinary(Charset charset) {
        int len = (int) readVarInt();
        return readCharSequence(len, charset);
    }

    @Override
    public String readUTF8Binary() {
        return readCharSequenceBinary(StandardCharsets.UTF_8).toString();
    }

    @Override
    public void close() {
        ReferenceCountUtil.safeRelease(buf);
    }

    private void maybeRefill(int atLeastReadableBytes) {
        if (buf.isReadable(atLeastReadableBytes))
            return;
        try {
            ByteBuf remaining = null;
            if (buf.isReadable()) {
                int remainingLen = buf.readableBytes();
                remaining = NettyUtil.alloc().buffer(remainingLen, remainingLen);
                buf.readBytes(remaining);
            }
            buf.clear();
            if (remaining != null) {
                buf.writeBytes(remaining);
                ReferenceCountUtil.safeRelease(remaining);
            }
            do {
                if (buf.writeBytes(in, buf.writableBytes()) <= 0) {
                    throw new UncheckedIOException(new EOFException("Attempt to read after eof."));
                }
            } while (!buf.isReadable(atLeastReadableBytes));
        } catch (IOException rethrow) {
            throw new UncheckedIOException(rethrow);
        }
    }
}