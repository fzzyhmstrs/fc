package me.fzzyhmstrs.amethyst_core.modifier_util

import com.google.common.collect.ArrayListMultimap
import com.google.common.collect.Multimap
import me.fzzyhmstrs.amethyst_core.AC
import me.fzzyhmstrs.amethyst_core.coding_util.PerLvlI
import me.fzzyhmstrs.amethyst_core.trinket_util.TrinketUtil
import me.fzzyhmstrs.amethyst_core.trinket_util.TrinketChecker
import net.minecraft.block.BlockState
import net.minecraft.entity.EquipmentSlot
import net.minecraft.entity.LivingEntity
import net.minecraft.entity.attribute.EntityAttribute
import net.minecraft.entity.attribute.EntityAttributeModifier
import net.minecraft.entity.damage.DamageSource
import net.minecraft.entity.player.PlayerEntity
import net.minecraft.item.*
import net.minecraft.loot.provider.number.ConstantLootNumberProvider
import net.minecraft.loot.provider.number.LootNumberProvider
import net.minecraft.tag.TagKey
import net.minecraft.util.Formatting
import net.minecraft.util.Identifier
import net.minecraft.util.math.BlockPos
import net.minecraft.util.registry.Registry
import net.minecraft.world.World
import java.util.*
import java.util.function.Predicate

class EquipmentModifier(
    modifierId: Identifier = ModifierDefaults.BLANK_ID, 
    val target: EquipmentModifierTarget = EquipmentModifierTarget.ANY,
    val weight: Int = 10,
    val rarity: Rarity = Rarity.COMMON,
    val persistent: Boolean = false, 
    val randomSelectable: Boolean = true): AbstractModifier<EquipmentModifier>(modifierId) {
    
    init{
        EquipmentModifierHelper.addToTargetMap(this)
    }
    
    private val attributeModifiers: Multimap<EntityAttribute, EntityAttributeModifier> = ArrayListMultimap.create()
    private val augmentModifiers: MutableList<AugmentModifier> = mutableListOf()
    private val postHitConsumers: MutableList<ToolConsumer> = mutableListOf()
    private val postMineConsumers: MutableList<MiningConsumer> = mutableListOf()
    private val onUseConsumers: MutableList<ToolConsumer> = mutableListOf()
    private val onDamagedFunctions: MutableList<DamageFunction> = mutableListOf()
    private val killOtherConsumers: MutableList<ToolConsumer> = mutableListOf()
    private val tickConsumers: MutableList<ToolConsumer> = mutableListOf()
    private var durabilityModifier: PerLvlI = PerLvlI()
    
    internal var toll: LootNumberProvider = ConstantLootNumberProvider.create(5f)

    override fun plus(other: EquipmentModifier): EquipmentModifier {
        attributeModifiers.putAll(other.attributeModifiers)
        augmentModifiers.addAll(other.augmentModifiers)
        postHitConsumers.addAll(other.postHitConsumers)
        postMineConsumers.addAll(other.postMineConsumers)
        onUseConsumers.addAll(other.onUseConsumers)
        onDamagedFunctions.addAll(other.onDamagedFunctions)
        killOtherConsumers.addAll(other.killOtherConsumers)
        tickConsumers.addAll(other.tickConsumers)
        durabilityModifier.plus(other.durabilityModifier)
        return this
    }

    fun withAttributeModifier(attribute: EntityAttribute, uuid: String, amount: Double, operation: EntityAttributeModifier.Operation): EquipmentModifier {
        val modifier = EntityAttributeModifier(UUID.fromString(uuid), this::getTranslationKey,amount,operation)
        attributeModifiers.put(attribute,modifier)
        return this
    }

    fun attributeModifiers(): Multimap<EntityAttribute, EntityAttributeModifier>{
        return attributeModifiers
    }

    fun withAugmentModifiers(vararg modifier: AugmentModifier): EquipmentModifier{
        if (modifier.isNotEmpty()) {
            augmentModifiers.addAll(modifier)
        }
        return this
    }

    fun augmentModifiers(): List<AugmentModifier>{
        return augmentModifiers
    }
    
    fun withDurabilityMod(durabilityMod: PerLvlI): EquipmentModifier{
        this.durabilityModifier = durabilityMod
        return this
    }
    
    fun modifyDurability(durability: Int): Int{
        val dur = PerLvlI(durability)
        return dur.plus(durabilityModifier).value(0)
    }

    fun withPostHit(onHit: ToolConsumer): EquipmentModifier {
        postHitConsumers.add(onHit)
        return this
    }

    fun postHit(stack: ItemStack, user: LivingEntity, target: LivingEntity?){
        postHitConsumers.forEach {
            it.apply(stack, user, target)
        }
    }

    fun withPostMine(onMine: MiningConsumer): EquipmentModifier {
        postMineConsumers.add(onMine)
        return this
    }

    fun postMine(stack: ItemStack, world: World, state: BlockState, pos: BlockPos, miner: PlayerEntity){
        postMineConsumers.forEach {
            it.apply(stack, world, state, pos, miner)
        }
    }

    fun withOnUse(onUse: ToolConsumer): EquipmentModifier {
        onUseConsumers.add(onUse)
        return this
    }

    fun onUse(stack: ItemStack, user: LivingEntity, target: LivingEntity?){
        onUseConsumers.forEach {
            it.apply(stack, user, target)
        }
    }

    fun withOnDamaged(onDamaged: DamageFunction): EquipmentModifier {
        onDamagedFunctions.add(onDamaged)
        return this
    }

    fun onDamaged(stack: ItemStack, user: LivingEntity, attacker: LivingEntity?, source: DamageSource, amount: Float): Float{
        var newAmount = amount
        onDamagedFunctions.forEach {
            newAmount = it.test(stack, user, attacker, source, newAmount)
        }
        return newAmount
    }

    fun withKilledOther(killOther: ToolConsumer): EquipmentModifier {
        killOtherConsumers.add(killOther)
        return this
    }

    fun killedOther(stack: ItemStack, user: LivingEntity, target: LivingEntity?){
        killOtherConsumers.forEach {
            it.apply(stack, user, target)
        }
    }

    fun withTick(tick: ToolConsumer): EquipmentModifier {
        tickConsumers.add(tick)
        return this
    }

    fun tick(stack: ItemStack, user: LivingEntity, target: LivingEntity?){
        tickConsumers.forEach {
            it.apply(stack, user, target)
        }
    }
    
    fun withDescendant(modifier: EquipmentModifier): EquipmentModifier {
        addDescendant(modifier)
        return this
    }
    
    fun withToll(toll: LootNumberProvider): EquipmentModifier{
        this.toll = toll
        return this
    }

    override fun compiler(): Compiler {
        return Compiler(mutableListOf(), EquipmentModifier())
    }

    override fun getModifierHelper(): AbstractModifierHelper<*> {
        return EquipmentModifierHelper
    }

    @FunctionalInterface
    fun interface ToolConsumer{
        fun apply(stack: ItemStack, user: LivingEntity, target: LivingEntity?)
    }

    @FunctionalInterface
    fun interface MiningConsumer{
        fun apply(stack: ItemStack, world: World, state: BlockState, pos: BlockPos, miner: PlayerEntity)
    }

    @FunctionalInterface
    fun interface DamageFunction{
        fun test(stack: ItemStack, user: LivingEntity, attacker: LivingEntity?, source: DamageSource, amount: Float): Float
    }
    
    abstract class EquipmentModifierTarget(val id: Identifier){
    
        companion object{
            
            val GLOBAL_EXCLUSIONS = TagKey.of(Registry.ITEM_KEY,Identifier(AC.MOD_ID,"global_modifier_exclusions"))
            internal val targets: MutableList<EquipmentModifierTarget> = mutableListOf()
            val trinketsPredicate by lazy {
                getTrinketCheck()
            }

            fun getTrinketCheck(): Predicate<ItemStack>{
                return TrinketUtil.trinketCheck
            }
            
            internal fun findTargetForItem(stack: ItemStack): List<EquipmentModifierTarget>{
                val list: MutableList<EquipmentModifierTarget> = mutableListOf()
                for (target in targets){
                    if (target.isStackAcceptable(stack)){
                        list.add(target)
                    }
                }
                return list
            }
            
            val ANY = object: EquipmentModifierTarget(Identifier(AC.MOD_ID,"any")){
                override fun isAcceptableItem(stack: ItemStack): Boolean{
                    return TOOL.isItemAcceptableOrTagged(stack) || ARMOR.isItemAcceptableOrTagged(stack) || TRINKET.isItemAcceptableOrTagged(stack)
                }
            }
            val BREAKABLE = object: EquipmentModifierTarget(Identifier(AC.MOD_ID,"breakable")){
                override fun isAcceptableItem(stack: ItemStack): Boolean{
                    return stack.item.isDamageable
                }
            }
            val WEAPON = object: EquipmentModifierTarget(Identifier(AC.MOD_ID,"weapon")){
                override fun isAcceptableItem(stack: ItemStack): Boolean{
                    return SWORD.isItemAcceptableOrTagged(stack) || AXE.isItemAcceptableOrTagged(stack) || TRIDENT.isItemAcceptableOrTagged(stack) || BOW.isItemAcceptableOrTagged(stack)
                }
            }
            val WEAPON_AND_TRINKET = object: EquipmentModifierTarget(Identifier(AC.MOD_ID,"weapon_and_trinket")){
                override fun isAcceptableItem(stack: ItemStack): Boolean{
                    return WEAPON.isItemAcceptableOrTagged(stack) || TRINKET.isItemAcceptableOrTagged(stack)
                }
            }
            val SWORD = object: EquipmentModifierTarget(Identifier(AC.MOD_ID,"sword")){
                override fun isAcceptableItem(stack: ItemStack): Boolean{
                    return stack.item is SwordItem
                }
            }
            val AXE = object: EquipmentModifierTarget(Identifier(AC.MOD_ID,"axe")){
                override fun isAcceptableItem(stack: ItemStack): Boolean{
                    return stack.item is AxeItem
                }
            }
            val TRIDENT = object: EquipmentModifierTarget(Identifier(AC.MOD_ID,"trident")){
                override fun isAcceptableItem(stack: ItemStack): Boolean{
                    return stack.item is TridentItem
                }
            }
            val BOW = object: EquipmentModifierTarget(Identifier(AC.MOD_ID,"bow")){
                override fun isAcceptableItem(stack: ItemStack): Boolean{
                    return stack.item is BowItem || stack.item is CrossbowItem
                }
            }
            val MINING = object: EquipmentModifierTarget(Identifier(AC.MOD_ID,"mining")){
                override fun isAcceptableItem(stack: ItemStack): Boolean{
                    return stack.item is MiningToolItem
                }
            }
            val SHIELD = object: EquipmentModifierTarget(Identifier(AC.MOD_ID,"shield")){
                override fun isAcceptableItem(stack: ItemStack): Boolean{
                    return stack.item is ShieldItem
                }
            }
            val TOOL = object: EquipmentModifierTarget(Identifier(AC.MOD_ID,"tool")){
                override fun isAcceptableItem(stack: ItemStack): Boolean{
                    return stack.item is ToolItem
                }
            }
            val ARMOR = object: EquipmentModifierTarget(Identifier(AC.MOD_ID,"armor")){
                override fun isAcceptableItem(stack: ItemStack): Boolean{
                    return stack.item is ArmorItem
                }
            }
            val ARMOR_AND_TRINKET = object: EquipmentModifierTarget(Identifier(AC.MOD_ID,"armor_and_trinket")){
                override fun isAcceptableItem(stack: ItemStack): Boolean{
                    return ARMOR.isItemAcceptableOrTagged(stack) || TRINKET.isItemAcceptableOrTagged(stack)
                }
            }
            val ARMOR_HEAD = object: EquipmentModifierTarget(Identifier(AC.MOD_ID,"armor_head")){
                override fun isAcceptableItem(stack: ItemStack): Boolean{
                    val item = stack.item
                    return (item is ArmorItem && item.slotType == EquipmentSlot.HEAD)
                }
            }
            val ARMOR_CHEST = object: EquipmentModifierTarget(Identifier(AC.MOD_ID,"armor_chest")){
                override fun isAcceptableItem(stack: ItemStack): Boolean{
                    val item = stack.item
                    return (item is ArmorItem && item.slotType == EquipmentSlot.CHEST)
                }
            }
            val ARMOR_LEGS = object: EquipmentModifierTarget(Identifier(AC.MOD_ID,"armor_legs")){
                override fun isAcceptableItem(stack: ItemStack): Boolean{
                    val item = stack.item
                    return (item is ArmorItem && item.slotType == EquipmentSlot.LEGS)
                }
            }
            val ARMOR_FEET = object: EquipmentModifierTarget(Identifier(AC.MOD_ID,"armor_feet")){
                override fun isAcceptableItem(stack: ItemStack): Boolean{
                    val item = stack.item
                    return (item is ArmorItem && item.slotType == EquipmentSlot.FEET)
                }
            }
            val TRINKET = object: EquipmentModifierTarget(Identifier(AC.MOD_ID,"trinket")){
                override fun isAcceptableItem(stack: ItemStack): Boolean{
                    return if (TrinketChecker.trinketsLoaded) {
                        trinketsPredicate.test(stack)
                    } else {
                        false
                    }
                }
            }
        }
        
        val tagIncluded: TagKey<Item>
        val tagExcluded: TagKey<Item>
        
        init{
            for (target in targets){
                if (target.id == id) throw IllegalStateException("Equipment Modifier target $id already instantiated!")
            }
            targets.add(getTarget())
            tagIncluded = TagKey.of(Registry.ITEM_KEY,id)
            tagExcluded = TagKey.of(Registry.ITEM_KEY,Identifier(id.namespace,id.path + "_excluded"))
        }

        private fun getTarget(): EquipmentModifierTarget{
            return this
        }
        
        override fun equals(other: Any?): Boolean{
            if (other == null) return false
            if (other !is EquipmentModifierTarget) return false
            return other.id == id
        }
        
        override fun hashCode(): Int{
            return id.hashCode() + 31 * id.hashCode()
        }
        
        fun isStackAcceptable(stack: ItemStack): Boolean{
            if (stack.isIn(GLOBAL_EXCLUSIONS)) return false
            return isItemAcceptableOrTagged(stack)
        }

        private fun isItemAcceptableOrTagged(stack: ItemStack): Boolean{
            if (stack.isIn(tagExcluded)) return false
            return stack.isIn(tagIncluded) || isAcceptableItem(stack)
        }
                
        protected abstract fun isAcceptableItem(stack: ItemStack): Boolean
        
    }
    
    enum class Rarity(vararg val formatting: Formatting){
        REALLY_BAD(Formatting.BOLD, Formatting.DARK_RED),
        BAD(Formatting.DARK_RED),
        COMMON(Formatting.WHITE),
        UNCOMMON(Formatting.DARK_GREEN),
        RARE(Formatting.AQUA),
        EPIC(Formatting.LIGHT_PURPLE),
        LEGENDARY(Formatting.BOLD, Formatting.GOLD)
    }
    
}