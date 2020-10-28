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

package com.github.housepower.jdbc.data.type;

import com.github.housepower.jdbc.data.IDataType;
import com.github.housepower.jdbc.misc.SQLLexer;
import com.github.housepower.jdbc.serializer.BinaryDeserializer;
import com.github.housepower.jdbc.serializer.BinarySerializer;

import java.io.IOException;
import java.sql.SQLException;
import java.sql.Types;

public class DataTypeFloat64 implements IDataType {

    private static final Double DEFAULT_VALUE = 0.0D;

    @Override
    public String name() {
        return "Float64";
    }

    @Override
    public int sqlTypeId() {
        return Types.DOUBLE;
    }

    @Override
    public Object defaultValue() {
        return DEFAULT_VALUE;
    }

    @Override
    public Class javaTypeClass() {
        return Double.class;
    }

    @Override
    public boolean nullable() {
        return false;
    }

	@Override
	public int getPrecision() {
		return 17;
	}

    @Override
    public int getScale() {
        return 17;
    }

    @Override
    public void serializeBinary(Object data, BinarySerializer serializer) throws SQLException, IOException {
        serializer.writeDouble(((Number) data).doubleValue());
    }

    @Override
    public Double deserializeBinary(BinaryDeserializer deserializer) throws SQLException, IOException {
        return deserializer.readDouble();
    }

    @Override
    public Double[] deserializeBinaryBulk(int rows, BinaryDeserializer deserializer) throws IOException {
        Double[] data = new Double[rows];
        for (int row = 0; row < rows; row++) {
            data[row] = deserializer.readDouble();
        }
        return data;
    }

    @Override
    public Object deserializeTextQuoted(SQLLexer lexer) throws SQLException {
        return lexer.numberLiteral().doubleValue();
    }
}
