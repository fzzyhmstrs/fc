package me.fzzyhmstrs.fzzy_core.coding_util

import com.google.common.reflect.TypeToken
import com.google.gson.*
import me.fzzyhmstrs.fzzy_core.FC
import me.fzzyhmstrs.fzzy_core.coding_util.SyncedConfigHelper.OldClass
import me.fzzyhmstrs.fzzy_core.coding_util.SyncedConfigHelper.SyncedConfig
import me.fzzyhmstrs.fzzy_core.coding_util.SyncedConfigHelper.readOrCreate
import me.fzzyhmstrs.fzzy_core.coding_util.SyncedConfigHelper.readOrCreateUpdated
import net.fabricmc.loader.api.FabricLoader
import net.minecraft.network.PacketByteBuf
import net.minecraft.util.Identifier
import java.io.File
import java.io.FileWriter
import java.util.function.Predicate
import kotlin.reflect.KMutableProperty
import kotlin.reflect.full.declaredMemberProperties
import kotlin.reflect.jvm.javaType

/**
 * Helper object for building a server-client synced configuration system. Credits for the basis of this implementation to Gabriel Henrique de Oliveira (GabrielOlvH)
 *
 * This system is based largely on GSON, and configs are created and read in json format.
 *
 * The builtin [FcConfig][me.fzzyhmstrs.fzzy_core.config.FcConfig] implementation serves as an example/tutorial.
 *
 * This configuration system supports updating configuration layout without invalidating the prior configuration choices made. This is achieved with [readOrCreateUpdated] and [OldClass]. See Amethyst Imbuements configuration system for examples of configuration updating in action.
 *
 * Basic Implementation Steps:
 *
 * 1) Create a configuration object/static class that extends [SyncedConfig]
 *
 * 2) In order to mesh with Gson, create 1 or more container classes that hold the configuration options you want. Again, look to [FcConfig][me.fzzyhmstrs.fzzy_core.config.FcConfig] for its example [Flavors][me.fzzyhmstrs.fzzy_core.config.FcConfig.Flavors] class.
 *
 * 3) create a variable of your container class type, and initialize it by using [readOrCreate], which will either apply the default values set in the container class and write a config json with the specified name and location, or read an existing file and apply the user-defined choices.
 *
 * 4) [SyncedConfig] comes with some abstract functions that need overriding. readFromServer and writeToClient are where syncing is implemented. InitConfig is used to register the configurator. See [FcConfig][me.fzzyhmstrs.fzzy_core.config.FcConfig]
 *
 * 5) Register your config with the [SyncedConfigRegistry][me.fzzyhmstrs.fzzy_core.registry.SyncedConfigRegistry]. Perform this registration however you wish in your common ModInitializer, but SyncedConfig does provide an initConfig() method for organizing your implementation.
 *
 * 6) reference your stored container instance when you are dealing with the relevant information stored within. In Amethyst Cores case, the [flavors][me.fzzyhmstrs.fzzy_core.config.FcConfig.flavors] property is called by the [Flavorful][me.fzzyhmstrs.fzzy_core.item_util.interfaces.Flavorful] interface to determine if it should append tooltip descriptions or not.
 */

object SyncedConfigHelper {

    val gson: Gson = GsonBuilder().create()

    /**
     * basic method for creating and passing the config settings for a particular configuration class to/from json.
     *
     * Deprecated as of version X.X.X due to implementation of validated configurations.
     */
     @Deprecated("This method does not correct invalid inputs, use readOrCreateAndValidate` to perform automatic input correction")
    inline fun <reified T> readOrCreate(file: String, child: String = "", base: String = FC.MOD_ID, configClass: () -> T): T {
        val (dir,dirCreated) = makeDir(child, base)
        if (!dirCreated) {
            return configClass()
        }
        val f = File(dir, file)
        try {
            if (f.exists()) {
                return gson.fromJson(f.readLines().joinToString(""), T::class.java)
            } else if (!f.createNewFile()) {
                println("Failed to create default config file ($file), using default config.")
            } else {
                f.writeText(gson.toJson(configClass()))
            }
            return configClass()
        } catch (e: Exception) {
            println("Failed to read config file! Using default values: " + e.message)
            return configClass()
        }
    }

    /**
     * more advanced method for serializing/deserializing a config class that provides for 1 layer of version control.
     *
     * Deprecated as of version X.X.X due to implementation of validated configurations.
     */
    @Suppress("UNUSED_PARAMETER")
    @Deprecated("This method does not correct invalid inputs, use readOrCreateAndValidate` to perform automatic input correction")
    inline fun <reified T, reified P: OldClass<T>> readOrCreateUpdated(file: String, previous: String, child: String = "", base: String = FC.MOD_ID, configClass: () -> T, previousClass: () -> P): T{
        val (dir,dirCreated) = makeDir(child, base)
        if (!dirCreated) {
            return configClass()
        }
        val p = File(dir, previous)
        try {
            if (p.exists()) {
                val previousConfig = gson.fromJson(p.readLines().joinToString(""), P::class.java)
                if (previousConfig is OldClass<T>){
                    val newClass = previousConfig.generateNewClass()
                    val f = File(dir,file)
                    if (f.exists()){
                        p.delete() //attempts to delete the now useless old config version file
                        return gson.fromJson(f.readLines().joinToString(""), T::class.java)
                    } else if (!f.createNewFile()){
                        //don't delete old file if the new one can't be generated to take its place
                        println("Failed to create new config file ($file), using old config with new defaults.")
                    } else {
                        p.delete() //attempts to delete the now useless old config version file
                        f.writeText(gson.toJson(newClass))
                    }
                    return newClass

                } else {
                    throw RuntimeException("Old config not properly set up as an OldConfig: ${P::class.simpleName}")
                }
            } else {
                return readOrCreate(file,child, base, configClass)
            }
        } catch (e: Exception) {
            println("Failed to read config file $file! Using default values: " + e.message)
            e.printStackTrace()
            return configClass()
        }
    }

    ////////////////////////////////////////

    /**
     * Improved basic config serializer/deserializer method that is no longer inline and created validated configs using [ValidatedField]
     *
     * incorrect inputs will automatically be corrected where possible, or reverted to default if not, and the validated config re-written to it's file
     */
    fun <T : Any> readOrCreateAndValidate(file: String, child: String = "", base: String = FC.MOD_ID, configClass: () -> T): T {
        val (dir,dirCreated) = makeDir(child, base)
        if (!dirCreated) {
            return configClass()
        }
        val f = File(dir, file)
        try {
            if (f.exists()) {
                val str = f.readLines().joinToString("")
                println(">>> Unvalidated config:")
                println(str)
                val readConfig = deserializeConfig(configClass(),JsonParser.parseString(str))
                if (readConfig.isError()) {
                    FC.LOGGER.warn("Errors found in $file per above log entries, attempting to correct invalid inputs automatically.")
                    val correctedConfig = serializeConfig(readConfig.get())
                    println(">>> Corrected config:")
                    println(correctedConfig)
                    f.writeText(correctedConfig)
                }
                return readConfig.get()
            } else if (!f.createNewFile()) {
                println("Failed to create default config file ($file), using default config.")
            } else {
                f.writeText(gson.toJson(configClass()))
            }
            return configClass()
        } catch (e: Exception) {
            println("Failed to read config file $file! Using default values: " + e.message)
            e.printStackTrace()
            return configClass()
        }
    }

    /**
     * Improved advanced config serializer/deserializer method that is no longer inline and created validated configs using [ValidatedField], allowing for 1 layer of version control
     *
     * incorrect inputs from the new and old config will be automatically corrected where possible, or reverted to default if not, and the validated and updated config written to it's file
     */
    fun <T: Any> readOrCreateUpdatedAndValidate(file: String, previous: String, child: String = "", base: String = FC.MOD_ID, configClass: () -> T, previousClass: () -> OldClass<T>): T{
        val (dir,dirCreated) = makeDir(child, base)
        if (!dirCreated) {
            return configClass()
        }
        val p = File(dir, previous)
        try {
            if (p.exists()) {
                val pStr = p.readLines().joinToString("")
                val previousConfig = deserializeConfig(previousClass(),JsonParser.parseString(pStr))
                if (previousConfig.isError()){
                    FC.LOGGER.error("Old config $previous had errors, attempted to correct before updating.")
                }
                val newClass = previousConfig.get().generateNewClass()
                    val f = File(dir,file)
                    if (f.exists()){
                        p.delete() //attempts to delete the now useless old config version file
                        val str = f.readLines().joinToString("")
                        println(">>> Unvalidated config:")
                        println(str)
                        val readConfig = deserializeConfig(configClass(),JsonParser.parseString(str))
                        if (readConfig.isError()){
                            FC.LOGGER.warn("Errors found in $file per above logs, attempting to correct invalid inputs automatically.")
                            val correctedConfig = serializeConfig(readConfig.get())
                            println(">>> Corrected config:")
                            println(correctedConfig)
                            f.writeText(correctedConfig)
                        }
                        return readConfig.get()
                    } else if (!f.createNewFile()){
                        //don't delete old file if the new one can't be generated to take its place
                        println("Failed to create new config file ($file), using old config with new defaults.")
                    } else {
                        p.delete() //attempts to delete the now useless old config version file
                        f.writeText(gson.toJson(newClass))
                    }
                    return newClass
            } else {
                return readOrCreateAndValidate(file,child, base, configClass)
            }
        } catch (e: Exception) {
            println("Failed to read config file $file! Using default values: " + e.message)
            e.printStackTrace()
            return configClass()
        }
    }

    /**
     * method can be used to create a directory in the config parent directory. If the directory can't be created, the right member of the returning Pair will be false.
     */
    fun makeDir(child: String, base: String): Pair<File,Boolean>{
        val dir = if (child != ""){
            File(File(FabricLoader.getInstance().configDir.toFile(), base), child)
        } else {
            File(FabricLoader.getInstance().configDir.toFile(), base)
        }
        if (!dir.exists() && !dir.mkdirs()) {
            println("Could not create directory, using default configs.")
            return Pair(dir,false)
        }
        return Pair(dir,true)
    }

    /**
     * basic interface for creating a synced configurator as described above
     */
    interface SyncedConfig{
        fun readFromServer(buf: PacketByteBuf)
        fun writeToClient(buf: PacketByteBuf)
        fun initConfig()
    }

    /**
     * used to generate backward compatibility with an old version of a config file and a new one. See Amethyst Imbuements config for example implementations.
     */
    interface OldClass<T>{

        fun generateNewClass(): T

    }

    /**
     * This interface can be used to create a README. See Amethyst Cores [ReadmeText][me.fzzyhmstrs.fzzy_core.config.ReadmeText] for an example implementation.
     */
    interface ReadMeWriter{
        fun writeReadMe(file: String, base: String = FC.MOD_ID){
            val textLines: List<String> = readmeText()
            val dirPair = makeDir("", base)
            if (!dirPair.second){
                println("Couldn't make directory for storing the readme")
            }
            val f = File(dirPair.first,file)
            val fw = FileWriter(f)
            textLines.forEach {
                    value -> fw.write(value)
                fw.write(System.getProperty("line.separator"))
            }
            fw.close()
        }

        fun readmeText(): List<String>
    }

    /////////////////////////////////////////////////////////////////////////

    interface ConfigSerializable{
        fun serialize():JsonElement
        fun deserialize(json: JsonElement, fieldName: String): ValidationResult<Boolean>
    }

    fun serializeConfig(config: Any): String{
        val json = JsonObject()
        config.javaClass.kotlin.declaredMemberProperties.forEach {
            if (it is KMutableProperty<*>){
                val propVal = it.get(config)
                val name = it.name
                val el = if (propVal is ValidatedField<*>){
                    propVal.serialize()
                } else {
                    gson.toJsonTree(propVal,it.returnType.javaType)

                }
                json.add(name,el)
            }
        }
        return gson.toJson(json)
    }

    fun <T: Any> deserializeConfig(config: T, json: JsonElement): ValidationResult<T>{
        if (!json.isJsonObject) return ValidationResult.error(config,"Config ${config.javaClass.canonicalName} is corrupted or improperly formatted for parsing")
        val jsonObject = json.asJsonObject
        var error = false
        for (it in config.javaClass.kotlin.declaredMemberProperties){
            if (it is KMutableProperty<*>){
                val propVal = it.get(config)
                val name = it.name
                val jsonElement = jsonObject.get(name) ?: continue
                if (propVal is ValidatedField<*>){
                    val result = propVal.deserialize(jsonElement, name)
                    if(result.isError()){
                        error = true
                    }
                } else {
                    it.setter.call(config, gson.fromJson(jsonElement,it.returnType.javaType))
                }
            }
        }
        return if(!error){
            ValidationResult.success(config)
        } else {
            ValidationResult.error(config,"Errors found!")
        }
    }

    class ValidationResult<T> private constructor(private val storedVal: T, private val error: String = ""){
        fun isError(): Boolean{
            return error.isNotEmpty()
        }
        fun getError(): String{
            return error
        }
        fun get(): T{
            return storedVal
        }
        companion object{
            fun <T>success(storedVal: T): ValidationResult<T>{
                return ValidationResult(storedVal)
            }
            fun <T>error(storedVal: T, error: String): ValidationResult<T>{
                return ValidationResult(storedVal,error)
            }
        }
    }

    ///////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////
    //Validated Field Collection - serialization indistinguishable from their wrapped values, but deserialized into a validated wrapper
    ///////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////

    abstract class ValidatedField<T>(protected var storedValue: T): ConfigSerializable {

        protected val gson: Gson = GsonBuilder().create()

        override fun deserialize(json: JsonElement, fieldName: String): ValidationResult<Boolean> {
            val tVal = deserializeHeldValue(json, fieldName)
            if (tVal.isError()){
                FC.LOGGER.error("Error deserializing manually entered config entry [$tVal], using default value [${tVal.get()}]")
                FC.LOGGER.error(">>> Possible reasons: ${tVal.getError()}")
                return ValidationResult.error(true,tVal.getError())
            }
            val tVal2 = validateAndCorrectInputs(tVal.get())
            storedValue = tVal2.get()
            if (tVal2.isError()){
                FC.LOGGER.error("Manually entered config entry [$tVal] had errors, corrected to [${tVal2.get()}]")
                FC.LOGGER.error(">>> Possible reasons: ${tVal2.getError()}")
                return ValidationResult.error(true,tVal2.getError())
            }

            return ValidationResult.success(false)
        }

        override fun serialize(): JsonElement {
            return serializeHeldValue()
        }

        protected abstract fun deserializeHeldValue(json: JsonElement, fieldName: String): ValidationResult<T>

        protected abstract fun serializeHeldValue():JsonElement

        protected abstract fun validateAndCorrectInputs(input: T): ValidationResult<T>

        open fun get(): T{
            return storedValue
        }

    }

    open class ValidatedNumber<T>(private val numberClass: Class<T>, defaultValue: T, private val minValue: T, private val maxValue: T)  : ValidatedField<T>(defaultValue) where T: Number, T: Comparable<T>{

        init{
            if (minValue > maxValue){
                throw IllegalArgumentException("Min value [$minValue] greater than max value [$maxValue] in validated number [${this.javaClass.canonicalName}] in config class [${this.javaClass.enclosingClass?.canonicalName}]")
            }
        }

        override fun deserializeHeldValue(json: JsonElement, fieldName: String): ValidationResult<T> {
            val i = try{
                ValidationResult.success(gson.fromJson(json, numberClass))
            } catch (e: Exception){
                ValidationResult.error(storedValue,"json [$json] at key $fieldName is not a properly formatted number")
            }
            return i
        }

        override fun serializeHeldValue(): JsonElement {
            return JsonPrimitive(storedValue)
        }

        override fun validateAndCorrectInputs(input: T): ValidationResult<T> {
            if (input < minValue) {
                val errorMessage = "Value {$input} is below the minimum bound of {$minValue}."
                return ValidationResult.error(minValue, errorMessage)
            }
            if (input > maxValue) {
                val errorMessage = "Value {$input} is above the maximum bound of {$maxValue}."
                return ValidationResult.error(maxValue, errorMessage)
            }
            return ValidationResult.success(input)
        }

    }

    open class ValidatedInt(defaultValue: Int,maxValue: Int, minValue:Int = 0): ValidatedNumber<Int>(Int::class.java,defaultValue,minValue,maxValue)

    open class ValidatedFloat(defaultValue: Float,maxValue: Float, minValue:Float = 0f): ValidatedNumber<Float>(Float::class.java,defaultValue,minValue,maxValue)

    open class ValidatedDouble(defaultValue: Double,maxValue: Double, minValue:Double = 0.0): ValidatedNumber<Double>(Double::class.java,defaultValue,minValue,maxValue)

    open class ValidatedLong(defaultValue: Long,maxValue: Long, minValue:Long = 0L): ValidatedNumber<Long>(Long::class.java,defaultValue,minValue,maxValue)

    open class ValidatedShort(defaultValue: Short,maxValue: Short, minValue:Short = 0): ValidatedNumber<Short>(Short::class.java,defaultValue,minValue,maxValue)

    open class ValidatedByte(defaultValue: Byte,maxValue: Byte, minValue:Byte = 0): ValidatedNumber<Byte>(Byte::class.java,defaultValue,minValue,maxValue)

    /*open class ValidatedInt(defaultValue: Int, private val minValue: Int, private val maxValue: Int): ValidatedField<Int>(defaultValue) {
        
        override fun deserializeHeldValue(json: JsonElement): Int {
            return gson.fromJson(json,Int::class.java)
        }

        override fun serializeHeldValue(): JsonElement {
            return JsonPrimitive(storedValue)
        }

        override fun validateAndCorrectInputs(input: Int): Int {
            if (input < minValue) {
                errorMessage = "value {$input} is below the minimum bound of {$minValue}. Correct to the minimum value."
                return minValue
            }
            if (input > maxValue) {
                errorMessage = "value {$input} is above the maximum bound of {$maxValue}. Correct to the maximum value."
                return maxValue
            }
            return input
        }
       
    }

    open class ValidatedFloat(defaultValue: Float, private val minValue: Float, private val maxValue: Float): ValidatedField<Float>(defaultValue) {

        override fun deserializeHeldValue(json: JsonElement): Float {
            return gson.fromJson(json,Float::class.java)
        }

        override fun serializeHeldValue(): JsonElement {
            return JsonPrimitive(storedValue)
        }

        override fun validateAndCorrectInputs(input: Float): Float {
            if (input < minValue) {
                errorMessage = "value {$input} is below the minimum bound of {$minValue}."
                return minValue
            }
            if (input > maxValue) {
                errorMessage = "value {$input} is above the maximum bound of {$maxValue}."
                return maxValue
            }
            return input
        }
    }

    open class ValidatedDouble(defaultValue: Double, private val minValue: Double, private val maxValue: Double): ValidatedField<Double>(defaultValue) {

        override fun deserializeHeldValue(json: JsonElement): Double {
            return gson.fromJson(json,Double::class.java)
        }

        override fun serializeHeldValue(): JsonElement {
            return JsonPrimitive(storedValue)
        }

        override fun validateAndCorrectInputs(input: Double): ValidatedField<Double> {
            if (input < minValue) {
                errorMessage = "value {$input} is below the minimum bound of {$minValue}."
                return ValidatedDouble(minValue, minValue, maxValue)
            }
            if (input > maxValue) {
                errorMessage = "value {$input} is above the maximum bound of {$maxValue}."
                return ValidatedDouble(maxValue, minValue, maxValue)
            }
            return ValidatedDouble(input,minValue, maxValue)
        }
    }

    open class ValidatedShort(defaultValue: Short, private val minValue: Short, private val maxValue: Short): ValidatedField<Short>(defaultValue) {

        override fun deserializeHeldValue(json: JsonElement): Short {
            return gson.fromJson(json,Short::class.java)
        }

        override fun serializeHeldValue(): JsonElement {
            return JsonPrimitive(storedValue)
        }

        override fun validateAndCorrectInputs(input: Short): ValidatedField<Short> {
            if (input < minValue) {
                errorMessage = "value {$input} is below the minimum bound of {$minValue}."
                return ValidatedShort(minValue, minValue, maxValue)
            }
            if (input > maxValue) {
                errorMessage = "value {$input} is above the maximum bound of {$maxValue}."
                return ValidatedShort(maxValue, minValue, maxValue)
            }
            return ValidatedShort(input,minValue, maxValue)
        }
    }

    open class ValidatedLong(defaultValue: Long, private val minValue: Long, private val maxValue: Long): ValidatedField<Long>(defaultValue) {

        override fun deserializeHeldValue(json: JsonElement): Long {
            return gson.fromJson(json,Long::class.java)
        }

        override fun serializeHeldValue(): JsonElement {
            return JsonPrimitive(storedValue)
        }

        override fun validateAndCorrectInputs(input: Long): ValidatedField<Long> {
            if (input < minValue) {
                errorMessage = "value {$input} is below the minimum bound of {$minValue}."
                return ValidatedLong(minValue, minValue, maxValue)
            }
            if (input > maxValue) {
                errorMessage = "value {$input} is above the maximum bound of {$maxValue}."
                return ValidatedLong(maxValue, minValue, maxValue)
            }
            return ValidatedLong(input,minValue, maxValue)
        }
    }*/


    open class ValidatedIdentifier(defaultValue: Identifier, private val idValidator: Predicate<Identifier> = Predicate {true}, private val invalidIdMessage: String = ""): ValidatedField<Identifier>(defaultValue) {

        init{
            if (!idValidator.test(defaultValue)){
                throw IllegalArgumentException("Default identifier [$defaultValue] not valid per defined idValidator in validated identifier [${this.javaClass.canonicalName}] in config class [${this.javaClass.enclosingClass?.canonicalName}]")
            }
        }

        override fun deserializeHeldValue(json: JsonElement,fieldName: String): ValidationResult<Identifier> {
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
    }

    open class ValidatedEnum<T:Enum<T>>(defaultValue: T, private val enum: Class<T>): ValidatedField<T>(defaultValue) {

        private val valuesMap: Map<String,T>
        init{
            val map: MutableMap<String,T> = mutableMapOf()
            enum.enumConstants?.forEach {
                map[it.name] = it
            }
            valuesMap = map
        }

        override fun deserializeHeldValue(json: JsonElement, fieldName: String): ValidationResult<T> {
            return try{
                val string = gson.fromJson(json, String::class.java)
                val chkEnum = valuesMap[string]
                if(chkEnum == null){
                    ValidationResult.error(storedValue,"Entered value isn't a valid selection from the possible values. Possible values are: ${valuesMap.keys}")
                } else {
                    ValidationResult.success(chkEnum)
                }
            } catch (e: Exception){
                ValidationResult.error(storedValue,"json [$json] at key $fieldName is not a properly formatted string")
            }
        }

        override fun serializeHeldValue(): JsonElement {
            return JsonPrimitive(storedValue.name)
        }

        override fun validateAndCorrectInputs(input: T): ValidationResult<T> {
            return ValidationResult.success(input)
        }
    }

    open class ValidatedList<R,T:List<R>>(defaultValue: List<R>, private val lType: TypeToken<T>, private val listEntryValidator: Predicate<R>, private val invalidEntryMessage: String = ""): ValidatedField<List<R>>(defaultValue) {

        override fun deserializeHeldValue(json: JsonElement, fieldName: String): ValidationResult<List<R>> {
            return try{
                ValidationResult.success(gson.fromJson(json,lType.type))
            } catch(e: Exception){
                ValidationResult.error(storedValue,"Couldn't deserialize list $json from key $fieldName in config class [${this.javaClass.enclosingClass?.canonicalName}]")
            }
        }

        override fun serializeHeldValue(): JsonElement {
            return gson.toJsonTree(storedValue,lType.type)
        }

        override fun validateAndCorrectInputs(input: List<R>): ValidationResult<List<R>> {
            val tempList: MutableList<R> = mutableListOf()
            val errorList:MutableList<String> = mutableListOf()
            input.forEach {
                if(listEntryValidator.test(it)){
                    tempList.add(it)
                } else {
                    errorList.add(it.toString())
                }
            }
            if (errorList.isNotEmpty()){
                return ValidationResult.error(tempList, "Config list has errors, entries need to follow these constraints: $invalidEntryMessage. The following entries couldn't be validated and were removed: $errorList")
            }
            return ValidationResult.success(input)
        }
    }
}
