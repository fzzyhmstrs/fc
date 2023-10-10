package me.fzzyhmstrs.fzzy_core.mixins;

import dev.emi.trinkets.api.SlotReference;
import dev.emi.trinkets.api.Trinket;
import me.fzzyhmstrs.fzzy_core.interfaces.Modifiable;
import me.fzzyhmstrs.fzzy_core.interfaces.ModifierHolding;
import me.fzzyhmstrs.fzzy_core.modifier_util.ModifierHelperType;
import net.minecraft.entity.LivingEntity;
import net.minecraft.item.ItemStack;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Pseudo;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Pseudo
@Mixin(Trinket.class)
public interface TrinketMixin extends Modifiable {
    @Inject(method = "onEquip", at = @At("TAIL"))
    private void fzzy_core_processOnEquipForAugments(ItemStack stack, SlotReference slot, LivingEntity entity, CallbackInfo ci){
        ModifierHelperType.Companion.add(stack, ((ModifierHolding) entity).fzzy_core_getModifierContainer());
    }

    @Inject(method = "onUnequip", at = @At("TAIL"))
    private void fzzy_core_processOnUnequipForAugments(ItemStack stack, SlotReference slot, LivingEntity entity, CallbackInfo ci){
        ModifierHelperType.Companion.remove(stack, ((ModifierHolding) entity).fzzy_core_getModifierContainer());
    }

}
