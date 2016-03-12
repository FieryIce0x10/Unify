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

import java.lang.reflect.Type;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import com.google.gson.JsonArray;
import com.google.gson.JsonDeserializationContext;
import com.google.gson.JsonDeserializer;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParseException;
import com.google.gson.JsonPrimitive;
import com.google.gson.JsonSerializationContext;
import com.google.gson.JsonSerializer;

/**
 * 
 */
public class ConfigSerializer
		implements JsonDeserializer<Config>,
		JsonSerializer<Config> {
		
	@Override
	public JsonElement serialize(Config src, Type typeOfSrc,
			JsonSerializationContext context) {
		JsonObject root = new JsonObject();
		JsonArray replacements = new JsonArray();
		root.add("replacements", replacements);
		
		List<String> oreNames = new ArrayList<String>(src.dictTagSet());
		Collections.sort(oreNames);
		for(String oreName : oreNames) {
			JsonObject replacement = new JsonObject();
			replacement.addProperty("oreName", oreName);
			
			JsonArray items = new JsonArray();
			for(String item : src.getReplacements(oreName)) {
				items.add(new JsonPrimitive(item));
			}
			replacement.add("items", items);
			
			replacements.add(replacement);
		}
		
		return root;
	}
	
	@Override
	public Config deserialize(JsonElement json, Type typeOfT,
			JsonDeserializationContext context) throws JsonParseException {
		Config config = new Config();
		
		JsonObject root = json.getAsJsonObject();
		JsonElement elemReplacements = root.get("replacements");
		if(elemReplacements != null) {
			JsonArray replacements = elemReplacements.getAsJsonArray();
			for(JsonElement elemRplc : replacements) {
				JsonObject entryReplacement = elemRplc.getAsJsonObject();
				
				JsonElement elemItems = entryReplacement.get("items");
				if(elemItems != null) {
					List<String> items = new ArrayList<String>();
					for(JsonElement elemItm : elemItems.getAsJsonArray()) {
						items.add(elemItm.getAsString());
					}
					
					JsonElement elemOreName = entryReplacement.get("oreName");
					if(elemOreName != null) {
						String oreName = elemOreName.getAsString();
						config.addReplacements(oreName, items);
					}
				}
			}
		}
		
		return config;
	}
}
