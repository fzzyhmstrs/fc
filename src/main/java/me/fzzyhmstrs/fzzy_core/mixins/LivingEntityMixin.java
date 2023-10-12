package me.fzzyhmstrs.fzzy_core.mixins;

import com.google.common.collect.ArrayListMultimap;
import com.llamalad7.mixinextras.injector.wrapoperation.Operation;
import com.llamalad7.mixinextras.injector.wrapoperation.WrapOperation;
import me.fzzyhmstrs.fzzy_core.interfaces.ModifierHolding;
import me.fzzyhmstrs.fzzy_core.modifier_util.ModifierContainer;
import me.fzzyhmstrs.fzzy_core.modifier_util.ModifierHelperType;
import net.minecraft.entity.LivingEntity;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NbtCompound;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(LivingEntity.class)
public class LivingEntityMixin implements ModifierHolding {

    @Unique
    private ModifierContainer fzzy_core_modifierHolder = null;

    @Override
    public ModifierContainer fzzy_core_getModifierContainer() {
        if (fzzy_core_modifierHolder == null){
            fzzy_core_modifierHolder = new ModifierContainer((LivingEntity) (Object) this, ArrayListMultimap.create());
        }
        return fzzy_core_modifierHolder;
    }

    @Override
    public void fzzy_core_setModifierContainer(ModifierContainer container) {
        fzzy_core_modifierHolder = container;
    }

    @Inject(method = "writeCustomDataToNbt", at = @At("TAIL"))
    private void fzzy_core_writeStackToNbt(NbtCompound nbt, CallbackInfo ci){
        if (fzzy_core_modifierHolder!= null){
            NbtCompound nbtCompound = new NbtCompound();
            fzzy_core_modifierHolder.save(nbtCompound);
            nbt.put("modifier_container",nbtCompound);
        }
    }

    @Inject(method = "readCustomDataFromNbt", at = @At("TAIL"))
    private void fzzy_core_readStackFromNbt(NbtCompound nbt, CallbackInfo ci){
        if (nbt.contains("modifier_container")){
            NbtCompound nbtCompound = nbt.getCompound("modifier_container");
            fzzy_core_modifierHolder = ModifierContainer.load((LivingEntity) (Object) this, nbtCompound);
        }
    }

    @WrapOperation(method = "getEquipmentChanges", at = @At(value = "INVOKE", target = "net/minecraft/entity/LivingEntity.areItemsDifferent (Lnet/minecraft/item/ItemStack;Lnet/minecraft/item/ItemStack;)Z"))
    private boolean fzzy_core_applyModifierChanges(LivingEntity instance, ItemStack old, ItemStack young, Operation<Boolean> operation){
        boolean bl = operation.call(instance, old, young);
        if (bl){
            if (ModifierHelperType.Companion.areModifiersEqual(old, young)) return true;
            if (!old.isEmpty()){
                ModifierHelperType.Companion.remove(old, fzzy_core_getModifierContainer());
            }
            if (!young.isEmpty()){
                ModifierHelperType.Companion.add(young, fzzy_core_getModifierContainer());
            }
        }
        return bl;
    }
}
