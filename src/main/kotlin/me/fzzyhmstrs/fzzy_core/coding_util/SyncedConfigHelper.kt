package me.fzzyhmstrs.fzzy_core.coding_util

import com.google.gson.Gson
import com.google.gson.GsonBuilder
import me.fzzyhmstrs.fzzy_core.FC
import me.fzzyhmstrs.fzzy_core.coding_util.SyncedConfigHelper.OldClass
import me.fzzyhmstrs.fzzy_core.coding_util.SyncedConfigHelper.SyncedConfig
import me.fzzyhmstrs.fzzy_core.coding_util.SyncedConfigHelper.readOrCreate
import me.fzzyhmstrs.fzzy_core.coding_util.SyncedConfigHelper.readOrCreateUpdated
import net.fabricmc.loader.api.FabricLoader
import net.minecraft.network.PacketByteBuf
import java.io.File
import java.io.FileWriter

/**
 * Helper object for building a server-client synced configuration system. Credits for the basis of this implementation go to Gabriel Henrique de Oliveira (GabrielOlvH)
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

@Deprecated("Scheduled for removal; the new config_util validated config system is replacing it")
object SyncedConfigHelper {

    val gson: Gson = GsonBuilder().setPrettyPrinting().create()

    /**
     * basic method for creating and passing the config settings for a particular configuration class to/from json.
     *
     * Deprecated as of version X.X.X due to implementation of validated configurations.
     */
     @Deprecated("This method does not correct invalid inputs and cannot properly read ValidatedFields, use readOrCreateAndValidate` to perform automatic input correction")
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
    @Deprecated("This method does not correct invalid inputs and cannot properly read ValidatedFields, use readOrCreateAndValidate` to perform automatic input correction")
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


    
    /////////////////////////////////////////////
    // basic helper methods and interfaces  /////
    /////////////////////////////////////////////
    
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

}
