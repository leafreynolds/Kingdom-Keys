package online.kingdomkeys.kingdomkeys.config;

import java.util.List;

import com.google.common.collect.Lists;

import net.minecraftforge.common.ForgeConfigSpec;
import online.kingdomkeys.kingdomkeys.KingdomKeys;
import online.kingdomkeys.kingdomkeys.entity.SpawningMode;


/**
 * Config file for config options shared between the server and the client
 */
public class CommonConfig {

    public ForgeConfigSpec.EnumValue<SpawningMode> heartlessSpawningMode;
    public ForgeConfigSpec.ConfigValue<List<? extends String>> moogleSpawnRate;
    public ForgeConfigSpec.ConfigValue<List<? extends String>> mobSpawnRate;
    
    public ForgeConfigSpec.BooleanValue mobLevelingUp;

    public ForgeConfigSpec.BooleanValue oreGen;
    public ForgeConfigSpec.BooleanValue bloxGen;

    public ForgeConfigSpec.BooleanValue debugConsoleOutput;
    public ForgeConfigSpec.BooleanValue bombExplodeWithFire;
    public ForgeConfigSpec.BooleanValue keybladeOpenDoors;
    
    public ForgeConfigSpec.IntValue driveHeal;
    
    public ForgeConfigSpec.DoubleValue drivePointsMultiplier;
    public ForgeConfigSpec.DoubleValue focusPointsMultiplier;
    
    public ForgeConfigSpec.DoubleValue limitLaserCircleMult;
    public ForgeConfigSpec.DoubleValue limitLaserDomeMult;
    public ForgeConfigSpec.DoubleValue limitArrowRainMult;
    
    public ForgeConfigSpec.IntValue hpDropProbability;
    public ForgeConfigSpec.IntValue mpDropProbability;
    public ForgeConfigSpec.IntValue munnyDropProbability;
    public ForgeConfigSpec.IntValue driveDropProbability;
    public ForgeConfigSpec.IntValue focusDropProbability;

    public ForgeConfigSpec.BooleanValue blizzardChangeBlocks;
    public ForgeConfigSpec.BooleanValue playerSpawnHeartless;
    public ForgeConfigSpec.ConfigValue<List<? extends String>> playerSpawnHeartlessData;
    
    public ForgeConfigSpec.DoubleValue shotlockMult;
    public ForgeConfigSpec.DoubleValue critMult;

    public ForgeConfigSpec.IntValue mobLevelStats;
    
    CommonConfig(final ForgeConfigSpec.Builder builder) {    	
		builder.push("general");

        oreGen = builder
                .comment("Allow Synthesis Materials ores to generate")
                .translation(KingdomKeys.MODID + ".config.ore_gen")
                .define("oreGen", true);

        bloxGen = builder
                .comment("Allow Blox to generate")
                .translation(KingdomKeys.MODID + ".config.blox_gen")
                .define("bloxGen", true);

        debugConsoleOutput = builder
                .comment("Enable debug console output")
                .translation(KingdomKeys.MODID + ".config.debug")
                .define("debugConsoleOutput", false);
        
        bombExplodeWithFire = builder
                .comment("Allow Bomb heartless to explode when lit on fire")
                .translation(KingdomKeys.MODID + ".config.bomb_explode_with_fire")
                .define("bombExplodeWithfire", true);
        
        blizzardChangeBlocks = builder
                .comment("Allow Blizzard to turn lava into obsidian and freeze water")
                .translation(KingdomKeys.MODID + ".config.blizzard_change_blocks")
                .define("blizzardChangeBlocks", true);
        
        keybladeOpenDoors = builder
                .comment("Allow keyblades to open iron doors with right click")
                .translation(KingdomKeys.MODID + ".config.keyblade_open_doors")
                .define("keybladeOpenDoors", true);

        driveHeal = builder
                .comment("Health % restored when using a drive form")
                .translation(KingdomKeys.MODID + ".config.drive_heal")
                .defineInRange("driveHeal",50,0,100);
        
        drivePointsMultiplier = builder
                .comment("Drive Points Drop Multiplier")
                .translation(KingdomKeys.MODID + ".config.drive_points_multiplier")
                .defineInRange("drivePointsMultiplier",1.0,0,100);
        
        focusPointsMultiplier = builder
                .comment("Focus Points Drop Multiplier")
                .translation(KingdomKeys.MODID + ".config.focus_points_multiplier")
                .defineInRange("focusPointsMultiplier",1.0,0,100);
        
        critMult = builder
                .comment("Critic Damage Multiplier")
                .translation(KingdomKeys.MODID + ".config.crit_mult")
                .defineInRange("critMult",1.5,0,100);
        
        builder.pop();

        builder.push("spawning");

        heartlessSpawningMode = builder
                .comment("Heartless spawning mode: NEVER, ALWAYS, AFTER_KEYCHAIN (after the first keychain is synthesized), AFTER_DRAGON (after the Ender Dragon is defeated)")
                .translation(KingdomKeys.MODID + ".config.heartless_spawning_mode")
                .defineEnum("heartlessSpawningMode", SpawningMode.AFTER_KEYCHAIN);

        moogleSpawnRate = builder
                .comment("Mob Spawn [weight, min, max]")
                .translation(KingdomKeys.MODID + ".config.moogle_spawn")
                .defineList("moogleSpawn", Lists.newArrayList("Moogle,2,0,1", "Enemies,200,1,5"), o -> o instanceof String);
        
        mobSpawnRate = builder
                .comment("Mob Spawn chance in percentage [type, chance] (if they don't add up to 100 there would be enemies not spawning)")
                .translation(KingdomKeys.MODID + ".config.mob_spawn")
                .defineList("mobSpawn", Lists.newArrayList("Pureblood,35", "Emblem,35", "Nobody,30"), o -> o instanceof String);

        playerSpawnHeartless = builder
                .comment("Allow a heartless and a nobody to spawn when a player gets killed by a heartless")
                .translation(KingdomKeys.MODID + ".config.player_spawn_heartless")
                .define("playerSpawnHeartless", true);
        
        mobLevelingUp = builder
                .comment("Allow heartless and nobodies to spawn with levels according to players")
                .translation(KingdomKeys.MODID + ".config.player_mob_leveling_up")
                .define("mobLevelingUp", true);
        
        playerSpawnHeartlessData = builder
                .comment("Heartless and nobody stats: name, hp (% of the player's), strength (% of the player's)")
                .translation(KingdomKeys.MODID + ".config.player_spawn_heartless_Data")
                .defineList("playerSpawnHeartlessData", Lists.newArrayList("Heartless,100,100", "Nobody,100,100"), o -> o instanceof String);
        
        mobLevelStats = builder
                .comment("Mob base stats multiplier out of 100% (default 10)")
                .translation(KingdomKeys.MODID + ".config.mob_level_stats")
                .defineInRange("mobLevelStats",10,0,100);
        
        builder.pop();
        
        builder.push("drops");
        
        hpDropProbability = builder
                .comment("HP Drops Probability")
                .translation(KingdomKeys.MODID + ".config.hp_drop_probability")
                .defineInRange("hpDropProbability",80,0,100);
        
        mpDropProbability = builder
                .comment("MP Drops Probability")
                .translation(KingdomKeys.MODID + ".config.mp_drop_probability")
                .defineInRange("mpDropProbability",80,0,100);
        
        munnyDropProbability = builder
                .comment("Munny Drops Probability")
                .translation(KingdomKeys.MODID + ".config.munny_drop_probability")
                .defineInRange("munnyDropProbability",80,0,100);
        
        driveDropProbability = builder
                .comment("Drive Drops Probability")
                .translation(KingdomKeys.MODID + ".config.drive_drop_probability")
                .defineInRange("driveDropProbability",80,0,100);
        
        focusDropProbability = builder
                .comment("Focus Drops Probability")
                .translation(KingdomKeys.MODID + ".config.focus_drop_probability")
                .defineInRange("focusDropProbability",80,0,100);
        
        builder.pop();
        
        builder.push("limits");
        
        limitLaserCircleMult = builder
                .comment("Laser Circle Damage Multiplier ((strength + magic) / 2 * multiplier)")
                .translation(KingdomKeys.MODID + ".config.laser_circle_mult")
                .defineInRange("laserCircleMult",2.0,0,100);
        
        limitLaserDomeMult = builder
                .comment("Laser Dome Damage Multiplier ((strength + magic) / 2 * multiplier)")
                .translation(KingdomKeys.MODID + ".config.laser_dome_mult")
                .defineInRange("laserDomeMult",0.3,0,100);

        limitArrowRainMult = builder
                .comment("Arrow Rain Damage Multiplier ((strength + magic) / 2 * multiplier)")
                .translation(KingdomKeys.MODID + ".config.arrow_rain_mult")
                .defineInRange("arrowRainMult",2.0,0,100);
        
        builder.pop();
        
        builder.push("shotlock");
        
        shotlockMult = builder
                .comment("Shotlock Damage Multiplier (magic * multiplier)")
                .translation(KingdomKeys.MODID + ".config.shotlock_mult")
                .defineInRange("shotlockMult",0.4,0,100);
        
        builder.pop();
    }
  
}
