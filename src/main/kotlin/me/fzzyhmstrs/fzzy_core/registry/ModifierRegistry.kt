@file:Suppress("unused")

package me.fzzyhmstrs.fzzy_core.registry

import com.google.common.collect.ArrayListMultimap
import com.google.gson.Gson
import com.google.gson.JsonObject
import com.google.gson.JsonParser
import com.google.gson.stream.JsonReader
import me.fzzyhmstrs.fzzy_core.FC
import me.fzzyhmstrs.fzzy_core.coding_util.FzzyPort
import me.fzzyhmstrs.fzzy_core.interfaces.Modifiable
import me.fzzyhmstrs.fzzy_core.modifier_util.AbstractModifier
import me.fzzyhmstrs.fzzy_core.modifier_util.AbstractModifierHelper
import me.fzzyhmstrs.fzzy_core.modifier_util.ModifierHelperType
import me.fzzyhmstrs.fzzy_core.modifier_util.ModifierHelperType.Companion.EmptyType.id
import net.fabricmc.api.EnvType
import net.fabricmc.api.Environment
import net.fabricmc.fabric.api.client.networking.v1.ClientPlayNetworking
import net.fabricmc.fabric.api.event.lifecycle.v1.ServerLifecycleEvents
import net.fabricmc.fabric.api.networking.v1.PacketByteBufs
import net.fabricmc.fabric.api.networking.v1.ServerPlayConnectionEvents
import net.fabricmc.fabric.api.networking.v1.ServerPlayNetworking
import net.fabricmc.fabric.api.resource.ResourceManagerHelper
import net.fabricmc.fabric.api.resource.SimpleSynchronousResourceReloadListener
import net.minecraft.item.Item
import net.minecraft.item.ItemStack
import net.minecraft.loot.function.LootFunction
import net.minecraft.loot.function.SetEnchantmentsLootFunction
import net.minecraft.loot.function.SetNbtLootFunction
import net.minecraft.nbt.NbtCompound
import net.minecraft.nbt.NbtList
import net.minecraft.network.PacketByteBuf
import net.minecraft.resource.ResourceManager
import net.minecraft.resource.ResourceType
import net.minecraft.util.Identifier

/**
 * registers Modifiers. Comes with a short list of default modifiers and debugging modifiers for use with Augment-style Scepters
 *
 * This registry accepts any modifier based on the [AbstractModifier] system, and provides methods for interacting with specific Modifier types.
 */

@Suppress("MemberVisibilityCanBePrivate")
object ModifierRegistry {
    private val registry: MutableMap<Identifier, AbstractModifier<*>> = mutableMapOf()
    private val loader = Loader()
    private var modifierDefaults = ModifierDefaults()
    private val gson = Gson()
    private val DEFAULTS_SYNC = Identifier(FC.MOD_ID,"defaults_sync")

    internal fun registerAll(){
        loader.registerServer()
    }

    @Environment(EnvType.CLIENT)
    internal fun registerClient(){
        loader.registerClient()
    }

    fun <T> getDefaultModifiers(obj: T, type: ModifierHelperType<*>): List<Identifier> where T: Item, T: Modifiable{
        return modifierDefaults.getModifiers(obj,type)
    }

    fun values(): Collection<AbstractModifier<*>> {
        return registry.values
    }
    /**
     * register a modifier with this.
     */
    fun register(modifier: AbstractModifier<*>){
        val id = modifier.modifierId
        if (registry.containsKey(id)){throw IllegalStateException("AbstractModifier with id $id already present in ModififerRegistry")}
        registry[id] = modifier
    }
    fun get(id: Identifier): AbstractModifier<*>?{
        return registry[id]
    }
    fun getByRawId(rawId: Int): AbstractModifier<*>?{
        return registry[getIdByRawId(rawId)]
    }
    fun getIdByRawId(rawId:Int): Identifier {
        return registry.keys.elementAtOrElse(rawId) { AbstractModifierHelper.BLANK }
    }
    fun getRawId(id: Identifier): Int{
        return registry.keys.indexOf(id)
    }
    fun isModifier(id: Identifier): Boolean{
        return this.get(id) != null
    }

    /**
     * get method that wraps in a type check, simplifying retrieval of only the relevant modifier type.
     */
    inline fun <reified T: AbstractModifier<T>> getByType(id: Identifier): T?{
        return get(id) as? T
    }

    /**
     * Alternative get-by-type that does reflective class checking.
     */
    fun <T: AbstractModifier<T>>getByType(id: Identifier, classType: Class<T>): T?{
        return try {
                classType.cast(get(id))
            } catch(e: ClassCastException){
                null
            }

    }

    inline fun <reified T: AbstractModifier<T>> getAllByType(): Set<T>{
        return values().mapNotNull { it as? T}.toSet()
    }

    /**
     * Alternative get-by-type that does reflective class checking.
     */
    fun <T: AbstractModifier<T>>getAllByType(classType: Class<T>): Set<T>{
        return values().mapNotNull { try{classType.cast(it)} catch (e: Exception) {null} }.toSet()
    }

    /**
     * [LootFunction.Builder] usable with loot pool building that will add default modifiers, a provided list of modifiers, or both.
     */
    fun<T: AbstractModifier<T>> modifiersLootFunctionBuilder(item: Item, modifiers: List<AbstractModifier<T>> = listOf(), type: ModifierHelperType<T>): LootFunction.Builder{
        val modList = NbtList()
        if (item is Modifiable) {
            if (getDefaultModifiers(item,type).isEmpty() && modifiers.isEmpty()){
                return SetEnchantmentsLootFunction.Builder() //empty builder for placehold purposes basically
            } else {
                getDefaultModifiers(item,type).forEach {
                    val nbtEl = NbtCompound()
                    nbtEl.putString(type.getModifierIdKey(),it.toString())
                    modList.add(nbtEl)
                }
                modifiers.forEach {
                    if (it.isAcceptableItem(ItemStack(item))) {
                        val nbtEl = NbtCompound()
                        nbtEl.putString(type.getModifierIdKey(), it.toString())
                        modList.add(nbtEl)
                    }
                }
            }
        } else if (modifiers.isEmpty()) {
            return SetEnchantmentsLootFunction.Builder()
        } else {
            modifiers.forEach {
                val nbtEl = NbtCompound()
                nbtEl.putString(type.getModifierIdKey(),it.toString())
                modList.add(nbtEl)
            }
        }
        val nbt = NbtCompound()
        nbt.put(type.getModifiersKey(), modList)
        @Suppress("DEPRECATION")
        return SetNbtLootFunction.builder(nbt)
    }

    private class Loader: SimpleSynchronousResourceReloadListener {

        override fun reload(manager: ResourceManager) {
            val defaults = ReloadableModifierDefaults()
            val jsons: MutableMap<Identifier,JsonObject> = mutableMapOf()
            manager.findResources("modifier_items") { path -> path.path.endsWith(".json") }
            .forEach {(id,resource) ->
                try {
                    val reader = resource.reader
                    val json = JsonParser.parseReader(reader).asJsonObject
                    jsons[id] = json
                } catch (e: Exception){
                    FC.LOGGER.warn("Error while loading item modifiers file $id!")
                    e.printStackTrace()
                }
            }
            loadFromJson(jsons, defaults)
            defaults.setJsonMap(jsons)
            modifierDefaults = defaults
        }

        override fun getFabricId(): Identifier {
            return Identifier(FC.MOD_ID,"default_modifier_loader")
        }

        fun registerServer(){
            ResourceManagerHelper.get(ResourceType.SERVER_DATA).registerReloadListener(this)
            ServerLifecycleEvents.END_DATA_PACK_RELOAD.register{server, _, _ ->
                if(!server.isDedicated) return@register
                for (player in server.playerManager.playerList) {
                    ServerPlayNetworking.send(player, DEFAULTS_SYNC, modifierDefaults.toBuf())
                }
            }
            ServerPlayConnectionEvents.JOIN.register {handler, _, server ->
                if(!server.isDedicated) return@register
                val player = handler.player
                ServerPlayNetworking.send(player, DEFAULTS_SYNC, modifierDefaults.toBuf())
            }
        }
        @Environment(EnvType.CLIENT)
        fun registerClient(){
            ClientPlayNetworking.registerGlobalReceiver(DEFAULTS_SYNC) {c,h,b,s ->
                val map: MutableMap<Identifier,JsonObject> = mutableMapOf()
                val size = b.readVarInt()
                for (i in 1..size){
                    val id = b.readIdentifier()
                    val jsonString = b.readString()
                    val json = try {
                        JsonParser.parseString(jsonString).asJsonObject
                    } catch (e: Exception){
                        println("Error while deserializing Json from Server!")
                        e.printStackTrace()
                        continue
                    }
                    map[id] = json
                }
                val defaults = ReloadableModifierDefaults()
                loadFromJson(map,defaults)
                modifierDefaults = defaults
            }
        }
    }

    private fun loadFromJson(jsons: Map<Identifier,JsonObject>, defaults: ReloadableModifierDefaults){
        val map: MutableMap<Item, ArrayListMultimap<ModifierHelperType<*>, Identifier>> = mutableMapOf()
        for ((id, json) in jsons) {
            try {
                for ((elName, el) in json.entrySet()) {
                    if (!el.isJsonObject) {
                        FC.LOGGER.warn("Error: element $elName not a valid JsonOblject, skipping!")
                    }
                    val itemId = Identifier.tryParse(elName)
                    if (itemId == null) {
                        FC.LOGGER.warn("Error: key $elName not a valid item identifier, skipping!")
                        continue
                    }
                    val item = FzzyPort.ITEM.get(itemId)
                    if (item !is Modifiable) {
                        FC.LOGGER.warn("Error: item $itemId isn't Modifiable, skipping!")
                        continue
                    }
                    val jsonTypes = el.asJsonObject
                    var replace = false
                    for ((typeName, typeEl) in jsonTypes.entrySet()) {
                        val typesMap: ArrayListMultimap<ModifierHelperType<*>, Identifier> = ArrayListMultimap.create()
                        if (typeName == "replace") {
                            if (!typeEl.isJsonPrimitive || !typeEl.asJsonPrimitive.isBoolean) {
                                FC.LOGGER.warn("Error: 'replace' key in modifier definition $typeName not a boolean, values are NOT being replaced per default!")
                            } else {
                                replace = typeEl.asBoolean
                            }
                            continue
                        }
                        if (!typeEl.isJsonArray && !typeEl.isJsonPrimitive) {
                            FC.LOGGER.warn("Error: modifier definition $typeName not an array or string, skipping!")
                            continue
                        }
                        val typeId = Identifier.tryParse(typeName)
                        if (typeId == null) {
                            FC.LOGGER.warn("Error: type key $typeName not a valid modifier type identifier, skipping!")
                            continue
                        }
                        val type = ModifierHelperType.REGISTRY.get(typeId)
                        if (type == null) {
                            FC.LOGGER.warn("Error: type key $typeName not a valid modifier type identifier, skipping!")
                            continue
                        }
                        if (!item.canBeModifiedBy(type)) {
                            FC.LOGGER.warn("Error: item $itemId can't be modified by ModifierType $typeId, skipping!")
                            continue
                        }
                        typesMap.putAll(type, item.defaultModifiers(type))
                        if (typeEl.isJsonArray) {
                            val modifierArray = typeEl.asJsonArray
                            for (modifierEl in modifierArray) {
                                if (!modifierEl.isJsonPrimitive) {
                                    FC.LOGGER.warn("Error: modifier element $modifierEl not a valid modifier id, skipping!")
                                    continue
                                }
                                val modifierId = Identifier.tryParse(modifierEl.asString)
                                if (modifierId == null) {
                                    FC.LOGGER.warn("Error: modifier element $modifierEl not a valid modifier id, skipping!")
                                    continue
                                }
                                val modifier = type.helper().getModifierByType(modifierId)
                                if (modifier == null) {
                                    FC.LOGGER.warn("Error: modifier $modifierId not found in the registry or not of type $typeId, skipping!")
                                    continue
                                }
                                typesMap.put(type, modifierId)
                            }
                        } else if (typeEl.isJsonPrimitive) {
                            val modifierId = Identifier.tryParse(typeEl.asString)
                            if (modifierId == null) {
                                FC.LOGGER.warn("Error: modifier element $typeEl not a valid modifier id, skipping!")
                                continue
                            }
                            val modifier = type.helper().getModifierByType(modifierId)
                            if (modifier == null) {
                                FC.LOGGER.warn("Error: modifier $modifierId not found in the registry or not of type $typeId, skipping!")
                                continue
                            }
                            typesMap.put(type, modifierId)
                        } else {
                            FC.LOGGER.warn("Unknown Error: something went wrong with the type key $typeId, skipping!")
                            continue
                        }
                        if (replace)
                            map[item] = typesMap
                        else
                            map.computeIfAbsent(item) { ArrayListMultimap.create() }.putAll(typesMap)
                    }
                }
            } catch (e: Exception){
                FC.LOGGER.warn("Error while loading item modifiers file $id!")
                e.printStackTrace()
            }
        }
        defaults.setMap(map)
    }

    private class ReloadableModifierDefaults: ModifierDefaults(){

        private var itemModifierMap: Map<Item, ArrayListMultimap<ModifierHelperType<*>, Identifier>> = mapOf()
        private var jsonMap: Map<Identifier,JsonObject> = mapOf()

        override fun <T> getModifiers(item: T, type: ModifierHelperType<*>): List<Identifier> where T: Item, T:Modifiable{
            return itemModifierMap[item]?.get(type) ?: item.defaultModifiers(type)
        }

        override fun toBuf(): PacketByteBuf {
            val buf = PacketByteBufs.create()
            buf.writeVarInt(jsonMap.size)
            for((id, json) in jsonMap){
                buf.writeIdentifier(id)
                buf.writeString(gson.toJson(json))
            }
            return buf
        }

        fun setMap(map: Map<Item, ArrayListMultimap<ModifierHelperType<*>, Identifier>>){
            itemModifierMap = map
        }

        fun setJsonMap(map: Map<Identifier,JsonObject>){
            jsonMap = map
        }
    }

    private open class ModifierDefaults{

        private val emptyList: List<Identifier> = listOf()
        open fun <T> getModifiers(item: T, type: ModifierHelperType<*>): List<Identifier> where T: Item, T:Modifiable{
            return item.defaultModifiers(type)
        }
        open fun toBuf(): PacketByteBuf{
            return PacketByteBufs.create().writeVarInt(0)
        }
    }
}