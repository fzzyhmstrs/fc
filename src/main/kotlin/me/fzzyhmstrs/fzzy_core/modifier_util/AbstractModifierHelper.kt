package me.fzzyhmstrs.fzzy_core.modifier_util

import me.fzzyhmstrs.fzzy_core.FC
import me.fzzyhmstrs.fzzy_core.coding_util.AcText
import me.fzzyhmstrs.fzzy_core.coding_util.PerLvlI
import me.fzzyhmstrs.fzzy_core.coding_util.PersistentEffectHelper
import me.fzzyhmstrs.fzzy_core.interfaces.Modifiable
import me.fzzyhmstrs.fzzy_core.interfaces.ModifierHolding
import me.fzzyhmstrs.fzzy_core.nbt_util.Nbt
import me.fzzyhmstrs.fzzy_core.nbt_util.NbtKeys
import me.fzzyhmstrs.fzzy_core.registry.ModifierRegistry
import net.minecraft.client.item.TooltipContext
import net.minecraft.entity.LivingEntity
import net.minecraft.item.ItemStack
import net.minecraft.nbt.NbtCompound
import net.minecraft.nbt.NbtElement
import net.minecraft.text.Text
import net.minecraft.util.Identifier

abstract class AbstractModifierHelper<T: AbstractModifier<T>> : ModifierInitializer {

    //private val modifiers = Collections.synchronizedMap(mutableMapOf<Long ,MutableList<Identifier>>())
    //private val activeModifiers = Collections.synchronizedMap(mutableMapOf<Long, AbstractModifier.CompiledModifiers<T>>())
    abstract val fallbackData: AbstractModifier.CompiledModifiers<T>

    open fun compile(input: List<Identifier>?): AbstractModifier.CompiledModifiers<T>{
        val modList = input?.mapNotNull { getModifierByType(it) } ?: return fallbackData
        val compiler = compiler()
        for (mod in modList){
            compiler.add(mod)
        }
        return compiler.compile()
    }

    abstract fun compiler(): AbstractModifier<T>.Compiler

    abstract fun gatherActiveModifiers(stack: ItemStack)

    abstract fun getTranslationKeyFromIdentifier(id: Identifier): String
    
    abstract fun getDescTranslationKeyFromIdentifier(id: Identifier): String

    abstract fun getType(): ModifierHelperType<T>

    override fun addModifierTooltip(stack: ItemStack, tooltip: MutableList<Text>, context: TooltipContext){
        val nbt = stack.nbt ?: return
        if (!nbt.contains(getType().getModifiersKey())) return
        val ids = getModifiersFromNbt(stack)
        for (id in ids){
            val mod = ModifierRegistry.get(id)?:continue
            tooltip.add(mod.getTranslation().append(AcText.literal(" - ")).append(mod.getDescTranslation()))
        }
    }

    /*//synchronization for the modifier lists
    fun getModifiersById(itemStackId: Long): List<Identifier>{
        return synchronized(modifiers){
            //secondary safety valve, copying the modifier list on creation. This makes back-mutation outside of sync impossible
            modifiers[itemStackId]?.toMutableList()?: listOf()
        }
    }*/
    /*fun checkModifiersKeyById(itemStackId: Long): Boolean{
        return synchronized(modifiers){
            modifiers.containsKey(itemStackId)
        }
    }*/
    /*fun checkModListContainsById(itemStackId: Long, mod: Identifier): Boolean{
        return synchronized(modifiers){
            modifiers[itemStackId]?.contains(mod) == true
        }
    }*/
    /*fun setModifiersById(itemStackId: Long,mods: MutableList<Identifier>){
        synchronized(modifiers){
            modifiers[itemStackId] = mods
        }
    }*/
    /*fun addModifierById(itemStackId: Long, mod: Identifier){
        synchronized(modifiers){
            modifiers[itemStackId]?.add(mod)
        }
    }*/
    /*fun removeModifierById(itemStackId: Long, mod: Identifier){
        synchronized(modifiers){
            modifiers[itemStackId]?.remove(mod)
        }
    }*/

    //synchronization for active modifiers
    /*fun setModifiersById(itemStackId: Long, compiledData: AbstractModifier.CompiledModifiers<T>){
        synchronized(activeModifiers) {
            activeModifiers[itemStackId] = compiledData
        }
    }*/
    /*fun getActiveModifiers(stack: ItemStack): AbstractModifier.CompiledModifiers<T> {
        val id = Nbt.getItemStackId(stack)
        *//*if (id != -1L && !activeModifiers.containsKey(id)){
            initializeModifiers(stack, stack.orCreateNbt)
        }*//*
        return synchronized(activeModifiers) {
            val compiledData = activeModifiers[id]
            compiledData ?: fallbackData
        }
    }*/
    /*fun removeActiveModifiersById(id: Long){
        synchronized(activeModifiers) {
            activeModifiers.remove(id)
        }
    }*/
    /*fun hasActiveModifiers(stack: ItemStack): Boolean{
        val id = Nbt.getItemStackId(stack)
        return synchronized(activeModifiers) {
            activeModifiers.containsKey(id)
        }
    }*/

    fun getModifiers(entity: LivingEntity): List<Identifier>{
        val container = (entity as ModifierHolding).fzzy_core_getModifierContainer()
        return container.getModifiers(getType(),ModifierContainer.EntryType.ALL)
    }
    fun getActiveModifiers(entity: LivingEntity): AbstractModifier.CompiledModifiers<T> {
        val container = (entity as ModifierHolding).fzzy_core_getModifierContainer()
        return container.getCompiledModifiers(getType()) ?: fallbackData
    }


    ////////////////////////////////////////

    fun addModifier(entity: LivingEntity, modifier: Identifier, uniqueOnly: Boolean = true): Boolean {
        val mod = getModifierByType(modifier) ?: return false
        val container = (entity as ModifierHolding).fzzy_core_getModifierContainer()
        val mods = container.getModifiers(getType(),ModifierContainer.EntryType.INNATE)
        if (uniqueOnly && mods.contains(modifier)){
            val descendant = mod.getDescendant() ?: return false
            container.removeModifier(mod,getType(),ModifierContainer.EntryType.INNATE)
            container.addModifier(descendant, getType(), ModifierContainer.EntryType.INNATE)
            return true
        }
        container.addModifier(mod, getType(), ModifierContainer.EntryType.INNATE)
        return true
    }

    fun addTemporaryModifier(modifier: Identifier, entity: LivingEntity, duration: Int){
        if (addModifier(entity, modifier, false)){
            val data = TemporaryModifiers.TemporaryModifierData(this, modifier, entity)
            PersistentEffectHelper.setPersistentTickerNeed(TemporaryModifiers,duration,duration,data)
        }
    }

    fun addModifier(modifier: Identifier, stack: ItemStack): Boolean{
        return addModifier(modifier, stack, uniqueOnly = true)
    }

    fun addModifier(modifier: Identifier, stack: ItemStack, uniqueOnly: Boolean = true): Boolean{
        val nbt = stack.orCreateNbt
        return addModifier(modifier, stack, nbt,uniqueOnly)
    }

    protected fun addModifier(modifier: Identifier, stack: ItemStack, nbt: NbtCompound, uniqueOnly: Boolean = true): Boolean {
        //val id = Nbt.makeItemStackId(stack)
        val mods = if (!modifiersInitialized(stack, nbt)) {
            initializeModifiers(stack)
        } else {
            getModifiersFromNbt(nbt)
        }
        val mod = getModifierByType(modifier) ?: return false
        val descendant = mod.getDescendant()
        return if (uniqueOnly && mods.contains(modifier)) {
            if (descendant == null) {
                false
            }else{
                removeModifierWithoutCheck(stack, modifier, nbt)
                addModifierToNbt(descendant.modifierId,nbt)
                true
            }
        } else {
            addModifierToNbt(modifier,nbt)
            true
        }
        /*if (highestModifier != null){
            return if (mod?.hasDescendant() == true){
                val highestDescendantPresent: Int = checkModifierLineage(mod, stack)
                if (highestDescendantPresent < 0){
                    if (!uniqueOnly){
                        addModifierToNbt(modifier, nbt)
                        gatherActiveModifiers(stack)
                        return true
                    }
                    false
                } else {
                    val lineage = mod.getModLineage()
                    val newDescendant = lineage[highestDescendantPresent]
                    val currentGeneration = lineage[max(highestDescendantPresent - 1,0)]
                    getModifierByType(newDescendant)?.onAdd(stack)
                    addModifierById(id,newDescendant)
                    addModifierToNbt(newDescendant, nbt, temporary)
                    removeModifierWithoutCheck(id, stack, currentGeneration, nbt)
                    gatherActiveModifiers(stack)
                    true
                }
            } else {
                false
            }
        }
        addModifierWithoutChecking(id, modifier, stack, nbt, temporary)
        return true*/
    }

    protected fun addModifierWithoutChecking(modifier: Identifier, stack: ItemStack, nbt: NbtCompound){
        val mod = getModifierByType(modifier)
        mod?.onAdd(stack)
        addModifierToNbt(modifier, nbt)
        //addModifierById(id,modifier)
        //gatherActiveModifiers(stack)
    }

    /*protected fun checkDescendant(modifier: Identifier, stack: ItemStack): Identifier?{
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

    protected fun checkDescendant(modifier: Identifier, stack: ItemStack): Identifier?{
        val mod = getModifierByType(modifier)
        val lineage = mod?.getModLineage() ?: return null
        val list = getModifiersFromNbt(stack)

        var highestModifier: Identifier? = null
        lineage.forEach { identifier ->
            if (checkModListContainsById(id,identifier)){
                highestModifier = identifier
            }
        }
        return highestModifier
    }*/

    fun removeModifier(entity: LivingEntity, modifier: Identifier, uniqueOnly: Boolean = true){
        val mod = getModifierByType(modifier) ?: return
        val container = (entity as ModifierHolding).fzzy_core_getModifierContainer()
        val mods = container.getModifiers(getType(),ModifierContainer.EntryType.INNATE)
        if (mods.contains(modifier) && uniqueOnly){
            val ancestor = mod.getAncestor()
            if (ancestor != null)
                container.addModifier(ancestor,getType(),ModifierContainer.EntryType.INNATE)
        }
        container.removeModifier(mod,getType(),ModifierContainer.EntryType.INNATE)
    }

    fun removeModifier(stack: ItemStack, modifier: Identifier, uniqueOnly: Boolean = true){
        val nbt = stack.nbt?:return
        removeModifier(stack, modifier, nbt, uniqueOnly)
    }

    protected fun removeModifier(stack: ItemStack, modifier: Identifier, nbt: NbtCompound, uniqueOnly: Boolean = true){
        //val id = Nbt.getItemStackId(nbt)
        if (!modifiersInitialized(stack, nbt)) return
        val mod = getModifierByType(modifier) ?: return
        val mods = getModifiersFromNbt(nbt)
        if (mods.contains(modifier) && uniqueOnly){
            val ancestor = mod.getAncestor()
            if (ancestor != null)
                addModifierWithoutChecking(ancestor.modifierId,stack,nbt)
        }
        removeModifierWithoutCheck(stack, modifier, nbt)
    }

    protected fun removeModifierWithoutCheck(stack: ItemStack, modifier: Identifier, nbt: NbtCompound){
        getModifierByType(modifier)?.onRemove(stack)
        //removeModifierById(id,modifier)
        //gatherActiveModifiers(stack)
        removeModifierFromNbt(modifier,nbt)
    }

    fun removeAllModifiers(stack: ItemStack){
        val nbt = stack.nbt ?: return
        //val id = Nbt.getItemStackId(nbt)
        //setModifiersById(id, mutableListOf())
        //removeActiveModifiersById(id)
        val list = nbt.getList(getType().getModifiersKey(), NbtElement.COMPOUND_TYPE.toInt())
        for (el in list){
            val modId = Identifier((el as NbtCompound).getString(getType().getModifierIdKey()))
            getModifierByType(modId)?.onRemove(stack)
        }
        nbt.remove(getType().getModifiersKey())
        //gatherActiveModifiers(stack)
    }


    fun addModifierToNbt(modifier: Identifier, stack: ItemStack){
        val nbt = stack.orCreateNbt
        addModifierWithoutChecking(modifier, stack, nbt)
    }

    fun addModifierToNbt(modifier: Identifier, nbt: NbtCompound){
        val newEl = NbtCompound()
        newEl.putString(getType().getModifierIdKey(),modifier.toString())
        Nbt.addNbtToList(newEl, getType().getModifiersKey(),nbt)
    }

    protected fun removeModifierFromNbt(modifier: Identifier, nbt: NbtCompound){
        Nbt.removeNbtFromList(getType().getModifiersKey(),nbt) { nbtEl: NbtCompound ->
            if (nbtEl.contains(getType().getModifierIdKey())){
                modifier.toString() == nbtEl.getString(getType().getModifierIdKey())
            } else {
                false
            }
        }
    }

    fun modifiersInitialized(stack: ItemStack, nbt: NbtCompound): Boolean{
        return nbt.contains(NbtKeys.MOD_INIT.str() + stack.translationKey)
    }

    fun modifiersInitialized(stack: ItemStack): Boolean{
        return if (stack.nbt == null) {
            false
        } else {
            modifiersInitialized(stack, stack.orCreateNbt)
        }
    }

    override fun initializeModifiers(stack: ItemStack): List<Identifier>{
        val item = stack.item
        val list = if(item is Modifiable){
            item.defaultModifiers(getType())
        } else {
            listOf()
        }
        if (list.isNotEmpty()){
            val nbt = stack.orCreateNbt
            if (!nbt.contains(NbtKeys.MOD_INIT.str() + stack.translationKey)){
                for (mod in list) {
                    addModifierToNbt(mod,nbt)
                }
                nbt.putBoolean(NbtKeys.MOD_INIT.str() + stack.translationKey,true)
            }
        }
        return list
    }

    /*protected fun initializeModifiers(nbt: NbtCompound, id: Long){
        val nbtList = nbt.getList(getType().getModifiersKey(),10)
        setModifiersById(id, mutableListOf())
        for (el in nbtList){
            val compound = el as NbtCompound
            if (compound.contains(getType().getModifierIdKey())){
                val modifier = compound.getString(getType().getModifierIdKey())
                addModifierById(id,Identifier(modifier))
            }
        }
    }*/

    fun getModifiers(stack: ItemStack): List<Identifier>{
        val nbt = stack.nbt?:return listOf()
        //var id = Nbt.getItemStackId(stack)
        /*if (id == -1L){
            if (nbt.contains(getType().getModifiersKey())) {
                id = Nbt.makeItemStackId(stack)
                initializeModifiers(nbt, id)
            } else{
                return listOf()
            }
        } else*/
        if (!modifiersInitialized(stack, nbt))
            return initializeModifiers(stack)

        return getModifiersFromNbt(stack)

        /*val nbt = stack.orCreateNbt
        val id = Nbt.makeItemStackId(stack)
        if (!checkModifiersKeyById(id)) {
            if (nbt.contains(getType().getModifiersKey())) {
                initializeModifiers(nbt, id)
            }
        }
        return synchronized(modifiers){
            modifiers[id] ?: listOf()
        }*/
    }

    fun getModifiersFromNbt(stack: ItemStack): List<Identifier>{
        val nbt = stack.nbt?:return listOf()
        return getModifiersFromNbt(nbt)
    }
    fun getModifiersFromNbt(nbt: NbtCompound): List<Identifier>{
        val list: MutableList<Identifier> = mutableListOf()
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

    fun modifiersFromNbt(stack: ItemStack): List<T>{
        return getModifiersFromNbt(stack).mapNotNull { getModifierByType(it) }
    }

    abstract fun getModifierByType(id: Identifier): T?

    inline fun <reified T : AbstractModifier<T>> gatherActiveAbstractModifiers(
        stack: ItemStack,
        objectToAffect: Identifier,
        compiler: AbstractModifier<T>.Compiler
    ): AbstractModifier.CompiledModifiers<T> {
        try {
            getModifiersFromNbt(stack).forEach { identifier ->
                val modifier = ModifierRegistry.getByType<T>(identifier)
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

    object TemporaryModifiers: PersistentEffectHelper.PersistentEffect {
        override val delay: PerLvlI
            get() = PerLvlI()
        override fun persistentEffect(data: PersistentEffectHelper.PersistentEffectData) {
            if(data is TemporaryModifierData){
                val helper = data.helper
                helper.removeModifier(data.entity,data.modifier, false)
            }
        }

        class TemporaryModifierData(
            val helper: AbstractModifierHelper<*>,
            val modifier: Identifier,
            val entity: LivingEntity)
            :
            PersistentEffectHelper.PersistentEffectData
    }

    companion object{

        val BLANK = Identifier(FC.MOD_ID,"blank")
        val EMPTY = EmptyModifier()

        fun getEmptyHelper(): EmptyModifierHelper{
            return EmptyModifierHelper
        }

        object EmptyModifierHelper: AbstractModifierHelper<EmptyModifier>() {
            override val fallbackData: AbstractModifier.CompiledModifiers<EmptyModifier> = AbstractModifier.CompiledModifiers(arrayListOf(),EMPTY)

            override fun compile(input: List<Identifier>?): AbstractModifier.CompiledModifiers<EmptyModifier> {
                return fallbackData
            }

            override fun compiler(): AbstractModifier<EmptyModifier>.Compiler {
                return EMPTY.compiler()
            }

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

            override fun getType(): ModifierHelperType<EmptyModifier> {
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

            override fun getModifierHelper(): AbstractModifierHelper<EmptyModifier> {
                return EmptyModifierHelper
            }

        }
    }
}
