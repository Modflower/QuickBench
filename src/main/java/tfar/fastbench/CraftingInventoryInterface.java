package tfar.fastbench;

import net.minecraft.item.ItemStack;
import net.minecraft.util.DefaultedList;

public interface CraftingInventoryInterface {
	DefaultedList<ItemStack> stacks();
}
