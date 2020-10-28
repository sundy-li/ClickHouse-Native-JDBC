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

package com.github.housepower.jdbc.benchmark;

import com.google.common.base.Strings;

import org.junit.Assert;
import org.junit.Test;
import org.openjdk.jmh.annotations.Benchmark;
import org.openjdk.jmh.annotations.BenchmarkMode;
import org.openjdk.jmh.annotations.Mode;
import org.openjdk.jmh.annotations.OutputTimeUnit;
import org.openjdk.jmh.annotations.Scope;
import org.openjdk.jmh.annotations.State;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.util.concurrent.TimeUnit;

/**
 */
@BenchmarkMode(Mode.AverageTime)
@OutputTimeUnit(TimeUnit.MILLISECONDS)
@State(Scope.Thread)
public class WideColumnStringInsertIBenchmark extends AbstractInsertIBenchmark {

    @Benchmark
    @Test
    public void benchInsertNative() throws Exception {
        withConnection(benchInsert, ConnectionType.NATIVE);
    }

    @Benchmark
    @Test
    public void benchInsertHttp() throws Exception {
        withConnection(benchInsert, ConnectionType.HTTP);
    }

    public WithConnection benchInsert = connection -> {
        wideColumnPrepare(connection, "String");
        String params = Strings.repeat("?, ", columnNum);
        PreparedStatement pstmt = connection.prepareStatement("INSERT INTO "  + getTableName() +" values("   + params.substring(0, params.length()-2) + ")");

        for (int i = 0; i < batchSize; i++) {
            for (int j = 0; j < columnNum; j++ ) {
                pstmt.setString(j + 1, j + 1 + "");
            }
            pstmt.addBatch();
        }
        int []res = pstmt.executeBatch();
        Assert.assertEquals(res.length, batchSize);
        wideColumnAfter(connection);
    };

}
