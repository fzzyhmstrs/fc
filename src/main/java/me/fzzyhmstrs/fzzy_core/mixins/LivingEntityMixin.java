package me.fzzyhmstrs.fzzy_core.mixins;

import me.fzzyhmstrs.fzzy_core.FC;
import me.fzzyhmstrs.fzzy_core.interfaces.StackHolding;
import me.fzzyhmstrs.fzzy_core.modifier_util.AbstractModifierHelper;
import net.minecraft.entity.LivingEntity;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NbtCompound;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(LivingEntity.class)
public class LivingEntityMixin implements StackHolding {

    private ItemStack fzzy_core_modifierHolder = ItemStack.EMPTY;

    @Override
    public ItemStack getStack() {
        if (fzzy_core_modifierHolder.isEmpty()){
            fzzy_core_modifierHolder = new ItemStack(FC.INSTANCE.getMODIFIER_HOLDER());
        }
        return fzzy_core_modifierHolder;
    }

    @Inject(method = "writeCustomDataToNbt", at = @At("TAIL"))
    private void fzzy_core_writeStackToNbt(NbtCompound nbt, CallbackInfo ci){
        if (!fzzy_core_modifierHolder.isEmpty()){
            AbstractModifierHelper.TemporaryModifiers.INSTANCE.removeTemporaryModifiersFromNbt(fzzy_core_modifierHolder);
            NbtCompound nbtCompound = new NbtCompound();
            fzzy_core_modifierHolder.writeNbt(nbtCompound);
            nbt.put("modifier_holder",nbtCompound);
        }
    }

    @Inject(method = "readCustomDataFromNbt", at = @At("TAIL"))
    private void fzzy_core_readStackFromNbt(NbtCompound nbt, CallbackInfo ci){
        if (nbt.contains("modifier_holder")){
            NbtCompound nbtCompound = nbt.getCompound("modifier_holder");
            fzzy_core_modifierHolder = ItemStack.fromNbt(nbtCompound);
        }
    }
}
