package me.fzzyhmstrs.fzzy_core.recipe

import com.google.common.collect.ImmutableMap
import com.google.gson.GsonBuilder
import com.google.gson.JsonElement
import com.mojang.logging.LogUtils
import me.fzzyhmstrs.fzzy_core.FC
import net.fabricmc.fabric.api.resource.IdentifiableResourceReloadListener
import net.minecraft.recipe.Recipe
import net.minecraft.recipe.RecipeManager
import net.minecraft.recipe.RecipeType
import net.minecraft.registry.Registries
import net.minecraft.resource.JsonDataLoader
import net.minecraft.resource.ResourceManager
import net.minecraft.util.Identifier
import net.minecraft.util.JsonHelper
import net.minecraft.util.profiler.Profiler

object OptionalRecipeLoader: JsonDataLoader(GsonBuilder().setPrettyPrinting().disableHtmlEscaping().create(),"optional_recipes"),IdentifiableResourceReloadListener {

    val optionalRecipes: MutableMap<Identifier,Recipe<*>> = mutableMapOf()
    private val LOGGER = LogUtils.getLogger()

    fun provideRecipeMap(original: Map<RecipeType<*>, Map<Identifier, Recipe<*>>>):Map<RecipeType<*>, Map<Identifier, Recipe<*>>>{
        val newMap: HashMap<RecipeType<*>, MutableMap<Identifier, Recipe<*>>> = hashMapOf()
        for (entry in original){
            newMap[entry.key] = entry.value.toMutableMap()
        }
        for (entry in optionalRecipes){
            newMap.computeIfAbsent(entry.value.type){type -> mutableMapOf()}[entry.key] = entry.value
        }
        return newMap.entries.stream().collect(ImmutableMap.toImmutableMap({entry: MutableMap.MutableEntry<RecipeType<*>, MutableMap<Identifier, Recipe<*>>> -> entry.key},{entry -> entry.value.toMap()}))
    }

    fun provideRecipesByIdMap(original: Map<Identifier, Recipe<*>>): Map<Identifier, Recipe<*>>{
        val newMap = original.toMutableMap()
        newMap.putAll(optionalRecipes)
        return newMap.toMap()
    }

    override fun prepare(resourceManager: ResourceManager?, profiler: Profiler?): MutableMap<Identifier, JsonElement> {
        val recipeJsons = super.prepare(resourceManager, profiler)
        optionalRecipes.clear()
        LOGGER.info("Loading optional recipes!")
        var loaded = 0
        var skipped = 0
        for (entry in recipeJsons){
            val id = entry.key
            try{
                val recipeJson = JsonHelper.asObject(entry.value,"top element")
                val typeString = JsonHelper.getString(recipeJson, "type")
                val typeStringId = Identifier(typeString)
                if(!Registries.RECIPE_SERIALIZER.containsId(typeStringId)) {
                    skipped++
                    continue
                }
                val recipe = RecipeManager.deserialize(id,recipeJson)
                loaded++
                optionalRecipes[id] = recipe
            } catch (e: Exception){
                skipped++
                LOGGER.error("Parsing error loading optional recipe {}",id,e)
            }
        }
        if (loaded > 0)
            LOGGER.info("Loaded $loaded optional recipes")
        if (skipped > 0)
            LOGGER.info("Skipped $skipped optional recipes")
        return recipeJsons
    }

    override fun apply(prepared: MutableMap<Identifier, JsonElement>, manager: ResourceManager, profiler: Profiler) {
    }

    override fun getFabricId(): Identifier {
        return Identifier(FC.MOD_ID,"optional_recipe_loader")
    }
}