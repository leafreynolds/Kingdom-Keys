package online.kingdomkeys.kingdomkeys.magic;

import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.util.Hand;
import online.kingdomkeys.kingdomkeys.entity.magic.MagnegaEntity;
import online.kingdomkeys.kingdomkeys.entity.magic.MagneraEntity;
import online.kingdomkeys.kingdomkeys.entity.magic.MagnetEntity;

public class MagicMagnet extends Magic {

	public MagicMagnet(String registryName, int maxLevel, boolean hasRC, int order) {
		super(registryName, false, maxLevel, hasRC, order);
	}

	@Override
	protected void magicUse(PlayerEntity player, PlayerEntity caster, int level, float fullMPBlastMult) {
		float dmg = /*ModCapabilities.getPlayer(player).isAbilityEquipped(Strings.waterBoost) ? getDamageMult(level) * 1.2F :*/ getDamageMult(level);
		dmg *= fullMPBlastMult;
		
		switch(level) {
		case 0:
			MagnetEntity magent = new MagnetEntity(player.world, player, dmg);
			magent.setCaster(player.getUniqueID());
			player.world.addEntity(magent);
			magent.setDirectionAndMovement(player, -90, player.rotationYaw, 0, 1F, 0);
			break;
		case 1:
			MagneraEntity magnera = new MagneraEntity(player.world, player, dmg);
			magnera.setCaster(player.getUniqueID());
			player.world.addEntity(magnera);
			magnera.setDirectionAndMovement(player, -90, player.rotationYaw, 0, 1F, 0);
			break;
		case 2:
			MagnegaEntity magnega = new MagnegaEntity(player.world, player, dmg);
			magnega.setCaster(player.getUniqueID());
			player.world.addEntity(magnega);
			magnega.setDirectionAndMovement(player, -90, player.rotationYaw, 0, 1F, 0);
			break;
		}
		
		player.swingArm(Hand.MAIN_HAND);
	}

}
