package me.fzzyhmstrs.fzzy_core.mixins;

import me.fzzyhmstrs.fzzy_core.coding_util.AbstractConfigDisableEnchantment;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.network.ClientPlayNetworkHandler;
import net.minecraft.network.packet.s2c.play.GameJoinS2CPacket;
import net.minecraft.util.registry.Registry;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(ClientPlayNetworkHandler.class)
public class ClientPlayNetworkHandlerMixin {

    @Shadow @Final private MinecraftClient client;

    @Inject(method = "onGameJoin", at = @At("TAIL"))
    private void fzzy_core_recheckEnchantmentEnable(GameJoinS2CPacket packet, CallbackInfo ci){
        if (!client.isIntegratedServerRunning()) {
            Registry.ENCHANTMENT.stream().sequential().forEach((enchant) -> {
                if (enchant instanceof AbstractConfigDisableEnchantment) {
                    ((AbstractConfigDisableEnchantment) enchant).updateEnabled();
                }
            });
        }
    }

}
