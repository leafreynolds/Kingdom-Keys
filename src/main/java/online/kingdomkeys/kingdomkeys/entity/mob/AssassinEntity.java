package online.kingdomkeys.kingdomkeys.entity.mob;

import net.minecraft.entity.EntityType;
import net.minecraft.entity.MobEntity;
import net.minecraft.entity.SpawnReason;
import net.minecraft.entity.ai.attributes.AttributeModifierMap;
import net.minecraft.entity.ai.attributes.Attributes;
import net.minecraft.entity.ai.goal.LookAtGoal;
import net.minecraft.entity.ai.goal.LookRandomlyGoal;
import net.minecraft.entity.ai.goal.MeleeAttackGoal;
import net.minecraft.entity.ai.goal.MoveTowardsRestrictionGoal;
import net.minecraft.entity.ai.goal.NearestAttackableTargetGoal;
import net.minecraft.entity.ai.goal.WaterAvoidingRandomWalkingGoal;
import net.minecraft.entity.merchant.villager.VillagerEntity;
import net.minecraft.entity.monster.MonsterEntity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.world.IWorld;
import net.minecraft.world.World;
import net.minecraftforge.fml.network.FMLPlayMessages;
import online.kingdomkeys.kingdomkeys.capability.ModCapabilities;
import online.kingdomkeys.kingdomkeys.entity.EntityHelper;
import online.kingdomkeys.kingdomkeys.entity.ModEntities;
import online.kingdomkeys.kingdomkeys.entity.mob.goal.AssassinGoal;

public class AssassinEntity extends BaseKHEntity {

	public AssassinEntity(EntityType<? extends BaseKHEntity> type, World worldIn) {
		super(type, worldIn);
	}

	public AssassinEntity(FMLPlayMessages.SpawnEntity spawnEntity, World world) {
		super(ModEntities.TYPE_ASSASSIN.get(), world);
	}

	@Override
    public boolean canSpawn(IWorld worldIn, SpawnReason spawnReasonIn) {
    	return ModCapabilities.getWorld((World)worldIn).getHeartlessSpawnLevel() > 0;
    }
	
	@Override
	protected void registerGoals() {
		//this.goalSelector.addGoal(1, new MeleeAttackGoal(this, 1.0D, true));
        this.goalSelector.addGoal(4, new MeleeAttackGoal(this, 1.0D, true));
        this.goalSelector.addGoal(5, new MoveTowardsRestrictionGoal(this, 1.0D));
        this.goalSelector.addGoal(7, new WaterAvoidingRandomWalkingGoal(this, 1.0D));
        this.goalSelector.addGoal(8, new LookAtGoal(this, PlayerEntity.class, 8.0F));
        this.goalSelector.addGoal(8, new LookRandomlyGoal(this));
        this.targetSelector.addGoal(1, new NearestAttackableTargetGoal<>(this, PlayerEntity.class, true));
		this.targetSelector.addGoal(2, new NearestAttackableTargetGoal<>(this, VillagerEntity.class, true));
		//this.targetSelector.addGoal(3, new NearestAttackableTargetGoal<>(this, AnimalEntity.class, true));
		this.targetSelector.addGoal(4, new AssassinGoal(this));
	}


	public static AttributeModifierMap.MutableAttribute registerAttributes() {
		return MobEntity.registerAttributes()
				.createMutableAttribute(Attributes.FOLLOW_RANGE, 35.0D)
				.createMutableAttribute(Attributes.MOVEMENT_SPEED, 0.3D)
				.createMutableAttribute(Attributes.MAX_HEALTH, 140.0D)
				.createMutableAttribute(Attributes.KNOCKBACK_RESISTANCE, 1000.0D)
				.createMutableAttribute(Attributes.ATTACK_KNOCKBACK, 1.0D)
				.createMutableAttribute(Attributes.ATTACK_DAMAGE, 6.0D);
	}

	@Override
	public int getMaxSpawnedInChunk() {
		return 4;
	}

	@Override
	protected void registerData() {
		super.registerData();
		this.dataManager.register(EntityHelper.STATE, 0);
	}

	@Override
	public EntityHelper.MobType getMobType() {
		return EntityHelper.MobType.NOBODY;
	}

	@Override
	public boolean onLivingFall(float distance, float damageMultiplier) {
		return false;
	}

}
