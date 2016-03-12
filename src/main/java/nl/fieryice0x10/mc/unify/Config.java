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
 * 
 */
public class Config implements Iterable<String> {
	private Map<String, List<String>> replacements;
	
	public Config() {
		replacements = new HashMap<String, List<String>>();
	}
	
	public List<String> getReplacements(String dictTag) {
		return replacements.get(dictTag);
	}
	
	public void addReplacements(String dictTag, List<String> items) {
		replacements.put(dictTag, items);
	}
	
	public void addEmptyReplacements(String dictTag) {
		replacements.put(dictTag, new ArrayList<String>());
	}
	
	@Override
	public Iterator<String> iterator() {
		return replacements.keySet().iterator();
	}
	
	public Set<String> dictTagSet() {
		return replacements.keySet();
	}
}
