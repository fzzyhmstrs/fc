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
import java.lang.reflect.Type
import java.util.function.Predicate

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

    val gson: Gson = GsonBuilder().setPrettyPrinting().create()

    /**
     * basic function for creating and passing the config settings for a particular configuration class to/from json.
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
            println("Failed to read config file! Using default values: " + e.message)
            return configClass()
        }
    }
    
    
    inline fun <reified T> readOrCreateAndValidate(file: String, child: String = "", base: String = FC.MOD_ID, configClass: () -> T): T {
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
                val readConfig = gson.fromJson(str, T::class.java)
                val chkStr = gson.toJson(readConfig)
                println(">>> Validated config:")
                println(chkStr)
                if (chkStr != str){
                    FC.LOGGER.warn("Errors found per above logs, attempting to correct invalid inputs automatically.")
                    f.writeText(chkStr)
                }
                return readConfig
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
    
    inline fun <reified T, reified P: OldClass<T>> readOrCreateUpdatedAndValidate(file: String, previous: String, child: String = "", base: String = FC.MOD_ID, configClass: () -> T, previousClass: () -> P): T{
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
                        val str = f.readLines().joinToString("")
                        println(">>> Unvalidated config:")
                        println(str)
                        val readConfig = gson.fromJson(str, T::class.java)
                        val chkStr = gson.toJson(readConfig)
                        println(">>> Validated config:")
                        println(chkStr)
                        if (chkStr != str){
                            FC.LOGGER.warn("Errors found per above logs, attempting to correct invalid inputs automatically.")
                            f.writeText(chkStr)
                        }
                        return readConfig
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
            println("Failed to read config file! Using default values: " + e.message)
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


    abstract class ValidatedField<T>(protected var storedValue: T): JsonSerializer<ValidatedField<T>>, JsonDeserializer<ValidatedField<T>> {

        protected val gson: Gson = GsonBuilder().create()
        protected var errorMessage = ""

        override fun deserialize(json: JsonElement, typeOfT: Type, context: JsonDeserializationContext): ValidatedField<T> {
            val tVal = deserializeHeldValue(json)
            val tVal2 = validateAndCorrectInputs(tVal)
            if (tVal != tVal2 || errorMessage != ""){
                FC.LOGGER.error("Manually entered config entry {$tVal} had errors, corrected to {$tVal2})
                FC.LOGGER.error("Possible reasons: ${errorResolutionMessage()})
            }
            return tVal2
        }

        override fun serialize(
            src: ValidatedField<T>,
            typeOfSrc: Type,
            context: JsonSerializationContext
        ): JsonElement {
            return src.serializeHeldValue()
        }

        abstract fun deserializeHeldValue(json: JsonElement): T

        abstract fun serializeHeldValue():JsonElement

        abstract fun validateAndCorrectInputs(input: T): ValidatedField<T>
                        
        private fun errorResolutionMessage(): String{
            val msg = errorMessage
            errorMessage = ""
            return msg
        }

        open fun get(): T{
            return storedValue
        }

    }

    open class ValidatedInt(defaultValue: Int, private val minValue: Int, private val maxValue: Int): ValidatedField<Int>(defaultValue) {
        
        override fun deserializeHeldValue(json: JsonElement): Int {
            return gson.fromJson(json,Int::class.java)
        }

        override fun serializeHeldValue(): JsonElement {
            return JsonPrimitive(storedValue)
        }

        override fun validateAndCorrectInputs(input: Int): ValidatedField<Int> {
            if (input < minValue) {
                errorMessage = "value {$input} is below the minimum bound of {$minValue}. Correct to the minimum value.
                return ValidatedInt(minValue, minValue, maxValue)
            }
            if (input > maxValue) {
                errorMessage = "value {$input} is above the maximum bound of {$maxValue}. Correct to the maximum value.
                return ValidatedInt(maxValue, minValue, maxValue)
            }
            return ValidatedInt(input,minValue, maxValue)
        }
       
    }

    open class ValidatedFloat(defaultValue: Float, private val minValue: Float, private val maxValue: Float): ValidatedField<Float>(defaultValue) {

        override fun deserializeHeldValue(json: JsonElement): Float {
            return gson.fromJson(json,Float::class.java)
        }

        override fun serializeHeldValue(): JsonElement {
            return JsonPrimitive(storedValue)
        }

        override fun validateAndCorrectInputs(input: Float): ValidatedField<Float> {
            if (input < minValue) {
                errorMessage = "value {$input} is below the minimum bound of {$minValue}.
                return ValidatedFloat(minValue, minValue, maxValue)
            }
            if (input > maxValue) {
                errorMessage = "value {$input} is above the maximum bound of {$maxValue}.
                return ValidatedFloat(maxValue, minValue, maxValue)
            }
            return ValidatedFloat(input,minValue, maxValue)
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
                errorMessage = "value {$input} is below the minimum bound of {$minValue}.
                return ValidatedDouble(minValue, minValue, maxValue)
            }
            if (input > maxValue) {
                errorMessage = "value {$input} is above the maximum bound of {$maxValue}.
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
                errorMessage = "value {$input} is below the minimum bound of {$minValue}.
                return ValidatedShort(minValue, minValue, maxValue)
            }
            if (input > maxValue) {
                errorMessage = "value {$input} is above the maximum bound of {$maxValue}.
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
                errorMessage = "value {$input} is below the minimum bound of {$minValue}.
                return ValidatedLong(minValue, minValue, maxValue)
            }
            if (input > maxValue) {
                errorMessage = "value {$input} is above the maximum bound of {$maxValue}.
                return ValidatedLong(maxValue, minValue, maxValue)
            }
            return ValidatedLong(input,minValue, maxValue)
        }
    }

    open class ValidatedIdentifier(defaultValue: Identifier, private val idValidator: Predicate<Identifier>, private val invalidIdMessage: String): ValidatedField<Identifier>(defaultValue) {

        override fun deserializeHeldValue(json: JsonElement): Identifier {
            val string = gson.fromJson(json, String::class.java)
            val id = Identifier.tryParse(string)
            if (id == null){
                FC.LOGGER.error("Identifier $id couldn't be parsed, resorting to fallback.")
            }
            return id ?: defaultValue
        }

        override fun serializeHeldValue(): JsonElement {
            return JsonPrimitive(storedValue.toString())
        }

        override fun validateAndCorrectInputs(input: Identifier): ValidatedField<Identifier> {
            if (!idValidator.test(input)) {
                errorMessage = "Config Identifier $input couldn't be validated. Needs to meet the following criteria: $invalidIdMessage"
                return ValidatedIdentifier(storedValue,idValidator)
            }
            return ValidatedIdentifier(input,idValidator)
        }
    }

    open class ValidatedEnum<T:Enum<T>>(defaultValue: T, private val enum: Class<T>): ValidatedField<T>(defaultValue) {

        private val values: Map<String,T>
        init{
            val map: MutableMap<String,T> = mutableMapOf()
            enum.enumConstants?.forEach {
                map[it.name] = it
            }
            values = map
        }

        override fun deserializeHeldValue(json: JsonElement): T {
            val string = gson.fromJson(json, String::class.java)
            val chkEnum = values[string]
            if(chkEnum == null){
                errorMessage = "Entered value isn't a valid selection from the possible values. Possible values are: ${defaultValue.values}"
            }
            return chkEnum?:storedValue
        }

        override fun serializeHeldValue(): JsonElement {
            return JsonPrimitive(storedValue.name)
        }

        override fun validateAndCorrectInputs(input: T): ValidatedField<T> {
            return ValidatedEnum(input,enum)
        }
    }

    open class ValidatedList<R,T:List<R>>(defaultValue: List<R>, private val lType: TypeToken<T>, private val listEntryValidator: Predicate<R>, private val invalidEntryMessage: String = ""): ValidatedField<List<R>>(defaultValue) {

        override fun deserializeHeldValue(json: JsonElement): T {
            return gson.fromJson(json,lType.type)
        }

        override fun serializeHeldValue(): JsonElement {
            return gson.toJsonTree(storedValue,lType.type)
        }

        override fun validateAndCorrectInputs(input: List<R>): ValidatedField<List<R>> {
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
                errorMessage = "Config list has errors, entries need follow these constrinats: $invalidEntryMessage. The following entries couldn't be validated: $errorList"
            }
            return ValidatedList(tempList,lType,listEntryValidator)
        }
    }
}
