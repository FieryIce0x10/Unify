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

import java.lang.reflect.Field;
import java.util.Map;

import net.minecraft.item.ItemStack;

import nl.fieryice0x10.mc.unify.ModProcessor;
import nl.fieryice0x10.mc.unify.Unify;

import org.apache.logging.log4j.Level;

import cofh.thermalexpansion.util.crafting.FurnaceManager;
import cofh.thermalexpansion.util.crafting.FurnaceManager.RecipeFurnace;
import cofh.thermalexpansion.util.crafting.PulverizerManager;
import cofh.thermalexpansion.util.crafting.PulverizerManager.RecipePulverizer;
import cofh.thermalexpansion.util.crafting.SawmillManager;
import cofh.thermalexpansion.util.crafting.SawmillManager.RecipeSawmill;
import cpw.mods.fml.common.FMLLog;

/**
 * Compatibility for Thermal Expansion / Thermal Foundation.
 * <br>
 * <br>
 * Replaces recipe output for:
 * <ul>
 * <li>Redstone furnace</li>
 * <li>Pulverizer</li>
 * <li>Sawmill</li>
 * </ul>
 */
public class ThermalExpansionProcessor implements ModProcessor {
	private Field pulverizerAllowOverridesField;
	
	/**
	 * 
	 */
	public ThermalExpansionProcessor() {
		try {
			pulverizerAllowOverridesField =
				PulverizerManager.class.getDeclaredField("allowOverwrite");
			pulverizerAllowOverridesField.setAccessible(true);
		} catch(NoSuchFieldException | SecurityException e) {
			FMLLog.log(Level.WARN, e,
					"Could not replace TE pulverizer recipes");
		}
	}
	
	@Override
	public String getModId() {
		return "ThermalExpansion";
	}
	
	@Override
	public boolean isModItem(ItemStack item) {
		if(item.getItem() != null) {
			String className = item.getItem().getClass().getName();
			return className.startsWith("cofh.thermalfoundation.")
					|| className.startsWith("cofh.core.");
		}
		return false;
	}
	
	@Override
	public void replaceRecipes(Map<String, ItemStack> replacements) {
		// Redstone furnace.
		/*
		 * The redstone furnace picks up most recipes from the vanilla furnace
		 * registry. However TE prefers their ingots when smelting dusts and
		 * ores.
		 */
		boolean furnaceRecipesChanged = false;
		for(RecipeFurnace recipe : FurnaceManager.getRecipeList()) {
			ItemStack original = recipe.getOutput();
			ItemStack replacement =
				Unify.firstMatchingReplacement(original,
						replacements);
			if(replacement != null) {
				FMLLog.info("Replace TE furnace recipe %s -> %s with %s",
						recipe.getInput(),
						original.getUnlocalizedName(),
						replacement.getUnlocalizedName());
				/*
				 * addTERecipe does not check if we override a current recipe,
				 * addRecipe does.
				 */
				if(!FurnaceManager.addTERecipe(recipe.getEnergy(),
						recipe.getInput(), replacement)) {
					FMLLog.info("Could not override TE furnace recipe "
							+ "%s -> %s with %s",
							recipe.getInput(),
							original.getUnlocalizedName(),
							replacement.getUnlocalizedName());
				} else {
					furnaceRecipesChanged = true;
				}
			}
		}
		
		if(furnaceRecipesChanged) {
			FurnaceManager.refreshRecipes();
		}
		
		// Pulverizer.
		if(pulverizerAllowOverridesField != null) {
			try {
				/*
				 * addTERecipe is not public for the pulverizer, this means we
				 * have to use addRecipe which checks if the recipe already
				 * exists. We can disable this check by setting the
				 * allowOverwrite field to true. This field is private, which is
				 * solved by some reflection magic.
				 */
				boolean tmpAllowOverride =
					(boolean) pulverizerAllowOverridesField.get(null);
				pulverizerAllowOverridesField.set(null, Boolean.TRUE);
				
				boolean pulverizerRecipesChanged = false;
				for(RecipePulverizer recipe : PulverizerManager.getRecipeList()) {
					ItemStack primary = recipe.getPrimaryOutput();
					ItemStack pReplacement =
						Unify.firstMatchingReplacement(primary,
								replacements);
					if(pReplacement == null) {
						pReplacement = primary;
					}
					
					// Secondary value may be null.
					ItemStack secondary = recipe.getSecondaryOutput();
					ItemStack sReplacement =
						Unify.firstMatchingReplacement(secondary,
								replacements);
					if(secondary != null && sReplacement == null) {
						sReplacement = secondary;
					}
					
					if(pReplacement != primary || sReplacement != secondary) {
						FMLLog.info(
								"Replace TE pulverizer recipe %s -> %s,%s with %s,%s",
								recipe.getInput(), primary.getUnlocalizedName(),
								secondary, pReplacement.getUnlocalizedName(),
								sReplacement);
						
						if(!PulverizerManager.addRecipe(recipe.getEnergy(),
								recipe.getInput(), pReplacement, sReplacement,
								recipe.getSecondaryOutputChance(), true)) {
							FMLLog.info("Could not override TE pulverizer"
									+ " recipe %s -> %s,%s with %s,%s",
									recipe.getInput(),
									primary.getUnlocalizedName(),
									secondary,
									pReplacement.getUnlocalizedName(),
									sReplacement);
						} else {
							pulverizerRecipesChanged = true;
						}
					}
				}
				
				// Reset the allowOverwrite value to it's original value.
				pulverizerAllowOverridesField.set(null, tmpAllowOverride);
				
				if(pulverizerRecipesChanged) {
					PulverizerManager.refreshRecipes();
				}
			} catch(IllegalArgumentException | IllegalAccessException e) {
				FMLLog.log(Level.WARN, e,
						"Could not replace TE pulverizer recipes");
			}
		}
		
		// Sawmill.
		boolean sawmillRecipesChanged = false;
		for(RecipeSawmill recipe : SawmillManager.getRecipeList()) {
			ItemStack primary = recipe.getPrimaryOutput();
			ItemStack pReplacement =
				Unify.firstMatchingReplacement(primary,
						replacements);
			if(pReplacement == null) {
				pReplacement = primary;
			}
			
			ItemStack secondary = recipe.getSecondaryOutput();
			ItemStack sReplacement =
				Unify.firstMatchingReplacement(secondary,
						replacements);
			if(secondary != null && sReplacement == null) {
				sReplacement = secondary;
			}
			
			if(pReplacement != primary || sReplacement != secondary) {
				FMLLog.info("Replace TE sawmill recipe %s -> %s,%s with %s,%s",
						recipe.getInput(), primary.getUnlocalizedName(),
						secondary, pReplacement.getUnlocalizedName(),
						sReplacement);
				
				if(!SawmillManager.addRecipe(recipe.getEnergy(),
						recipe.getInput(), pReplacement, sReplacement,
						recipe.getSecondaryOutputChance(), true)) {
					FMLLog.info("Could not override TE sawmill"
							+ " recipe %s -> %s,%s with %s,%s",
							recipe.getInput(), primary.getUnlocalizedName(),
							secondary, pReplacement.getUnlocalizedName(),
							sReplacement);
				} else {
					sawmillRecipesChanged = true;
				}
			}
		}
		
		if(sawmillRecipesChanged) {
			SawmillManager.refreshRecipes();
		}
	}
}
