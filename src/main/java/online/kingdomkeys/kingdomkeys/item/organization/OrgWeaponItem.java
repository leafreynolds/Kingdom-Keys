package online.kingdomkeys.kingdomkeys.item.organization;

import java.util.List;

import javax.annotation.Nullable;

import net.minecraft.client.Minecraft;
import net.minecraft.client.util.ITooltipFlag;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.item.SwordItem;
import net.minecraft.util.text.ITextComponent;
import net.minecraft.util.text.TextFormatting;
import net.minecraft.util.text.TranslationTextComponent;
import net.minecraft.world.World;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;
import online.kingdomkeys.kingdomkeys.KingdomKeys;
import online.kingdomkeys.kingdomkeys.lib.DamageCalculation;
import online.kingdomkeys.kingdomkeys.util.IExtendedReach;

public abstract class OrgWeaponItem extends SwordItem implements IOrgWeapon, IExtendedReach{

	protected OrganizationData data;

    public OrgWeaponItem() {
        super(new OrganizationItemTier(0), 0, 1, new Item.Properties().group(KingdomKeys.orgWeaponsGroup).maxStackSize(1));
    }

    //Get strength from the data based on level
    public int getStrength() {
        return data.getStrength();
    }

    //Get magic from the data based on level
    public int getMagic() {
        return data.getMagic();
    }

    public void setDescription(String description) {
        data.description = description;
    }

    public String getDescription() {
        return data.getDescription();
    }

    public void setStrength(int str) {
        data.baseStrength = str;
    }

    public void setMagic(int mag) {
        data.baseMagic = mag;
    }

    public void setOrganizationData(OrganizationData data) {
        this.data = data;
    }

    public OrganizationData getOrganizationData() {
        return data;
    }
    
    @Override
	public float getReach() {
		return data.getReach();
	}

    @OnlyIn(Dist.CLIENT)
    @Override
    public void addInformation(ItemStack stack, @Nullable World worldIn, List<ITextComponent> tooltip, ITooltipFlag flagIn) {
        if (data != null) {
            tooltip.add(new TranslationTextComponent(TextFormatting.YELLOW+""+getMember()));
            tooltip.add(new TranslationTextComponent(TextFormatting.RED+"Strength %s", getStrength()+DamageCalculation.getSharpnessDamage(stack)+" ["+DamageCalculation.getOrgStrengthDamage(Minecraft.getInstance().player,stack)+"]"));
            tooltip.add(new TranslationTextComponent(TextFormatting.BLUE+"Magic %s", getMagic()+" ["+DamageCalculation.getOrgMagicDamage(Minecraft.getInstance().player,this)+"]"));
            tooltip.add(new TranslationTextComponent(TextFormatting.WHITE+""+TextFormatting.ITALIC + getDescription()));
        }
    }
}
