package me.fzzyhmstrs.fzzy_core.modifier_util

import com.google.common.collect.ArrayListMultimap
import net.minecraft.nbt.NbtCompound
import net.minecraft.nbt.NbtElement
import net.minecraft.nbt.NbtList
import net.minecraft.nbt.NbtString
import net.minecraft.util.Identifier

class ModifierContainer(innateModifiers: ArrayListMultimap<ModifierHelperType<*>, Identifier> = ArrayListMultimap.create()) {

    private val compiledMap: MutableMap<ModifierHelperType<*>, AbstractModifier.CompiledModifiers<*>> = mutableMapOf()
    private val normalMultimap: ArrayListMultimap<ModifierHelperType<*>, Identifier> = ArrayListMultimap.create()
    private val innateMultimap: ArrayListMultimap<ModifierHelperType<*>, Identifier> = innateModifiers
    private val dirtyTypes: MutableSet<ModifierHelperType<*>> = mutableSetOf()

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
        if (entryType == EntryType.NORMAL)
            normalMultimap.put(type,modifier.modifierId)
        else
            innateMultimap.put(type, modifier.modifierId)
        dirtyTypes.add(type)
    }
    fun<T: AbstractModifier<T>> removeModifier(modifier: AbstractModifier<T>, type: ModifierHelperType<T>, entryType: EntryType = EntryType.NORMAL){
        if (entryType == EntryType.NORMAL)
            normalMultimap.remove(type,modifier.modifierId)
        else
            innateMultimap.remove(type, modifier.modifierId)
        dirtyTypes.add(type)
    }

    fun<T: AbstractModifier<T>> getCompiledModifiers(type: ModifierHelperType<T>): AbstractModifier.CompiledModifiers<T>?{
        return if (dirtyTypes.contains(type)){
            dirtyTypes.remove(type)
            val list: MutableList<Identifier> = mutableListOf()
            list.addAll(normalMultimap.get(type) ?: listOf())
            list.addAll(innateMultimap.get(type) ?: listOf())
            compiledMap.put(type, type.compile(list)) as AbstractModifier.CompiledModifiers<T>
        } else {
            compiledMap[type] as? AbstractModifier.CompiledModifiers<T>
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
        fun load(nbtCompound: NbtCompound): ModifierContainer{
            val innateModifiers: ArrayListMultimap<ModifierHelperType<*>, Identifier> = ArrayListMultimap.create()
            for (key in nbtCompound.keys){
                val type = ModifierHelperType.REGISTRY.get(Identifier(key)) ?: continue
                val list = nbtCompound.getList(key,NbtElement.STRING_TYPE.toInt())
                for (modIdElement in list){
                    val mod = Identifier(modIdElement.asString())
                    innateModifiers.put(type, mod)
                }
            }
            return ModifierContainer(innateModifiers)
        }
    }

    enum class EntryType{
        INNATE,
        NORMAL,
        ALL
    }
}