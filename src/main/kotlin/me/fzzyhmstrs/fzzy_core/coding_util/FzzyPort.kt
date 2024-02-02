package me.fzzyhmstrs.fzzy_core.coding_util

import com.google.common.collect.ImmutableSet
import com.mojang.blaze3d.systems.RenderSystem
import com.mojang.datafixers.util.Pair
import me.fzzyhmstrs.fzzy_core.coding_util.compat.FzzyDefaultedRegistry
import me.fzzyhmstrs.fzzy_core.coding_util.compat.FzzyRegistry
import net.fabricmc.fabric.api.`object`.builder.v1.world.poi.PointOfInterestHelper
import net.minecraft.block.Block
import net.minecraft.client.gui.widget.ButtonWidget
import net.minecraft.client.gui.widget.ButtonWidget.PressAction
import net.minecraft.client.render.GameRenderer
import net.minecraft.inventory.Inventory
import net.minecraft.item.Item
import net.minecraft.recipe.Recipe
import net.minecraft.recipe.RecipeManager
import net.minecraft.recipe.RecipeType
import net.minecraft.screen.ScreenHandler
import net.minecraft.screen.ScreenHandlerType
import net.minecraft.screen.ScreenHandlerType.Factory
import net.minecraft.sound.SoundEvent
import net.minecraft.text.Text
import net.minecraft.util.Identifier
import net.minecraft.util.registry.Registry
import net.minecraft.util.registry.RegistryEntry
import net.minecraft.village.VillagerProfession
import net.minecraft.world.World
import net.minecraft.world.poi.PointOfInterestType
import java.util.*
import java.util.function.Predicate


@Suppress("unused", "MemberVisibilityCanBePrivate")
object FzzyPort {

    val ATTRIBUTE = FzzyRegistry(Registry.ATTRIBUTE)
    val BLOCK = FzzyDefaultedRegistry( Registry.BLOCK)
    val BLOCK_ENTITY_TYPE = FzzyRegistry( Registry.BLOCK_ENTITY_TYPE)
    val ENCHANTMENT = FzzyRegistry( Registry.ENCHANTMENT)
    val ENTITY_TYPE = FzzyDefaultedRegistry( Registry.ENTITY_TYPE)
    val ITEM = FzzyDefaultedRegistry( Registry.ITEM)
    val PARTICLE_TYPE = FzzyRegistry( Registry.PARTICLE_TYPE)
    val POTION = FzzyDefaultedRegistry( Registry.POTION)
    val SCREEN_HANDLER = FzzyRegistry( Registry.SCREEN_HANDLER)
    val SOUND_EVENT = FzzyRegistry( Registry.SOUND_EVENT)
    val STATUS_EFFECT = FzzyRegistry( Registry.STATUS_EFFECT)
    val VILLAGER_PROFESSION = FzzyDefaultedRegistry( Registry.VILLAGER_PROFESSION)

    fun createAndRegisterVillagerProfession(id: Identifier, workSound: SoundEvent, vararg poi: Block): VillagerProfession{
        return createAndRegisterVillagerProfession(id, workSound, setOf(), setOf(), *poi)
    }

    fun createAndRegisterVillagerProfession(id: Identifier, workSound: SoundEvent, harvestableItems: Set<Item>, secondaryJobSites: Set<Block>, vararg poi: Block): VillagerProfession{
        val pointOfInterest = PointOfInterestHelper.register(id,1,1,*poi)
        val poiKey =  Registry.POINT_OF_INTEREST_TYPE.getKey(pointOfInterest).get()
        val workstation = Predicate<RegistryEntry<PointOfInterestType>>{ entry -> entry.matchesKey(poiKey)}
        val jobSite = Predicate<RegistryEntry<PointOfInterestType>>{ entry -> entry.matchesKey(poiKey)}
        return VILLAGER_PROFESSION.register(id,VillagerProfession(id.toString(),workstation,jobSite, ImmutableSet.copyOf(harvestableItems), ImmutableSet.copyOf(secondaryJobSites),workSound))
    }

    fun clientBlockSpriteRegister(@Suppress("UNUSED_PARAMETER") id: Identifier){
        //do absolutely nothing lol
    }

    fun setPositionTexShader() {
        RenderSystem.setShader { GameRenderer.getPositionTexShader() }
    }

    fun setPositionColorTexShader() {
        RenderSystem.setShader { GameRenderer.getPositionColorTexShader() }
    }

    fun newButton(x: Int, y: Int, w: Int, h: Int, name: Text, action: PressAction): ButtonWidget {
        return ButtonWidget(x, y,w, h,name, action)
    }

    fun getRecipe(id: Identifier, manager: RecipeManager): Optional<out Recipe<*>>{
        return manager.get(id)
    }

    fun <C: Inventory, R: Recipe<C>> getFirstMatch(type: RecipeType<R>, inventory: C, world: World): Optional<R>{
        return world.recipeManager.getFirstMatch(type,inventory, world)
    }

    fun <C: Inventory, R: Recipe<C>> getFirstMatch(type: RecipeType<R>, inventory: C, world: World, id: Identifier?): Optional<Pair<Identifier,R>>{
        return world.recipeManager.getFirstMatch(type,inventory, world, id)
    }

    fun <C: Inventory, R: Recipe<C>> getAllMatches(type: RecipeType<R>, inventory: C, world: World): List<R>{
        return world.recipeManager.getAllMatches(type,inventory, world)
    }

    fun <C: Inventory, R: Recipe<C>> listAllOfType(type: RecipeType<R>, recipeManager: RecipeManager): List<R>{
        return recipeManager.listAllOfType(type)
    }

    fun <T: ScreenHandler> buildHandlerType(factory: Factory<T>): ScreenHandlerType<T>{
        return ScreenHandlerType(factory)
    }
}