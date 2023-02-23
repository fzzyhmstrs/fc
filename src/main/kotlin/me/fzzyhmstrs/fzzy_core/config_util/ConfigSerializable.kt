package me.fzzyhmstrs.fzzy_core.config_util

import com.google.gson.JsonElement

interface ConfigSerializable{
    fun serialize(): JsonElement
    fun deserialize(json: JsonElement, fieldName: String): ValidationResult<Boolean>
}