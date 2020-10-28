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

package com.github.housepower.jdbc.buffer;

import com.github.housepower.jdbc.tool.FragmentBuffedReader;
import org.junit.Assert;
import org.junit.Test;

import net.jpountz.lz4.LZ4Compressor;
import net.jpountz.lz4.LZ4Factory;

public class CompressedBuffedReaderTest {

    @Test
    public void successfullyReadCompressedData() throws Exception {

        CompressedBuffedReader compressedBuffed = new CompressedBuffedReader(
            new FragmentBuffedReader(compressedData(new byte[] {1, 2, 3}), compressedData(new byte[] {4, 5, 6, 7}))
        );

        Assert.assertEquals(compressedBuffed.readBinary(), 1);

        byte[] bytes = new byte[5];
        compressedBuffed.readBinary(bytes);

        Assert.assertEquals(bytes[0], 2);
        Assert.assertEquals(bytes[1], 3);
        Assert.assertEquals(bytes[2], 4);
        Assert.assertEquals(bytes[3], 5);
        Assert.assertEquals(bytes[4], 6);

        Assert.assertEquals(compressedBuffed.readBinary(), 7);
    }


    private byte[] compressedData(byte[] bytes) {
        LZ4Compressor lz4Compressor = LZ4Factory.safeInstance().fastCompressor();
        byte[] compressData = lz4Compressor.compress(bytes);
        byte[] data = new byte[compressData.length + 9 + 16];

        data[16] = (byte) (0x82 & 0xFF);
        System.arraycopy(compressData, 0, data, 9 + 16, compressData.length);
        System.arraycopy(littleEndian(compressData.length + 9), 0, data, 17, 4);
        System.arraycopy(littleEndian(bytes.length), 0, data, 21, 4);

        return data;
    }

    private byte[] littleEndian(int x) {
        byte[] data = new byte[4];

        data[0] = (byte) (x & 0xFF);
        data[1] = (byte) ((byte) (x >> 8) & 0xFF);
        data[2] = (byte) ((byte) (x >> 16) & 0xFF);
        data[3] = (byte) ((byte) (x >> 24) & 0xFF);

        return data;
    }
}
