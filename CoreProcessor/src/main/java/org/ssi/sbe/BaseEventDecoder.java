/* Generated SBE (Simple Binary Encoding) message codec */
package org.ssi.sbe;

import org.agrona.DirectBuffer;
import org.agrona.sbe.*;

/**
 * Description of a basic Base Event
 */
@SuppressWarnings("all")
public class BaseEventDecoder implements MessageDecoderFlyweight
{
    public static final int BLOCK_LENGTH = 65;
    public static final int TEMPLATE_ID = 1;
    public static final int SCHEMA_ID = 1;
    public static final int SCHEMA_VERSION = 0;
    public static final java.nio.ByteOrder BYTE_ORDER = java.nio.ByteOrder.LITTLE_ENDIAN;

    private final BaseEventDecoder parentMessage = this;
    private DirectBuffer buffer;
    protected int offset;
    protected int limit;
    protected int actingBlockLength;
    protected int actingVersion;

    public int sbeBlockLength()
    {
        return BLOCK_LENGTH;
    }

    public int sbeTemplateId()
    {
        return TEMPLATE_ID;
    }

    public int sbeSchemaId()
    {
        return SCHEMA_ID;
    }

    public int sbeSchemaVersion()
    {
        return SCHEMA_VERSION;
    }

    public String sbeSemanticType()
    {
        return "";
    }

    public DirectBuffer buffer()
    {
        return buffer;
    }

    public int offset()
    {
        return offset;
    }

    public BaseEventDecoder wrap(
        final DirectBuffer buffer, final int offset, final int actingBlockLength, final int actingVersion)
    {
        if (buffer != this.buffer)
        {
            this.buffer = buffer;
        }
        this.offset = offset;
        this.actingBlockLength = actingBlockLength;
        this.actingVersion = actingVersion;
        limit(offset + actingBlockLength);

        return this;
    }

    public int encodedLength()
    {
        return limit - offset;
    }

    public int limit()
    {
        return limit;
    }

    public void limit(final int limit)
    {
        this.limit = limit;
    }

    public static int eventTypeId()
    {
        return 1;
    }

    public static int eventTypeSinceVersion()
    {
        return 0;
    }

    public static int eventTypeEncodingOffset()
    {
        return 0;
    }

    public static int eventTypeEncodingLength()
    {
        return 1;
    }

    public static String eventTypeMetaAttribute(final MetaAttribute metaAttribute)
    {
        switch (metaAttribute)
        {
            case EPOCH: return "";
            case TIME_UNIT: return "";
            case SEMANTIC_TYPE: return "";
            case PRESENCE: return "required";
        }

        return "";
    }

    public static byte eventTypeNullValue()
    {
        return (byte)-128;
    }

    public static byte eventTypeMinValue()
    {
        return (byte)-127;
    }

    public static byte eventTypeMaxValue()
    {
        return (byte)127;
    }

    public byte eventType()
    {
        return buffer.getByte(offset + 0);
    }


    public static int orderIdId()
    {
        return 2;
    }

    public static int orderIdSinceVersion()
    {
        return 0;
    }

    public static int orderIdEncodingOffset()
    {
        return 1;
    }

    public static int orderIdEncodingLength()
    {
        return 8;
    }

    public static String orderIdMetaAttribute(final MetaAttribute metaAttribute)
    {
        switch (metaAttribute)
        {
            case EPOCH: return "";
            case TIME_UNIT: return "";
            case SEMANTIC_TYPE: return "";
            case PRESENCE: return "required";
        }

        return "";
    }

    public static long orderIdNullValue()
    {
        return 0xffffffffffffffffL;
    }

    public static long orderIdMinValue()
    {
        return 0x0L;
    }

    public static long orderIdMaxValue()
    {
        return 0xfffffffffffffffeL;
    }

    public long orderId()
    {
        return buffer.getLong(offset + 1, java.nio.ByteOrder.LITTLE_ENDIAN);
    }


    public static int timestampId()
    {
        return 3;
    }

    public static int timestampSinceVersion()
    {
        return 0;
    }

    public static int timestampEncodingOffset()
    {
        return 9;
    }

    public static int timestampEncodingLength()
    {
        return 8;
    }

    public static String timestampMetaAttribute(final MetaAttribute metaAttribute)
    {
        switch (metaAttribute)
        {
            case EPOCH: return "";
            case TIME_UNIT: return "";
            case SEMANTIC_TYPE: return "";
            case PRESENCE: return "required";
        }

        return "";
    }

    public static long timestampNullValue()
    {
        return 0xffffffffffffffffL;
    }

    public static long timestampMinValue()
    {
        return 0x0L;
    }

    public static long timestampMaxValue()
    {
        return 0xfffffffffffffffeL;
    }

    public long timestamp()
    {
        return buffer.getLong(offset + 9, java.nio.ByteOrder.LITTLE_ENDIAN);
    }


    public static int symbolId()
    {
        return 4;
    }

    public static int symbolSinceVersion()
    {
        return 0;
    }

    public static int symbolEncodingOffset()
    {
        return 17;
    }

    public static int symbolEncodingLength()
    {
        return 4;
    }

    public static String symbolMetaAttribute(final MetaAttribute metaAttribute)
    {
        switch (metaAttribute)
        {
            case EPOCH: return "";
            case TIME_UNIT: return "";
            case SEMANTIC_TYPE: return "";
            case PRESENCE: return "required";
        }

        return "";
    }

    public static int symbolNullValue()
    {
        return -2147483648;
    }

    public static int symbolMinValue()
    {
        return -2147483647;
    }

    public static int symbolMaxValue()
    {
        return 2147483647;
    }

    public int symbol()
    {
        return buffer.getInt(offset + 17, java.nio.ByteOrder.LITTLE_ENDIAN);
    }


    public static int priceId()
    {
        return 5;
    }

    public static int priceSinceVersion()
    {
        return 0;
    }

    public static int priceEncodingOffset()
    {
        return 21;
    }

    public static int priceEncodingLength()
    {
        return 8;
    }

    public static String priceMetaAttribute(final MetaAttribute metaAttribute)
    {
        switch (metaAttribute)
        {
            case EPOCH: return "";
            case TIME_UNIT: return "";
            case SEMANTIC_TYPE: return "";
            case PRESENCE: return "required";
        }

        return "";
    }

    public static long priceNullValue()
    {
        return 0xffffffffffffffffL;
    }

    public static long priceMinValue()
    {
        return 0x0L;
    }

    public static long priceMaxValue()
    {
        return 0xfffffffffffffffeL;
    }

    public long price()
    {
        return buffer.getLong(offset + 21, java.nio.ByteOrder.LITTLE_ENDIAN);
    }


    public static int amountId()
    {
        return 6;
    }

    public static int amountSinceVersion()
    {
        return 0;
    }

    public static int amountEncodingOffset()
    {
        return 29;
    }

    public static int amountEncodingLength()
    {
        return 8;
    }

    public static String amountMetaAttribute(final MetaAttribute metaAttribute)
    {
        switch (metaAttribute)
        {
            case EPOCH: return "";
            case TIME_UNIT: return "";
            case SEMANTIC_TYPE: return "";
            case PRESENCE: return "required";
        }

        return "";
    }

    public static long amountNullValue()
    {
        return 0xffffffffffffffffL;
    }

    public static long amountMinValue()
    {
        return 0x0L;
    }

    public static long amountMaxValue()
    {
        return 0xfffffffffffffffeL;
    }

    public long amount()
    {
        return buffer.getLong(offset + 29, java.nio.ByteOrder.LITTLE_ENDIAN);
    }


    public static int stopPriceId()
    {
        return 7;
    }

    public static int stopPriceSinceVersion()
    {
        return 0;
    }

    public static int stopPriceEncodingOffset()
    {
        return 37;
    }

    public static int stopPriceEncodingLength()
    {
        return 8;
    }

    public static String stopPriceMetaAttribute(final MetaAttribute metaAttribute)
    {
        switch (metaAttribute)
        {
            case EPOCH: return "";
            case TIME_UNIT: return "";
            case SEMANTIC_TYPE: return "";
            case PRESENCE: return "required";
        }

        return "";
    }

    public static long stopPriceNullValue()
    {
        return 0xffffffffffffffffL;
    }

    public static long stopPriceMinValue()
    {
        return 0x0L;
    }

    public static long stopPriceMaxValue()
    {
        return 0xfffffffffffffffeL;
    }

    public long stopPrice()
    {
        return buffer.getLong(offset + 37, java.nio.ByteOrder.LITTLE_ENDIAN);
    }


    public static int orderSideId()
    {
        return 8;
    }

    public static int orderSideSinceVersion()
    {
        return 0;
    }

    public static int orderSideEncodingOffset()
    {
        return 45;
    }

    public static int orderSideEncodingLength()
    {
        return 1;
    }

    public static String orderSideMetaAttribute(final MetaAttribute metaAttribute)
    {
        switch (metaAttribute)
        {
            case EPOCH: return "";
            case TIME_UNIT: return "";
            case SEMANTIC_TYPE: return "";
            case PRESENCE: return "required";
        }

        return "";
    }

    public static byte orderSideNullValue()
    {
        return (byte)-128;
    }

    public static byte orderSideMinValue()
    {
        return (byte)-127;
    }

    public static byte orderSideMaxValue()
    {
        return (byte)127;
    }

    public byte orderSide()
    {
        return buffer.getByte(offset + 45);
    }


    public static int orderTypeId()
    {
        return 9;
    }

    public static int orderTypeSinceVersion()
    {
        return 0;
    }

    public static int orderTypeEncodingOffset()
    {
        return 46;
    }

    public static int orderTypeEncodingLength()
    {
        return 1;
    }

    public static String orderTypeMetaAttribute(final MetaAttribute metaAttribute)
    {
        switch (metaAttribute)
        {
            case EPOCH: return "";
            case TIME_UNIT: return "";
            case SEMANTIC_TYPE: return "";
            case PRESENCE: return "required";
        }

        return "";
    }

    public static byte orderTypeNullValue()
    {
        return (byte)-128;
    }

    public static byte orderTypeMinValue()
    {
        return (byte)-127;
    }

    public static byte orderTypeMaxValue()
    {
        return (byte)127;
    }

    public byte orderType()
    {
        return buffer.getByte(offset + 46);
    }


    public static int tradeTypeId()
    {
        return 10;
    }

    public static int tradeTypeSinceVersion()
    {
        return 0;
    }

    public static int tradeTypeEncodingOffset()
    {
        return 47;
    }

    public static int tradeTypeEncodingLength()
    {
        return 1;
    }

    public static String tradeTypeMetaAttribute(final MetaAttribute metaAttribute)
    {
        switch (metaAttribute)
        {
            case EPOCH: return "";
            case TIME_UNIT: return "";
            case SEMANTIC_TYPE: return "";
            case PRESENCE: return "required";
        }

        return "";
    }

    public static byte tradeTypeNullValue()
    {
        return (byte)-128;
    }

    public static byte tradeTypeMinValue()
    {
        return (byte)-127;
    }

    public static byte tradeTypeMaxValue()
    {
        return (byte)127;
    }

    public byte tradeType()
    {
        return buffer.getByte(offset + 47);
    }


    public static int tradeOptionId()
    {
        return 11;
    }

    public static int tradeOptionSinceVersion()
    {
        return 0;
    }

    public static int tradeOptionEncodingOffset()
    {
        return 48;
    }

    public static int tradeOptionEncodingLength()
    {
        return 1;
    }

    public static String tradeOptionMetaAttribute(final MetaAttribute metaAttribute)
    {
        switch (metaAttribute)
        {
            case EPOCH: return "";
            case TIME_UNIT: return "";
            case SEMANTIC_TYPE: return "";
            case PRESENCE: return "required";
        }

        return "";
    }

    public static byte tradeOptionNullValue()
    {
        return (byte)-128;
    }

    public static byte tradeOptionMinValue()
    {
        return (byte)-127;
    }

    public static byte tradeOptionMaxValue()
    {
        return (byte)127;
    }

    public byte tradeOption()
    {
        return buffer.getByte(offset + 48);
    }


    public static int userIdId()
    {
        return 12;
    }

    public static int userIdSinceVersion()
    {
        return 0;
    }

    public static int userIdEncodingOffset()
    {
        return 49;
    }

    public static int userIdEncodingLength()
    {
        return 8;
    }

    public static String userIdMetaAttribute(final MetaAttribute metaAttribute)
    {
        switch (metaAttribute)
        {
            case EPOCH: return "";
            case TIME_UNIT: return "";
            case SEMANTIC_TYPE: return "";
            case PRESENCE: return "required";
        }

        return "";
    }

    public static long userIdNullValue()
    {
        return 0xffffffffffffffffL;
    }

    public static long userIdMinValue()
    {
        return 0x0L;
    }

    public static long userIdMaxValue()
    {
        return 0xfffffffffffffffeL;
    }

    public long userId()
    {
        return buffer.getLong(offset + 49, java.nio.ByteOrder.LITTLE_ENDIAN);
    }


    public static int clientOrderIdId()
    {
        return 13;
    }

    public static int clientOrderIdSinceVersion()
    {
        return 0;
    }

    public static int clientOrderIdEncodingOffset()
    {
        return 57;
    }

    public static int clientOrderIdEncodingLength()
    {
        return 8;
    }

    public static String clientOrderIdMetaAttribute(final MetaAttribute metaAttribute)
    {
        switch (metaAttribute)
        {
            case EPOCH: return "";
            case TIME_UNIT: return "";
            case SEMANTIC_TYPE: return "";
            case PRESENCE: return "required";
        }

        return "";
    }

    public static long clientOrderIdNullValue()
    {
        return 0xffffffffffffffffL;
    }

    public static long clientOrderIdMinValue()
    {
        return 0x0L;
    }

    public static long clientOrderIdMaxValue()
    {
        return 0xfffffffffffffffeL;
    }

    public long clientOrderId()
    {
        return buffer.getLong(offset + 57, java.nio.ByteOrder.LITTLE_ENDIAN);
    }



    public String toString()
    {
        return appendTo(new StringBuilder(100)).toString();
    }

    public StringBuilder appendTo(final StringBuilder builder)
    {
        final int originalLimit = limit();
        limit(offset + actingBlockLength);
        builder.append("[BaseEvent](sbeTemplateId=");
        builder.append(TEMPLATE_ID);
        builder.append("|sbeSchemaId=");
        builder.append(SCHEMA_ID);
        builder.append("|sbeSchemaVersion=");
        if (parentMessage.actingVersion != SCHEMA_VERSION)
        {
            builder.append(parentMessage.actingVersion);
            builder.append('/');
        }
        builder.append(SCHEMA_VERSION);
        builder.append("|sbeBlockLength=");
        if (actingBlockLength != BLOCK_LENGTH)
        {
            builder.append(actingBlockLength);
            builder.append('/');
        }
        builder.append(BLOCK_LENGTH);
        builder.append("):");
        //Token{signal=BEGIN_FIELD, name='eventType', referencedName='null', description='null', id=1, version=0, deprecated=0, encodedLength=1, offset=0, componentTokenCount=3, encoding=Encoding{presence=REQUIRED, primitiveType=null, byteOrder=LITTLE_ENDIAN, minValue=null, maxValue=null, nullValue=null, constValue=null, characterEncoding='null', epoch='null', timeUnit=null, semanticType='null'}}
        //Token{signal=ENCODING, name='int8', referencedName='null', description='null', id=-1, version=0, deprecated=0, encodedLength=1, offset=0, componentTokenCount=1, encoding=Encoding{presence=REQUIRED, primitiveType=INT8, byteOrder=LITTLE_ENDIAN, minValue=null, maxValue=null, nullValue=null, constValue=null, characterEncoding='null', epoch='null', timeUnit=null, semanticType='null'}}
        builder.append("eventType=");
        builder.append(eventType());
        builder.append('|');
        //Token{signal=BEGIN_FIELD, name='orderId', referencedName='null', description='null', id=2, version=0, deprecated=0, encodedLength=8, offset=1, componentTokenCount=3, encoding=Encoding{presence=REQUIRED, primitiveType=null, byteOrder=LITTLE_ENDIAN, minValue=null, maxValue=null, nullValue=null, constValue=null, characterEncoding='null', epoch='null', timeUnit=null, semanticType='null'}}
        //Token{signal=ENCODING, name='uint64', referencedName='null', description='null', id=-1, version=0, deprecated=0, encodedLength=8, offset=1, componentTokenCount=1, encoding=Encoding{presence=REQUIRED, primitiveType=UINT64, byteOrder=LITTLE_ENDIAN, minValue=null, maxValue=null, nullValue=null, constValue=null, characterEncoding='null', epoch='null', timeUnit=null, semanticType='null'}}
        builder.append("orderId=");
        builder.append(orderId());
        builder.append('|');
        //Token{signal=BEGIN_FIELD, name='timestamp', referencedName='null', description='null', id=3, version=0, deprecated=0, encodedLength=8, offset=9, componentTokenCount=3, encoding=Encoding{presence=REQUIRED, primitiveType=null, byteOrder=LITTLE_ENDIAN, minValue=null, maxValue=null, nullValue=null, constValue=null, characterEncoding='null', epoch='null', timeUnit=null, semanticType='null'}}
        //Token{signal=ENCODING, name='uint64', referencedName='null', description='null', id=-1, version=0, deprecated=0, encodedLength=8, offset=9, componentTokenCount=1, encoding=Encoding{presence=REQUIRED, primitiveType=UINT64, byteOrder=LITTLE_ENDIAN, minValue=null, maxValue=null, nullValue=null, constValue=null, characterEncoding='null', epoch='null', timeUnit=null, semanticType='null'}}
        builder.append("timestamp=");
        builder.append(timestamp());
        builder.append('|');
        //Token{signal=BEGIN_FIELD, name='symbol', referencedName='null', description='null', id=4, version=0, deprecated=0, encodedLength=4, offset=17, componentTokenCount=3, encoding=Encoding{presence=REQUIRED, primitiveType=null, byteOrder=LITTLE_ENDIAN, minValue=null, maxValue=null, nullValue=null, constValue=null, characterEncoding='null', epoch='null', timeUnit=null, semanticType='null'}}
        //Token{signal=ENCODING, name='int32', referencedName='null', description='null', id=-1, version=0, deprecated=0, encodedLength=4, offset=17, componentTokenCount=1, encoding=Encoding{presence=REQUIRED, primitiveType=INT32, byteOrder=LITTLE_ENDIAN, minValue=null, maxValue=null, nullValue=null, constValue=null, characterEncoding='null', epoch='null', timeUnit=null, semanticType='null'}}
        builder.append("symbol=");
        builder.append(symbol());
        builder.append('|');
        //Token{signal=BEGIN_FIELD, name='price', referencedName='null', description='null', id=5, version=0, deprecated=0, encodedLength=8, offset=21, componentTokenCount=3, encoding=Encoding{presence=REQUIRED, primitiveType=null, byteOrder=LITTLE_ENDIAN, minValue=null, maxValue=null, nullValue=null, constValue=null, characterEncoding='null', epoch='null', timeUnit=null, semanticType='null'}}
        //Token{signal=ENCODING, name='uint64', referencedName='null', description='null', id=-1, version=0, deprecated=0, encodedLength=8, offset=21, componentTokenCount=1, encoding=Encoding{presence=REQUIRED, primitiveType=UINT64, byteOrder=LITTLE_ENDIAN, minValue=null, maxValue=null, nullValue=null, constValue=null, characterEncoding='null', epoch='null', timeUnit=null, semanticType='null'}}
        builder.append("price=");
        builder.append(price());
        builder.append('|');
        //Token{signal=BEGIN_FIELD, name='amount', referencedName='null', description='null', id=6, version=0, deprecated=0, encodedLength=8, offset=29, componentTokenCount=3, encoding=Encoding{presence=REQUIRED, primitiveType=null, byteOrder=LITTLE_ENDIAN, minValue=null, maxValue=null, nullValue=null, constValue=null, characterEncoding='null', epoch='null', timeUnit=null, semanticType='null'}}
        //Token{signal=ENCODING, name='uint64', referencedName='null', description='null', id=-1, version=0, deprecated=0, encodedLength=8, offset=29, componentTokenCount=1, encoding=Encoding{presence=REQUIRED, primitiveType=UINT64, byteOrder=LITTLE_ENDIAN, minValue=null, maxValue=null, nullValue=null, constValue=null, characterEncoding='null', epoch='null', timeUnit=null, semanticType='null'}}
        builder.append("amount=");
        builder.append(amount());
        builder.append('|');
        //Token{signal=BEGIN_FIELD, name='stopPrice', referencedName='null', description='null', id=7, version=0, deprecated=0, encodedLength=8, offset=37, componentTokenCount=3, encoding=Encoding{presence=REQUIRED, primitiveType=null, byteOrder=LITTLE_ENDIAN, minValue=null, maxValue=null, nullValue=null, constValue=null, characterEncoding='null', epoch='null', timeUnit=null, semanticType='null'}}
        //Token{signal=ENCODING, name='uint64', referencedName='null', description='null', id=-1, version=0, deprecated=0, encodedLength=8, offset=37, componentTokenCount=1, encoding=Encoding{presence=REQUIRED, primitiveType=UINT64, byteOrder=LITTLE_ENDIAN, minValue=null, maxValue=null, nullValue=null, constValue=null, characterEncoding='null', epoch='null', timeUnit=null, semanticType='null'}}
        builder.append("stopPrice=");
        builder.append(stopPrice());
        builder.append('|');
        //Token{signal=BEGIN_FIELD, name='orderSide', referencedName='null', description='null', id=8, version=0, deprecated=0, encodedLength=1, offset=45, componentTokenCount=3, encoding=Encoding{presence=REQUIRED, primitiveType=null, byteOrder=LITTLE_ENDIAN, minValue=null, maxValue=null, nullValue=null, constValue=null, characterEncoding='null', epoch='null', timeUnit=null, semanticType='null'}}
        //Token{signal=ENCODING, name='int8', referencedName='null', description='null', id=-1, version=0, deprecated=0, encodedLength=1, offset=45, componentTokenCount=1, encoding=Encoding{presence=REQUIRED, primitiveType=INT8, byteOrder=LITTLE_ENDIAN, minValue=null, maxValue=null, nullValue=null, constValue=null, characterEncoding='null', epoch='null', timeUnit=null, semanticType='null'}}
        builder.append("orderSide=");
        builder.append(orderSide());
        builder.append('|');
        //Token{signal=BEGIN_FIELD, name='orderType', referencedName='null', description='null', id=9, version=0, deprecated=0, encodedLength=1, offset=46, componentTokenCount=3, encoding=Encoding{presence=REQUIRED, primitiveType=null, byteOrder=LITTLE_ENDIAN, minValue=null, maxValue=null, nullValue=null, constValue=null, characterEncoding='null', epoch='null', timeUnit=null, semanticType='null'}}
        //Token{signal=ENCODING, name='int8', referencedName='null', description='null', id=-1, version=0, deprecated=0, encodedLength=1, offset=46, componentTokenCount=1, encoding=Encoding{presence=REQUIRED, primitiveType=INT8, byteOrder=LITTLE_ENDIAN, minValue=null, maxValue=null, nullValue=null, constValue=null, characterEncoding='null', epoch='null', timeUnit=null, semanticType='null'}}
        builder.append("orderType=");
        builder.append(orderType());
        builder.append('|');
        //Token{signal=BEGIN_FIELD, name='tradeType', referencedName='null', description='null', id=10, version=0, deprecated=0, encodedLength=1, offset=47, componentTokenCount=3, encoding=Encoding{presence=REQUIRED, primitiveType=null, byteOrder=LITTLE_ENDIAN, minValue=null, maxValue=null, nullValue=null, constValue=null, characterEncoding='null', epoch='null', timeUnit=null, semanticType='null'}}
        //Token{signal=ENCODING, name='int8', referencedName='null', description='null', id=-1, version=0, deprecated=0, encodedLength=1, offset=47, componentTokenCount=1, encoding=Encoding{presence=REQUIRED, primitiveType=INT8, byteOrder=LITTLE_ENDIAN, minValue=null, maxValue=null, nullValue=null, constValue=null, characterEncoding='null', epoch='null', timeUnit=null, semanticType='null'}}
        builder.append("tradeType=");
        builder.append(tradeType());
        builder.append('|');
        //Token{signal=BEGIN_FIELD, name='tradeOption', referencedName='null', description='null', id=11, version=0, deprecated=0, encodedLength=1, offset=48, componentTokenCount=3, encoding=Encoding{presence=REQUIRED, primitiveType=null, byteOrder=LITTLE_ENDIAN, minValue=null, maxValue=null, nullValue=null, constValue=null, characterEncoding='null', epoch='null', timeUnit=null, semanticType='null'}}
        //Token{signal=ENCODING, name='int8', referencedName='null', description='null', id=-1, version=0, deprecated=0, encodedLength=1, offset=48, componentTokenCount=1, encoding=Encoding{presence=REQUIRED, primitiveType=INT8, byteOrder=LITTLE_ENDIAN, minValue=null, maxValue=null, nullValue=null, constValue=null, characterEncoding='null', epoch='null', timeUnit=null, semanticType='null'}}
        builder.append("tradeOption=");
        builder.append(tradeOption());
        builder.append('|');
        //Token{signal=BEGIN_FIELD, name='userId', referencedName='null', description='null', id=12, version=0, deprecated=0, encodedLength=8, offset=49, componentTokenCount=3, encoding=Encoding{presence=REQUIRED, primitiveType=null, byteOrder=LITTLE_ENDIAN, minValue=null, maxValue=null, nullValue=null, constValue=null, characterEncoding='null', epoch='null', timeUnit=null, semanticType='null'}}
        //Token{signal=ENCODING, name='uint64', referencedName='null', description='null', id=-1, version=0, deprecated=0, encodedLength=8, offset=49, componentTokenCount=1, encoding=Encoding{presence=REQUIRED, primitiveType=UINT64, byteOrder=LITTLE_ENDIAN, minValue=null, maxValue=null, nullValue=null, constValue=null, characterEncoding='null', epoch='null', timeUnit=null, semanticType='null'}}
        builder.append("userId=");
        builder.append(userId());
        builder.append('|');
        //Token{signal=BEGIN_FIELD, name='clientOrderId', referencedName='null', description='null', id=13, version=0, deprecated=0, encodedLength=8, offset=57, componentTokenCount=3, encoding=Encoding{presence=REQUIRED, primitiveType=null, byteOrder=LITTLE_ENDIAN, minValue=null, maxValue=null, nullValue=null, constValue=null, characterEncoding='null', epoch='null', timeUnit=null, semanticType='null'}}
        //Token{signal=ENCODING, name='uint64', referencedName='null', description='null', id=-1, version=0, deprecated=0, encodedLength=8, offset=57, componentTokenCount=1, encoding=Encoding{presence=REQUIRED, primitiveType=UINT64, byteOrder=LITTLE_ENDIAN, minValue=null, maxValue=null, nullValue=null, constValue=null, characterEncoding='null', epoch='null', timeUnit=null, semanticType='null'}}
        builder.append("clientOrderId=");
        builder.append(clientOrderId());

        limit(originalLimit);

        return builder;
    }
}
