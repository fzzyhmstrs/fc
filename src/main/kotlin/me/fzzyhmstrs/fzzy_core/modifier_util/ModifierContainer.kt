package me.fzzyhmstrs.fzzy_core.modifier_util

import com.google.common.collect.ArrayListMultimap
import net.minecraft.entity.LivingEntity
import net.minecraft.entity.player.PlayerEntity
import net.minecraft.nbt.NbtCompound
import net.minecraft.nbt.NbtElement
import net.minecraft.nbt.NbtList
import net.minecraft.nbt.NbtString
import net.minecraft.util.Identifier

class ModifierContainer(val livingEntity: LivingEntity, innateModifiers: ArrayListMultimap<ModifierHelperType<*>, Identifier> = ArrayListMultimap.create()) {

    private val compiledMap: MutableMap<ModifierHelperType<*>, AbstractModifier.CompiledModifiers<*>> = mutableMapOf()
    private val predicateInstanceMap: MutableMap<ModifierHelperType<*>, MutableMap<Identifier,AbstractModifier.CompiledModifiers<*>>> = mutableMapOf()
    private val normalMultimap: ArrayListMultimap<ModifierHelperType<*>, Identifier> = ArrayListMultimap.create()
    private val innateMultimap: ArrayListMultimap<ModifierHelperType<*>, Identifier> = innateModifiers
    private val dirtyTypes: MutableSet<ModifierHelperType<*>> = mutableSetOf()

    fun<T: AbstractModifier<T>> compileSpecialModifier(predicateId: Identifier, type: ModifierHelperType<T>): AbstractModifier.CompiledModifiers<T>? {
        return predicateInstanceMap
            .computeIfAbsent(type) { mutableMapOf()}
                .computeIfAbsent(predicateId) {compileSpecial(predicateId, type)} as? AbstractModifier.CompiledModifiers<T>
    }

    private fun <T: AbstractModifier<T>> compileSpecial(predicateId: Identifier, type: ModifierHelperType<T>): AbstractModifier.CompiledModifiers<T>{
        val list: MutableList<Identifier> = mutableListOf()
        list.addAll(normalMultimap.get(type) ?: listOf())
        list.addAll(innateMultimap.get(type) ?: listOf())
        return type.compile(list,predicateId).also { println("Compiling special: $it for $livingEntity") }
    }

    fun getModifiers(type: ModifierHelperType<*>, entryType: EntryType): List<Identifier>{
        return when(entryType){
            EntryType.NORMAL -> normalMultimap.get(type)
            EntryType.INNATE -> innateMultimap.get(type)
            EntryType.ALL -> {
                val list = mutableListOf<Identifier>()
                list.addAll(normalMultimap.get(type))
                list.addAll(innateMultimap.get(type))
                list
            }
        }
    }

    fun<T: AbstractModifier<T>> addModifier(modifier: AbstractModifier<T>, type: ModifierHelperType<T>, entryType: EntryType = EntryType.NORMAL){
        println("Adding modifier $modifier to container for $livingEntity")
        if (entryType == EntryType.NORMAL)
            normalMultimap.put(type,modifier.modifierId)
        else
            innateMultimap.put(type, modifier.modifierId)
        dirtyTypes.add(type)
        predicateInstanceMap.remove(type) //reset special instances here, as the baseline compiled instance has changed
    }
    fun<T: AbstractModifier<T>> removeModifier(modifier: AbstractModifier<T>, type: ModifierHelperType<T>, entryType: EntryType = EntryType.NORMAL){
        if (entryType == EntryType.NORMAL)
            normalMultimap.remove(type,modifier.modifierId)
        else
            innateMultimap.remove(type, modifier.modifierId)
        dirtyTypes.add(type)
        predicateInstanceMap.remove(type) //reset special instances here, as the baseline compiled instance has changed
    }

    fun<T: AbstractModifier<T>> getCompiledModifiers(type: ModifierHelperType<T>): AbstractModifier.CompiledModifiers<T>?{
        return if (dirtyTypes.contains(type)){
            dirtyTypes.remove(type)
            val list: MutableList<Identifier> = mutableListOf()
            list.addAll(normalMultimap.get(type) ?: listOf())
            list.addAll(innateMultimap.get(type) ?: listOf())
            compiledMap.put(type, type.compile(list, null)).also { println("Getting compiled modifier $it for $livingEntity") } as AbstractModifier.CompiledModifiers<T>
        } else {
            compiledMap[type]/*.also { println("Getting pre-compiled modifier $it for $livingEntity") }*/ as? AbstractModifier.CompiledModifiers<T>
        }

    }

    fun save(nbtCompound: NbtCompound){
        for (key in innateMultimap.keys()){
            val keyId = key.id.toString()
            val newList = NbtList()
            for (mod in innateMultimap.get(key)){
                newList.add(NbtString.of(mod.toString()))
            }
            nbtCompound.put(keyId,newList)
        }
    }

    companion object{
        @JvmStatic
        fun load(livingEntity: LivingEntity, nbtCompound: NbtCompound): ModifierContainer{
            val innateModifiers: ArrayListMultimap<ModifierHelperType<*>, Identifier> = ArrayListMultimap.create()
            for (key in nbtCompound.keys){
                val type = ModifierHelperType.REGISTRY.get(Identifier(key)) ?: continue
                val list = nbtCompound.getList(key,NbtElement.STRING_TYPE.toInt())
                for (modIdElement in list){
                    val mod = Identifier(modIdElement.asString())
                    innateModifiers.put(type, mod)
                }
            }
            return ModifierContainer(livingEntity,innateModifiers)
        }
    }

    enum class EntryType{
        INNATE,
        NORMAL,
        ALL
    }
}