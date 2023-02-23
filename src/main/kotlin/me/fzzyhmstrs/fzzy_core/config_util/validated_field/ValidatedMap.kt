package me.fzzyhmstrs.fzzy_core.config_util.validated_field

import com.google.common.reflect.TypeToken
import com.google.gson.JsonElement
import me.fzzyhmstrs.fzzy_core.config_util.ValidationResult
import java.util.function.BiPredicate
import java.util.function.Predicate

open class ValidatedMap<R,T>(
    defaultValue: Map<R,T>,
    private val lType: TypeToken<Map<R,T>>,
    private val listEntryValidator: BiPredicate<R,T> = BiPredicate{_,_ -> true},
    private val invalidEntryMessage: String = "None"
): ValidatedField<Map<R,T>>(defaultValue) {

    override fun deserializeHeldValue(json: JsonElement, fieldName: String): ValidationResult<Map<R,T>> {
        return try{
            ValidationResult.success(gson.fromJson(json,lType.type))
        } catch(e: Exception){
            ValidationResult.error(storedValue,"Couldn't deserialize list $json from key $fieldName in config class [${this.javaClass.enclosingClass?.canonicalName}]")
        }
    }

    override fun serializeHeldValue(): JsonElement {
        return gson.toJsonTree(storedValue,lType.type)
    }

    override fun validateAndCorrectInputs(input: Map<R,T>): ValidationResult<Map<R,T>> {
        val tempList: MutableMap<R,T> = mutableMapOf()
        val errorList:MutableList<String> = mutableListOf()
        input.forEach {
            if(listEntryValidator.test(it.key,it.value)){
                tempList[it.key] = it.value
            } else {
                errorList.add(it.toString())
            }
        }
        if (errorList.isNotEmpty()){
            return ValidationResult.error(tempList, "Config map has errors, entries need to follow these constraints: $invalidEntryMessage. The following entries couldn't be validated and were removed: $errorList")
        }
        return ValidationResult.success(input)
    }

    override fun readmeText(): String{
        return "Map of key to values that meet the following criteria: $invalidEntryMessage"
    }
}