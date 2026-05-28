package org.agmas.noellesroles.mixin.framing;

import dev.doctor4t.wathe.cca.GameWorldComponent;
import dev.doctor4t.wathe.cca.PlayerPoisonComponent;
import net.minecraft.entity.player.PlayerEntity;
import org.agmas.noellesroles.Noellesroles;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import java.util.UUID;

@Mixin(PlayerPoisonComponent.class)
public abstract class DoNotApplyFakePoisonMixin {
    //属于旧 fake poison 兼容思路，继续挂回去反而会和现在的新托盘逻辑冲突，所以不需要再加回mixin.json里面

    @Shadow @Final private PlayerEntity player;

    @Shadow public int poisonTicks;


    @Shadow public abstract void reset();

    @Shadow public UUID poisoner;

    @Inject(method = "serverTick", at = @At("HEAD"), cancellable = true)
    private void defenseVialApply(CallbackInfo ci) {
        GameWorldComponent gameWorldComponent = GameWorldComponent.KEY.get(player.getWorld());
        if (poisoner == null) return;
        PlayerEntity poisonerPlayer = player.getWorld().getPlayerByUuid(poisoner);
        if (poisonerPlayer == null) return;
        if (!gameWorldComponent.canUseKillerFeatures(poisonerPlayer)) {
            if (gameWorldComponent.getRole(poisonerPlayer) == null) {
                reset();
                ci.cancel();
                return;
            }
            if (gameWorldComponent.getRole(poisonerPlayer).identifier().getNamespace().equals(Noellesroles.MOD_ID)) { // Don't interfere with any custom non-killer poisoning roles from other mods
                if (poisonTicks <= 5) {
                    reset();
                    ci.cancel();
                }
            }
        }
    }
}
