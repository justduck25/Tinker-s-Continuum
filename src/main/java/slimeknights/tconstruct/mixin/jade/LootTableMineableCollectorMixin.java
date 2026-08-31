package slimeknights.tconstruct.mixin.jade;

import net.minecraft.core.HolderLookup;
import net.minecraft.core.registries.Registries;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(targets = "snownee.jade.addon.harvest.LootTableMineableCollector", remap = false)
public class LootTableMineableCollectorMixin {
  @Inject(method = "onTagsUpdated", at = @At("HEAD"), cancellable = true)
  private static void tconstruct$skipUntilLootTablesExist(HolderLookup.Provider lookupProvider, boolean client, CallbackInfo ci) {
    if (lookupProvider.lookup(Registries.LOOT_TABLE).isEmpty()) {
      ci.cancel();
    }
  }
}
