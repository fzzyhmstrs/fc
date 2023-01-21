package me.fzzyhmstrs.fzzy_core.mixins;

import com.llamalad7.mixinextras.injector.wrapoperation.Operation;
import com.llamalad7.mixinextras.injector.wrapoperation.WrapOperation;
import me.fzzyhmstrs.fzzy_core.interfaces.Modifiable;
import net.minecraft.client.item.TooltipContext;
import net.minecraft.item.Item;
import net.minecraft.item.ItemConvertible;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NbtCompound;
import net.minecraft.text.Text;
import net.minecraft.world.World;
import org.jetbrains.annotations.Nullable;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import java.util.List;

@Mixin(ItemStack.class)
public abstract class ItemStackMixin {

    @Shadow public abstract Item getItem();

    @Shadow public abstract int getMaxDamage();

    @Shadow public @Nullable abstract NbtCompound getNbt();

    @Shadow public abstract NbtCompound getOrCreateNbt();


    @Inject(method = "<init>(Lnet/minecraft/nbt/NbtCompound;)V", at = @At("TAIL"))
    private void fzzy_core_initializeFromNbt(NbtCompound nbt, CallbackInfo ci){
        if (getItem() == null) return;
        if (getItem() instanceof Modifiable modifiableItem){
            modifiableItem.getModifierInitializer().initializeModifiers((ItemStack) (Object) this, getOrCreateNbt(), modifiableItem.defaultModifiers());
        }
    }

    @Inject(method = "<init>(Lnet/minecraft/item/ItemConvertible;I)V", at = @At("TAIL"))
    private void fzzy_core_initializeFromItem(ItemConvertible item, int count, CallbackInfo ci){
        if (getItem() == null) return;
        if (getItem() instanceof Modifiable modifiableItem){
            modifiableItem.getModifierInitializer().initializeModifiers((ItemStack) (Object) this, getOrCreateNbt(), modifiableItem.defaultModifiers());
        }
    }

    @WrapOperation(method = "getTooltip", at = @At(value = "INVOKE", target = "net/minecraft/item/Item.appendTooltip (Lnet/minecraft/item/ItemStack;Lnet/minecraft/world/World;Ljava/util/List;Lnet/minecraft/client/item/TooltipContext;)V"))
    private void fzzy_core_appendModifiersToTooltip(Item instance,ItemStack stack, World world, List<Text> tooltip, TooltipContext context, Operation<Void> operation){
        operation.call(instance,stack, world, tooltip,context);
        if (stack.getItem() instanceof Modifiable modifiable){
            modifiable.addModifierTooltip(stack, tooltip, context);
        }
    }

}
