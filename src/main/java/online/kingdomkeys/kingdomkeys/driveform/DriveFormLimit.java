package online.kingdomkeys.kingdomkeys.driveform;

import net.minecraft.entity.monster.MonsterEntity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.entity.player.ServerPlayerEntity;
import net.minecraft.util.ResourceLocation;
import net.minecraftforge.event.entity.living.LivingAttackEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;
import online.kingdomkeys.kingdomkeys.KingdomKeys;
import online.kingdomkeys.kingdomkeys.capability.IPlayerCapabilities;
import online.kingdomkeys.kingdomkeys.capability.ModCapabilities;
import online.kingdomkeys.kingdomkeys.config.ModConfigs;
import online.kingdomkeys.kingdomkeys.lib.Strings;
import online.kingdomkeys.kingdomkeys.network.PacketHandler;
import online.kingdomkeys.kingdomkeys.network.stc.SCSyncCapabilityPacket;

@Mod.EventBusSubscriber(modid = KingdomKeys.MODID)
public class DriveFormLimit extends DriveForm {

	public DriveFormLimit(String registryName, int order, ResourceLocation skinRL, boolean hasKeychain) {
		super(registryName, order, hasKeychain);
		this.color = new float[] { 0.6F, 0.3F, 1F };
		this.skinRL = skinRL;
	}
	
	@Override
	public String getBaseAbilityForLevel(int driveFormLevel) {
		switch (driveFormLevel) {
		case 1:
			return "";
		case 2:
			return Strings.autoLimit;
		case 3:
			return Strings.dodgeRoll;
		case 4:
			return Strings.treasureMagnet;
		case 5:
			return Strings.dodgeRoll;
		case 6:
			return "";
		case 7:
			return Strings.dodgeRoll;
		}
		return null;	
	}

	@Override
	public String getDFAbilityForLevel(int driveFormLevel) {
		switch (driveFormLevel) {
		case 1:
			return Strings.dodgeRoll;
		case 2:
			return "";
		case 3:
			return Strings.dodgeRoll;
		case 4:
			return "";
		case 5:
			return Strings.dodgeRoll;
		case 6:
			return "";
		case 7:
			return Strings.dodgeRoll;
		}
		return null;
	}
	
	@SubscribeEvent
	public static void getLimitFormXP(LivingAttackEvent event) {
		if (!event.getEntity().world.isRemote && event.getEntityLiving() instanceof MonsterEntity) {
			if (event.getSource().getTrueSource() instanceof PlayerEntity) {
				PlayerEntity player = (PlayerEntity) event.getSource().getTrueSource();
				IPlayerCapabilities playerData = ModCapabilities.getPlayer(player);
				if (playerData != null && playerData.getActiveDriveForm().equals(Strings.Form_Limit) && playerData.hasShotMaxShotlock()) {
					double mult = Double.parseDouble(ModConfigs.driveFormXPMultiplier.get(2).split(",")[1]);
					playerData.setDriveFormExp(player, playerData.getActiveDriveForm(), (int) (playerData.getDriveFormExp(playerData.getActiveDriveForm()) + (1*mult)));
					playerData.setHasShotMaxShotlock(false);
					PacketHandler.sendTo(new SCSyncCapabilityPacket(playerData), (ServerPlayerEntity)player);
				}
			}
		}
	}	
}