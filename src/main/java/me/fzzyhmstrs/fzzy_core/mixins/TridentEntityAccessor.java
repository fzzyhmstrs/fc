package me.fzzyhmstrs.fzzy_core.mixins;

import net.minecraft.entity.data.TrackedData;
import net.minecraft.entity.projectile.TridentEntity;
import net.minecraft.item.ItemStack;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.gen.Accessor;

@Mixin(TridentEntity.class)
public interface TridentEntityAccessor {
    @Accessor
    static TrackedData<Byte> getLOYALTY() {
        throw new UnsupportedOperationException();
    }

    @Accessor
    static TrackedData<Boolean> getENCHANTED() {
        throw new UnsupportedOperationException();
    }

    @Accessor
    ItemStack getTridentStack();

    @Accessor
    void setTridentStack(ItemStack tridentStack);


    @Accessor
    boolean isDealtDamage();

    @Accessor
    void setDealtDamage(boolean dealtDamage);
}
