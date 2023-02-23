package me.fzzyhmstrs.fzzy_core.config_util.validated_field

import com.google.gson.JsonElement
import com.google.gson.JsonPrimitive
import me.fzzyhmstrs.fzzy_core.config_util.ValidationResult
import net.minecraft.util.Identifier
import java.util.function.Predicate

open class ValidatedIdentifier(defaultValue: Identifier, private val idValidator: Predicate<Identifier> = Predicate {true}, private val invalidIdMessage: String = "None"): ValidatedField<Identifier>(defaultValue) {

    init{
        if (!idValidator.test(defaultValue)){
            throw IllegalArgumentException("Default identifier [$defaultValue] not valid per defined idValidator in validated identifier [${this.javaClass.canonicalName}] in config class [${this.javaClass.enclosingClass?.canonicalName}]")
        }
    }

    override fun deserializeHeldValue(json: JsonElement, fieldName: String): ValidationResult<Identifier> {
        return  try {
            val string = gson.fromJson(json, String::class.java)
            val id = Identifier.tryParse(string)
            if (id == null){
                ValidationResult.error(storedValue,"Identifier $id couldn't be parsed, resorting to fallback.")
            } else {
                ValidationResult.success(id)
            }
        } catch (e: Exception){
            ValidationResult.error(storedValue,"json [$json] at key $fieldName is not a properly formatted string")
        }
    }

    override fun serializeHeldValue(): JsonElement {
        return JsonPrimitive(storedValue.toString())
    }

    override fun validateAndCorrectInputs(input: Identifier): ValidationResult<Identifier> {
        if (!idValidator.test(input)) {
            val errorMessage = "Config Identifier $input couldn't be validated. Needs to meet the following criteria: $invalidIdMessage"
            return ValidationResult.error(storedValue,errorMessage)
        }
        return ValidationResult.success(input)
    }

    override fun readmeText(): String{
        return "Identifier stored as a string that needs to meet the following criteria: $invalidIdMessage"
    }
}