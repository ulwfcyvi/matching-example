/* Generated SBE (Simple Binary Encoding) message codec */
package org.ssi.sbe;

import org.agrona.MutableDirectBuffer;
import org.agrona.sbe.*;

/**
 * Description of a basic Base Event
 */
@SuppressWarnings("all")
public class BaseEventEncoder implements MessageEncoderFlyweight
{
    public static final int BLOCK_LENGTH = 65;
    public static final int TEMPLATE_ID = 1;
    public static final int SCHEMA_ID = 1;
    public static final int SCHEMA_VERSION = 0;
    public static final java.nio.ByteOrder BYTE_ORDER = java.nio.ByteOrder.LITTLE_ENDIAN;

    private final BaseEventEncoder parentMessage = this;
    private MutableDirectBuffer buffer;
    protected int offset;
    protected int limit;

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

    public MutableDirectBuffer buffer()
    {
        return buffer;
    }

    public int offset()
    {
        return offset;
    }

    public BaseEventEncoder wrap(final MutableDirectBuffer buffer, final int offset)
    {
        if (buffer != this.buffer)
        {
            this.buffer = buffer;
        }
        this.offset = offset;
        limit(offset + BLOCK_LENGTH);

        return this;
    }

    public BaseEventEncoder wrapAndApplyHeader(
        final MutableDirectBuffer buffer, final int offset, final MessageHeaderEncoder headerEncoder)
    {
        headerEncoder
            .wrap(buffer, offset)
            .blockLength(BLOCK_LENGTH)
            .templateId(TEMPLATE_ID)
            .schemaId(SCHEMA_ID)
            .version(SCHEMA_VERSION);

        return wrap(buffer, offset + MessageHeaderEncoder.ENCODED_LENGTH);
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

    public BaseEventEncoder eventType(final byte value)
    {
        buffer.putByte(offset + 0, value);
        return this;
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

    public BaseEventEncoder orderId(final long value)
    {
        buffer.putLong(offset + 1, value, java.nio.ByteOrder.LITTLE_ENDIAN);
        return this;
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

    public BaseEventEncoder timestamp(final long value)
    {
        buffer.putLong(offset + 9, value, java.nio.ByteOrder.LITTLE_ENDIAN);
        return this;
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

    public BaseEventEncoder symbol(final int value)
    {
        buffer.putInt(offset + 17, value, java.nio.ByteOrder.LITTLE_ENDIAN);
        return this;
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

    public BaseEventEncoder price(final long value)
    {
        buffer.putLong(offset + 21, value, java.nio.ByteOrder.LITTLE_ENDIAN);
        return this;
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

    public BaseEventEncoder amount(final long value)
    {
        buffer.putLong(offset + 29, value, java.nio.ByteOrder.LITTLE_ENDIAN);
        return this;
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

    public BaseEventEncoder stopPrice(final long value)
    {
        buffer.putLong(offset + 37, value, java.nio.ByteOrder.LITTLE_ENDIAN);
        return this;
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

    public BaseEventEncoder orderSide(final byte value)
    {
        buffer.putByte(offset + 45, value);
        return this;
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

    public BaseEventEncoder orderType(final byte value)
    {
        buffer.putByte(offset + 46, value);
        return this;
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

    public BaseEventEncoder tradeType(final byte value)
    {
        buffer.putByte(offset + 47, value);
        return this;
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

    public BaseEventEncoder tradeOption(final byte value)
    {
        buffer.putByte(offset + 48, value);
        return this;
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

    public BaseEventEncoder userId(final long value)
    {
        buffer.putLong(offset + 49, value, java.nio.ByteOrder.LITTLE_ENDIAN);
        return this;
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

    public BaseEventEncoder clientOrderId(final long value)
    {
        buffer.putLong(offset + 57, value, java.nio.ByteOrder.LITTLE_ENDIAN);
        return this;
    }



    public String toString()
    {
        return appendTo(new StringBuilder(100)).toString();
    }

    public StringBuilder appendTo(final StringBuilder builder)
    {
        BaseEventDecoder writer = new BaseEventDecoder();
        writer.wrap(buffer, offset, BLOCK_LENGTH, SCHEMA_VERSION);

        return writer.appendTo(builder);
    }
}
