package me.fzzyhmstrs.fzzy_core.config_util.validated_field

import com.google.gson.Gson
import com.google.gson.GsonBuilder
import com.google.gson.JsonElement
import me.fzzyhmstrs.fzzy_core.FC
import me.fzzyhmstrs.fzzy_core.config_util.ConfigSerializable
import me.fzzyhmstrs.fzzy_core.config_util.ReadMeTextProvider
import me.fzzyhmstrs.fzzy_core.config_util.ValidationResult

/**
 * Validated Field Collection - serialization indistinguishable from their wrapped values, but deserialized into a validated wrapper
 *
 * Validated Fields CANNOT be serialized and deserialized by GSON properly. The JSON provided does not provide enough context as the validation is hidden within code only, and not serialized. These fields are not building new classes from scratch, they are updating and validating a pre-existing default class framework.
 *
 * Helper methods are provided to more easily sync configs directly with the PacketByteBuf framework, rather than serializing and then deserializing the entire JSON
 */

abstract class ValidatedField<T>(protected var storedValue: T): ConfigSerializable,
    ReadMeTextProvider {

    protected val gson: Gson = GsonBuilder().setPrettyPrinting().create()

    /////// fzzy core deserialization, NOT gson compatible ////////
    override fun deserialize(json: JsonElement, fieldName: String): ValidationResult<Boolean> {
        val tVal = deserializeHeldValue(json, fieldName)
        if (tVal.isError()){
            FC.LOGGER.error("Error deserializing manually entered config entry [$tVal], using default value [${tVal.get()}]")
            FC.LOGGER.error(">>> Possible reasons: ${tVal.getError()}")
            return ValidationResult.error(true, tVal.getError())
        }
        val tVal2 = validateAndCorrectInputs(tVal.get())
        storedValue = tVal2.get()
        if (tVal2.isError()){
            FC.LOGGER.error("Manually entered config entry [$tVal] had errors, corrected to [${tVal2.get()}]")
            FC.LOGGER.error(">>> Possible reasons: ${tVal2.getError()}")
            return ValidationResult.error(true, tVal2.getError())
        }

        return ValidationResult.success(false)
    }

    override fun serialize(): JsonElement {
        return serializeHeldValue()
    }

    protected abstract fun deserializeHeldValue(json: JsonElement, fieldName: String): ValidationResult<T>

    protected abstract fun serializeHeldValue(): JsonElement

    protected abstract fun validateAndCorrectInputs(input: T): ValidationResult<T>

    open fun get(): T{
        return storedValue
    }

}