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

import nl.fieryice0x10.mc.unify.ModProcessor;
import nl.fieryice0x10.mc.unify.Unify;

import cpw.mods.fml.common.FMLLog;
import tconstruct.TConstruct;
import tconstruct.library.crafting.CastingRecipe;

/**
 * Compatibility for Railcraft.
 * <br>
 * <br>
 * Replaces recipe output for:
 * <ul>
 * <li>Casting basin</li>
 * <li>Casting table</li>
 * </ul>
 */
public class TinkersConstructProcessor implements ModProcessor {
	@Override
	public String getModId() {
		return "TinkersConstruct";
	}
	
	@Override
	public boolean isModItem(ItemStack item) {
		if(item.getItem() != null) {
			String className = item.getItem().getClass().getName();
			
			/*
			 * Blacklist oreberry's as they sometimes appear before nuggets,
			 * which can cause a oreberry to become the preferred option over a
			 * nugget.
			 */
			return className.startsWith("tconstruct.")
					&& !className.contains("oreberry");
		}
		return false;
	}
	
	@Override
	public void replaceRecipes(Map<String, ItemStack> replacements) {
		for(CastingRecipe cast : TConstruct	.getTableCasting()
											.getCastingRecipes()) {
			replaceCasting(cast, replacements);
		}
		
		for(CastingRecipe cast : TConstruct	.getBasinCasting()
											.getCastingRecipes()) {
			replaceCasting(cast, replacements);
		}
	}
	
	private void replaceCasting(CastingRecipe recipe,
			Map<String, ItemStack> replacements) {
		ItemStack original = recipe.getResult();
		ItemStack replacement =
			Unify.firstMatchingReplacement(original, replacements);
		if(replacement != null) {
			FMLLog.info("Replace casting recipe %s into %s with %s",
					recipe.castingMetal,
					original.getUnlocalizedName(),
					replacement.getUnlocalizedName());
			
			recipe.output = replacement;
		}
	}
}
