/*
 * Copyright (c) 2022-2023 Ampflower
 * Copyright (c) 2020-2021 Tfarcenim
 * Copyright (c) 2018-2021 Brennan Ward
 *
 * This software is subject to the terms of the MIT License.
 * If a copy was not distributed with this file, you can obtain one at
 * https://github.com/Modflower/QuickBench/blob/trunk/LICENSE-MIT
 *
 * Sources:
 *  - https://github.com/Modflower/QuickBench
 *  - https://github.com/Tfarcenim/FabricFastBench
 *  - https://github.com/Shadows-of-Fire/FastWorkbench
 *
 * SPDX-License-Identifier: MIT
 *
 * Contributions from Ampflower may additionally be available under CC0-1.0,
 * as part of the pull-request for upstreaming to FabricFastBench.
 * If a copy was not distributed with this file, you can obtain one at
 * https://github.com/Modflower/QuickBench/blob/trunk/LICENSE-CC0
 *
 * Additional details are outlined in LICENSE.md, which you can obtain at
 * https://github.com/Modflower/QuickBench/blob/trunk/LICENSE.md
 */
// This file has been traced to be sourced from `shadows.fastbench.util.FastBenchUtil`.

package tfar.fastbench;

import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.AbstractContainerMenu;
import net.minecraft.world.inventory.CraftingContainer;
import net.minecraft.world.inventory.ResultContainer;
import net.minecraft.world.inventory.Slot;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.crafting.Recipe;
import net.minecraft.world.item.crafting.RecipeType;
import net.minecraft.world.level.Level;
import tfar.fastbench.interfaces.CraftingInventoryDuck;
import tfar.fastbench.mixin.ContainerAccessor;

public class MixinHooks {

	public static boolean hascachedrecipe = false;

	public static Recipe<CraftingContainer> lastRecipe;

	public static void slotChangedCraftingGrid(Level level, CraftingContainer inv, ResultContainer result) {
		if (!level.isClientSide) {

			ItemStack itemstack = ItemStack.EMPTY;

			Recipe<CraftingContainer> recipe = (Recipe<CraftingContainer>) result.getRecipeUsed();
			if (recipe == null || !recipe.matches(inv, level)) recipe = findRecipe(inv, level);

			if (recipe != null) {
				itemstack = recipe.assemble(inv, level.registryAccess());
			}

			result.setItem(0, itemstack);

			result.setRecipeUsed(recipe);
		}
	}

	public static ItemStack handleShiftCraft(Player player, AbstractContainerMenu container, Slot resultSlot, CraftingContainer input, ResultContainer craftResult, int outStart, int outEnd) {
		ItemStack outputCopy = ItemStack.EMPTY;
		CraftingInventoryDuck duck = (CraftingInventoryDuck) input;
		duck.setCheckMatrixChanges(false);
		Recipe<CraftingContainer> recipe = (Recipe<CraftingContainer>) craftResult.getRecipeUsed();

		if (recipe != null && resultSlot != null && resultSlot.hasItem()) {
			boolean firedCriterion = false;

			while (recipe.matches(input, player.level())) {
				ItemStack recipeOutput = resultSlot.getItem().copy();
				outputCopy = recipeOutput.copy();

				recipeOutput.getItem().onCraftedBy(recipeOutput, player.level(), player);

				if (!player.level().isClientSide && !((ContainerAccessor) container).insert(
					recipeOutput,
					outStart,
					outEnd,
					true
				)) {
					duck.setCheckMatrixChanges(true);
					return ItemStack.EMPTY;
				}

				if (!firedCriterion) {
					// Award the player the recipe for using it. Mimics vanilla behaviour.
					craftResult.awardUsedRecipes(player, input.getItems());
					// Prevents duplication.
					// Ideally, setRecipeUsed is suppressed in the container,
					// but we don't have sufficient context to do that.
					craftResult.setRecipeUsed(recipe);
					firedCriterion = true;
				}

				resultSlot.onQuickCraft(recipeOutput, outputCopy);
				resultSlot.setChanged();

				if (!player.level().isClientSide && recipeOutput.getCount() == outputCopy.getCount()) {
					duck.setCheckMatrixChanges(true);
					return ItemStack.EMPTY;
				}

				if (!recipeOutput.isEmpty()) {
					player.drop(recipeOutput.copyAndClear(), false, true);
				}

				resultSlot.onTake(player, recipeOutput);
			}
			duck.setCheckMatrixChanges(true);
			slotChangedCraftingGrid(player.level(), input, craftResult);
		}
		duck.setCheckMatrixChanges(true);
		return recipe == null ? ItemStack.EMPTY : outputCopy;
	}

	public static Recipe<CraftingContainer> findRecipe(CraftingContainer inv, Level level) {
		return level.getRecipeManager().getRecipeFor(RecipeType.CRAFTING, inv, level).orElse(null);
	}
}
