/*******************************************************************************
 *
 * The MIT License (MIT)
 *
 * Copyright (c) 2016 FieryIce0x10
 *
 * Permission is hereby granted, free of charge, to any person obtaining a copy
 * of this software and associated documentation files (the "Software"), to deal
 * in the Software without restriction, including without limitation the rights
 * to use, copy, modify, merge, publish, distribute, sublicense, and/or sell
 * copies of the Software, and to permit persons to whom the Software is
 * furnished to do so, subject to the following conditions:
 *
 * The above copyright notice and this permission notice shall be included in
 * all copies or substantial portions of the Software.
 *
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS
 * OR IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF
 * MERCHANTABILITY, FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT.
 * IN NO EVENT SHALL THE AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY
 * CLAIM, DAMAGES OR OTHER LIABILITY, WHETHER IN AN ACTION OF CONTRACT,
 * TORT OR OTHERWISE, ARISING FROM, OUT OF OR IN CONNECTION WITH THE
 * SOFTWARE OR THE USE OR OTHER DEALINGS IN THE SOFTWARE.
 *
 *******************************************************************************/
package nl.fieryice0x10.mc.unify.compat;

import java.util.Map;

import net.minecraft.item.ItemStack;
import net.minecraft.item.crafting.IRecipe;

import nl.fieryice0x10.mc.unify.ModProcessor;
import nl.fieryice0x10.mc.unify.Unify;
import nl.fieryice0x10.mc.unify.crafting.CraftingAdapter;
import nl.fieryice0x10.mc.unify.crafting.CraftingProcessor;

import cpw.mods.fml.common.FMLLog;
import cpw.mods.fml.relauncher.ReflectionHelper;
import mekanism.common.recipe.RecipeHandler.Recipe;
import mekanism.common.recipe.ShapedMekanismRecipe;
import mekanism.common.recipe.ShapelessMekanismRecipe;
import mekanism.common.recipe.machines.MachineRecipe;
import mekanism.common.recipe.outputs.ChanceOutput;
import mekanism.common.recipe.outputs.ItemStackOutput;
import mekanism.common.recipe.outputs.MachineOutput;

/**
 * Compatibility for Mekanism.
 * <br />
 * <br />
 * Replaces recipe output for:
 * <ul>
 * <li>All machines registered in mekanism.common.recipe.RecipeHandler.Recipe
 * </li>
 * <li>All Mekanism shaped recipes</li>
 * <li>All Mekanism shapeless recipes</li>
 * </ul>
 */
public class MekanismProcessor implements ModProcessor {
	/**
	 * 
	 */
	public MekanismProcessor() {
		CraftingProcessor.addAdapter(new CraftingAdapter() {
			@Override
			public Class<? extends IRecipe> getRecipeClass() {
				return ShapelessMekanismRecipe.class;
			}
			
			@Override
			public void adaptRecipe(IRecipe recipe, ItemStack replacement) {
				ReflectionHelper.setPrivateValue(ShapelessMekanismRecipe.class,
						(ShapelessMekanismRecipe) recipe, replacement,
						"output");
			}
		});
		
		CraftingProcessor.addAdapter(new CraftingAdapter() {
			@Override
			public Class<? extends IRecipe> getRecipeClass() {
				return ShapedMekanismRecipe.class;
			}
			
			@Override
			public void adaptRecipe(IRecipe recipe, ItemStack replacement) {
				ReflectionHelper.setPrivateValue(ShapedMekanismRecipe.class,
						(ShapedMekanismRecipe) recipe, replacement, "output");
			}
		});
	}
	
	@Override
	public String getModId() {
		return "Mekanism";
	}
	
	@Override
	public boolean isModItem(ItemStack item) {
		if(item.getItem() != null) {
			return item	.getItem().getClass().getName()
						.startsWith("mekanism.");
		}
		return false;
	}
	
	@Override
	public void replaceRecipes(Map<String, ItemStack> replacements) {
		for(Recipe recipe : Recipe.values()) {
			for(Object o : recipe.get().values()) {
				if(o instanceof MachineRecipe) {
					MachineOutput<?> output =
						((MachineRecipe<?, ?, ?>) o).recipeOutput;
					if(output instanceof ItemStackOutput) {
						ItemStack original = ((ItemStackOutput) output).output;
						ItemStack replacement =
							Unify.firstMatchingReplacement(original,
									replacements);
						if(replacement != null) {
							FMLLog.info(
									"Replace mekanism recipe "
											+ "(itemstack) [%s] %s -> %s",
									recipe,
									original.getUnlocalizedName(),
									replacement.getUnlocalizedName());
							
							((ItemStackOutput) output).output = replacement;
						}
					} else if(output instanceof ChanceOutput) {
						ChanceOutput chance = (ChanceOutput) output;
						if(chance.hasPrimary()) {
							ItemStack original = chance.primaryOutput;
							ItemStack replacement =
								Unify.firstMatchingReplacement(original,
										replacements);
							if(replacement != null) {
								FMLLog.info(
										"Replace mekanism recipe "
												+ "(chance-primary) [%s] %s -> %s",
										recipe,
										original.getUnlocalizedName(),
										replacement.getUnlocalizedName());
								
								chance.primaryOutput = replacement;
							}
						}
						
						if(chance.hasSecondary()) {
							ItemStack original = chance.secondaryOutput;
							ItemStack replacement =
								Unify.firstMatchingReplacement(original,
										replacements);
							if(replacement != null) {
								FMLLog.info(
										"Replace mekanism recipe "
												+ "(chance-secondary) [%s] %s -> %s",
										recipe,
										original.getUnlocalizedName(),
										replacement.getUnlocalizedName());
								
								chance.secondaryOutput = replacement;
							}
						}
					}
				}
			}
		}
	}
	
}
