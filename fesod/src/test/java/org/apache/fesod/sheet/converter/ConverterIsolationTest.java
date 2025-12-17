/*
 * Licensed to the Apache Software Foundation (ASF) under one
 * or more contributor license agreements.  See the NOTICE file
 * distributed with this work for additional information
 * regarding copyright ownership.  The ASF licenses this file
 * to you under the Apache License, Version 2.0 (the
 * "License"); you may not use this file except in compliance
 * with the License.  You may obtain a copy of the License at
 *
 *   http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing,
 * software distributed under the License is distributed on an
 * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 * KIND, either express or implied.  See the License for the
 * specific language governing permissions and limitations
 * under the License.
 */

package org.apache.fesod.sheet.converter;

import static org.junit.jupiter.api.Assertions.*;
import java.io.File;
import org.apache.fesod.sheet.ExcelWriter;
import org.apache.fesod.sheet.FesodSheet;
import org.apache.fesod.sheet.converters.Converter;
import org.apache.fesod.sheet.enums.CellDataTypeEnum;
import org.apache.fesod.sheet.metadata.GlobalConfiguration;
import org.apache.fesod.sheet.metadata.data.WriteCellData;
import org.apache.fesod.sheet.metadata.property.ExcelContentProperty;
import org.apache.fesod.sheet.util.TestFileUtil;
import org.junit.jupiter.api.Test;

public class ConverterIsolationTest {

    public static class TestData {}

    public static class ConverterA implements Converter<String> {

        @Override
        public Class<String> supportJavaTypeKey() {
            return String.class;
        }

        @Override
        public CellDataTypeEnum supportExcelTypeKey() {
            return CellDataTypeEnum.STRING;
        }

        @Override
        public WriteCellData<?> convertToExcelData(String value, ExcelContentProperty p, GlobalConfiguration g) {
            return new WriteCellData<>("A-" + value);
        }
    }

    @Test
    public void testConverterIsolation() {
        ExcelWriter writer1 = FesodSheet.write(new File(TestFileUtil.getPath() + "writer1.xlsx"), TestData.class)
                .registerConverter(new ConverterA())
                .build();

        ExcelWriter writer2 = FesodSheet.write(new File(TestFileUtil.getPath() + "writer2.xlsx"), TestData.class)
                .build();

        boolean writer2HasConverterA = writer2.writeContext().currentWriteHolder().converterMap().values().stream()
                .anyMatch(c -> c instanceof ConverterA);

        writer1.finish();
        writer2.finish();

        assertFalse(writer2HasConverterA, "Custom converter should not leak between ExcelWriter instances");
    }
}
