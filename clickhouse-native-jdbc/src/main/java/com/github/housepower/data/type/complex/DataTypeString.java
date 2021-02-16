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

package com.github.housepower.data.type.complex;

import com.github.housepower.client.NativeContext;
import com.github.housepower.data.IDataType;
import com.github.housepower.io.ByteBufHelper;
import com.github.housepower.misc.SQLLexer;
import io.netty.buffer.ByteBuf;
import io.netty.util.AsciiString;

import java.nio.charset.Charset;
import java.nio.charset.StandardCharsets;
import java.sql.SQLException;
import java.sql.Types;

public class DataTypeString implements IDataType<CharSequence, String>, ByteBufHelper {

    public static DataTypeCreator<CharSequence, String> CREATOR = (lexer, serverContext) -> new DataTypeString(serverContext);

    private final Charset charset;

    public DataTypeString(NativeContext.ServerContext serverContext) {
        this.charset = serverContext.getConfigure().charset();
    }

    @Override
    public String name() {
        return "String";
    }

    @Override
    public int sqlTypeId() {
        return Types.VARCHAR;
    }

    @Override
    public String defaultValue() {
        return "";
    }

    @Override
    public Class<CharSequence> javaType() {
        return CharSequence.class;
    }

    @Override
    public Class<String> jdbcJavaType() {
        return String.class;
    }

    @Override
    public int getPrecision() {
        return 0;
    }

    @Override
    public int getScale() {
        return 0;
    }

    @Override
    public void encode(ByteBuf buf, CharSequence data) {
        if (data instanceof AsciiString) {
            buf.writeCharSequence(data, StandardCharsets.ISO_8859_1);
        } else {
            writeCharSeqBinary(buf, data, charset);
        }
    }

    @Override
    public CharSequence decode(ByteBuf buf) {
        return readCharSeqBinary(buf, charset);
    }

    @Override
    public CharSequence deserializeText(SQLLexer lexer) throws SQLException {
        return lexer.stringView();
    }

    @Override
    public String[] getAliases() {
        return new String[]{
                "LONGBLOB",
                "MEDIUMBLOB",
                "TINYBLOB",
                "MEDIUMTEXT",
                "CHAR",
                "VARCHAR",
                "TEXT",
                "TINYTEXT",
                "LONGTEXT",
                "BLOB"};
    }
}
