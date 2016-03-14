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
import java.util.Map.Entry;

import net.minecraft.item.ItemStack;

import nl.fieryice0x10.mc.unify.ModProcessor;
import nl.fieryice0x10.mc.unify.Unify;

import cpw.mods.fml.common.FMLLog;
import crazypants.enderio.machine.crusher.CrusherRecipeManager;
import crazypants.enderio.machine.recipe.Recipe;
import crazypants.enderio.machine.recipe.RecipeOutput;
import crazypants.enderio.material.OreDictionaryPreferences;

/**
 * Compatibility for EnderIO.
 * <br />
 * <br />
 * Replaces recipe output for:
 * <ul>
 * <li>Sag mill</li>
 * <li>Alloy smelter</li>
 * </ul>
 */
public class EnderIOProcessor implements ModProcessor {
	@Override
	public String getModId() {
		return "EnderIO";
	}
	
	@Override
	public boolean isModItem(ItemStack item) {
		if(item.getItem() != null) {
			return item	.getItem().getClass().getName()
						.startsWith("crazypants.enderio.");
		}
		return false;
	}
	
	@Override
	public void replaceRecipes(Map<String, ItemStack> replacements) {
		// Crusher.
		for(Recipe recipe : CrusherRecipeManager.getInstance().getRecipes()) {
			RecipeOutput[] outputs = recipe.getOutputs();
			for(int i = 0; i < outputs.length; i++) {
				ItemStack original = outputs[i].getOutput();
				ItemStack replacement =
					Unify.firstMatchingReplacement(original,
							replacements);
				if(replacement != null) {
					FMLLog.info("Replace crusher recipe %s -> %s with %s",
							recipe.getInputs(),
							original.getUnlocalizedName(),
							replacement.getUnlocalizedName());
					
					/*
					 * The array field may be final, the array values are not.
					 * We can simply replace the output.
					 */
					outputs[i] = new RecipeOutput(replacement,
							outputs[i].getChance(), outputs[i].getExperiance());
				}
			}
		}
		
		// Smelting.
		/*
		 * The alloy smelter seems to pick the output for a recipe from
		 * EnderIO's ore dictionary preference.
		 */
		OreDictionaryPreferences orePref = OreDictionaryPreferences.instance;
		for(Entry<String, ItemStack> e : replacements.entrySet()) {
			orePref.setPreference(e.getKey(), e.getValue());
		}
	}
	
}
