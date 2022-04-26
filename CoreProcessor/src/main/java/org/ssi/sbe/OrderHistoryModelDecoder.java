/* Generated SBE (Simple Binary Encoding) message codec */
package org.ssi.sbe;

import org.agrona.DirectBuffer;
import org.agrona.sbe.MessageDecoderFlyweight;

/**
 * Description of a order history model
 */
@SuppressWarnings("all")
public class OrderHistoryModelDecoder implements MessageDecoderFlyweight
{
    public static final int BLOCK_LENGTH = 103;
    public static final int TEMPLATE_ID = 1;
    public static final int SCHEMA_ID = 1;
    public static final int SCHEMA_VERSION = 0;
    public static final java.nio.ByteOrder BYTE_ORDER = java.nio.ByteOrder.LITTLE_ENDIAN;

    private final OrderHistoryModelDecoder parentMessage = this;
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

    public OrderHistoryModelDecoder wrap(
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

    public static int orderIdId()
    {
        return 1;
    }

    public static int orderIdSinceVersion()
    {
        return 0;
    }

    public static int orderIdEncodingOffset()
    {
        return 0;
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
        return buffer.getLong(offset + 0, java.nio.ByteOrder.LITTLE_ENDIAN);
    }


    public static int userIdId()
    {
        return 2;
    }

    public static int userIdSinceVersion()
    {
        return 0;
    }

    public static int userIdEncodingOffset()
    {
        return 8;
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
        return buffer.getLong(offset + 8, java.nio.ByteOrder.LITTLE_ENDIAN);
    }


    public static int symbolIdId()
    {
        return 3;
    }

    public static int symbolIdSinceVersion()
    {
        return 0;
    }

    public static int symbolIdEncodingOffset()
    {
        return 16;
    }

    public static int symbolIdEncodingLength()
    {
        return 4;
    }

    public static String symbolIdMetaAttribute(final MetaAttribute metaAttribute)
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

    public static int symbolIdNullValue()
    {
        return -2147483648;
    }

    public static int symbolIdMinValue()
    {
        return -2147483647;
    }

    public static int symbolIdMaxValue()
    {
        return 2147483647;
    }

    public int symbolId()
    {
        return buffer.getInt(offset + 16, java.nio.ByteOrder.LITTLE_ENDIAN);
    }


    public static int createdId()
    {
        return 4;
    }

    public static int createdSinceVersion()
    {
        return 0;
    }

    public static int createdEncodingOffset()
    {
        return 20;
    }

    public static int createdEncodingLength()
    {
        return 8;
    }

    public static String createdMetaAttribute(final MetaAttribute metaAttribute)
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

    public static long createdNullValue()
    {
        return 0xffffffffffffffffL;
    }

    public static long createdMinValue()
    {
        return 0x0L;
    }

    public static long createdMaxValue()
    {
        return 0xfffffffffffffffeL;
    }

    public long created()
    {
        return buffer.getLong(offset + 20, java.nio.ByteOrder.LITTLE_ENDIAN);
    }


    public static int updatedId()
    {
        return 5;
    }

    public static int updatedSinceVersion()
    {
        return 0;
    }

    public static int updatedEncodingOffset()
    {
        return 28;
    }

    public static int updatedEncodingLength()
    {
        return 8;
    }

    public static String updatedMetaAttribute(final MetaAttribute metaAttribute)
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

    public static long updatedNullValue()
    {
        return 0xffffffffffffffffL;
    }

    public static long updatedMinValue()
    {
        return 0x0L;
    }

    public static long updatedMaxValue()
    {
        return 0xfffffffffffffffeL;
    }

    public long updated()
    {
        return buffer.getLong(offset + 28, java.nio.ByteOrder.LITTLE_ENDIAN);
    }


    public static int orderTypeId()
    {
        return 6;
    }

    public static int orderTypeSinceVersion()
    {
        return 0;
    }

    public static int orderTypeEncodingOffset()
    {
        return 36;
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
        return buffer.getByte(offset + 36);
    }


    public static int orderSideId()
    {
        return 7;
    }

    public static int orderSideSinceVersion()
    {
        return 0;
    }

    public static int orderSideEncodingOffset()
    {
        return 37;
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
        return buffer.getByte(offset + 37);
    }


    public static int orderStatusId()
    {
        return 8;
    }

    public static int orderStatusSinceVersion()
    {
        return 0;
    }

    public static int orderStatusEncodingOffset()
    {
        return 38;
    }

    public static int orderStatusEncodingLength()
    {
        return 1;
    }

    public static String orderStatusMetaAttribute(final MetaAttribute metaAttribute)
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

    public static byte orderStatusNullValue()
    {
        return (byte)-128;
    }

    public static byte orderStatusMinValue()
    {
        return (byte)-127;
    }

    public static byte orderStatusMaxValue()
    {
        return (byte)127;
    }

    public byte orderStatus()
    {
        return buffer.getByte(offset + 38);
    }


    public static int priceId()
    {
        return 9;
    }

    public static int priceSinceVersion()
    {
        return 0;
    }

    public static int priceEncodingOffset()
    {
        return 39;
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
        return buffer.getLong(offset + 39, java.nio.ByteOrder.LITTLE_ENDIAN);
    }


    public static int filledId()
    {
        return 10;
    }

    public static int filledSinceVersion()
    {
        return 0;
    }

    public static int filledEncodingOffset()
    {
        return 47;
    }

    public static int filledEncodingLength()
    {
        return 8;
    }

    public static String filledMetaAttribute(final MetaAttribute metaAttribute)
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

    public static long filledNullValue()
    {
        return 0xffffffffffffffffL;
    }

    public static long filledMinValue()
    {
        return 0x0L;
    }

    public static long filledMaxValue()
    {
        return 0xfffffffffffffffeL;
    }

    public long filled()
    {
        return buffer.getLong(offset + 47, java.nio.ByteOrder.LITTLE_ENDIAN);
    }


    public static int totalFilledId()
    {
        return 11;
    }

    public static int totalFilledSinceVersion()
    {
        return 0;
    }

    public static int totalFilledEncodingOffset()
    {
        return 55;
    }

    public static int totalFilledEncodingLength()
    {
        return 8;
    }

    public static String totalFilledMetaAttribute(final MetaAttribute metaAttribute)
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

    public static long totalFilledNullValue()
    {
        return 0xffffffffffffffffL;
    }

    public static long totalFilledMinValue()
    {
        return 0x0L;
    }

    public static long totalFilledMaxValue()
    {
        return 0xfffffffffffffffeL;
    }

    public long totalFilled()
    {
        return buffer.getLong(offset + 55, java.nio.ByteOrder.LITTLE_ENDIAN);
    }


    public static int amountId()
    {
        return 12;
    }

    public static int amountSinceVersion()
    {
        return 0;
    }

    public static int amountEncodingOffset()
    {
        return 63;
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
        return buffer.getLong(offset + 63, java.nio.ByteOrder.LITTLE_ENDIAN);
    }


    public static int averagePriceId()
    {
        return 13;
    }

    public static int averagePriceSinceVersion()
    {
        return 0;
    }

    public static int averagePriceEncodingOffset()
    {
        return 71;
    }

    public static int averagePriceEncodingLength()
    {
        return 8;
    }

    public static String averagePriceMetaAttribute(final MetaAttribute metaAttribute)
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

    public static long averagePriceNullValue()
    {
        return 0xffffffffffffffffL;
    }

    public static long averagePriceMinValue()
    {
        return 0x0L;
    }

    public static long averagePriceMaxValue()
    {
        return 0xfffffffffffffffeL;
    }

    public long averagePrice()
    {
        return buffer.getLong(offset + 71, java.nio.ByteOrder.LITTLE_ENDIAN);
    }


    public static int stopPriceId()
    {
        return 14;
    }

    public static int stopPriceSinceVersion()
    {
        return 0;
    }

    public static int stopPriceEncodingOffset()
    {
        return 79;
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
        return buffer.getLong(offset + 79, java.nio.ByteOrder.LITTLE_ENDIAN);
    }


    public static int clientOrderIdId()
    {
        return 15;
    }

    public static int clientOrderIdSinceVersion()
    {
        return 0;
    }

    public static int clientOrderIdEncodingOffset()
    {
        return 87;
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
        return buffer.getLong(offset + 87, java.nio.ByteOrder.LITTLE_ENDIAN);
    }


    public static int matchingPriceId()
    {
        return 16;
    }

    public static int matchingPriceSinceVersion()
    {
        return 0;
    }

    public static int matchingPriceEncodingOffset()
    {
        return 95;
    }

    public static int matchingPriceEncodingLength()
    {
        return 8;
    }

    public static String matchingPriceMetaAttribute(final MetaAttribute metaAttribute)
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

    public static long matchingPriceNullValue()
    {
        return 0xffffffffffffffffL;
    }

    public static long matchingPriceMinValue()
    {
        return 0x0L;
    }

    public static long matchingPriceMaxValue()
    {
        return 0xfffffffffffffffeL;
    }

    public long matchingPrice()
    {
        return buffer.getLong(offset + 95, java.nio.ByteOrder.LITTLE_ENDIAN);
    }



    public String toString()
    {
        return appendTo(new StringBuilder(100)).toString();
    }

    public StringBuilder appendTo(final StringBuilder builder)
    {
        final int originalLimit = limit();
        limit(offset + actingBlockLength);
        builder.append("[OrderHistoryModel](sbeTemplateId=");
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
        //Token{signal=BEGIN_FIELD, name='orderId', referencedName='null', description='null', id=1, version=0, deprecated=0, encodedLength=8, offset=0, componentTokenCount=3, encoding=Encoding{presence=REQUIRED, primitiveType=null, byteOrder=LITTLE_ENDIAN, minValue=null, maxValue=null, nullValue=null, constValue=null, characterEncoding='null', epoch='null', timeUnit=null, semanticType='null'}}
        //Token{signal=ENCODING, name='uint64', referencedName='null', description='null', id=-1, version=0, deprecated=0, encodedLength=8, offset=0, componentTokenCount=1, encoding=Encoding{presence=REQUIRED, primitiveType=UINT64, byteOrder=LITTLE_ENDIAN, minValue=null, maxValue=null, nullValue=null, constValue=null, characterEncoding='null', epoch='null', timeUnit=null, semanticType='null'}}
        builder.append("orderId=");
        builder.append(orderId());
        builder.append('|');
        //Token{signal=BEGIN_FIELD, name='userId', referencedName='null', description='null', id=2, version=0, deprecated=0, encodedLength=8, offset=8, componentTokenCount=3, encoding=Encoding{presence=REQUIRED, primitiveType=null, byteOrder=LITTLE_ENDIAN, minValue=null, maxValue=null, nullValue=null, constValue=null, characterEncoding='null', epoch='null', timeUnit=null, semanticType='null'}}
        //Token{signal=ENCODING, name='uint64', referencedName='null', description='null', id=-1, version=0, deprecated=0, encodedLength=8, offset=8, componentTokenCount=1, encoding=Encoding{presence=REQUIRED, primitiveType=UINT64, byteOrder=LITTLE_ENDIAN, minValue=null, maxValue=null, nullValue=null, constValue=null, characterEncoding='null', epoch='null', timeUnit=null, semanticType='null'}}
        builder.append("userId=");
        builder.append(userId());
        builder.append('|');
        //Token{signal=BEGIN_FIELD, name='symbolId', referencedName='null', description='null', id=3, version=0, deprecated=0, encodedLength=4, offset=16, componentTokenCount=3, encoding=Encoding{presence=REQUIRED, primitiveType=null, byteOrder=LITTLE_ENDIAN, minValue=null, maxValue=null, nullValue=null, constValue=null, characterEncoding='null', epoch='null', timeUnit=null, semanticType='null'}}
        //Token{signal=ENCODING, name='int32', referencedName='null', description='null', id=-1, version=0, deprecated=0, encodedLength=4, offset=16, componentTokenCount=1, encoding=Encoding{presence=REQUIRED, primitiveType=INT32, byteOrder=LITTLE_ENDIAN, minValue=null, maxValue=null, nullValue=null, constValue=null, characterEncoding='null', epoch='null', timeUnit=null, semanticType='null'}}
        builder.append("symbolId=");
        builder.append(symbolId());
        builder.append('|');
        //Token{signal=BEGIN_FIELD, name='created', referencedName='null', description='null', id=4, version=0, deprecated=0, encodedLength=8, offset=20, componentTokenCount=3, encoding=Encoding{presence=REQUIRED, primitiveType=null, byteOrder=LITTLE_ENDIAN, minValue=null, maxValue=null, nullValue=null, constValue=null, characterEncoding='null', epoch='null', timeUnit=null, semanticType='null'}}
        //Token{signal=ENCODING, name='uint64', referencedName='null', description='null', id=-1, version=0, deprecated=0, encodedLength=8, offset=20, componentTokenCount=1, encoding=Encoding{presence=REQUIRED, primitiveType=UINT64, byteOrder=LITTLE_ENDIAN, minValue=null, maxValue=null, nullValue=null, constValue=null, characterEncoding='null', epoch='null', timeUnit=null, semanticType='null'}}
        builder.append("created=");
        builder.append(created());
        builder.append('|');
        //Token{signal=BEGIN_FIELD, name='updated', referencedName='null', description='null', id=5, version=0, deprecated=0, encodedLength=8, offset=28, componentTokenCount=3, encoding=Encoding{presence=REQUIRED, primitiveType=null, byteOrder=LITTLE_ENDIAN, minValue=null, maxValue=null, nullValue=null, constValue=null, characterEncoding='null', epoch='null', timeUnit=null, semanticType='null'}}
        //Token{signal=ENCODING, name='uint64', referencedName='null', description='null', id=-1, version=0, deprecated=0, encodedLength=8, offset=28, componentTokenCount=1, encoding=Encoding{presence=REQUIRED, primitiveType=UINT64, byteOrder=LITTLE_ENDIAN, minValue=null, maxValue=null, nullValue=null, constValue=null, characterEncoding='null', epoch='null', timeUnit=null, semanticType='null'}}
        builder.append("updated=");
        builder.append(updated());
        builder.append('|');
        //Token{signal=BEGIN_FIELD, name='orderType', referencedName='null', description='null', id=6, version=0, deprecated=0, encodedLength=1, offset=36, componentTokenCount=3, encoding=Encoding{presence=REQUIRED, primitiveType=null, byteOrder=LITTLE_ENDIAN, minValue=null, maxValue=null, nullValue=null, constValue=null, characterEncoding='null', epoch='null', timeUnit=null, semanticType='null'}}
        //Token{signal=ENCODING, name='int8', referencedName='null', description='null', id=-1, version=0, deprecated=0, encodedLength=1, offset=36, componentTokenCount=1, encoding=Encoding{presence=REQUIRED, primitiveType=INT8, byteOrder=LITTLE_ENDIAN, minValue=null, maxValue=null, nullValue=null, constValue=null, characterEncoding='null', epoch='null', timeUnit=null, semanticType='null'}}
        builder.append("orderType=");
        builder.append(orderType());
        builder.append('|');
        //Token{signal=BEGIN_FIELD, name='orderSide', referencedName='null', description='null', id=7, version=0, deprecated=0, encodedLength=1, offset=37, componentTokenCount=3, encoding=Encoding{presence=REQUIRED, primitiveType=null, byteOrder=LITTLE_ENDIAN, minValue=null, maxValue=null, nullValue=null, constValue=null, characterEncoding='null', epoch='null', timeUnit=null, semanticType='null'}}
        //Token{signal=ENCODING, name='int8', referencedName='null', description='null', id=-1, version=0, deprecated=0, encodedLength=1, offset=37, componentTokenCount=1, encoding=Encoding{presence=REQUIRED, primitiveType=INT8, byteOrder=LITTLE_ENDIAN, minValue=null, maxValue=null, nullValue=null, constValue=null, characterEncoding='null', epoch='null', timeUnit=null, semanticType='null'}}
        builder.append("orderSide=");
        builder.append(orderSide());
        builder.append('|');
        //Token{signal=BEGIN_FIELD, name='orderStatus', referencedName='null', description='null', id=8, version=0, deprecated=0, encodedLength=1, offset=38, componentTokenCount=3, encoding=Encoding{presence=REQUIRED, primitiveType=null, byteOrder=LITTLE_ENDIAN, minValue=null, maxValue=null, nullValue=null, constValue=null, characterEncoding='null', epoch='null', timeUnit=null, semanticType='null'}}
        //Token{signal=ENCODING, name='int8', referencedName='null', description='null', id=-1, version=0, deprecated=0, encodedLength=1, offset=38, componentTokenCount=1, encoding=Encoding{presence=REQUIRED, primitiveType=INT8, byteOrder=LITTLE_ENDIAN, minValue=null, maxValue=null, nullValue=null, constValue=null, characterEncoding='null', epoch='null', timeUnit=null, semanticType='null'}}
        builder.append("orderStatus=");
        builder.append(orderStatus());
        builder.append('|');
        //Token{signal=BEGIN_FIELD, name='price', referencedName='null', description='null', id=9, version=0, deprecated=0, encodedLength=8, offset=39, componentTokenCount=3, encoding=Encoding{presence=REQUIRED, primitiveType=null, byteOrder=LITTLE_ENDIAN, minValue=null, maxValue=null, nullValue=null, constValue=null, characterEncoding='null', epoch='null', timeUnit=null, semanticType='null'}}
        //Token{signal=ENCODING, name='uint64', referencedName='null', description='null', id=-1, version=0, deprecated=0, encodedLength=8, offset=39, componentTokenCount=1, encoding=Encoding{presence=REQUIRED, primitiveType=UINT64, byteOrder=LITTLE_ENDIAN, minValue=null, maxValue=null, nullValue=null, constValue=null, characterEncoding='null', epoch='null', timeUnit=null, semanticType='null'}}
        builder.append("price=");
        builder.append(price());
        builder.append('|');
        //Token{signal=BEGIN_FIELD, name='filled', referencedName='null', description='null', id=10, version=0, deprecated=0, encodedLength=8, offset=47, componentTokenCount=3, encoding=Encoding{presence=REQUIRED, primitiveType=null, byteOrder=LITTLE_ENDIAN, minValue=null, maxValue=null, nullValue=null, constValue=null, characterEncoding='null', epoch='null', timeUnit=null, semanticType='null'}}
        //Token{signal=ENCODING, name='uint64', referencedName='null', description='null', id=-1, version=0, deprecated=0, encodedLength=8, offset=47, componentTokenCount=1, encoding=Encoding{presence=REQUIRED, primitiveType=UINT64, byteOrder=LITTLE_ENDIAN, minValue=null, maxValue=null, nullValue=null, constValue=null, characterEncoding='null', epoch='null', timeUnit=null, semanticType='null'}}
        builder.append("filled=");
        builder.append(filled());
        builder.append('|');
        //Token{signal=BEGIN_FIELD, name='totalFilled', referencedName='null', description='null', id=11, version=0, deprecated=0, encodedLength=8, offset=55, componentTokenCount=3, encoding=Encoding{presence=REQUIRED, primitiveType=null, byteOrder=LITTLE_ENDIAN, minValue=null, maxValue=null, nullValue=null, constValue=null, characterEncoding='null', epoch='null', timeUnit=null, semanticType='null'}}
        //Token{signal=ENCODING, name='uint64', referencedName='null', description='null', id=-1, version=0, deprecated=0, encodedLength=8, offset=55, componentTokenCount=1, encoding=Encoding{presence=REQUIRED, primitiveType=UINT64, byteOrder=LITTLE_ENDIAN, minValue=null, maxValue=null, nullValue=null, constValue=null, characterEncoding='null', epoch='null', timeUnit=null, semanticType='null'}}
        builder.append("totalFilled=");
        builder.append(totalFilled());
        builder.append('|');
        //Token{signal=BEGIN_FIELD, name='amount', referencedName='null', description='null', id=12, version=0, deprecated=0, encodedLength=8, offset=63, componentTokenCount=3, encoding=Encoding{presence=REQUIRED, primitiveType=null, byteOrder=LITTLE_ENDIAN, minValue=null, maxValue=null, nullValue=null, constValue=null, characterEncoding='null', epoch='null', timeUnit=null, semanticType='null'}}
        //Token{signal=ENCODING, name='uint64', referencedName='null', description='null', id=-1, version=0, deprecated=0, encodedLength=8, offset=63, componentTokenCount=1, encoding=Encoding{presence=REQUIRED, primitiveType=UINT64, byteOrder=LITTLE_ENDIAN, minValue=null, maxValue=null, nullValue=null, constValue=null, characterEncoding='null', epoch='null', timeUnit=null, semanticType='null'}}
        builder.append("amount=");
        builder.append(amount());
        builder.append('|');
        //Token{signal=BEGIN_FIELD, name='averagePrice', referencedName='null', description='null', id=13, version=0, deprecated=0, encodedLength=8, offset=71, componentTokenCount=3, encoding=Encoding{presence=REQUIRED, primitiveType=null, byteOrder=LITTLE_ENDIAN, minValue=null, maxValue=null, nullValue=null, constValue=null, characterEncoding='null', epoch='null', timeUnit=null, semanticType='null'}}
        //Token{signal=ENCODING, name='uint64', referencedName='null', description='null', id=-1, version=0, deprecated=0, encodedLength=8, offset=71, componentTokenCount=1, encoding=Encoding{presence=REQUIRED, primitiveType=UINT64, byteOrder=LITTLE_ENDIAN, minValue=null, maxValue=null, nullValue=null, constValue=null, characterEncoding='null', epoch='null', timeUnit=null, semanticType='null'}}
        builder.append("averagePrice=");
        builder.append(averagePrice());
        builder.append('|');
        //Token{signal=BEGIN_FIELD, name='stopPrice', referencedName='null', description='null', id=14, version=0, deprecated=0, encodedLength=8, offset=79, componentTokenCount=3, encoding=Encoding{presence=REQUIRED, primitiveType=null, byteOrder=LITTLE_ENDIAN, minValue=null, maxValue=null, nullValue=null, constValue=null, characterEncoding='null', epoch='null', timeUnit=null, semanticType='null'}}
        //Token{signal=ENCODING, name='uint64', referencedName='null', description='null', id=-1, version=0, deprecated=0, encodedLength=8, offset=79, componentTokenCount=1, encoding=Encoding{presence=REQUIRED, primitiveType=UINT64, byteOrder=LITTLE_ENDIAN, minValue=null, maxValue=null, nullValue=null, constValue=null, characterEncoding='null', epoch='null', timeUnit=null, semanticType='null'}}
        builder.append("stopPrice=");
        builder.append(stopPrice());
        builder.append('|');
        //Token{signal=BEGIN_FIELD, name='clientOrderId', referencedName='null', description='null', id=15, version=0, deprecated=0, encodedLength=8, offset=87, componentTokenCount=3, encoding=Encoding{presence=REQUIRED, primitiveType=null, byteOrder=LITTLE_ENDIAN, minValue=null, maxValue=null, nullValue=null, constValue=null, characterEncoding='null', epoch='null', timeUnit=null, semanticType='null'}}
        //Token{signal=ENCODING, name='uint64', referencedName='null', description='null', id=-1, version=0, deprecated=0, encodedLength=8, offset=87, componentTokenCount=1, encoding=Encoding{presence=REQUIRED, primitiveType=UINT64, byteOrder=LITTLE_ENDIAN, minValue=null, maxValue=null, nullValue=null, constValue=null, characterEncoding='null', epoch='null', timeUnit=null, semanticType='null'}}
        builder.append("clientOrderId=");
        builder.append(clientOrderId());
        builder.append('|');
        //Token{signal=BEGIN_FIELD, name='matchingPrice', referencedName='null', description='null', id=16, version=0, deprecated=0, encodedLength=8, offset=95, componentTokenCount=3, encoding=Encoding{presence=REQUIRED, primitiveType=null, byteOrder=LITTLE_ENDIAN, minValue=null, maxValue=null, nullValue=null, constValue=null, characterEncoding='null', epoch='null', timeUnit=null, semanticType='null'}}
        //Token{signal=ENCODING, name='uint64', referencedName='null', description='null', id=-1, version=0, deprecated=0, encodedLength=8, offset=95, componentTokenCount=1, encoding=Encoding{presence=REQUIRED, primitiveType=UINT64, byteOrder=LITTLE_ENDIAN, minValue=null, maxValue=null, nullValue=null, constValue=null, characterEncoding='null', epoch='null', timeUnit=null, semanticType='null'}}
        builder.append("matchingPrice=");
        builder.append(matchingPrice());

        limit(originalLimit);

        return builder;
    }
}