package online.kingdomkeys.kingdomkeys.item;

import java.util.ArrayList;
import java.util.List;

import net.minecraft.client.util.ITooltipFlag;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.entity.player.ServerPlayerEntity;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.CompoundNBT;
import net.minecraft.util.ActionResult;
import net.minecraft.util.Hand;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.Util;
import net.minecraft.util.text.ITextComponent;
import net.minecraft.util.text.TextFormatting;
import net.minecraft.util.text.TranslationTextComponent;
import net.minecraft.world.World;
import online.kingdomkeys.kingdomkeys.api.item.IItemCategory;
import online.kingdomkeys.kingdomkeys.api.item.ItemCategory;
import online.kingdomkeys.kingdomkeys.capability.IPlayerCapabilities;
import online.kingdomkeys.kingdomkeys.capability.ModCapabilities;
import online.kingdomkeys.kingdomkeys.network.PacketHandler;
import online.kingdomkeys.kingdomkeys.network.stc.SCSyncCapabilityPacket;
import online.kingdomkeys.kingdomkeys.synthesis.recipe.Recipe;
import online.kingdomkeys.kingdomkeys.synthesis.recipe.RecipeRegistry;
import online.kingdomkeys.kingdomkeys.util.Utils;

public class RecipeItem extends Item implements IItemCategory {

	public RecipeItem(Properties properties) {
		super(properties);
	}

	@Override
	public ActionResult<ItemStack> onItemRightClick(World world, PlayerEntity player, Hand hand) {
		if (hand == Hand.MAIN_HAND) {
			if (!world.isRemote) {
				ItemStack stack = player.getHeldItemMainhand();

				//Allow recipes to be given with pre-set keyblades
				//If a recipe already has a tag, it will try learn those
				//If the player already has learnt them, the recipe item will be refreshed to try get new recipes.
				if (stack.hasTag()) {
					learnRecipes(player, stack);
				} else {
					IPlayerCapabilities playerData = ModCapabilities.getPlayer(player);
					List<ResourceLocation> missingKeyblades = getMissingRecipes(playerData, "keyblade");
					List<ResourceLocation> missingItems = getMissingRecipes(playerData, "item");
					
					List<String> types = new ArrayList<String>();
					types.add("keyblade");
					types.add("item");
					if(missingKeyblades.size() == 0) {
						types.remove("keyblade");
					}
					if(missingItems.size() == 0) {
						types.remove("item");
					}
					
					String type = "";
					if(types.size() > 1) {
						int num = world.rand.nextInt(types.size());
						type = types.get(num);
					} else if(types.size() == 1){
						type = types.get(0);
					} else {
						player.sendStatusMessage(new TranslationTextComponent("No more recipes to learn"), true);
						return super.onItemRightClick(world, player, hand);
					}
					
					player.sendStatusMessage(new TranslationTextComponent("Opened "+type+" recipe"), true);

					//Set up the recipe item with the given type
					//We get here if there are recipes still available to learn.
					shuffleRecipes(stack, player, type);
				}
			}
		}
		return super.onItemRightClick(world, player, hand);
	}

	private void learnRecipes(PlayerEntity player, ItemStack stack)
	{
		final CompoundNBT stackTag = stack.getTag();
		String[] recipes = { stackTag.getString("recipe1"), stackTag.getString("recipe2"), stackTag.getString("recipe3") };
		IPlayerCapabilities playerData = ModCapabilities.getPlayer(player);
		// /give Dev kingdomkeys:recipe{type:"keyblade",recipe1:"kingdomkeys:oathkeeper",recipe2:"kingdomkeys:fenrir"} 16

		boolean consume = false;
		for (String recipe : recipes) {
			ResourceLocation rl = new ResourceLocation(recipe);
			if (RecipeRegistry.getInstance().containsKey(rl)) {
				ItemStack outputStack = new ItemStack(RecipeRegistry.getInstance().getValue(rl).getResult());
				if (recipe == null || !RecipeRegistry.getInstance().containsKey(rl)) { // If recipe is not valid
					String message = "ERROR: Recipe for " + Utils.translateToLocal(rl.toString()) + " was not learnt because it is not a valid recipe, Report this to a dev";
					player.sendMessage(new TranslationTextComponent(TextFormatting.RED + message), Util.DUMMY_UUID);
				} else if (playerData.hasKnownRecipe(rl)) { // If recipe already known
					String message = "Recipe for " + Utils.translateToLocal(outputStack.getTranslationKey()) + " already learnt";
					player.sendMessage(new TranslationTextComponent(TextFormatting.YELLOW + message), Util.DUMMY_UUID);
				} else { // If recipe is not known, learn it
					playerData.addKnownRecipe(rl);
					consume = true;
					String message = "Recipe " + Utils.translateToLocal(outputStack.getTranslationKey()) + " learnt successfully";
					player.sendMessage(new TranslationTextComponent(TextFormatting.GREEN + message), Util.DUMMY_UUID);
					PacketHandler.sendTo(new SCSyncCapabilityPacket(playerData), (ServerPlayerEntity) player);
				}
			}
		}

		if (consume) {
			//remove all child tags so we don't contaminate the stack
			//This will set the stack's tag field to null once all are removed.
			stack.removeChildTag("recipe1");
			stack.removeChildTag("recipe2");
			stack.removeChildTag("recipe3");
			stack.removeChildTag("type");
			//reduce stack size by one.
			player.getHeldItemMainhand().shrink(1);
		} else {
			//try for fresh recipes, based on what type this stack was set to. No swapping from keyblade to item recipes etc.
			//will fail successfully if none left.
			shuffleRecipes(stack, player, stackTag.getString("type"));
		}
	}

	public void shuffleRecipes(ItemStack stack, PlayerEntity player, String type) {
		IPlayerCapabilities playerData = ModCapabilities.getPlayer(player);

		ResourceLocation recipe1=null, recipe2=null, recipe3=null;
		
		List<ResourceLocation> list;
		switch(type) {
		case "keyblade":
			list = getMissingRecipes(playerData, "keyblade");
			
			if(list.size() == 0)
				return;
			
			if(list.size() > 0) {
				recipe1 = list.get(Utils.randomWithRange(0, list.size() - 1));
			}
			
			if(list.size() > 1) {
				do {
					recipe2 = list.get(Utils.randomWithRange(0, list.size() - 1));
				} while(recipe2 == recipe1);
			}
			
			if(list.size() > 2) {
				do {
					recipe3 = list.get(Utils.randomWithRange(0, list.size() - 1));
				} while(recipe3 == recipe1 || recipe3 == recipe2);

			}

			break;
		case "item":
			list = getMissingRecipes(playerData, "item");
			if(list.size() > 0) {
				recipe1 = list.get(Utils.randomWithRange(0, list.size() - 1));
			}
			break;
		}

		stack.getOrCreateTag().putString("type", type);

		//if any recipes are on this stack, such as already learned ones, they should get overwritten
		if(recipe1 != null)
			stack.getOrCreateTag().putString("recipe1", recipe1.toString());
		if(recipe2 != null)
			stack.getOrCreateTag().putString("recipe2", recipe2.toString());
		if(recipe3 != null)
			stack.getOrCreateTag().putString("recipe3", recipe3.toString());

		//Call learn recipes immediately.
		//This will remove all child tags and then reduce stack size by one
		//recipe1 is not null if any recipes exist to learn
		if (recipe1 != null)
		{
			learnRecipes(player, stack);
		}
	}

	private List<ResourceLocation> getMissingRecipes(IPlayerCapabilities playerData, String type) {
		List<ResourceLocation> list = new ArrayList<ResourceLocation>();
			for(Recipe r : RecipeRegistry.getInstance().getValues()) {
				if(!playerData.hasKnownRecipe(r.getRegistryName())) {
					if(r.getType().equals(type)) {
						list.add(r.getRegistryName());
					}
				}
			}
		return list;
	}

	@Override
	public void addInformation(ItemStack stack, World worldIn, List<ITextComponent> tooltip, ITooltipFlag flagIn) {
		if (stack.hasTag()) {
			for (int i = 1; i <= 3; i++) {
				String recipeName = stack.getTag().getString("recipe" + i);
				if(RecipeRegistry.getInstance().containsKey(new ResourceLocation(recipeName))) {
					Recipe recipe = RecipeRegistry.getInstance().getValue(new ResourceLocation(recipeName));
					if (recipe != null) {
						String name;
						if(recipe.getType().equals("keyblade")) {
							KeychainItem item = (KeychainItem) recipe.getResult().getItem();
							name = new ItemStack(item.keyblade).getTranslationKey();
						} else {
							name = new ItemStack(recipe.getResult()).getTranslationKey();
						}
						tooltip.add(new TranslationTextComponent(Utils.translateToLocal(name)));
					}
				}
			}
		}
	}

	@Override
	public ItemCategory getCategory() {
		return ItemCategory.MISC;
	}
}
