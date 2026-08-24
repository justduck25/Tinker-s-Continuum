package slimeknights.tconstruct.plugin.jade;

import net.minecraft.ChatFormatting;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.Identifier;
import net.neoforged.neoforge.fluids.FluidStack;
import net.neoforged.neoforge.fluids.capability.IFluidHandler;
import slimeknights.tconstruct.TConstruct;
import slimeknights.tconstruct.smeltery.block.CastingTankBlock;
import slimeknights.tconstruct.smeltery.block.FluidCannonBlock;
import slimeknights.tconstruct.smeltery.block.ProxyTankBlock;
import slimeknights.tconstruct.smeltery.block.SearedLanternBlock;
import slimeknights.tconstruct.smeltery.block.component.SearedTankBlock;
import slimeknights.tconstruct.smeltery.block.entity.ITankBlockEntity;
import slimeknights.tconstruct.smeltery.block.entity.ProxyTankBlockEntity;
import snownee.jade.api.BlockAccessor;
import snownee.jade.api.IBlockComponentProvider;
import snownee.jade.api.ITooltip;
import snownee.jade.api.IWailaClientRegistration;
import snownee.jade.api.IWailaPlugin;
import snownee.jade.api.WailaPlugin;
import snownee.jade.api.config.IPluginConfig;

/** Jade plugin for Tinkers' Construct block tooltips. */
@WailaPlugin
public class TConstructJadePlugin implements IWailaPlugin {
  private static final TankProvider TANK_PROVIDER = new TankProvider();

  @Override
  public void registerClient(IWailaClientRegistration registration) {
    registration.addConfig(TankProvider.UID, true);
    registration.registerBlockComponent(TANK_PROVIDER, SearedTankBlock.class);
    registration.registerBlockComponent(TANK_PROVIDER, SearedLanternBlock.class);
    registration.registerBlockComponent(TANK_PROVIDER, FluidCannonBlock.class);
    registration.registerBlockComponent(TANK_PROVIDER, CastingTankBlock.class);
    registration.registerBlockComponent(TANK_PROVIDER, ProxyTankBlock.class);
  }

  private static class TankProvider implements IBlockComponentProvider {
    private static final Identifier UID = TConstruct.getResource("tank_fluid");

    @Override
    public Identifier getUid() {
      return UID;
    }

    @Override
    public void appendTooltip(ITooltip tooltip, BlockAccessor accessor, IPluginConfig config) {
      IFluidHandler tank = getTank(accessor);
      if (tank == null || tank.getTanks() <= 0) {
        return;
      }
      int capacity = tank.getTankCapacity(0);
      FluidStack fluid = tank.getFluidInTank(0);
      if (fluid.isEmpty()) {
        tooltip.add(Component.translatable("jade.tconstruct.tank.empty", capacity).withStyle(ChatFormatting.GRAY));
      } else {
        tooltip.add(Component.translatable("jade.tconstruct.tank.fluid", fluid.getHoverName(), fluid.getAmount(), capacity).withStyle(ChatFormatting.GRAY));
      }
    }

    private static IFluidHandler getTank(BlockAccessor accessor) {
      if (accessor.getBlockEntity() instanceof ITankBlockEntity tank) {
        return tank.getTank();
      }
      if (accessor.getBlockEntity() instanceof ProxyTankBlockEntity tank) {
        return tank.getItemTank();
      }
      return null;
    }
  }
}
