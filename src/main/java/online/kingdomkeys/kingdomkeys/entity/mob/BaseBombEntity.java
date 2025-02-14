package online.kingdomkeys.kingdomkeys.entity.mob;

import net.minecraft.entity.EntityType;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.MobEntity;
import net.minecraft.entity.SpawnReason;
import net.minecraft.entity.ai.attributes.AttributeModifierMap;
import net.minecraft.entity.ai.attributes.Attributes;
import net.minecraft.entity.ai.goal.Goal;
import net.minecraft.entity.ai.goal.HurtByTargetGoal;
import net.minecraft.entity.ai.goal.LeapAtTargetGoal;
import net.minecraft.entity.ai.goal.LookAtGoal;
import net.minecraft.entity.ai.goal.LookRandomlyGoal;
import net.minecraft.entity.ai.goal.MeleeAttackGoal;
import net.minecraft.entity.ai.goal.NearestAttackableTargetGoal;
import net.minecraft.entity.ai.goal.SwimGoal;
import net.minecraft.entity.ai.goal.WaterAvoidingRandomWalkingGoal;
import net.minecraft.entity.merchant.villager.VillagerEntity;
import net.minecraft.entity.monster.MonsterEntity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.network.IPacket;
import net.minecraft.network.PacketBuffer;
import net.minecraft.util.DamageSource;
import net.minecraft.util.ResourceLocation;
import net.minecraft.world.Explosion;
import net.minecraft.world.IWorld;
import net.minecraft.world.World;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;
import net.minecraftforge.event.ForgeEventFactory;
import net.minecraftforge.fml.common.registry.IEntityAdditionalSpawnData;
import net.minecraftforge.fml.network.FMLPlayMessages;
import net.minecraftforge.fml.network.NetworkHooks;
import online.kingdomkeys.kingdomkeys.capability.ModCapabilities;
import online.kingdomkeys.kingdomkeys.config.ModConfigs;
import online.kingdomkeys.kingdomkeys.entity.EntityHelper;
import online.kingdomkeys.kingdomkeys.entity.magic.FireEntity;

public abstract class BaseBombEntity extends BaseKHEntity implements IEntityAdditionalSpawnData {

    public int ticksToExplode;

    protected BaseBombEntity(EntityType<? extends MonsterEntity> type, World worldIn) {
        super(type, worldIn);
        this.ticksToExplode = 100;
    }

    public BaseBombEntity(EntityType<? extends MonsterEntity> type, FMLPlayMessages.SpawnEntity spawnEntity, World world) {
        super(type, world);
    }
    
    @Override
    public boolean canSpawn(IWorld worldIn, SpawnReason spawnReasonIn) {
    	return ModCapabilities.getWorld((World)worldIn).getHeartlessSpawnLevel() > 0;
    }

    @OnlyIn(Dist.CLIENT)
    public abstract ResourceLocation getTexture();

    public abstract float getExplosionStength();

    public static AttributeModifierMap.MutableAttribute registerAttributes() {
        return MobEntity.registerAttributes()
                .createMutableAttribute(Attributes.FOLLOW_RANGE, 35.0D)
                .createMutableAttribute(Attributes.MOVEMENT_SPEED, 0.4D)
                .createMutableAttribute(Attributes.ATTACK_DAMAGE, 1.0D)
				.createMutableAttribute(Attributes.ATTACK_KNOCKBACK, 1.0D)
                ;
    }

    @Override
    protected void registerGoals() {
        super.registerGoals();
        this.goalSelector.addGoal(0, new BombGoal(this));
        this.goalSelector.addGoal(0, new SwimGoal(this));
        this.goalSelector.addGoal(3, new LeapAtTargetGoal(this, 0.4F));
        this.goalSelector.addGoal(4, new MeleeAttackGoal(this, 1.0D, true));
        this.goalSelector.addGoal(7, new WaterAvoidingRandomWalkingGoal(this, 1.0D));
        this.goalSelector.addGoal(8, new LookAtGoal(this, PlayerEntity.class, 8.0F));
        this.goalSelector.addGoal(8, new LookRandomlyGoal(this));
        this.targetSelector.addGoal(0, new HurtByTargetGoal(this));
        this.targetSelector.addGoal(1, new NearestAttackableTargetGoal<>(this, PlayerEntity.class, true));
		this.targetSelector.addGoal(2, new NearestAttackableTargetGoal<>(this, VillagerEntity.class, true));

    }

    @Override
    public void writeSpawnData(PacketBuffer buffer) {
        buffer.writeInt(ticksToExplode);
    }

    @Override
    public void readSpawnData(PacketBuffer additionalData) {
        ticksToExplode = additionalData.readInt();
    }

    @Override
    public IPacket<?> createSpawnPacket() {
        return NetworkHooks.getEntitySpawningPacket(this);
    }

    @Override
    public boolean attackEntityFrom(DamageSource source, float amount) {
        if(!this.world.isRemote) {
            if (ModConfigs.bombExplodeWithfire && (isBurning() || source.getImmediateSource() instanceof FireEntity)) {
                explode();
            }
        }
        return super.attackEntityFrom(source, amount);
    }

    @Override
    public void tick() {
        if (EntityHelper.getState(this) == 1) 
        	ticksToExplode--;
        super.tick();
    }

    boolean hasExploded = false;

    public void explode() {
        if (!hasExploded) {
            Explosion.Mode explosion$mode = ForgeEventFactory.getMobGriefingEvent(this.world, this) ? Explosion.Mode.DESTROY : Explosion.Mode.NONE;
            this.world.createExplosion(this, this.getPosX(), this.getPosY(), this.getPosZ(), getExplosionStength(), false, explosion$mode);
            for (LivingEntity enemy : EntityHelper.getEntitiesNear(this, getExplosionStength()+1))
                this.attackEntityAsMob(enemy);
            this.remove();
            hasExploded = true;
        }
    }

    @Override
    public EntityHelper.MobType getMobType() {
        return EntityHelper.MobType.HEARTLESS_EMBLEM;
    }

    @Override
    protected void registerData() {
        super.registerData();
        this.dataManager.register(EntityHelper.STATE, 0);
    }

    class BombGoal extends Goal {
        private BaseBombEntity bomb;

        public BombGoal(BaseBombEntity bomb) {
            this.bomb = bomb;
        }

        @Override
        public boolean shouldExecute() {
            return bomb.getAttackTarget() != null && bomb.getDistanceSq(bomb.getAttackTarget()) < 64 && bomb.getHealth() < bomb.getMaxHealth();
        }

        @Override
        public boolean shouldContinueExecuting() {
            if (shouldExecute()) {
                EntityHelper.setState(bomb, 1);
                bomb.getAttribute(Attributes.MOVEMENT_SPEED).setBaseValue(0.8D);
                if (bomb.ticksToExplode <= 0) {
                    bomb.explode();
                }
            }
            return false;
        }

    }
}
