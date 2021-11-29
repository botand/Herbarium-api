package dev.xavierc.herbarium.api.utils

import com.google.gson.*
import org.joda.time.DateTime
import org.joda.time.format.ISODateTimeFormat
import org.json.simple.JSONValue
import java.lang.reflect.Type
import java.util.*

class DateTimeSerializer : JsonSerializer<DateTime>, JsonDeserializer<Any> {
    override fun serialize(src: DateTime?, typeOfSrc: Type?, context: JsonSerializationContext?): JsonElement {
        return JsonPrimitive(src?.toString(ISODateTimeFormat.dateTime()))
    }

    override fun deserialize(json: JsonElement?, typeOfT: Type?, context: JsonDeserializationContext?): Any {
        if (json == null)
            throw JsonParseException("Cannot be null");

        val primitive = json.asJsonPrimitive.asString

        return DateTime.parse(primitive, ISODateTimeFormat.dateTime())
    }

}