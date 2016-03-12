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

import java.lang.reflect.Field;

import net.minecraft.item.ItemStack;
import net.minecraft.item.crafting.IRecipe;
import net.minecraft.item.crafting.ShapelessRecipes;

import org.apache.logging.log4j.Level;

import cpw.mods.fml.common.FMLLog;

/**
 * 
 */
public class ShapelessCraftingAdapter implements CraftingAdapter {
	private static final String FIELDNAME = "recipeOutput";
	private static final String OBF_FIELDNAME = "a";
	private static final String SRG_FIELDNAME = "field_77580_a";
	
	private Field field;
	
	public ShapelessCraftingAdapter() {
		try {
			field = getRecipeClass().getDeclaredField(SRG_FIELDNAME);
		} catch(Exception srg) {
			try {
				field = getRecipeClass().getDeclaredField(OBF_FIELDNAME);
			} catch(Exception obf) {
				try {
					field = getRecipeClass().getDeclaredField(FIELDNAME);
				} catch(Exception deobf) {
					FMLLog.log(Level.WARN, deobf,
							"Could not adapt shapeless crafting");
					return;
				}
			}
		}
		
		field.setAccessible(true);
	}
	
	@Override
	public Class<? extends IRecipe> getRecipeClass() {
		return ShapelessRecipes.class;
	}
	
	@Override
	public void adaptRecipe(IRecipe recipe, ItemStack replacement) {
		try {
			if(field != null) {
				field.set(recipe, replacement);
			}
		} catch(IllegalArgumentException | IllegalAccessException e) {
			FMLLog.log(Level.WARN, e, "Fail to adapt shapeless recipe %s -> %s",
					recipe, replacement);
		}
	}
}
