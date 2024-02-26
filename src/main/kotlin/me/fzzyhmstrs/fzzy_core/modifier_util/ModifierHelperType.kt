package me.fzzyhmstrs.fzzy_core.modifier_util

import com.google.common.collect.ArrayListMultimap
import com.google.gson.JsonElement
import com.mojang.datafixers.util.Either
import com.mojang.serialization.Codec
import com.mojang.serialization.DataResult
import me.fzzyhmstrs.fzzy_core.FC
import me.fzzyhmstrs.fzzy_core.coding_util.FzzyPort
import net.minecraft.item.ItemStack
import net.minecraft.nbt.NbtCompound
import net.minecraft.registry.Registry
import net.minecraft.util.Identifier

abstract class ModifierHelperType <T: AbstractModifier<T>> (val id: Identifier, private val helper: AbstractModifierHelper<T>) {

    val modifierCodec: Codec<T> = Identifier.CODEC.comapFlatMap(
        {i -> this.helper().getModifierByType(i)?.let { DataResult.success(it) } ?: DataResult.error{"Modifier $i of type ${this.id} not found in Modifier Registry!"}},
        {mod -> mod.modifierId}
    )

    open val codec: Codec<Either<T,List<T>>> = Codec.either(modifierCodec, Codec.list(modifierCodec))

    open fun getModifierInitializer(): ModifierInitializer {
        return helper
    }

    open fun helper(): AbstractModifierHelper<T>{
        return helper
    }

    open fun compile(input: List<Identifier>?, predicateId: Identifier?): AbstractModifier.CompiledModifiers<T> {
        return helper.compile(input, predicateId)
    }

    open fun add(stack: ItemStack, id: SlotId, modifierContainer: ModifierContainer){
        //println("Adding modifiers to ${modifierContainer.livingEntity} from stack $stack")
        for (mod in helper.modifiersFromNbt(stack)) {
            modifierContainer.addModifier(mod, this)
        }
    }

    open fun remove(stack: ItemStack, id: SlotId, modifierContainer: ModifierContainer){
        for (mod in helper.modifiersFromNbt(stack)) {
            modifierContainer.removeModifier(mod, this)
        }
    }

    open fun areModifiersEqual(old: ItemStack, young: ItemStack): Boolean{
        return helper.modifiersAreEqual(old, young)
    }

    abstract fun getModifierIdKey(): String

    abstract fun getModifiersKey(): String

    abstract fun getModifierInitKey(): String

    //for API compat
    fun initializeModifiers(stack: ItemStack, @Suppress("UNUSED_PARAMETER") nbtCompound: NbtCompound, @Suppress("UNUSED_PARAMETER") defaultMods: List<Identifier>){
        getModifierInitializer().initializeModifiers(stack)
    }

    open fun initializeModifiers(stack: ItemStack){
        getModifierInitializer().initializeModifiers(stack)
    }

    open fun deserialize(id:Identifier, json:JsonElement): T?{
        return null
    }

    override fun equals(other: Any?): Boolean {
        if (other == null) return false
        if (other !is ModifierHelperType<*>) return false
        return other.id == id
    }

    override fun hashCode(): Int {
        return id.hashCode()
    }

    companion object{
        val REGISTRY = FzzyPort.simpleRegistry<ModifierHelperType<*>>(Identifier(FC.MOD_ID,"modifier_helper_type"))
        val EMPTY_TYPE = Registry.register(REGISTRY,EmptyType.id,EmptyType)
        val CODEC: Codec<ArrayListMultimap<ModifierHelperType<*>,AbstractModifier<*>>> =
            Codec.list(REGISTRY.codec.partialDispatch(
                "type",
                {mod: Either<out AbstractModifier<*>, out List<AbstractModifier<*>>> -> getTypeFromEither(mod)},
                {type -> DataResult.success(type.codec)})
            ).xmap(
                {list -> list.stream()
                    .map{either ->  either.map({l -> listOf(l)},{r -> r}) }
                    .collect<ArrayListMultimap<ModifierHelperType<*>,AbstractModifier<*>>>(
                        { ArrayListMultimap.create()},
                        { m, l -> l.forEach { m.put(it.getModifierHelper().getType(),it) }},
                        {map1, map2 -> map1.putAll(map2)})
                },
                {map ->
                    val l: MutableList<Either<out AbstractModifier<*>, out List<AbstractModifier<*>>>> = mutableListOf()
                    for (key in map.keys()){
                        val collection = map.get(key)
                        if (collection.isEmpty()) continue
                        l.add(if(collection.size == 1) Either.left(collection[0]) else Either.right(collection))
                    }
                     l
                }
            )

        private fun getTypeFromEither(either: Either<out AbstractModifier<*>, out List<AbstractModifier<*>>>): DataResult<ModifierHelperType<*>>{
            var dataResult: DataResult<ModifierHelperType<*>> = DataResult.error{"Modifier helper type can't be generated from provided values: $either"}
            either.ifLeft{ dataResult = DataResult.success(it.getModifierHelper().getType()) }
            either.ifRight{
                if (it.isNotEmpty()) {
                    val type = it[0].getModifierHelper().getType()
                    var failed = false
                    for (mod in it){
                        if (mod.getModifierHelper().getType() != type){
                            failed = true
                            dataResult = DataResult.error{ "Modifier list has modifiers with multiple types!: $it" }
                            break
                        }
                    }
                    if (!failed)
                        dataResult = DataResult.success(type)
                }
            }
            return dataResult
        }

        fun<T: AbstractModifier<T>> register(type: ModifierHelperType<T>): ModifierHelperType<T>{
            return Registry.register(REGISTRY,type.id,type)
        }

        fun add(stack: ItemStack, id: SlotId, modifierContainer: ModifierContainer) {
            for (type in REGISTRY){
                type.add(stack, id, modifierContainer)
            }
        }
        fun remove(stack: ItemStack, id: SlotId, modifierContainer: ModifierContainer){
            for (type in REGISTRY){
                type.remove(stack, id, modifierContainer)
            }
        }

        fun areModifiersEqual(old: ItemStack, young: ItemStack): Boolean{
            for (type in REGISTRY){
                if (!type.areModifiersEqual(old, young)) return false
            }
            return true
        }

        object EmptyType: ModifierHelperType<AbstractModifierHelper.Companion.EmptyModifier>(Identifier(FC.MOD_ID,"empty_helper"), AbstractModifierHelper.getEmptyHelper()){
            override fun getModifierInitializer(): ModifierInitializer {
                return AbstractModifierHelper.getEmptyHelper()
            }

            override fun getModifierIdKey(): String {
                return "empty_modifier_id"
            }

            override fun getModifiersKey(): String {
                return "empty_modifiers"
            }

            override fun getModifierInitKey(): String {
                return "empty_init"
            }

        }
    }

}