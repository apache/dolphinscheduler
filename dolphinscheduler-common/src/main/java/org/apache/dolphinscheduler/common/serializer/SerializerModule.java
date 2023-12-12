package org.apache.dolphinscheduler.common.serializer;

import com.fasterxml.jackson.databind.module.SimpleModule;
import com.xxdb.data.BasicAnyVector;
import com.xxdb.data.BasicArrayVector;
import com.xxdb.data.BasicBoolean;
import com.xxdb.data.BasicBooleanMatrix;
import com.xxdb.data.BasicBooleanVector;
import com.xxdb.data.BasicByte;
import com.xxdb.data.BasicByteMatrix;
import com.xxdb.data.BasicByteVector;
import com.xxdb.data.BasicChart;
import com.xxdb.data.BasicChunkMeta;
import com.xxdb.data.BasicComplex;
import com.xxdb.data.BasicComplexMatrix;
import com.xxdb.data.BasicComplexVector;
import com.xxdb.data.BasicDate;
import com.xxdb.data.BasicDateHour;
import com.xxdb.data.BasicDateHourMatrix;
import com.xxdb.data.BasicDateHourVector;
import com.xxdb.data.BasicDateMatrix;
import com.xxdb.data.BasicDateTime;
import com.xxdb.data.BasicDateTimeMatrix;
import com.xxdb.data.BasicDateTimeVector;
import com.xxdb.data.BasicDateVector;
import com.xxdb.data.BasicDecimal128;
import com.xxdb.data.BasicDecimal128Vector;
import com.xxdb.data.BasicDecimal32;
import com.xxdb.data.BasicDecimal32Vector;
import com.xxdb.data.BasicDecimal64;
import com.xxdb.data.BasicDecimal64Vector;
import com.xxdb.data.BasicDictionary;
import com.xxdb.data.BasicDouble;
import com.xxdb.data.BasicDoubleMatrix;
import com.xxdb.data.BasicDoubleVector;
import com.xxdb.data.BasicDuration;
import com.xxdb.data.BasicDurationVector;
import com.xxdb.data.BasicFloat;
import com.xxdb.data.BasicFloatMatrix;
import com.xxdb.data.BasicFloatVector;
import com.xxdb.data.BasicIPAddr;
import com.xxdb.data.BasicIPAddrVector;
import com.xxdb.data.BasicInt;
import com.xxdb.data.BasicInt128;
import com.xxdb.data.BasicInt128Vector;
import com.xxdb.data.BasicIntMatrix;
import com.xxdb.data.BasicIntVector;
import com.xxdb.data.BasicLong;
import com.xxdb.data.BasicLongMatrix;
import com.xxdb.data.BasicLongVector;
import com.xxdb.data.BasicMinute;
import com.xxdb.data.BasicMinuteMatrix;
import com.xxdb.data.BasicMinuteVector;
import com.xxdb.data.BasicMonth;
import com.xxdb.data.BasicMonthMatrix;
import com.xxdb.data.BasicMonthVector;
import com.xxdb.data.BasicNanoTime;
import com.xxdb.data.BasicNanoTimeMatrix;
import com.xxdb.data.BasicNanoTimeVector;
import com.xxdb.data.BasicNanoTimestamp;
import com.xxdb.data.BasicNanoTimestampMatrix;
import com.xxdb.data.BasicNanoTimestampVector;
import com.xxdb.data.BasicPoint;
import com.xxdb.data.BasicPointVector;
import com.xxdb.data.BasicSecond;
import com.xxdb.data.BasicSecondMatrix;
import com.xxdb.data.BasicSecondVector;
import com.xxdb.data.BasicSet;
import com.xxdb.data.BasicShort;
import com.xxdb.data.BasicShortMatrix;
import com.xxdb.data.BasicShortVector;
import com.xxdb.data.BasicString;
import com.xxdb.data.BasicStringMatrix;
import com.xxdb.data.BasicStringVector;
import com.xxdb.data.BasicSymbolVector;
import com.xxdb.data.BasicTable;
import com.xxdb.data.BasicTableSchema;
import com.xxdb.data.BasicTime;
import com.xxdb.data.BasicTimeMatrix;
import com.xxdb.data.BasicTimeVector;
import com.xxdb.data.BasicTimestamp;
import com.xxdb.data.BasicTimestampMatrix;
import com.xxdb.data.BasicTimestampVector;
import com.xxdb.data.BasicUuid;
import com.xxdb.data.BasicUuidVector;
import com.xxdb.data.BasicVoidVector;
import com.xxdb.data.Void;
public class SerializerModule {

    public static SimpleModule getModule() {
        return new SimpleModule()
                .addSerializer(BasicAnyVector.class, new AnyVectorSerializer())
                .addSerializer(BasicArrayVector.class, new ArrayVectorSerializer())
                .addSerializer(BasicBooleanMatrix.class, new BooleanMatrixSerializer())
                .addSerializer(BasicBoolean.class, new BooleanSerializer())
                .addSerializer(BasicBooleanVector.class, new BooleanVectorSerializer())
                .addSerializer(BasicByteMatrix.class, new ByteMatrixSerializer())
                .addSerializer(BasicByte.class, new ByteSerializer())
                .addSerializer(BasicByteVector.class, new ByteVectorSerializer())
                .addSerializer(BasicChart.class, new ChartSerializer())
                .addSerializer(BasicChunkMeta.class, new ChunkMetaSerializer())
                .addSerializer(BasicComplex.class, new ComplexSerializer())
                .addSerializer(BasicComplexMatrix.class, new ComplexMatrixSerializer())
                .addSerializer(BasicComplexVector.class, new ComplexVectorSerializer())
                .addSerializer(BasicDate.class, new DateSerializer())
                .addSerializer(BasicDateMatrix.class, new DateMatrixSerializer())
                .addSerializer(BasicDateVector.class, new DateVectorSerializer())
                .addSerializer(BasicDateTime.class, new DateTimeSerializer())
                .addSerializer(BasicDateTimeMatrix.class, new DateTimeMatrixSerializer())
                .addSerializer(BasicDateTimeVector.class, new DateTimeVectorSerializer())
                .addSerializer(BasicDateHour.class, new DateHourSerializer())
                .addSerializer(BasicDateHourMatrix.class, new DateHourMatrixSerializer())
                .addSerializer(BasicDateHourVector.class, new DateHourVectorSerializer())
                .addSerializer(BasicDecimal32.class, new Decimal32Serializer())
                .addSerializer(BasicDecimal32Vector.class, new Decimal32VectorSerializer())
                .addSerializer(BasicDecimal64.class, new Decimal64Serializer())
                .addSerializer(BasicDecimal64Vector.class, new Decimal64VectorSerializer())
                .addSerializer(BasicDecimal128.class, new Decimal128Serializer())
                .addSerializer(BasicDecimal128Vector.class, new Decimal128VectorSerializer())
                .addSerializer(BasicDictionary.class, new DictionarySerializer())
                .addSerializer(BasicDouble.class, new DoubleSerializer())
                .addSerializer(BasicDoubleMatrix.class, new DoubleMatrixSerializer())
                .addSerializer(BasicDoubleVector.class, new DoubleVectorSerializer())
                .addSerializer(BasicDuration.class, new DurationSerializer())
                .addSerializer(BasicDurationVector.class, new DurationVectorSerializer())
                .addSerializer(BasicFloat.class, new FloatSerializer())
                .addSerializer(BasicFloatMatrix.class, new FloatMatrixSerializer())
                .addSerializer(BasicFloatVector.class, new FloatVectorSerializer())
                .addSerializer(BasicInt.class, new IntSerializer())
                .addSerializer(BasicIntMatrix.class, new IntMatrixSerializer())
                .addSerializer(BasicIntVector.class, new IntVectorSerializer())
                .addSerializer(BasicInt128.class, new Int128Serializer())
                .addSerializer(BasicInt128Vector.class, new Int128VectorSerializer())
                .addSerializer(BasicIPAddr.class, new IPAddrSerializer())
                .addSerializer(BasicIPAddrVector.class, new IPAddrVectorSerializer())
                .addSerializer(BasicLong.class, new LongSerializer())
                .addSerializer(BasicLongMatrix.class, new LongMatrixSerializer())
                .addSerializer(BasicLongVector.class, new LongVectorSerializer())
                .addSerializer(BasicMinute.class, new MinuteSerializer())
                .addSerializer(BasicMinuteVector.class, new MinuteVectorSerializer())
                .addSerializer(BasicMinuteMatrix.class, new MinuteMatrixSerializer())
                .addSerializer(BasicMonth.class, new MonthSerializer())
                .addSerializer(BasicMonthVector.class, new MonthVectorSerializer())
                .addSerializer(BasicMonthMatrix.class, new MonthMatrixSerializer())
                .addSerializer(BasicNanoTime.class, new NanoTimeSerializer())
                .addSerializer(BasicNanoTimeVector.class, new NanoTimeVectorSerializer())
                .addSerializer(BasicNanoTimeMatrix.class, new NanoTimeMatrixSerializer())
                .addSerializer(BasicNanoTimestamp.class, new NanoTimestampSerializer())
                .addSerializer(BasicNanoTimestampVector.class, new NanoTimestampVectorSerializer())
                .addSerializer(BasicNanoTimestampMatrix.class, new NanoTimestampMatrixSerializer())
                .addSerializer(BasicPoint.class, new PointSerializer())
                .addSerializer(BasicPointVector.class, new PointVectorSerializer())
                .addSerializer(BasicSecond.class, new SecondSerializer())
                .addSerializer(BasicSecondVector.class, new SecondVectorSerializer())
                .addSerializer(BasicSecondMatrix.class, new SecondMatrixSerializer())
                .addSerializer(BasicSet.class, new SetSerializer())
                .addSerializer(BasicShort.class, new ShortSerializer())
                .addSerializer(BasicShortMatrix.class, new ShortMatrixSerializer())
                .addSerializer(BasicShortVector.class, new ShortVectorSerializer())
                .addSerializer(BasicString.class, new StringSerializer())
                .addSerializer(BasicStringVector.class, new StringVectorSerializer())
                .addSerializer(BasicStringMatrix.class, new StringMatrixSerializer())
                .addSerializer(BasicSymbolVector.class, new SymbolVectorSerializer())
                .addSerializer(BasicTable.class, new TableSerializer())
                .addSerializer(BasicTableSchema.class, new TableSchemaSerializer())
                .addSerializer(BasicTime.class, new TimeSerializer())
                .addSerializer(BasicTimeVector.class, new TimeVectorSerializer())
                .addSerializer(BasicTimeMatrix.class, new TimeMatrixSerializer())
                .addSerializer(BasicTimestamp.class, new TimestampSerializer())
                .addSerializer(BasicTimestampVector.class, new TimestampVectorSerializer())
                .addSerializer(BasicTimestampMatrix.class, new TimestampMatrixSerializer())
                .addSerializer(BasicUuid.class, new UuidSerializer())
                .addSerializer(BasicUuidVector.class, new UuidVectorSerializer())
                .addSerializer(BasicVoidVector.class, new VoidVectorSerializer())
                .addSerializer(Void.class, new VoidSerializer());
    }
}
