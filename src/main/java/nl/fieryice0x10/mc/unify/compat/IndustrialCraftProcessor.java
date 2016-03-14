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

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;

import net.minecraft.item.ItemStack;
import net.minecraft.item.crafting.IRecipe;

import nl.fieryice0x10.mc.unify.ModProcessor;
import nl.fieryice0x10.mc.unify.Unify;
import nl.fieryice0x10.mc.unify.crafting.CraftingAdapter;
import nl.fieryice0x10.mc.unify.crafting.CraftingProcessor;

import cpw.mods.fml.common.FMLLog;
import ic2.api.recipe.IRecipeInput;
import ic2.api.recipe.RecipeOutput;
import ic2.api.recipe.Recipes;
import ic2.core.AdvShapelessRecipe;

/**
 * Compatibility for Industrialcraft 2.
 * <br />
 * <br />
 * Replaces recipe output for:
 * <ul>
 * <li>Macerator</li>
 * </ul>
 */
public class IndustrialCraftProcessor implements ModProcessor {
	/**
	 * 
	 */
	public IndustrialCraftProcessor() {
		CraftingProcessor.addAdapter(new CraftingAdapter() {
			@Override
			public Class<? extends IRecipe> getRecipeClass() {
				return AdvShapelessRecipe.class;
			}
			
			@Override
			public void adaptRecipe(IRecipe recipe, ItemStack replacement) {
				((AdvShapelessRecipe) recipe).output = replacement;
			}
		});
	}
	
	@Override
	public String getModId() {
		return "IC2";
	}
	
	@Override
	public boolean isModItem(ItemStack item) {
		if(item.getItem() != null) {
			return item	.getItem().getClass().getName()
						.startsWith("ic2.");
		}
		return false;
	}
	
	@Override
	public void replaceRecipes(Map<String, ItemStack> replacements) {
		/**
		 * Guess who's the mad evil scientist.
		 */
		
		// Macerator.
		Map<IRecipeInput, RecipeOutput> add = new HashMap<>();
		
		Iterator<Entry<IRecipeInput, RecipeOutput>> it =
			Recipes.macerator.getRecipes().entrySet().iterator();
		while(it.hasNext()) {
			Entry<IRecipeInput, RecipeOutput> e = it.next();
			RecipeOutput output = e.getValue();
			
			List<ItemStack> newItems = new ArrayList<>(output.items.size());
			
			// Did the recipe change?
			boolean changed = false;
			for(ItemStack original : output.items) {
				ItemStack replacement =
					Unify.firstMatchingReplacement(original,
							replacements);
				if(replacement != null) {
					/*
					 * RecipeOutput.items is an immutable list, so we can't just
					 * add the replacement and delete the original.
					 */
					newItems.add(replacement);
					changed = true;
				} else {
					newItems.add(original);
				}
			}
			
			/*
			 * At this point it would be the best to replace the
			 * RecipeOutput.items list with our newItems list. However
			 * RecipeOutput.items is final. This means we have to delete the
			 * entire old recipe, and insert a new one containing our list.
			 */
			if(changed) {
				FMLLog.info("Replace ic2 macerator %s -> %s with %s",
						e.getKey(),
						output.items, newItems);
				
				it.remove();
				
				add.put(e.getKey(),
						new RecipeOutput(output.metadata, newItems));
			}
		}
		
		/*
		 * Add all the new recipes after iterating over the list. Otherwise a
		 * ConcurrentModificationEception is thrown.
		 */
		Recipes.macerator.getRecipes().putAll(add);
	}
}
