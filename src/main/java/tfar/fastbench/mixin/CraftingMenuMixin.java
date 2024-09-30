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
// This file has been traced to be sourced from `shadows.fastbench.mixin.MixinWorkbenchContainer`.

package tfar.fastbench.mixin;

import net.minecraft.world.Container;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.AbstractContainerMenu;
import net.minecraft.world.inventory.CraftingContainer;
import net.minecraft.world.inventory.CraftingMenu;
import net.minecraft.world.inventory.MenuType;
import net.minecraft.world.inventory.RecipeBookMenu;
import net.minecraft.world.inventory.ResultContainer;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import org.jetbrains.annotations.Nullable;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.Redirect;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;
import tfar.fastbench.MixinHooks;
import tfar.fastbench.interfaces.CraftingInventoryDuck;

@Mixin(CraftingMenu.class)
abstract class CraftingMenuMixin<C extends Container> extends RecipeBookMenu<C> implements CraftingInventoryDuck {

	@Shadow
	@Final
	private CraftingContainer craftSlots;
	@Shadow
	@Final
	private ResultContainer resultSlots;

	protected CraftingMenuMixin(@Nullable MenuType<?> type, int syncId) {
		super(type, syncId);
	}

	@Redirect(method = "method_17401", at = @At(value = "INVOKE", target = "Lnet/minecraft/world/inventory/CraftingMenu;slotChangedCraftingGrid(Lnet/minecraft/world/inventory/AbstractContainerMenu;Lnet/minecraft/world/level/Level;Lnet/minecraft/world/entity/player/Player;Lnet/minecraft/world/inventory/CraftingContainer;Lnet/minecraft/world/inventory/ResultContainer;)V"))
	private void hookChangedCraftingGrid(AbstractContainerMenu self, Level level, Player player, CraftingContainer craftSlots, ResultContainer resultSlots) {
		MixinHooks.slotChangedCraftingGrid(level, craftSlots, resultSlots);
	}

	@Inject(method = "quickMoveStack", at = @At("HEAD"), cancellable = true)
	private void handleShiftCraft(Player player, int index, CallbackInfoReturnable<ItemStack> cir) {
		if (index != 0) return;
		cir.setReturnValue(MixinHooks.handleShiftCraft(player, this, this.slots.get(index), this.craftSlots, this.resultSlots, 10, 46));
	}

	/**
	 * Resets the checkMatrixChanges flag on return.
	 */
	@Inject(method = "clearCraftingContent", at = @At("RETURN"))
	private void fastbench$setCheckMatrixTrue(CallbackInfo ci) {
		setCheckMatrixChanges(true);
	}

	/**
	 * {@inheritDoc}
	 * <p>
	 * Proxies the setCheckMatrixChanges call to the {@link #craftSlots}.
	 */
	@Override
	public void setCheckMatrixChanges(final boolean checkMatrixChanges) {
		((CraftingInventoryDuck) craftSlots).setCheckMatrixChanges(checkMatrixChanges);
	}

	/**
	 * {@inheritDoc}
	 * <p>
	 * Proxies the getCheckMatrixChanges call to the {@link #craftSlots}.
	 */
	@Override
	public boolean getCheckMatrixChanges() {
		return ((CraftingInventoryDuck) craftSlots).getCheckMatrixChanges();
	}
}
