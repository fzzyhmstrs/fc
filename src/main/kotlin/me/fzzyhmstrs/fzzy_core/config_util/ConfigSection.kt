package me.fzzyhmstrs.fzzy_core.config_util

import com.google.gson.JsonElement
import com.google.gson.JsonParser

open class ConfigSection(
    headerText: List<String> = listOf(),
    decorator: LineDecorator = LineDecorator.DEFAULT)
    :
    ReadMeBuilder("","",headerText, decorator,1),
    ConfigSerializable,
    ServerClientSynced
{

    constructor(sectionLabel: String): this(listOf(sectionLabel))

    override fun serialize(): JsonElement {
        val str = SyncedConfigHelperV1.serializeConfig(this)
        return JsonParser.parseString(str)
    }

    override fun deserialize(json: JsonElement, fieldName: String): ValidationResult<Boolean> {
        val validatedSection = SyncedConfigHelperV1.deserializeConfig(this,json)
        return if (validatedSection.isError()){
            ValidationResult.error(true,validatedSection.getError())
        } else {
            ValidationResult.success(false)
        }
    }

    override fun toString(): String {
        return SyncedConfigHelperV1.serializeConfig(this)
    }
}