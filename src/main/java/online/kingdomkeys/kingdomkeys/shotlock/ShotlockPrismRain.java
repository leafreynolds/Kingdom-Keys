package online.kingdomkeys.kingdomkeys.shotlock;

import java.util.List;

import net.minecraft.entity.Entity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.entity.player.ServerPlayerEntity;
import net.minecraftforge.fml.common.Mod;
import online.kingdomkeys.kingdomkeys.KingdomKeys;
import online.kingdomkeys.kingdomkeys.capability.IPlayerCapabilities;
import online.kingdomkeys.kingdomkeys.capability.ModCapabilities;
import online.kingdomkeys.kingdomkeys.config.ModConfigs;
import online.kingdomkeys.kingdomkeys.entity.shotlock.PrismRainCoreEntity;
import online.kingdomkeys.kingdomkeys.lib.DamageCalculation;
import online.kingdomkeys.kingdomkeys.network.PacketHandler;
import online.kingdomkeys.kingdomkeys.network.stc.SCSyncCapabilityPacket;

@Mod.EventBusSubscriber(modid = KingdomKeys.MODID)
public class ShotlockPrismRain extends Shotlock {

	public ShotlockPrismRain(String registryName, int order, int cooldown, int max) {
		super(registryName, order, cooldown, max);
	}

	@Override
	public void onUse(PlayerEntity player, List<Entity> targetList) {
		IPlayerCapabilities playerData = ModCapabilities.getPlayer(player);
		playerData.setLimitCooldownTicks(cooldown);
		PacketHandler.sendTo(new SCSyncCapabilityPacket(playerData), (ServerPlayerEntity)player);
		float damage = (float) (DamageCalculation.getMagicDamage(player) * ModConfigs.shotlockMult);
		PrismRainCoreEntity core = new PrismRainCoreEntity(player.world, player, targetList, damage);
		core.setPosition(player.getPosX(), player.getPosY(), player.getPosZ());
		player.world.addEntity(core);
	}
}