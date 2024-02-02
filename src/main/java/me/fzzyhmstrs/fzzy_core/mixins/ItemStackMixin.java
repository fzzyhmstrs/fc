package me.fzzyhmstrs.fzzy_core.mixins;

import com.google.common.collect.ArrayListMultimap;
import com.google.common.collect.Multimap;
import com.llamalad7.mixinextras.injector.ModifyReturnValue;
import com.llamalad7.mixinextras.injector.wrapoperation.Operation;
import com.llamalad7.mixinextras.injector.wrapoperation.WrapOperation;
import me.fzzyhmstrs.fzzy_core.enchantment_util.AttributeProviding;
import me.fzzyhmstrs.fzzy_core.interfaces.Modifiable;
import me.fzzyhmstrs.fzzy_core.modifier_util.ModifierHelperType;
import net.minecraft.client.item.TooltipContext;
import net.minecraft.enchantment.EnchantmentHelper;
import net.minecraft.entity.EquipmentSlot;
import net.minecraft.entity.attribute.EntityAttribute;
import net.minecraft.entity.attribute.EntityAttributeModifier;
import net.minecraft.item.EnchantedBookItem;
import net.minecraft.item.Item;
import net.minecraft.item.ItemConvertible;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NbtCompound;
import net.minecraft.text.Text;
import net.minecraft.world.World;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import java.util.List;

@Mixin(ItemStack.class)
public abstract class ItemStackMixin {

    @Shadow @Final
    private Item item;

    @Shadow public abstract Item getItem();

    @Inject(method = "<init>(Lnet/minecraft/nbt/NbtCompound;)V", at = @At("TAIL"))
    private void fzzy_core_initializeFromNbt(NbtCompound nbt, CallbackInfo ci){
        if (this.item == null) return;
        if (this.item instanceof Modifiable modifiableItem){
            for (ModifierHelperType<?> type : ModifierHelperType.Companion.getREGISTRY()){
                if (!modifiableItem.canBeModifiedBy(type)) continue;
                type.initializeModifiers((ItemStack) (Object) this);
            }
        }
    }

    @Inject(method = "<init>(Lnet/minecraft/item/ItemConvertible;I)V", at = @At("TAIL"))
    private void fzzy_core_initializeFromItem(ItemConvertible item, int count, CallbackInfo ci){
        if (this.item == null) return;
        if (this.item instanceof Modifiable modifiableItem){
            for (ModifierHelperType<?> type : ModifierHelperType.Companion.getREGISTRY()){
                if (!modifiableItem.canBeModifiedBy(type)) continue;
                type.initializeModifiers((ItemStack) (Object) this);
            }
            //modifiableItem.getModifierInitializer().initializeModifiers((ItemStack) (Object) this, getOrCreateNbt(), modifiableItem.defaultModifiers());
        }
    }

    @WrapOperation(method = "getTooltip", at = @At(value = "INVOKE", target = "net/minecraft/item/Item.appendTooltip (Lnet/minecraft/item/ItemStack;Lnet/minecraft/world/World;Ljava/util/List;Lnet/minecraft/client/item/TooltipContext;)V"))
    private void fzzy_core_appendModifiersToTooltip(Item instance,ItemStack stack, World world, List<Text> tooltip, TooltipContext context, Operation<Void> operation){
        operation.call(instance,stack, world, tooltip,context);
        if (stack.getItem() instanceof Modifiable modifiable){
            for (ModifierHelperType<?> type : ModifierHelperType.Companion.getREGISTRY()) {
                if (!modifiable.canBeModifiedBy(type)) continue;
                modifiable.addModifierTooltip(stack, tooltip, context, type);
            }
        }
    }

    @ModifyReturnValue(method = "getAttributeModifiers", at = @At("RETURN"))
    private Multimap<EntityAttribute, EntityAttributeModifier> amethyst_imbuement_addResilientAttributesToModifiers(Multimap<EntityAttribute, EntityAttributeModifier> original, EquipmentSlot slot){
        if (getItem() instanceof EnchantedBookItem) return original;
        if (original.isEmpty()) return original;
        Multimap<EntityAttribute, EntityAttributeModifier> newMap = null;
        boolean modified = false;
        for (var entry: EnchantmentHelper.get((ItemStack) (Object) this).entrySet()){
            if (entry.getKey() instanceof AttributeProviding attributeProvidingEnchant){
                if (newMap == null) newMap = ArrayListMultimap.create(original);
                attributeProvidingEnchant.modifyAttributeMap(newMap,slot, entry.getValue());
                modified = true;
            }
        }
        return modified ? newMap : original;
    }

}
