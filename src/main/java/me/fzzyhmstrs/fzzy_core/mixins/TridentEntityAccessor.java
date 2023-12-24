package me.fzzyhmstrs.fzzy_core.mixins;

import net.minecraft.entity.projectile.TridentEntity;
import net.minecraft.item.ItemStack;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.gen.Accessor;

@Mixin(TridentEntity.class)
public interface TridentEntityAccessor {
    @Accessor
    ItemStack getTridentStack();

    @Accessor
    void setTridentStack(ItemStack tridentStack);
}
