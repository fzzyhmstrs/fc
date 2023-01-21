package me.fzzyhmstrs.fzzy_core.nbt_util

import me.fzzyhmstrs.fzzy_core.FC
import net.minecraft.item.ItemStack
import net.minecraft.nbt.NbtCompound
import net.minecraft.nbt.NbtList
import net.minecraft.util.math.BlockPos
import java.util.function.Predicate

/**
 * simple functions that arrange Nbt functions in a way I prefer and add some functionalities vanilla nbt doens't make as easy
 */
object Nbt {

    /**
     * stores blockpos on the object as a long, and then back converts it. This method is used as opposed to an X/Y/Z three tag implementation for brevity.
     */
    fun writeBlockPos(key: String, pos: BlockPos, nbt: NbtCompound){
        nbt.putLong(key,pos.asLong())
    }
    fun readBlockPos(key: String, nbt: NbtCompound): BlockPos{
        return if (nbt.contains(key)){
            BlockPos.fromLong(nbt.getLong(key))
        } else {
            BlockPos.ORIGIN
        }
    }

    /**
     * Returns a [NbtList] if the compound contains it, or makes a new list and passes it back. Failure can be checked with [NbtList.isEmpty]
     */
    fun readNbtList(nbt: NbtCompound, key: String): NbtList {
        return if (nbt.contains(key)){
            nbt.getList(key,10)
        } else {
            NbtList()
        }
    }

    /**
     * adds a Nbtcompound into an [NbtList] at the specified key. Will create an empty list and add the [newNbt] as the first entry if needed.
     */
    fun addNbtToList(newNbt: NbtCompound, listKey: String, baseNbt: NbtCompound){
        val nbtList = readNbtList(baseNbt, listKey)
        nbtList.add(newNbt)
        baseNbt.put(listKey,nbtList)
    }

    /**
     * searches through an [NbtList] and removes elements specified by the [removalTest]. This test usually involves checking through the NbtCompounds in the list for a key of interest.
     */
    fun removeNbtFromList(listKey: String, baseNbt: NbtCompound, removalTest: Predicate<NbtCompound>){
        val nbtList = readNbtList(baseNbt, listKey)
        val nbtList2 = NbtList()
        for (el in nbtList){
            val nbtEl = el as NbtCompound
            if (removalTest.test(nbtEl)){
                continue
            }
            nbtList2.add(el)
        }
        baseNbt.put(listKey, nbtList2)
    }

    /**
     * utility for providing an [ItemStack] with a unique and relatively immutable identifier. This allows for an ItemStack to be uniquely tracked even across crafting, enchanting, etc. etc.
     */
    fun makeItemStackId(stack: ItemStack): Long{
        val nbt = stack.orCreateNbt
        return if (!nbt.contains(NbtKeys.ITEM_STACK_ID.str())){
            val long = (FC.fcRandom.nextDouble() * Long.MAX_VALUE).toLong()
            nbt.putLong(NbtKeys.ITEM_STACK_ID.str(),long)
            long
        } else {
            getItemStackId(nbt)
        }
    }
    /**
     * utility for providing an [ItemStack] with a unique and relatively immutable identifier. This allows for an ItemStack to be uniquely tracked even across crafting, enchanting, etc. etc.
     */
    fun getItemStackId(stack: ItemStack): Long{
        val nbt = stack.orCreateNbt
        return if (nbt.contains(NbtKeys.ITEM_STACK_ID.str())){
            nbt.getLong(NbtKeys.ITEM_STACK_ID.str())
        } else {
            -1L
        }
    }
    /**
     * utility for providing an [ItemStack] with a unique and relatively immutable identifier. This allows for an ItemStack to be uniquely tracked even across crafting, enchanting, etc. etc.
     */
    fun getItemStackId(nbt: NbtCompound): Long{
        return if (nbt.contains(NbtKeys.ITEM_STACK_ID.str())){
            nbt.getLong(NbtKeys.ITEM_STACK_ID.str())
        } else {
            -1L
        }
    }

    /**
     * method for transferring nbt between two item stacks.
     *
     * does NOT transfer enchantments. Minecraft has methods for that.
     *
     * useful for maintaining custom nbt between stacks. For example, when crafting an item into a new tier of that item, nbt can be maintained with this function.
     */
    fun transferNbt(stack1: ItemStack, stack2: ItemStack){
        val nbt1 = stack1.nbt ?: return
        val nbt2 = stack2.orCreateNbt
        for(nbtKey in nbt1.keys){
            if(nbtKey == ItemStack.ENCHANTMENTS_KEY){
                continue
            }
            nbt2.put(nbtKey,nbt1[nbtKey])
        }
    }

    fun getOrCreateSubCompound(nbtCompound: NbtCompound, key: String): NbtCompound {
        val subCompound = nbtCompound.get(key) ?: NbtCompound()
        if (subCompound == NbtCompound()){
            nbtCompound.put(key,subCompound)
        }
        return subCompound as NbtCompound
    }
}
