package online.kingdomkeys.kingdomkeys.client.gui.synthesis;

import java.awt.Color;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.Iterator;
import java.util.List;
import java.util.Map.Entry;

import com.mojang.blaze3d.matrix.MatrixStack;
import com.mojang.blaze3d.systems.RenderSystem;

import net.minecraft.client.gui.widget.button.Button;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.ItemStack;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.SoundCategory;
import net.minecraft.util.text.TranslationTextComponent;
import net.minecraftforge.registries.ForgeRegistries;
import online.kingdomkeys.kingdomkeys.ability.Ability;
import online.kingdomkeys.kingdomkeys.ability.ModAbilities;
import online.kingdomkeys.kingdomkeys.capability.IPlayerCapabilities;
import online.kingdomkeys.kingdomkeys.capability.ModCapabilities;
import online.kingdomkeys.kingdomkeys.client.gui.elements.MenuBox;
import online.kingdomkeys.kingdomkeys.client.gui.elements.MenuFilterable;
import online.kingdomkeys.kingdomkeys.client.gui.elements.buttons.MenuButton;
import online.kingdomkeys.kingdomkeys.client.gui.elements.buttons.MenuScrollBar;
import online.kingdomkeys.kingdomkeys.client.gui.elements.buttons.MenuStockItem;
import online.kingdomkeys.kingdomkeys.client.sound.ModSounds;
import online.kingdomkeys.kingdomkeys.item.KeybladeItem;
import online.kingdomkeys.kingdomkeys.item.KeychainItem;
import online.kingdomkeys.kingdomkeys.lib.Strings;
import online.kingdomkeys.kingdomkeys.network.PacketHandler;
import online.kingdomkeys.kingdomkeys.network.cts.CSLevelUpKeybladePacket;
import online.kingdomkeys.kingdomkeys.synthesis.material.Material;
import online.kingdomkeys.kingdomkeys.synthesis.recipe.Recipe;
import online.kingdomkeys.kingdomkeys.synthesis.recipe.RecipeRegistry;
import online.kingdomkeys.kingdomkeys.util.Utils;

public class SynthesisForgeScreen extends MenuFilterable {

	// MenuFilterBar filterBar;
	MenuScrollBar scrollBar;
	MenuBox boxL, boxM, boxR;
	int itemsX = 100, itemsY = 100, itemWidth = 140, itemHeight = 10;

	Button prev, next, upgrade;
	int itemsPerPage = 10;
	private MenuButton back;

	public SynthesisForgeScreen() {
		super("Forge", new Color(0, 255, 0));
		drawSeparately = true;
	}

	@Override
	public boolean mouseScrolled(double mouseX, double mouseY, double delta)
	{
		if (delta > 0 && prev.visible)
		{
			action("prev");
			return true;
		}
		else if  (delta < 0 && next.visible)
		{
			action("next");
			return true;
		}

		return false;
	}

	protected void action(String string) {
		switch (string) {
		case "prev":
			page--;
			minecraft.world.playSound(minecraft.player, minecraft.player.getPosition(), ModSounds.menu_in.get(), SoundCategory.MASTER, 1.0f, 1.0f);
			break;
		case "next":
			page++;
			minecraft.world.playSound(minecraft.player, minecraft.player.getPosition(), ModSounds.menu_in.get(), SoundCategory.MASTER, 1.0f, 1.0f);
			break;
		case "upgrade":
			IPlayerCapabilities playerData = ModCapabilities.getPlayer(minecraft.player);
			minecraft.world.playSound(minecraft.player, minecraft.player.getPosition(), ModSounds.itemget.get(), SoundCategory.MASTER, 1.0f, 1.0f);
			
			ItemStack stack = selected.copy();
			KeychainItem kcItem = (KeychainItem) stack.getItem();
			KeybladeItem item = (KeybladeItem) kcItem.getKeyblade();

			Iterator<Entry<Material, Integer>> itMats = item.data.getLevelData(item.getKeybladeLevel(stack)).getMaterialList().entrySet().iterator();
			boolean hasMaterials = true;
			while(itMats.hasNext()) { //Check if the player has the materials
				Entry<Material, Integer> m = itMats.next();
				
				if(playerData.getMaterialAmount(m.getKey()) < m.getValue()) {
					hasMaterials = false;
				}
			}
			
			if(hasMaterials) { //If the player has the materials substract them and give the item
			Iterator<Entry<Material, Integer>> ite = item.data.getLevelData(item.getKeybladeLevel(stack)).getMaterialList().entrySet().iterator();
				while(ite.hasNext()) {
					Entry<Material, Integer> m = ite.next();
					playerData.removeMaterial(m.getKey(), m.getValue());
				}
				kcItem.setKeybladeLevel(stack, kcItem.getKeybladeLevel(stack)+1);
				minecraft.player.inventory.setInventorySlotContents(minecraft.player.inventory.getSlotFor(selected), stack);
			}
			PacketHandler.sendToServer(new CSLevelUpKeybladePacket(selected));
			init();
			selected = stack;
			break;
		}

	}
	
	@Override
	public void init() {
		float boxPosX = (float) width * 0.1437F;
		float topBarHeight = (float) height * 0.17F;
		float boxWidth = (float) width * 0.3F;
		float middleHeight = (float) height * 0.6F;
		boxL = new MenuBox((int) boxPosX, (int) topBarHeight, (int) boxWidth, (int) middleHeight, new Color(4, 4, 68));
		boxM = new MenuBox((int) boxPosX + (int) boxWidth, (int) topBarHeight, (int) (boxWidth*0.7F), (int) middleHeight, new Color(4, 4, 68));
		boxR = new MenuBox((int) boxM.x + (int) (boxWidth*0.7F), (int) topBarHeight, (int) (boxWidth*1.17F), (int) middleHeight, new Color(4, 4, 68));
		
		//float filterPosX = width * 0.3F;
		//float filterPosY = height * 0.02F;
		//filterBar = new MenuFilterBar((int) filterPosX, (int) filterPosY, this);
		//filterBar.init();
		initItems();
		// addButton(scrollBar = new MenuScrollBar());
		buttonPosX -= 10;
		buttonWidth = ((float)width * 0.07F);
		addButton(back = new MenuButton((int)this.buttonPosX, this.buttonPosY, (int)buttonWidth, new TranslationTextComponent(Strings.Gui_Menu_Back).getString(), MenuButton.ButtonType.BUTTON, b -> minecraft.displayGuiScreen(new SynthesisScreen())));

		super.init();
		itemsPerPage = (int) (middleHeight / 14);
	}

	@Override
	public void initItems() {
		PlayerEntity player = minecraft.player;
		float invPosX = (float) width * 0.1494F;
		float invPosY = (float) height * 0.1851F;
		inventory.clear();
		children.clear();
		buttons.clear();
		//filterBar.buttons.forEach(this::addButton);

		List<ItemStack> items = new ArrayList<>();
		
		for (int i = 0; i < player.inventory.getSizeInventory(); i++) {
			if (player.inventory.getStackInSlot(i).getItem() instanceof KeychainItem) {
				items.add(player.inventory.getStackInSlot(i));
			}
		}
		items.sort(Comparator.comparing(Utils::getCategoryForStack).thenComparing(stack -> stack.getDisplayName().getUnformattedComponentText()));

		for (int i = 0; i < items.size(); i++) {
			if(items.get(i).getItem() instanceof KeychainItem) {
				inventory.add(new MenuStockItem(this, items.get(i), (int) invPosX, (int) invPosY + (i * 14),  (int)(width * 0.28F), false, ((KeychainItem)items.get(i).getItem()).toSummon().getName().getString()));
			} else {
				inventory.add(new MenuStockItem(this, items.get(i), (int) invPosX, (int) invPosY + (i * 14),  (int)(width * 0.28F), false));
			}
		}
		
		inventory.forEach(this::addButton);
		
		super.init();
		
		float buttonPosX = (float) width * 0.03F;
		addButton(prev = new Button((int) buttonPosX + 10, (int)(height * 0.1F), 30, 20, new TranslationTextComponent(Utils.translateToLocal("<--")), (e) -> {
			action("prev");
		}));
		addButton(next = new Button((int) buttonPosX + 10 + 76, (int)(height * 0.1F), 30, 20, new TranslationTextComponent(Utils.translateToLocal("-->")), (e) -> { //MenuButton((int) buttonPosX, button_statsY + (0 * 18), (int) 100, Utils.translateToLocal(Strings.Gui_Synthesis_Materials_Deposit), ButtonType.BUTTON, (e) -> { //
			action("next");
		}));
		addButton(upgrade = new Button((int) (boxM.x+3), (int) (height * 0.67), 70, 20, new TranslationTextComponent(Utils.translateToLocal(Strings.Gui_Synthesis_Forge_Upgrade)), (e) -> {
			action("upgrade");
		}));
	}

	@Override
	public void render(MatrixStack matrixStack, int mouseX, int mouseY, float partialTicks) {
		drawMenuBackground(matrixStack, mouseX, mouseY, partialTicks);
		boxL.draw(matrixStack);
		boxM.draw(matrixStack);
		boxR.draw(matrixStack);
		super.render(matrixStack, mouseX, mouseY, partialTicks);

		prev.visible = page > 0;
		next.visible = page < inventory.size() / itemsPerPage;

		if (selected != ItemStack.EMPTY && ((KeychainItem)selected.getItem()).getKeybladeLevel(selected) < 10) {
			IPlayerCapabilities playerData = ModCapabilities.getPlayer(minecraft.player);
			boolean enoughMats = true;
			KeychainItem kcItem = (KeychainItem)selected.getItem();
			KeybladeItem kb = ((KeychainItem)selected.getItem()).getKeyblade();
			Recipe recipe = RecipeRegistry.getInstance().getValue(kb.getRegistryName());
			
			//Set create button state
			if(kcItem.getKeybladeLevel(selected) < 10) {
				KeychainItem kChain = (KeychainItem) selected.getItem();
				KeybladeItem kBlade = kChain.getKeyblade();
				if(recipe != null) {
					upgrade.visible = true;
					Iterator<Entry<Material, Integer>> materials = kBlade.data.getLevelData(kBlade.getKeybladeLevel(selected)).getMaterialList().entrySet().iterator();
					while(materials.hasNext()) {
						Entry<Material, Integer> m = materials.next();
						if(playerData.getMaterialAmount(m.getKey()) < m.getValue()) {
							enoughMats = false;
						}
					}
				}
			}

			upgrade.active = enoughMats;
			upgrade.visible = recipe != null;
		} else {
			upgrade.visible = false;
		}
		
		//Page renderer
		matrixStack.push();
		{
			matrixStack.translate(width * 0.03F + 45, (height * 0.15) - 18, 1);
			drawString(matrixStack, minecraft.fontRenderer, Utils.translateToLocal("Page: " + (page + 1)), 0, 10, 0xFF9900);
			
		}
		matrixStack.pop();

		for (int i = 0; i < inventory.size(); i++) {
			inventory.get(i).active = false;
		}

		for (int i = page * itemsPerPage; i < page * itemsPerPage + itemsPerPage; i++) {
			if (i < inventory.size()) {
				if (inventory.get(i) != null) {
					inventory.get(i).visible = true;
					inventory.get(i).y = (int) (topBarHeight) + (i % itemsPerPage) * 14 + 5; // 6 = offset
					inventory.get(i).render(matrixStack, mouseX, mouseY, partialTicks);
					inventory.get(i).active = true;
				}
			}
		}
		
		prev.render(matrixStack, mouseX,  mouseY,  partialTicks);
		next.render(matrixStack, mouseX,  mouseY,  partialTicks);
		upgrade.render(matrixStack, mouseX,  mouseY,  partialTicks);
		back.render(matrixStack, mouseX, mouseY, partialTicks);
	}

	@Override
	protected void renderSelectedData(MatrixStack matrixStack, int mouseX, int mouseY, float partialTicks) {
		float tooltipPosX = width * 0.3333F;
		float tooltipPosY = height * 0.8F;

		float iconPosX = boxR.x;
		float iconPosY = boxR.y + 25;

		if (selected.getItem() != null && selected.getItem() instanceof KeychainItem) {
			KeychainItem kc = (KeychainItem) selected.getItem();
			KeybladeItem kb = (KeybladeItem) kc.getKeyblade();

			IPlayerCapabilities playerData = ModCapabilities.getPlayer(minecraft.player);
	
			//Icon
			RenderSystem.pushMatrix();
			{
				double offset = (boxM.getWidth()*0.1F);
				RenderSystem.translated(boxM.x + offset/2, iconPosY, 1);
				RenderSystem.scalef((float)(boxM.getWidth() / 16F - offset / 16F), (float)(boxM.getWidth() / 16F - offset / 16F), 1); //TODO looks ok with items but not keyblades
				RenderSystem.scalef(0.8F, 0.8F, 0.8F);
				//RenderSystem.scalef((float)(boxM.getWidth() / 24F - offset / 24F), (float)(boxM.getWidth() / 24F - offset / 24F), 1);
				itemRenderer.renderItemIntoGUI(new ItemStack(kb), 2, -4);
			}
			RenderSystem.popMatrix();
			
			//Description
			matrixStack.push();
			{
				String text = Utils.translateToLocal(kb.getTranslationKey());
				drawString(matrixStack, minecraft.fontRenderer, text, (int)(tooltipPosX + 5), (int) (tooltipPosY)+5, 0xFF9900);
				Utils.drawSplitString(font, kb.getDescription(), (int) tooltipPosX + 5, (int) tooltipPosY + 5 + minecraft.fontRenderer.FONT_HEIGHT, (int) (width * 0.6F), 0xFFFFFF);
			}
			matrixStack.pop();
			
			matrixStack.push();
			{
				matrixStack.translate(boxM.x+10, height*0.58, 1);
				int level = kb.getKeybladeLevel(selected);
				if(level < 10) {
					drawString(matrixStack, minecraft.fontRenderer, Utils.translateToLocal(Strings.Gui_Menu_Status_Level)+": "+level+" -> "+(level+1), 0, -20, 0xFFFF00);				
					int actualStr = kb.getStrength(level);
					int nextStr = kb.getStrength(level+1);
					int actualMag = kb.getMagic(level);
					int nextMag = kb.getMagic(level+1);
					String nextAbility = kb.data.getLevelAbility(level+1);
					drawString(matrixStack, minecraft.fontRenderer, Utils.translateToLocal(Strings.Gui_Menu_Status_Strength)+": "+actualStr+" -> "+nextStr, 0, -10, 0xFF0000);
					drawString(matrixStack, minecraft.fontRenderer, Utils.translateToLocal(Strings.Gui_Menu_Status_Magic)+": "+actualMag+" -> "+nextMag, 0, 0, 0x4444FF);
					if(nextAbility != null) {
						Ability a = ModAbilities.registry.getValue(new ResourceLocation(nextAbility));
						if(a != null)
							drawString(matrixStack, minecraft.fontRenderer, Utils.translateToLocal(a.getTranslationKey()), 0, 10, 0x44FF44);
					}
				} else {
					drawString(matrixStack, minecraft.fontRenderer, Utils.translateToLocal(Strings.Gui_Menu_Status_Level)+": "+level, 0, -20, 0xFFFF00);				
					int actualStr = kb.getStrength(kb.getKeybladeLevel(selected));
					int actualMag = kb.getMagic(kb.getKeybladeLevel(selected));
					drawString(matrixStack, minecraft.fontRenderer, Utils.translateToLocal(Strings.Gui_Menu_Status_Strength)+": "+actualStr, 0, -10, 0xFF0000);
					drawString(matrixStack, minecraft.fontRenderer, Utils.translateToLocal(Strings.Gui_Menu_Status_Magic)+": "+actualMag, 0, 0, 0x4444FF);
				}
			}
			matrixStack.pop();
		
			//Materials display
			RenderSystem.pushMatrix();
			{
				RenderSystem.translated(iconPosX + 20, height*0.2, 1);
				if(kb.getKeybladeLevel(selected) < 10) {
					Iterator<Entry<Material, Integer>> itMats = kb.data.getLevelData(kb.getKeybladeLevel(selected)).getMaterialList().entrySet().iterator();
					int i = 0;
					while(itMats.hasNext()) {
						Entry<Material, Integer> m = itMats.next();
						ItemStack stack = new ItemStack(ForgeRegistries.ITEMS.getValue(new ResourceLocation(m.getKey().getMaterialName())),m.getValue());
						String n = Utils.translateToLocal(stack.getTranslationKey());
						//playerData.setMaterial(m.getKey(), 1);
						int color = playerData.getMaterialAmount(m.getKey()) >= m.getValue() ?  0x00FF00 : 0xFF0000;
						drawString(matrixStack, minecraft.fontRenderer, n+" x"+m.getValue()+" ("+playerData.getMaterialAmount(m.getKey())+")", 0, (i*16), color);
						itemRenderer.renderItemIntoGUI(stack, -17, (i*16)-4);
						i++;
					}
				}
			}
			RenderSystem.popMatrix();
		}
	}
	
	@Override
	public boolean isPauseScreen() {
		return false;
	}
}
