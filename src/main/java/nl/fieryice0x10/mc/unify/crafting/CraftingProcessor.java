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
package nl.fieryice0x10.mc.unify.crafting;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import net.minecraft.item.ItemStack;
import net.minecraft.item.crafting.IRecipe;
import net.minecraftforge.oredict.ShapedOreRecipe;
import net.minecraftforge.oredict.ShapelessOreRecipe;

import nl.fieryice0x10.mc.unify.Unify;

import cpw.mods.fml.common.FMLLog;
import cpw.mods.fml.relauncher.ReflectionHelper;

/**
 * 
 */
public class CraftingProcessor {
	private static Map<Class<? extends IRecipe>, CraftingAdapter> adapters;
	
	static {
		adapters = new HashMap<Class<? extends IRecipe>, CraftingAdapter>();
		
		// Shaped.
		addAdapter(new ShapedCraftingAdapter());
		
		// Shaped with ore dictionary inputs.
		addAdapter(new CraftingAdapter() {
			@Override
			public Class<? extends IRecipe> getRecipeClass() {
				return ShapedOreRecipe.class;
			}
			
			@Override
			public void adaptRecipe(IRecipe recipe, ItemStack replacement) {
				ReflectionHelper.setPrivateValue(ShapedOreRecipe.class,
						(ShapedOreRecipe) recipe, replacement, "output");
			}
		});
		
		// Shapeless.
		addAdapter(new ShapelessCraftingAdapter());
		
		// Shapeless with ore dictionary inputs.
		addAdapter(new CraftingAdapter() {
			@Override
			public Class<? extends IRecipe> getRecipeClass() {
				return ShapelessOreRecipe.class;
			}
			
			@Override
			public void adaptRecipe(IRecipe recipe, ItemStack replacement) {
				ReflectionHelper.setPrivateValue(ShapelessOreRecipe.class,
						(ShapelessOreRecipe) recipe, replacement, "output");
			}
		});
	}
	
	public static void addAdapter(CraftingAdapter adapter) {
		adapters.put(adapter.getRecipeClass(), adapter);
	}
	
	public static void process(Map<String, ItemStack> replacements,
			List<?> recipes) {
		for(Object obj : recipes) {
			if(obj instanceof IRecipe) {
				IRecipe recipe = (IRecipe) obj;
				ItemStack original = recipe.getRecipeOutput();
				
				CraftingAdapter adapter =
					adapters.get(recipe.getClass());
					
				if(adapter != null) {
					ItemStack replacement =
						Unify.firstMatchingReplacement(original,
								replacements);
					if(replacement != null) {
						FMLLog.fine("Replace crafting recipe %s %s with %s",
								recipe, original, replacement);
						adapter.adaptRecipe(recipe, replacement);
					}
				}
			}
			
		}
	}
}
