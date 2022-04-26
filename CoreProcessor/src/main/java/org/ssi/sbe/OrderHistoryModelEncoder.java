/* Generated SBE (Simple Binary Encoding) message codec */
package org.ssi.sbe;

import org.agrona.MutableDirectBuffer;
import org.agrona.sbe.MessageEncoderFlyweight;

/**
 * Description of a order history model
 */
@SuppressWarnings("all")
public class OrderHistoryModelEncoder implements MessageEncoderFlyweight
{
    public static final int BLOCK_LENGTH = 103;
    public static final int TEMPLATE_ID = 1;
    public static final int SCHEMA_ID = 1;
    public static final int SCHEMA_VERSION = 0;
    public static final java.nio.ByteOrder BYTE_ORDER = java.nio.ByteOrder.LITTLE_ENDIAN;

    private final OrderHistoryModelEncoder parentMessage = this;
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

    public OrderHistoryModelEncoder wrap(final MutableDirectBuffer buffer, final int offset)
    {
        if (buffer != this.buffer)
        {
            this.buffer = buffer;
        }
        this.offset = offset;
        limit(offset + BLOCK_LENGTH);

        return this;
    }

    public OrderHistoryModelEncoder wrapAndApplyHeader(
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

    public OrderHistoryModelEncoder orderId(final long value)
    {
        buffer.putLong(offset + 0, value, java.nio.ByteOrder.LITTLE_ENDIAN);
        return this;
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

    public OrderHistoryModelEncoder userId(final long value)
    {
        buffer.putLong(offset + 8, value, java.nio.ByteOrder.LITTLE_ENDIAN);
        return this;
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

    public OrderHistoryModelEncoder symbolId(final int value)
    {
        buffer.putInt(offset + 16, value, java.nio.ByteOrder.LITTLE_ENDIAN);
        return this;
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

    public OrderHistoryModelEncoder created(final long value)
    {
        buffer.putLong(offset + 20, value, java.nio.ByteOrder.LITTLE_ENDIAN);
        return this;
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

    public OrderHistoryModelEncoder updated(final long value)
    {
        buffer.putLong(offset + 28, value, java.nio.ByteOrder.LITTLE_ENDIAN);
        return this;
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

    public OrderHistoryModelEncoder orderType(final byte value)
    {
        buffer.putByte(offset + 36, value);
        return this;
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

    public OrderHistoryModelEncoder orderSide(final byte value)
    {
        buffer.putByte(offset + 37, value);
        return this;
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

    public OrderHistoryModelEncoder orderStatus(final byte value)
    {
        buffer.putByte(offset + 38, value);
        return this;
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

    public OrderHistoryModelEncoder price(final long value)
    {
        buffer.putLong(offset + 39, value, java.nio.ByteOrder.LITTLE_ENDIAN);
        return this;
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

    public OrderHistoryModelEncoder filled(final long value)
    {
        buffer.putLong(offset + 47, value, java.nio.ByteOrder.LITTLE_ENDIAN);
        return this;
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

    public OrderHistoryModelEncoder totalFilled(final long value)
    {
        buffer.putLong(offset + 55, value, java.nio.ByteOrder.LITTLE_ENDIAN);
        return this;
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

    public OrderHistoryModelEncoder amount(final long value)
    {
        buffer.putLong(offset + 63, value, java.nio.ByteOrder.LITTLE_ENDIAN);
        return this;
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

    public OrderHistoryModelEncoder averagePrice(final long value)
    {
        buffer.putLong(offset + 71, value, java.nio.ByteOrder.LITTLE_ENDIAN);
        return this;
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

    public OrderHistoryModelEncoder stopPrice(final long value)
    {
        buffer.putLong(offset + 79, value, java.nio.ByteOrder.LITTLE_ENDIAN);
        return this;
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

    public OrderHistoryModelEncoder clientOrderId(final long value)
    {
        buffer.putLong(offset + 87, value, java.nio.ByteOrder.LITTLE_ENDIAN);
        return this;
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

    public OrderHistoryModelEncoder matchingPrice(final long value)
    {
        buffer.putLong(offset + 95, value, java.nio.ByteOrder.LITTLE_ENDIAN);
        return this;
    }



    public String toString()
    {
        return appendTo(new StringBuilder(100)).toString();
    }

    public StringBuilder appendTo(final StringBuilder builder)
    {
        OrderHistoryModelDecoder writer = new OrderHistoryModelDecoder();
        writer.wrap(buffer, offset, BLOCK_LENGTH, SCHEMA_VERSION);

        return writer.appendTo(builder);
    }
}