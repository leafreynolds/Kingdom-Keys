package online.kingdomkeys.kingdomkeys.item;

import net.minecraft.item.Item;
import net.minecraft.item.ItemSword;
import online.kingdomkeys.kingdomkeys.KingdomKeys;

public class ItemKeychain extends ItemSword {

    public ItemKeychain(String name) {
        super(new ItemTierKeyblade(0), 0, 0, new Item.Properties().group(KingdomKeys.keybladesGroup).maxStackSize(1));
        setRegistryName(KingdomKeys.MODID, name);
    }
}
