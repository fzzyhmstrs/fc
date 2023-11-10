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

    open fun compile(input: List<Identifier>?, predicateId: Identifier? = null): AbstractModifier.CompiledModifiers<T>{
        //println("compiling with modifiers $input and predicate $predicateId")
        if (input == null) return fallbackData
        val modList = getRelevantModifiers(input, predicateId)
        //println("Filtered to modifiers $modList")
        val compiler = compiler()
        for (mod in modList){
            compiler.add(mod)
        }
        return compiler.compile()
    }

    abstract fun compiler(): AbstractModifier<T>.Compiler

    //abstract fun gatherActiveModifiers(stack: ItemStack)

    abstract fun getTranslationKeyFromIdentifier(id: Identifier): String
    
    abstract fun getDescTranslationKeyFromIdentifier(id: Identifier): String

    abstract fun getType(): ModifierHelperType<T>

    abstract fun getModifierByType(id: Identifier): T?

    override fun addModifierTooltip(stack: ItemStack, tooltip: MutableList<Text>, context: TooltipContext){
        val nbt = stack.nbt ?: return
        if (!nbt.contains(getType().getModifiersKey())) return
        val ids = getModifiersFromNbt(stack)
        for (id in ids){
            val mod = ModifierRegistry.get(id)?:continue
            tooltip.add(mod.getTranslation().append(AcText.literal(" - ")).append(mod.getDescTranslation()))
        }
    }

    fun getModifiers(entity: LivingEntity): List<Identifier>{
        val container = (entity as ModifierHolding).fzzy_core_getModifierContainer()
        return container.getModifiers(getType(),ModifierContainer.EntryType.ALL)
    }
    fun getActiveModifiers(entity: LivingEntity): AbstractModifier.CompiledModifiers<T> {
        val container = (entity as ModifierHolding).fzzy_core_getModifierContainer()
        return container.getCompiledModifiers(getType()) ?: fallbackData
    }
    fun getSpecialActiveModifiers(entity: LivingEntity, predicateId: Identifier): AbstractModifier.CompiledModifiers<T> {
        val container = (entity as ModifierHolding).fzzy_core_getModifierContainer()
        return container.compileSpecialModifier(predicateId,getType()) ?: fallbackData
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
                addModifierToNbt(stack,descendant.modifierId,nbt)
                true
            }
        } else {
            addModifierToNbt(stack,modifier,nbt)
            true
        }
    }

    protected fun addModifierWithoutChecking(modifier: Identifier, stack: ItemStack, nbt: NbtCompound){
        val mod = getModifierByType(modifier)
        mod?.onAdd(stack)
        addModifierToNbt(stack, modifier, nbt)
    }

    fun removeModifier(entity: LivingEntity, modifier: Identifier, uniqueOnly: Boolean = true){
        val mod = getModifierByType(modifier) ?: return
        val container = (entity as ModifierHolding).fzzy_core_getModifierContainer()
        if (uniqueOnly) {
            val mods = container.getModifiers(getType(), ModifierContainer.EntryType.INNATE)
            if (mods.contains(modifier)) {
                val ancestor = mod.getAncestor()
                if (ancestor != null)
                    container.addModifier(ancestor, getType(), ModifierContainer.EntryType.INNATE)
            }
        }
        container.removeModifier(mod,getType(),ModifierContainer.EntryType.INNATE)
    }

    fun removeModifier(stack: ItemStack, modifier: Identifier, uniqueOnly: Boolean = true){
        val nbt = stack.nbt?:return
        removeModifier(stack, modifier, nbt, uniqueOnly)
    }

    protected fun removeModifier(stack: ItemStack, modifier: Identifier, nbt: NbtCompound, uniqueOnly: Boolean = true){
        //val id = Nbt.getItemStackId(nbt)
        val mod = getModifierByType(modifier) ?: return
        if (uniqueOnly) {
            val mods = getModifiersFromNbt(nbt)
            if (mods.contains(modifier)) {
                val ancestor = mod.getAncestor()
                if (ancestor != null)
                    addModifierWithoutChecking(ancestor.modifierId, stack, nbt)
            }
        }
        removeModifierWithoutCheck(stack, modifier, nbt)
    }

    protected fun removeModifierWithoutCheck(stack: ItemStack, modifier: Identifier, nbt: NbtCompound){
        getModifierByType(modifier)?.onRemove(stack)
        //removeModifierById(id,modifier)
        //gatherActiveModifiers(stack)
        removeModifierFromNbt(stack,modifier,nbt)
    }

    fun removeAllModifiers(stack: ItemStack){
        val nbt = stack.nbt ?: return
        //val id = Nbt.getItemStackId(nbt)
        //setModifiersById(id, mutableListOf())
        //removeActiveModifiersById(id)
        val list = nbt.getList(getType().getModifiersKey(), NbtElement.COMPOUND_TYPE.toInt()).copy()
        for (el in list){
            val modId = Identifier((el as NbtCompound).getString(getType().getModifierIdKey()))
            getModifierByType(modId)?.onRemove(stack)
            removeModifierFromNbt(stack, modId, nbt)
        }
        nbt.remove(getType().getModifiersKey())
        //gatherActiveModifiers(stack)
    }


    fun addModifierToNbt(modifier: Identifier, stack: ItemStack){
        val nbt = stack.orCreateNbt
        addModifierWithoutChecking(modifier, stack, nbt)
    }

    protected open fun addModifierToNbt(stack: ItemStack, modifier: Identifier, nbt: NbtCompound){
        val newEl = NbtCompound()
        newEl.putString(getType().getModifierIdKey(),modifier.toString())
        Nbt.addNbtToList(newEl, getType().getModifiersKey(),nbt)
    }

    protected open fun removeModifierFromNbt(stack: ItemStack, modifier: Identifier, nbt: NbtCompound){
        Nbt.removeNbtFromList(getType().getModifiersKey(),nbt) { nbtEl: NbtCompound ->
            if (nbtEl.contains(getType().getModifierIdKey())){
                modifier.toString() == nbtEl.getString(getType().getModifierIdKey())
            } else {
                false
            }
        }
    }

    fun modifiersInitialized(stack: ItemStack, nbt: NbtCompound): Boolean{
        return nbt.contains(getType().getModifierInitKey() + stack.translationKey)
    }

    fun modifiersInitialized(stack: ItemStack): Boolean{
        return if (stack.nbt == null) {
            false
        } else {
            modifiersInitialized(stack, stack.orCreateNbt)
        }
    }

    //for API compat
    fun initializeModifiers(stack: ItemStack, nbt: NbtCompound,  defaultMods: List<Identifier>){
        initializeModifiers(stack)
    }

    override fun initializeModifiers(stack: ItemStack): List<Identifier>{
        val item = stack.item
        val list = if(item is Modifiable){
            item.defaultModifiers(getType())
        } else {
            listOf()
        }
        val nbt = stack.orCreateNbt
        if (list.isNotEmpty()){
            if (!nbt.contains(getType().getModifierInitKey() + stack.translationKey)){
                for (mod in list) {
                    addModifierToNbt(stack,mod,nbt)
                }
                nbt.putBoolean(getType().getModifierInitKey() + stack.translationKey,true)
            }
        }
        return getModifiersFromNbt(stack)
    }

    open fun getModifiers(stack: ItemStack): List<Identifier>{
        val nbt = stack.nbt?:return listOf()
        if (!modifiersInitialized(stack, nbt))
            return initializeModifiers(stack)

        return getModifiersFromNbt(stack)
    }

    fun getRelevantModifiers(livingEntity: LivingEntity, stack: ItemStack): List<T>{
        val item = stack.item
        val mods = getModifiersFromNbt(stack)
        if (item is Modifiable){
            val list: MutableList<T> = mutableListOf()
            val predicateId = item.modifierObjectPredicate(livingEntity, stack)
            for (modId in mods){
                val mod = getModifierByType(modId) ?: continue
                if (!mod.hasObjectToAffect()) {
                    list.add(mod)
                } else if (mod.checkObjectsToAffect(predicateId)) {
                    list.add(mod)
                }
            }
            return list
        }
        return mods.mapNotNull { getModifierByType(it) }
    }

    fun getRelevantModifiers(mods: List<Identifier>, predicateId: Identifier?): List<T>{
        if (predicateId == null)
            return mods.mapNotNull { getModifierByType(it) }
        val list: MutableList<T> = mutableListOf()
        for (modId in mods){
            val mod = getModifierByType(modId) ?: continue
            if (!mod.hasObjectToAffect()) {
                list.add(mod)
            } else if (mod.checkObjectsToAffect(predicateId)) {
                list.add(mod)
            }
        }
        return list
    }

    fun getRelevantModifierIds(mods: List<Identifier>, predicateId: Identifier?): List<Identifier>{
        if (predicateId == null)
            return mods
        val list: MutableList<Identifier> = mutableListOf()
        for (modId in mods){
            val mod = getModifierByType(modId) ?: continue
            if (!mod.hasObjectToAffect()) {
                list.add(modId)
            } else if (mod.checkObjectsToAffect(predicateId)) {
                list.add(modId)
            }
        }
        return list
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

    fun modifiersAreEqual(old: ItemStack, young: ItemStack): Boolean{
        val oldNbt = old.nbt
        val youngNbt = young.nbt
        if (oldNbt == null && youngNbt == null) return true
        if (oldNbt == null || youngNbt == null) return false
        return Nbt.readNbtList(oldNbt, getType().getModifiersKey()) == Nbt.readNbtList(youngNbt, getType().getModifiersKey())
    }

    fun modifiersFromNbt(stack: ItemStack): List<T>{
        return getModifiersFromNbt(stack).mapNotNull { getModifierByType(it) }
    }

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

            override fun compile(input: List<Identifier>?, predicateId: Identifier?): AbstractModifier.CompiledModifiers<EmptyModifier> {
                return fallbackData
            }

            override fun compiler(): AbstractModifier<EmptyModifier>.Compiler {
                return EMPTY.compiler()
            }

            /*override fun gatherActiveModifiers(stack: ItemStack) {
            }*/

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
