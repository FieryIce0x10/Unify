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

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;

/**
 * Configuration containing the replacement item id's for the dictionary tags.
 */
public class Config implements Iterable<String> {
	private Map<String, List<String>> replacements;
	
	Config() {
		replacements = new HashMap<String, List<String>>();
	}
	
	/**
	 * Get the list of configured replacements for a given dictionary tag.
	 * The higher on the list, the higher the priority.
	 * 
	 * @param dictTag
	 *            the dictionary tag
	 * @return a list of possible replacement item id's, or null if the tag is
	 *         unknown
	 */
	public List<String> getReplacements(String dictTag) {
		return replacements.get(dictTag);
	}
	
	/**
	 * Add a list of item id's for a specific dictionary tag.
	 * 
	 * @param dictTag
	 *            the dictionary tag
	 * @param items
	 *            the list of item id's
	 */
	public void addReplacements(String dictTag, List<String> items) {
		replacements.put(dictTag, items);
	}
	
	/**
	 * Add a empty list of item id's for a specific dictionary tag. This makes
	 * sure it is inserted into the config file, allowing future runs to
	 * recognize the dict tag.
	 * 
	 * @param dictTag
	 *            the dictionary tag
	 */
	public void addEmptyReplacements(String dictTag) {
		replacements.put(dictTag, new ArrayList<String>());
	}
	
	@Override
	public Iterator<String> iterator() {
		return replacements.keySet().iterator();
	}
	
	/**
	 * Get the set of dictionary tags in this config.
	 * 
	 * @return the set of dictionary tags
	 */
	public Set<String> dictTagSet() {
		return replacements.keySet();
	}
}
