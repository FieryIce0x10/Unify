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
import java.lang.reflect.Modifier;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import net.minecraft.item.ItemStack;

import nl.fieryice0x10.mc.unify.Unify;
import nl.fieryice0x10.mc.unify.ModProcessor;
import nl.fieryice0x10.mc.unify.crafting.CraftingProcessor;

import cpw.mods.fml.common.FMLLog;
import mods.railcraft.api.crafting.IBlastFurnaceRecipe;
import mods.railcraft.api.crafting.ICokeOvenRecipe;
import mods.railcraft.api.crafting.IRockCrusherRecipe;
import mods.railcraft.api.crafting.RailcraftCraftingManager;
import mods.railcraft.common.util.crafting.BlastFurnaceCraftingManager.BlastFurnaceRecipe;
import mods.railcraft.common.util.crafting.CokeOvenCraftingManager.CokeOvenRecipe;

/**
 * 
 */
public class RailcraftProcessor_ implements ModProcessor {
	@Override
	public String getModId() {
		return "Railcraft";
	}
	
	@Override
	public boolean isModItem(ItemStack item) {
		if(item.getItem() != null) {
			return item	.getItem().getClass().getName()
						.startsWith("mods.railcraft");
		}
		return false;
	}
	
	@Override
	public void replaceRecipes(Map<String, ItemStack> replacements) {
		// Blast furnace
		try {
			// TODO: Remove final hack, replace with add new recipe
			Field blastOutput =
				BlastFurnaceRecipe.class.getDeclaredField("output");
			blastOutput.setAccessible(true);
			
			Field modifiersField = Field.class.getDeclaredField("modifiers");
			modifiersField.setAccessible(true);
			modifiersField.setInt(blastOutput,
					blastOutput.getModifiers() & ~Modifier.FINAL);
					
			for(IBlastFurnaceRecipe recipe : RailcraftCraftingManager.blastFurnace.getRecipes()) {
				ItemStack original = recipe.getOutput();
				ItemStack replacement =
					Unify.firstMatchingReplacement(original,
							replacements);
				if(replacement != null && replacement != original) {
					FMLLog.info("Replace blast furnace %s -> %s with %s",
							recipe.getInput(),
							original.getUnlocalizedName(),
							replacement.getUnlocalizedName());
							
					blastOutput.set(recipe, replacement);
				}
			}
		} catch(NoSuchFieldException | SecurityException
				| IllegalArgumentException | IllegalAccessException e) {
			e.printStackTrace();
		}
		
		// Coke oven
		try {
			Field cokeOutput =
				CokeOvenRecipe.class.getDeclaredField("output");
			cokeOutput.setAccessible(true);
			
			Field modifiersField = Field.class.getDeclaredField("modifiers");
			modifiersField.setAccessible(true);
			modifiersField.setInt(cokeOutput,
					cokeOutput.getModifiers() & ~Modifier.FINAL);
					
			for(ICokeOvenRecipe recipe : RailcraftCraftingManager.cokeOven.getRecipes()) {
				ItemStack original = recipe.getOutput();
				ItemStack replacement =
					Unify.firstMatchingReplacement(original,
							replacements);
				if(replacement != null && replacement != original) {
					FMLLog.info("Replace coke oven %s -> %s with %s",
							recipe.getInput(),
							original.getUnlocalizedName(),
							replacement.getUnlocalizedName());
							
					cokeOutput.set(recipe, replacement);
				}
			}
		} catch(NoSuchFieldException | SecurityException
				| IllegalArgumentException | IllegalAccessException e) {
			e.printStackTrace();
		}
		
		// Rock crusher
		for(IRockCrusherRecipe recipe : RailcraftCraftingManager.rockCrusher.getRecipes()) {
			List<Map.Entry<ItemStack, Float>> outputs =
				new ArrayList<>(recipe.getOutputs());
			for(Map.Entry<ItemStack, Float> e : outputs) {
				ItemStack original = e.getKey();
				ItemStack replacement =
					Unify.firstMatchingReplacement(original,
							replacements);
				if(replacement != null && replacement != original) {
					FMLLog.info("Replace rock crusher %s -> %s with %s",
							recipe.getInput(),
							original.getUnlocalizedName(),
							replacement.getUnlocalizedName());
							
					recipe.getOutputs().remove(e);
					recipe.addOutput(replacement, e.getValue());
				}
			}
		}
		
		// Rolling machine
		CraftingProcessor.process(replacements,
				RailcraftCraftingManager.rollingMachine.getRecipeList());
	}
	
}
