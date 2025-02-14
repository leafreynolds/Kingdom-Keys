package online.kingdomkeys.kingdomkeys.client.gui;

import com.mojang.blaze3d.matrix.MatrixStack;
import com.mojang.blaze3d.systems.RenderSystem;

import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.screen.Screen;
import net.minecraft.entity.Entity;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.Util;
import net.minecraft.util.math.vector.Vector3f;
import net.minecraft.util.text.TranslationTextComponent;
import net.minecraftforge.client.event.RenderGameOverlayEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import online.kingdomkeys.kingdomkeys.KingdomKeys;
import online.kingdomkeys.kingdomkeys.capability.IPlayerCapabilities;
import online.kingdomkeys.kingdomkeys.capability.ModCapabilities;
import online.kingdomkeys.kingdomkeys.config.ModConfigs;
import online.kingdomkeys.kingdomkeys.handler.InputHandler;
import online.kingdomkeys.kingdomkeys.lib.Strings;

public class LockOnGui extends Screen {
	int guiWidth = 256;
	int guiHeight = 256;

	int hpGuiWidth = 173;
	float hpBarWidth;
	float missingHpBarWidth;
	float hpPerBar;
	int hpBars;
	int currentBar;
	int oldBar;

	int hpGuiHeight = 10;
	int noborderguiwidth = 171;

	float lockOnScale;
	float hpBarScale;
	LivingEntity lastTrackedTarget;
	private float targetHealth;
	private long lastSystemTime;
	private float lastTargetHealth;

	public LockOnGui() {
		super(new TranslationTextComponent(""));
		minecraft = Minecraft.getInstance();
	}

	@SubscribeEvent
	public void onRenderOverlayPost(RenderGameOverlayEvent event) {
		PlayerEntity player = minecraft.player;
		MatrixStack matrixStack = event.getMatrixStack();
		IPlayerCapabilities playerData = ModCapabilities.getPlayer(player);
		if (playerData != null) {
			Entity target = InputHandler.lockOn;
			if (target == null) {
				missingHpBarWidth = 0;
				return;
			} else {
				if(player.getDistance(target) > 35){
					InputHandler.lockOn = null;
					return;
				}
				if (event.getType() == RenderGameOverlayEvent.ElementType.CROSSHAIRS) {
					event.setCanceled(true);
				}
				
				if (event.getType() == RenderGameOverlayEvent.ElementType.TEXT) {
					float size = 6;

					minecraft.textureManager.bindTexture(new ResourceLocation(KingdomKeys.MODID, "textures/gui/lockon_0.png"));

					int screenWidth = minecraft.getMainWindow().getScaledWidth();
					int screenHeight = minecraft.getMainWindow().getScaledHeight();

					lockOnScale = ModConfigs.lockOnIconScale/100F;
					hpBarScale = ModConfigs.lockOnHPScale/100F;
					
					// Icon
					matrixStack.push();
					{
						matrixStack.translate((screenWidth / 2) - (guiWidth / 2) * lockOnScale / size - 0.5F, (screenHeight / 2) - (guiHeight / 2) * lockOnScale / size - 0.5F, 0);
						matrixStack.scale(lockOnScale / size, lockOnScale / size, lockOnScale / size);
						this.blit(matrixStack, 0, 0, 0, 0, guiWidth, guiHeight);

						minecraft.textureManager.bindTexture(new ResourceLocation(KingdomKeys.MODID, "textures/gui/lockon_1.png"));
						matrixStack.translate(guiWidth / 2, guiWidth / 2, 0);
						matrixStack.rotate(Vector3f.ZP.rotation((player.ticksExisted % 360) * 0.2F));
						matrixStack.translate(-guiWidth / 2, -guiWidth / 2, 0);
						this.blit(matrixStack, 0, 0, 0, 0, guiWidth, guiHeight);
					}
					matrixStack.pop();

					minecraft.textureManager.bindTexture(new ResourceLocation(KingdomKeys.MODID, "textures/gui/hpbar.png"));

					matrixStack.push();

					//int[] scan = playerData.getEquippedAbilityLevel(Strings.scan);
					// If ability level > 0 and amount of equipped is > 0
					//if (target != null && scan[0] > 0 && scan[1] > 0) {
					if(target != null && playerData.isAbilityEquipped(Strings.scan)) {
						matrixStack.push();
						{
							RenderSystem.enableBlend();
							matrixStack.translate(ModConfigs.lockOnXPos, ModConfigs.lockOnYPos, 0);
							drawString(matrixStack, minecraft.fontRenderer, target.getName().getString(), screenWidth - minecraft.fontRenderer.getStringWidth(target.getName().getString()), (int) (26*hpBarScale), 0xFFFFFF);
							drawHPBar(event, (LivingEntity) target);
							RenderSystem.disableBlend();
						}
						matrixStack.pop();
					}

					matrixStack.scale(hpBarScale, hpBarScale, hpBarScale);
					matrixStack.pop();
				}
			}
		}
	}

	public void drawHPBar(RenderGameOverlayEvent event, LivingEntity target) {
		int screenWidth = minecraft.getMainWindow().getScaledWidth();
		MatrixStack matrixStack = event.getMatrixStack();

		if (event.getType() == RenderGameOverlayEvent.ElementType.TEXT) {
			minecraft.textureManager.bindTexture(new ResourceLocation(KingdomKeys.MODID, "textures/gui/hpbar.png"));

			hpPerBar = ModConfigs.lockOnHpPerBar;
			float widthMultiplier = 10 * hpBarScale;
			
			float targetHealth = Math.min(target.getHealth(), target.getMaxHealth());

			if (target.getMaxHealth() % hpPerBar == 0) {
				hpBars = (int) (target.getMaxHealth() / hpPerBar);
			} else {
				hpBars = (int) (target.getMaxHealth() / hpPerBar) + 1;
			}

			if (targetHealth % hpPerBar == 0) {
				currentBar = (int) (targetHealth / hpPerBar);
			} else {
				currentBar = (int) (targetHealth / hpPerBar) + 1;
			}

			float firstBar =  (target.getMaxHealth() > hpPerBar ? target.getMaxHealth() % hpPerBar : target.getMaxHealth());
			float oneBar = (target.getMaxHealth() > hpPerBar ? hpPerBar : target.getMaxHealth());// (int) (target.getMaxHealth() / hpBars);

			if (targetHealth % hpPerBar == 0 && targetHealth != 0) {
				hpBarWidth = oneBar * widthMultiplier;
			} else {
				hpBarWidth = (float) ((targetHealth % hpPerBar) * widthMultiplier);
			}

			float i = (targetHealth);
			long j = Util.milliTime();
			if (i < this.targetHealth && target.hurtResistantTime > 0) {
				this.lastSystemTime = j;
			} else if (i > this.targetHealth && target.hurtResistantTime > 0) {
				this.lastSystemTime = j;
			}

			if ((j - this.lastSystemTime > 1000L || this.targetHealth < targetHealth)) { // If 1 second since last attack has passed update variables
				this.targetHealth = i;
				this.lastTargetHealth = i;
				this.lastSystemTime = j;
				oldBar = currentBar;
				missingHpBarWidth = 0;
			}

			//Basically get the Max of the hp bar or 0 (so weird values don't show up) and then out of that a max of that and the missing hp of the bar(so it doesn't go above the limit)
			missingHpBarWidth = targetHealth % hpPerBar == 0 ? 0 : Math.min(Math.max(((lastTargetHealth - targetHealth)),0), hpPerBar - targetHealth % hpPerBar) % hpPerBar * widthMultiplier;
			float hpBarMaxWidth, bgHPBarMaxWidth = 0;
			
			// Background HP width
			if (target.getMaxHealth() >= hpPerBar) {
				if(targetHealth + hpPerBar > target.getMaxHealth() && currentBar == hpBars) { //If it's first bar
					hpBarMaxWidth = (firstBar * widthMultiplier);
					bgHPBarMaxWidth = hpPerBar * widthMultiplier;
				} else if(currentBar == 1) {//If it's the last bar
					hpBarMaxWidth = (oneBar * widthMultiplier);
					bgHPBarMaxWidth = 0;
				} else { //Middle bar in entity with hp > 20
					hpBarMaxWidth = (oneBar * widthMultiplier);
					bgHPBarMaxWidth = hpPerBar * widthMultiplier;
				}
			} else { //Target has less than 20 hp
				hpBarMaxWidth = (target.getMaxHealth() % hpPerBar) * widthMultiplier;
			}

			matrixStack.push();
			{
				drawHPBarBack(matrixStack, (screenWidth - hpBarMaxWidth - 4 * hpBarScale), 0 * hpBarScale, hpBarMaxWidth, hpBarScale, (screenWidth - bgHPBarMaxWidth - 4 * hpBarScale), bgHPBarMaxWidth);
				drawHPBarTop(matrixStack, (screenWidth - hpBarWidth - 2 * hpBarScale), 2 * hpBarScale, hpBarWidth, hpBarScale);
				drawDamagedHPBarTop(matrixStack, (screenWidth - hpBarWidth - missingHpBarWidth - 2 * hpBarScale), 2 * hpBarScale, missingHpBarWidth, hpBarScale, target);
				drawHPBars(matrixStack, (screenWidth - hpBarMaxWidth - 4 * hpBarScale), 0 * hpBarScale, hpBarMaxWidth, hpBarScale, target);
				drawDamagedHPBars(matrixStack, (screenWidth - hpBarMaxWidth - 4 * hpBarScale), 0 * hpBarScale, hpBarMaxWidth, hpBarScale, target);
			}
			matrixStack.pop();
		}
	}

	public void drawHPBarBack(MatrixStack matrixStack, float posX, float posY, float width, float scale, float bgPosX, float bgHPBarMaxWidth) {
		minecraft.textureManager.bindTexture(new ResourceLocation(KingdomKeys.MODID, "textures/gui/hpbar.png"));
		
		matrixStack.push();
		{
			matrixStack.translate(posX, posY, 0);

			//Green bg bar render
			matrixStack.push();
			{
				matrixStack.translate(bgPosX - posX, posY, 0);
				// Left Margin
				matrixStack.push();
				{
					matrixStack.scale(scale, scale, 0);
					blit(matrixStack, 0, 0, 0, 0, 2, 12);
				}
				matrixStack.pop();

				// Background
				matrixStack.push(); //Empty bg (last bar)
				{
					matrixStack.translate(2*scale, 0, 0);
					matrixStack.scale(bgHPBarMaxWidth, scale, 0);
					blit(matrixStack, 0, 0, 14, 0, 1, 12);
				}
				matrixStack.pop();

				// Right Margin
				matrixStack.push();
				{
					matrixStack.translate(2 * scale + bgHPBarMaxWidth, 0, 0);
					matrixStack.scale(scale, scale, 0);
					blit(matrixStack, 0, 0, 3, 0, 2, 12);
				}
				matrixStack.pop();
			}
			matrixStack.pop();

			//Normal bar render
			// Left Margin
			matrixStack.push();
			{
				matrixStack.scale(scale, scale, 0);
				blit(matrixStack, 0, 0, 0, 0, 2, 12);
			}
			matrixStack.pop();

			// Background
			matrixStack.push();
			{
				matrixStack.translate(2*scale, 0, 0);
				matrixStack.scale(width, scale, 0);
				blit(matrixStack, 0, 0, 2, 0, 1, 12);
			}
			matrixStack.pop();

			// Right Margin
			matrixStack.push();
			{
				matrixStack.translate(2 * scale + width, 0, 0);
				matrixStack.scale(scale, scale, 0);
				blit(matrixStack, 0, 0, 3, 0, 2, 12);
			}
			matrixStack.pop();
			
			// HP Icon
			matrixStack.push();
			{
				matrixStack.translate(width - 20*scale, 12*scale, 0);
				matrixStack.scale(scale, scale, 0);
				blit(matrixStack, 1, 0, 0, 32, 23, 12);
			}
			matrixStack.pop();

			// HP Bars
			for (int i = 0; i < hpBars - 1; i++) {
				matrixStack.push();
				{
					matrixStack.translate(width - 19*scale - (17*scale * (i + 1)), 12*scale, 0);
					matrixStack.scale(scale, scale, 0);
					blit(matrixStack, 0, 0, 0, 46, 17, 12);
				}
				matrixStack.pop();
			}
		}
		matrixStack.pop();

	}

	public void drawHPBarTop(MatrixStack matrixStack, float posX, float posY, float width, float scale) {
		minecraft.textureManager.bindTexture(new ResourceLocation(KingdomKeys.MODID, "textures/gui/hpbar.png"));	
		// HP Bar
		matrixStack.push();
		{
			matrixStack.translate(posX, posY, 0);
			matrixStack.scale(width, scale, 0);
			blit(matrixStack, 0, 0, 2, 12, 1, 8);
		}
		matrixStack.pop();
	}
	
	private void drawHPBars(MatrixStack matrixStack, float posX, float posY, float width, float scale, LivingEntity target) {
		// HP Bars
		if(target.isAlive()) {
			// HP Bars
			for (int i = 1; i < currentBar; i++) {
				matrixStack.push();
				{
					matrixStack.translate(posX + width - 17 * scale - (17 * scale * i) - 2 * scale, posY + 12 * scale, 0);
					matrixStack.scale(scale, scale, 0);
					blit(matrixStack, 0, 0, 0, 60, 17, 12);
				}
				matrixStack.pop();
			}
		}
	}
	
	private void drawDamagedHPBarTop(MatrixStack matrixStack, float posX, float posY, float width, float scale, LivingEntity target) {
		minecraft.textureManager.bindTexture(new ResourceLocation(KingdomKeys.MODID, "textures/gui/hpbar.png"));
		matrixStack.push();
		{
			// HP Bar
			matrixStack.push();
			{				
				matrixStack.translate(posX, posY, 0);
				matrixStack.scale(width, scale, 0);
				blit(matrixStack, 0, 0, 2, 22, 1, 8);
			}
			matrixStack.pop();
			
		}
		matrixStack.pop();
	}
	
	private void drawDamagedHPBars(MatrixStack matrixStack, float posX, float posY, float width, float scale, LivingEntity target) {
		// HP Bars
		if(target.isAlive()) {
			for (int i = currentBar; i < oldBar; i++) {
				matrixStack.push();
				{
					matrixStack.translate(posX + width - 17 * scale - (17 * scale * i) - 2 * scale, posY + 12 * scale, 0);
					matrixStack.scale(scale, scale, 0);
					blit(matrixStack, 0, 0, 17, 60, 17, 12);
				}
				matrixStack.pop();
			}
		}
	}
}
