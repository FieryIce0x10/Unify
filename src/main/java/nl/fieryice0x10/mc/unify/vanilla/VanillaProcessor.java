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
package nl.fieryice0x10.mc.unify.vanilla;

import java.lang.reflect.Field;
import java.util.List;
import java.util.Map;

import net.minecraft.item.ItemStack;
import net.minecraft.item.crafting.CraftingManager;
import net.minecraft.item.crafting.FurnaceRecipes;
import net.minecraft.util.WeightedRandomChestContent;
import net.minecraftforge.common.ChestGenHooks;

import nl.fieryice0x10.mc.unify.ModProcessor;
import nl.fieryice0x10.mc.unify.Unify;
import nl.fieryice0x10.mc.unify.crafting.CraftingProcessor;

import org.apache.logging.log4j.Level;

import cpw.mods.fml.common.FMLLog;

/**
 * Basic processor for Minecraft recipes.
 * <br />
 * <br />
 * Replaces recipe output for:
 * <ul>
 * <li>Crafting</li>
 * <li>Furnace</li>
 * <li>Dungeon loot</li>
 * </ul>
 */
public class VanillaProcessor implements ModProcessor {
	private Field lootChestInfo;
	private Field lootContents;
	
	public VanillaProcessor() {
		try {
			lootChestInfo =
				ChestGenHooks.class.getDeclaredField("chestInfo");
			lootChestInfo.setAccessible(true);
			
			lootContents =
				ChestGenHooks.class.getDeclaredField("contents");
			lootContents.setAccessible(true);
		} catch(NoSuchFieldException | SecurityException e) {
			FMLLog.log(Level.WARN, e,
					"Could not replace loot");
		}
		
	}
	
	@Override
	public String getModId() {
		return "Vanilla";
	}
	
	@Override
	public boolean isModItem(ItemStack item) {
		if(item.getItem() != null) {
			return item	.getItem().getClass().getName()
						.startsWith("net.minecraft.");
		}
		return false;
	}
	
	@Override
	public void replaceRecipes(Map<String, ItemStack> replacements) {
		// Crafting.
		CraftingProcessor.process(replacements,
				CraftingManager.getInstance().getRecipeList());
		
		// Smelting.
		@SuppressWarnings("unchecked")
		Map<Object, Object> smelting =
			FurnaceRecipes.smelting().getSmeltingList();
		for(Object keyObj : smelting.keySet()) {
			Object valueObj = smelting.get(keyObj);
			if(valueObj instanceof ItemStack) {
				ItemStack original = (ItemStack) valueObj;
				ItemStack replacement =
					Unify.firstMatchingReplacement(original,
							replacements);
				
				if(replacement != null) {
					FMLLog.info("Replace smelting recipe %s->%s with %s",
							keyObj,
							original, replacement);
					smelting.put(keyObj, replacement);
				}
			}
		}
		
		// Dungeon loot.
		if(lootChestInfo != null && lootContents != null) {
			try {
				@SuppressWarnings("unchecked")
				Map<String, ChestGenHooks> lootMap =
					(Map<String, ChestGenHooks>) lootChestInfo.get(null);
				
				for(String hook : lootMap.keySet()) {
					ChestGenHooks instance = lootMap.get(hook);
					
					@SuppressWarnings("unchecked")
					List<WeightedRandomChestContent> contents =
						(List<WeightedRandomChestContent>) lootContents.get(
								instance);
					
					for(WeightedRandomChestContent content : contents) {
						ItemStack replacement =
							Unify.firstMatchingReplacement(
									content.theItemId,
									replacements);
						
						if(replacement != null) {
							FMLLog.fine("Replace dungeon loot %s with %s",
									content.theItemId, replacement);
							content.theItemId = replacement;
						}
					}
				}
			} catch(IllegalArgumentException | IllegalAccessException e) {
				FMLLog.log(Level.WARN, e, "Could not replace loot");
			}
		}
	}
}
