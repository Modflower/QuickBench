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
// This file has been traced to be sourced from `shadows.fastbench.util.CraftingInventoryExt`

package tfar.fastbench.mixin;

import com.llamalad7.mixinextras.injector.v2.WrapWithCondition;
import net.minecraft.world.Container;
import net.minecraft.world.inventory.AbstractContainerMenu;
import net.minecraft.world.inventory.TransientCraftingContainer;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import tfar.fastbench.interfaces.CraftingInventoryDuck;

@Mixin(TransientCraftingContainer.class)
public class CraftingContainerMixin implements CraftingInventoryDuck {

	@Unique
	public boolean checkMatrixChanges = true;


	@Override
	public void setCheckMatrixChanges(boolean checkMatrixChanges) {
		this.checkMatrixChanges = checkMatrixChanges;
	}

	@Override
	public boolean getCheckMatrixChanges() {
		return this.checkMatrixChanges;
	}

	@WrapWithCondition(method = {"removeItem",
			"setItem"}, at = @At(value = "INVOKE", target = "Lnet/minecraft/world/inventory/AbstractContainerMenu;slotsChanged(Lnet/minecraft/world/Container;)V"))
	private boolean checkForChanges(AbstractContainerMenu screenHandler, Container inventory) {
		return checkMatrixChanges;
	}
}
