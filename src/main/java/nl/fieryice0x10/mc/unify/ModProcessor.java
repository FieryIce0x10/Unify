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
package nl.fieryice0x10.mc.unify;

import java.util.Map;

import net.minecraft.item.ItemStack;

/**
 * 
 */
public interface ModProcessor {
	/**
	 * Get the id/name of the mod this processor handles.
	 * 
	 * @return the id of the mod
	 */
	public String getModId();
	
	/**
	 * Is this item registered by this mod?
	 * 
	 * @param item
	 *            the item to check
	 * @return true if this mod registered the given item, otherwise false
	 */
	public boolean isModItem(ItemStack item);
	
	/**
	 * Process any custom (machine) recipes for this mod.
	 * 
	 * @param replacements
	 *            the map of ore dictionary names to replacement items
	 */
	public void replaceRecipes(Map<String, ItemStack> replacements);
}
