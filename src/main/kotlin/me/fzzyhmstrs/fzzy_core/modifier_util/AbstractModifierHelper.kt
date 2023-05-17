package me.fzzyhmstrs.fzzy_core.modifier_util

import kotlinx.coroutines.sync.Mutex
import me.fzzyhmstrs.fzzy_core.FC
import me.fzzyhmstrs.fzzy_core.coding_util.AcText
import me.fzzyhmstrs.fzzy_core.nbt_util.Nbt
import me.fzzyhmstrs.fzzy_core.nbt_util.NbtKeys
import me.fzzyhmstrs.fzzy_core.registry.ModifierRegistry
import net.minecraft.client.item.TooltipContext
import net.minecraft.item.ItemStack
import net.minecraft.nbt.NbtCompound
import net.minecraft.text.Text
import net.minecraft.util.Identifier
import java.util.*
import java.util.concurrent.atomic.AtomicReference
import kotlin.math.max

abstract class AbstractModifierHelper<T: AbstractModifier<T>> : ModifierInitializer{

    private val modifiers = Collections.synchronizedMap(mutableMapOf<Long ,MutableList<Identifier>>())
    private val activeModifiers = Collections.synchronizedMap(mutableMapOf<Long, AbstractModifier.CompiledModifiers<T>>())
    abstract val fallbackData: AbstractModifier.CompiledModifiers<T>

    abstract fun gatherActiveModifiers(stack: ItemStack)

    abstract fun getTranslationKeyFromIdentifier(id: Identifier): String
    
    abstract fun getDescTranslationKeyFromIdentifier(id: Identifier): String

    abstract fun getType(): ModifierHelperType

    override fun addModifierTooltip(stack: ItemStack, tooltip: MutableList<Text>, context: TooltipContext){
        val nbt = stack.nbt ?: return
        if (!nbt.contains(getType().getModifiersKey())) return
        val ids = getModifiersFromNbt(stack)
        for (id in ids){
            val mod = ModifierRegistry.get(id)?:continue
            tooltip.add(mod.getTranslation().append(AcText.literal(" - ")).append(mod.getDescTranslation()))
        }
    }

    //synchronization for the modifier lists
    fun getModifiersById(itemStackId: Long): List<Identifier>{
        return synchronized(modifiers){
            //secondary safety valve, copying the modifier list on creation. This makes back-mutation outside of sync impossible
            modifiers[itemStackId]?.toMutableList()?: listOf()
        }
    }
    fun checkModifiersKeyById(itemStackId: Long): Boolean{
        return synchronized(modifiers){
            modifiers.containsKey(itemStackId)
        }
    }
    fun checkModListContainsById(itemStackId: Long, mod: Identifier): Boolean{
        return synchronized(modifiers){
            modifiers[itemStackId]?.contains(mod) == true
        }
    }
    fun setModifiersById(itemStackId: Long,mods: MutableList<Identifier>){
        synchronized(modifiers){
            modifiers[itemStackId] = mods
        }
    }
    fun addModifierById(itemStackId: Long, mod: Identifier){
        synchronized(modifiers){
            modifiers[itemStackId]?.add(mod)
        }
    }
    fun removeModifierById(itemStackId: Long, mod: Identifier){
        synchronized(modifiers){
            modifiers[itemStackId]?.remove(mod)
        }
    }

    //synchronization for active modifiers
    fun setModifiersById(itemStackId: Long, compiledData: AbstractModifier.CompiledModifiers<T>){
        synchronized(activeModifiers) {
            activeModifiers[itemStackId] = compiledData
        }
    }
    fun getActiveModifiers(stack: ItemStack): AbstractModifier.CompiledModifiers<T> {
        val id = Nbt.getItemStackId(stack)
        /*if (id != -1L && !activeModifiers.containsKey(id)){
            initializeModifiers(stack, stack.orCreateNbt)
        }*/
        return synchronized(activeModifiers) {
            val compiledData = activeModifiers[id]
            compiledData ?: fallbackData
        }
    }

    fun addModifier(modifier: Identifier, stack: ItemStack): Boolean{
        val nbt = stack.orCreateNbt
        return addModifier(modifier, stack, nbt)
    }

    protected fun addModifier(modifier: Identifier, stack: ItemStack, nbt: NbtCompound): Boolean{
        val id = Nbt.makeItemStackId(stack)
        if (!checkModifiersKeyById(id)) {
            initializeModifiers(nbt, id)
        }
        val highestModifier = checkDescendant(modifier,stack)
        val mod = getModifierByType(modifier)
        if (highestModifier != null){
            return if (mod?.hasDescendant() == true){
                val highestDescendantPresent: Int = checkModifierLineage(mod, stack)
                if (highestDescendantPresent < 0){
                    false
                } else {
                    val lineage = mod.getModLineage()
                    val newDescendant = lineage[highestDescendantPresent]
                    val currentGeneration = lineage[max(highestDescendantPresent - 1,0)]
                    getModifierByType(newDescendant)?.onAdd(stack)
                    addModifierById(id,newDescendant)
                    addModifierToNbt(newDescendant, nbt)
                    removeModifier(stack, currentGeneration, nbt)
                    gatherActiveModifiers(stack)
                    true
                }
            } else {
                false
            }
        }

        mod?.onAdd(stack)
        addModifierToNbt(modifier, nbt)
        addModifierById(id,modifier)
        gatherActiveModifiers(stack)
        return true
    }

    protected fun checkDescendant(modifier: Identifier, stack: ItemStack): Identifier?{
        val id = Nbt.getItemStackId(stack)
        if (id == -1L) return null
        val mod = getModifierByType(modifier)
        val lineage = mod?.getModLineage() ?: return null
        var highestModifier: Identifier? = null
        lineage.forEach { identifier ->
            if (checkModListContainsById(id,identifier)){
                highestModifier = identifier
            }
        }
        return highestModifier
    }

    protected fun removeModifier(stack: ItemStack, modifier: Identifier, nbt: NbtCompound){
        val id = Nbt.getItemStackId(nbt)
        getModifierByType(modifier)?.onRemove(stack)
        removeModifierById(id,modifier)
        gatherActiveModifiers(stack)
        removeModifierFromNbt(modifier,nbt)
    }

    fun addModifierToNbt(modifier: Identifier, stack: ItemStack){
        val nbt = stack.orCreateNbt
        addModifierToNbt(modifier, nbt)
    }

    fun addModifierToNbt(modifier: Identifier, nbt: NbtCompound){
        val newEl = NbtCompound()
        newEl.putString(getType().getModifierIdKey(),modifier.toString())
        Nbt.addNbtToList(newEl, getType().getModifiersKey(),nbt)
    }

    protected fun removeModifierFromNbt(modifier: Identifier, nbt: NbtCompound){
        Nbt.removeNbtFromList(getType().getModifiersKey(),nbt) { nbtEl: NbtCompound ->
            if (nbtEl.contains(getType().getModifierIdKey())){
                val chk = Identifier(nbtEl.getString(getType().getModifierIdKey()))
                chk == modifier
            } else {
                false
            }
        }
    }

    override fun initializeModifiers(stack: ItemStack, nbt: NbtCompound, list: List<Identifier>){
        if (list.isNotEmpty()){
            if (!nbt.contains(NbtKeys.MOD_INIT.str() + stack.translationKey)){
                if (nbt.contains(getType().getModifiersKey())){
                    list.forEach{
                        addModifier(it,stack,nbt)
                    }
                } else{
                    list.forEach{
                        addModifierToNbt(it,nbt)
                    }
                }
                nbt.putBoolean(NbtKeys.MOD_INIT.str() + stack.translationKey,true)
            }
        }
        if (nbt.contains(getType().getModifiersKey())){
            val id = Nbt.makeItemStackId(stack)
            initializeModifiers(nbt, id)
            gatherActiveModifiers(stack)
        }
    }

    protected fun initializeModifiers(nbt: NbtCompound, id: Long){
        val nbtList = nbt.getList(getType().getModifiersKey(),10)
        setModifiersById(id, mutableListOf())
        for (el in nbtList){
            val compound = el as NbtCompound
            if (compound.contains(getType().getModifierIdKey())){
                val modifier = compound.getString(getType().getModifierIdKey())
                addModifierById(id,Identifier(modifier))
            }
        }
    }

    fun getModifiers(stack: ItemStack): List<Identifier>{
        val nbt = stack.orCreateNbt
        val id = Nbt.makeItemStackId(stack)
        if (!checkModifiersKeyById(id)) {
            if (nbt.contains(getType().getModifiersKey())) {
                initializeModifiers(nbt, id)
            }
        }
        return synchronized(modifiers){
            modifiers[id] ?: listOf()
        }
    }

    fun getModifiersFromNbt(stack: ItemStack): List<Identifier>{
        val list: MutableList<Identifier> = mutableListOf()
        val nbt = stack.nbt?:return list
        if (nbt.contains(getType().getModifiersKey())){
            val nbtList = Nbt.readNbtList(nbt, getType().getModifiersKey())
            nbtList.forEach {
                val nbtCompound = it as NbtCompound
                if (nbtCompound.contains(getType().getModifierIdKey())){
                    list.add(Identifier(nbtCompound.getString(getType().getModifierIdKey())))
                }
            }
        }
        return list
    }

    fun checkModifierLineage(modifier: Identifier, stack: ItemStack): Boolean{
        val mod = getModifierByType(modifier)
        return if (mod != null){
            checkModifierLineage(mod, stack) >= 0
        } else {
            false
        }
    }

    protected fun checkModifierLineage(mod: T, stack: ItemStack): Int{
        val id = Nbt.getItemStackId(stack)
        val lineage = mod.getModLineage()
        val highestOrderDescendant = lineage.size
        var highestDescendantPresent = 0
        lineage.forEachIndexed { index, identifier ->
            if (checkModListContainsById(id,identifier)){
                highestDescendantPresent = index + 1
            }
        }
        return if(highestDescendantPresent < highestOrderDescendant){
            highestDescendantPresent
        } else {
            -1
        }
    }

    fun getNextInLineage(modifier: Identifier, stack: ItemStack): Identifier{
        val mod = getModifierByType(modifier)
        return if (mod != null){
            val lineage = mod.getModLineage()
            val nextInLineIndex = checkModifierLineage(mod, stack)
            if (nextInLineIndex == -1){
                modifier
            } else {
                lineage[nextInLineIndex]
            }
        } else {
            modifier
        }
    }

    fun getMaxInLineage(modifier: Identifier): Identifier{
        val mod = getModifierByType(modifier)
        return mod?.getModLineage()?.last() ?: return modifier
    }

    abstract fun getModifierByType(id: Identifier): T?

    inline fun <reified A : AbstractModifier<A>> gatherActiveAbstractModifiers(
        stack: ItemStack,
        objectToAffect: Identifier,
        compiler: AbstractModifier<A>.Compiler
    ): AbstractModifier.CompiledModifiers<A> {
        val id = Nbt.getItemStackId(stack)
        try {
            getModifiersById(id).forEach { identifier ->
                val modifier = ModifierRegistry.getByType<A>(identifier)
                if (modifier != null) {
                    if (!modifier.hasObjectToAffect()) {
                        compiler.add(modifier)
                    } else {
                        if (modifier.checkObjectsToAffect(objectToAffect)) {
                            compiler.add(modifier)
                        }
                    }
                }
            }
        } catch (e: Exception){
            e.printStackTrace()
        }
        return compiler.compile()
    }

    companion object{

        val BLANK = Identifier(FC.MOD_ID,"blank")
        val EMPTY = EmptyModifier()

        fun getEmptyHelper(): EmptyModifierHelper{
            return EmptyModifierHelper
        }

        object EmptyModifierHelper: AbstractModifierHelper<EmptyModifier>() {
            override val fallbackData: AbstractModifier.CompiledModifiers<EmptyModifier> = AbstractModifier.CompiledModifiers(arrayListOf(),EMPTY)

            override fun gatherActiveModifiers(stack: ItemStack) {
            }

            override fun getTranslationKeyFromIdentifier(id: Identifier): String {
                return ""
            }

            override fun getDescTranslationKeyFromIdentifier(id: Identifier): String {
                return ""
            }

            override fun getModifierByType(id: Identifier): EmptyModifier? {
                return null
            }

            override fun getType(): ModifierHelperType {
                return ModifierHelperType.EMPTY_TYPE
            }

        }

        class EmptyModifier: AbstractModifier<EmptyModifier>(BLANK){
            override fun plus(other: EmptyModifier): EmptyModifier {
                return this
            }

            override fun compiler(): Compiler {
                return Compiler(arrayListOf(), EmptyModifier())
            }

            override fun getModifierHelper(): AbstractModifierHelper<*> {
                return EmptyModifierHelper
            }

        }
    }
}
