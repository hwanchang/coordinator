package com.coordinator.common.serializer

import com.fasterxml.jackson.core.JsonParser
import com.fasterxml.jackson.databind.DeserializationContext
import com.fasterxml.jackson.databind.JsonDeserializer
import java.math.BigDecimal

class BigDecimalPriceDeserializer : JsonDeserializer<BigDecimal>() {
    override fun deserialize(p: JsonParser, ctxt: DeserializationContext): BigDecimal {
        val text = p.text.replace(",", "")
        return BigDecimal(text)
    }
}
