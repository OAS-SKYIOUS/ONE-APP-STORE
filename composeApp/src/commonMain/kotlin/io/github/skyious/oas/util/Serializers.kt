package io.github.skyious.oas.util

import kotlinx.serialization.*
import kotlinx.serialization.descriptors.*
import kotlinx.serialization.encoding.*
import kotlinx.serialization.json.*

/**
 * Custom serializer for Map<String, Any?> that can handle basic JSON types.
 * This is needed because Kotlinx Serialization doesn't support Any? type directly.
 */
object MapStringAnySerializer : KSerializer<Map<String, Any?>> {
    override val descriptor: SerialDescriptor = 
        buildClassSerialDescriptor("Map<String, Any?>")

    override fun serialize(encoder: Encoder, value: Map<String, Any?>) {
        val jsonEncoder = encoder as? JsonEncoder ?: 
            throw SerializationException("This serializer can be used only with Json format")
        
        val jsonObject = buildJsonObject {
            value.forEach { (key, value) ->
                when (value) {
                    null -> put(key, JsonNull)
                    is Boolean -> put(key, JsonPrimitive(value))
                    is Number -> put(key, JsonPrimitive(value))
                    is String -> put(key, JsonPrimitive(value))
                    is List<*> -> put(key, value.toJsonElement())
                    is Map<*, *> -> put(key, (value as Map<String, Any?>).toJsonElement())
                    else -> throw SerializationException("Unsupported type: ${value::class.simpleName}")
                }
            }
        }
        jsonEncoder.encodeJsonElement(jsonObject)
    }

    override fun deserialize(decoder: Decoder): Map<String, Any?> {
        val jsonDecoder = decoder as? JsonDecoder ?: 
            throw SerializationException("This serializer can be used only with Json format")
        
        val jsonElement = jsonDecoder.decodeJsonElement()
        require(jsonElement is JsonObject) { "Expected JsonObject, got ${jsonElement::class.simpleName}" }
        
        return jsonElement.mapValues { (_, value) ->
            when (value) {
                is JsonPrimitive -> {
                    when {
                        value.isString -> value.content
                        value.booleanOrNull != null -> value.boolean
                        value.intOrNull != null -> value.int
                        value.longOrNull != null -> value.long
                        value.doubleOrNull != null -> value.double
                        else -> value.content
                    }
                }
                is JsonArray -> value.map { element ->
                    when (element) {
                        is JsonPrimitive -> {
                            when {
                                element.isString -> element.content
                                element.booleanOrNull != null -> element.boolean
                                element.intOrNull != null -> element.int
                                element.longOrNull != null -> element.long
                                element.doubleOrNull != null -> element.double
                                else -> element.content
                            }
                        }
                        is JsonObject -> element.toMap()
                        is JsonArray -> element.toList()
                        is JsonNull -> null
                    }
                }
                is JsonObject -> value.toMap()
                is JsonNull -> null
            }
        }
    }

    private fun Any?.toJsonElement(): JsonElement = when (this) {
        null -> JsonNull
        is Boolean -> JsonPrimitive(this)
        is Number -> JsonPrimitive(this)
        is String -> JsonPrimitive(this)
        is List<*> -> JsonArray(this.map { it.toJsonElement() })
        is Map<*, *> -> JsonObject((this as Map<String, Any?>).mapValues { it.value.toJsonElement() })
        else -> throw SerializationException("Unsupported type: ${this::class.simpleName}")
    }

    private fun JsonObject.toMap(): Map<String, Any?> =
        entries.associate { (key, value) -> key to value.toKotlinValue() }
}

/**
 * Converts a JsonElement to a Kotlin value
 */
private fun JsonElement.toKotlinValue(): Any? = when (this) {
    is JsonPrimitive -> {
        when {
            isString -> content
            booleanOrNull != null -> boolean
            intOrNull != null -> int
            longOrNull != null -> long
            doubleOrNull != null -> double
            else -> content
        }
    }
    is JsonArray -> map { it.toKotlinValue() }
    is JsonObject -> toMap()
    is JsonNull -> null
}

/**
 * Extension function to convert a Map to a JsonElement
 */
private fun Map<*, *>.toJsonElement(): JsonElement {
    return JsonObject(
        mapValues { (_, value) -> value.toJsonElement() } as Map<String, JsonElement>
    )
}

/**
 * Extension function to convert a List to a JsonElement
 */
private fun List<*>.toJsonElement(): JsonElement {
    return JsonArray(map { it.toJsonElement() })
}

/**
 * Extension function to convert Any? to JsonElement
 */
private fun Any?.toJsonElement(): JsonElement = when (this) {
    null -> JsonNull
    is Boolean -> JsonPrimitive(this)
    is Number -> JsonPrimitive(this)
    is String -> JsonPrimitive(this)
    is List<*> -> this.toJsonElement()
    is Map<*, *> -> this.toJsonElement()
    else -> throw SerializationException("Unsupported type: ${this::class.simpleName}")
}
