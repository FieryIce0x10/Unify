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
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

import net.minecraft.item.ItemStack;

import nl.fieryice0x10.mc.unify.ModProcessor;
import nl.fieryice0x10.mc.unify.Unify;
import nl.fieryice0x10.mc.unify.crafting.CraftingProcessor;

import org.apache.logging.log4j.Level;

import cpw.mods.fml.common.FMLLog;
import mods.railcraft.api.crafting.IRockCrusherRecipe;
import mods.railcraft.api.crafting.RailcraftCraftingManager;
import mods.railcraft.common.util.crafting.BlastFurnaceCraftingManager.BlastFurnaceRecipe;
import mods.railcraft.common.util.crafting.CokeOvenCraftingManager.CokeOvenRecipe;

/**
 * Compatibility for Railcraft.
 * <br />
 * <br />
 * Replaces recipe output for:
 * <ul>
 * <li>Blast furnace</li>
 * <li>Coke oven</li>
 * <li>Rock crusher</li>
 * <li>Rolling machine</li>
 * </ul>
 */
public class RailcraftProcessor implements ModProcessor {
	private Field cokeMatchDamage;
	private Field cokeMatchNBT;
	
	public RailcraftProcessor() {
		try {
			cokeMatchDamage =
				CokeOvenRecipe.class.getDeclaredField("matchDamage");
			cokeMatchDamage.setAccessible(true);
			cokeMatchNBT =
				CokeOvenRecipe.class.getDeclaredField("matchNBT");
			cokeMatchNBT.setAccessible(true);
		} catch(NoSuchFieldException | SecurityException e) {
			FMLLog.log(Level.WARN, e,
					"Could not replace Railcraft coke oven recipes");
		}
	}
	
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
		// Blast furnace.
		@SuppressWarnings("unchecked")
		List<BlastFurnaceRecipe> blast =
			(List<BlastFurnaceRecipe>) RailcraftCraftingManager.blastFurnace.getRecipes();
		
		List<BlastFurnaceRecipe> blastAdd = new ArrayList<>();
		Iterator<BlastFurnaceRecipe> blastIt = blast.iterator();
		while(blastIt.hasNext()) {
			BlastFurnaceRecipe recipe = blastIt.next();
			
			ItemStack original = recipe.getOutput();
			ItemStack replacement =
				Unify.firstMatchingReplacement(original,
						replacements);
			if(replacement != null) {
				blastAdd.add(new BlastFurnaceRecipe(recipe.getInput(),
						recipe.matchDamage(), recipe.matchNBT(),
						recipe.getCookTime(), replacement));
				
				// Remove original recipe.
				blastIt.remove();
				
				FMLLog.info("Replace blast furnace recipe %s -> %s with %s",
						recipe.getInput(),
						original.getUnlocalizedName(),
						replacement.getUnlocalizedName());
			}
		}
		// Add new recipes.
		blast.addAll(blastAdd);
		
		// Coke oven.
		if(cokeMatchDamage != null && cokeMatchNBT != null) {
			@SuppressWarnings("unchecked")
			List<CokeOvenRecipe> coke =
				(List<CokeOvenRecipe>) RailcraftCraftingManager.cokeOven.getRecipes();
			List<CokeOvenRecipe> cokeAdd = new ArrayList<>();
			Iterator<CokeOvenRecipe> cokeIt = coke.iterator();
			while(cokeIt.hasNext()) {
				CokeOvenRecipe recipe = cokeIt.next();
				ItemStack original = recipe.getOutput();
				ItemStack replacement =
					Unify.firstMatchingReplacement(original,
							replacements);
				if(replacement != null) {
					try {
						/*
						 * No public methods exists for matchDamage and matchNBT
						 * in CokeOvenRecipe. To solve this we have to fetch the
						 * private values trough reflection.
						 */
						boolean matchDamage =
							cokeMatchDamage.getBoolean(recipe);
						boolean matchNBT = cokeMatchNBT.getBoolean(recipe);
						
						cokeAdd.add(
								new CokeOvenRecipe(recipe.getInput(),
										matchDamage,
										matchNBT, replacement,
										recipe.getFluidOutput(),
										recipe.getCookTime()));
						
						// Remove original recipe.
						cokeIt.remove();
						
						FMLLog.info("Replace coke oven recipe %s -> %s with %s",
								recipe.getInput(),
								original.getUnlocalizedName(),
								replacement.getUnlocalizedName());
					} catch(IllegalArgumentException
							| IllegalAccessException e) {
						FMLLog.log(Level.INFO, e,
								"Could not replace coke oven recipe %s -> %s with %s",
								recipe.getInput(),
								original.getUnlocalizedName(),
								replacement.getUnlocalizedName());
					}
				}
			}
			// Add new recipes.
			coke.addAll(cokeAdd);
		}
		
		// Rock crusher.
		for(IRockCrusherRecipe recipe : RailcraftCraftingManager.rockCrusher.getRecipes()) {
			List<Map.Entry<ItemStack, Float>> outputs =
				new ArrayList<>(recipe.getOutputs());
			for(Map.Entry<ItemStack, Float> e : outputs) {
				ItemStack original = e.getKey();
				ItemStack replacement =
					Unify.firstMatchingReplacement(original,
							replacements);
				if(replacement != null) {
					FMLLog.info("Replace rock crusher recipe %s -> %s with %s",
							recipe.getInput(),
							original.getUnlocalizedName(),
							replacement.getUnlocalizedName());
					
					recipe.getOutputs().remove(e);
					recipe.addOutput(replacement, e.getValue());
				}
			}
		}
		
		// Rolling machine.
		/*
		 * The recipes for the rolling machine are implemented using
		 * ShapedRecipes. We can reuse the crafting processor as it already
		 * handles ShapedRecipes for the crafting.
		 */
		CraftingProcessor.process(replacements,
				RailcraftCraftingManager.rollingMachine.getRecipeList());
	}
}
