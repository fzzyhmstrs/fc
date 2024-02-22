package me.fzzyhmstrs.fzzy_core.mixins;

import me.fzzyhmstrs.fzzy_core.FC;
import net.minecraft.advancement.Advancement;
import net.minecraft.advancement.PlayerAdvancementTracker;
import net.minecraft.server.network.ServerPlayerEntity;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(PlayerAdvancementTracker.class)
public class PlayerAdvancementTrackerMixin {

    @Shadow private ServerPlayerEntity owner;

    @Inject(method = "grantCriterion", at = @At(value = "INVOKE", target = "net/minecraft/advancement/PlayerAdvancementTracker.onStatusUpdate (Lnet/minecraft/advancement/Advancement;)V", shift = At.Shift.AFTER))
    private void fzzy_core_advancementCompletionCriterionHook(Advancement advancement, String criterionName, CallbackInfoReturnable<Boolean> cir){
        FC.INSTANCE.getADVANCEMENT_COMPLETION().trigger(owner,advancement);
    }


}
