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

import java.io.IOException;
import java.io.Writer;
import java.nio.charset.Charset;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardOpenOption;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.TreeMap;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonIOException;
import com.google.gson.JsonSyntaxException;

import net.minecraft.item.ItemStack;
import net.minecraftforge.oredict.OreDictionary;

import nl.fieryice0x10.mc.unify.vanilla.VanillaProcessor;

import cpw.mods.fml.common.FMLLog;
import cpw.mods.fml.common.Mod;
import cpw.mods.fml.common.Mod.EventHandler;
import cpw.mods.fml.common.Mod.Instance;
import cpw.mods.fml.common.event.FMLInitializationEvent;
import cpw.mods.fml.common.event.FMLLoadCompleteEvent;
import cpw.mods.fml.common.event.FMLPostInitializationEvent;
import cpw.mods.fml.common.event.FMLPreInitializationEvent;

/**
 * Main class of the Unify mod.
 */
@Mod(modid = Unify.MODID,
	name = Unify.MODID,
	version = "@VERSION@",
	dependencies = "after:*")
public class Unify {
	public static final String MODID = "Unify";
	
	/**
	 * The list of prefixes that are added to the config if no config file
	 * exists.
	 */
	public static final String[] DICT_PREFIXES =
		new String[] {"block", "ingot", "nugget", "ore", "gear", "dust",
			"pulp"};
	/**
	 * Blacklist of dictionary items that may have been selected via the
	 * DICT_PREFIXES prefixes.
	 */
	public static final String[] DICT_PREFIXES_BLACKLIST =
		new String[] {"blockCloth", "blockGlass", "blockHopper"};
	
	private static final Charset CONFIG_CHARSET = StandardCharsets.UTF_8;
	private static final String CONFIG_FILE = MODID + ".cfg.json";
	
	/**
	 * The instance of this mod, as created by Forge.
	 */
	@Instance
	public static Unify INSTANCE;
	
	private static List<ModProcessor> processors;
	
	private Config config;
	private Path replacementPath;
	private Map<String, ItemStack> replacements;
	
	static {
		processors = new ArrayList<ModProcessor>();
		
		addModProcessor(new VanillaProcessor());
		
		final String compatBase =
			Unify.class.getName().replace("Unify", "compat.");
		
		addModProcessor("cofh.thermalfoundation.ThermalFoundation",
				compatBase + "ThermalExpansionProcessor");
		
		addModProcessor("mods.railcraft.common.core.Railcraft",
				compatBase + "RailcraftProcessor");
		
		addModProcessor("crazypants.enderio.EnderIO",
				compatBase + "EnderIOProcessor");
		
		addModProcessor("mekanism.common.Mekanism",
				compatBase + "MekanismProcessor");
		
		addModProcessor("ic2.core.IC2",
				compatBase + "IndustrialCraftProcessor");
		
		addModProcessor("forestry.Forestry",
				compatBase + "ForestryProcessor");
		
		addModProcessor("buildcraft.BuildCraftCore",
				compatBase + "BuildcraftProcessor");
		
		addModProcessor("micdoodle8.mods.galacticraft.core.GalacticraftCore",
				compatBase + "GalacticraftProcesssor");
		
		addModProcessor("mrtjp.projectred.ProjectRedCore",
				compatBase + "ProjectRedProcessor");
		
		addModProcessor("tconstruct.TConstruct",
				compatBase + "TinkersConstructProcessor");
	}
	
	/**
	 * Add a mod processor
	 * 
	 * @param processor
	 *            the mod processor
	 */
	public static void addModProcessor(ModProcessor processor) {
		if(processors != null) {
			processors.add(processor);
		} else {
			throw new RuntimeException("Tried to add mod processor too late");
		}
	}
	
	/**
	 * Add a mod processor identified by its class name.
	 * 
	 * @param className
	 *            the class name of the ModProcessor to create and add
	 * @return the instance of the ModProcessor
	 * @throws Exception
	 *             if creation of the instance of the class failed
	 */
	public static ModProcessor addModProcessor(String className)
			throws Exception {
		Class<?> clazz = Class.forName(className);
		if(ModProcessor.class.isAssignableFrom(clazz)) {
			ModProcessor processor = (ModProcessor) clazz.newInstance();
			addModProcessor(processor);
			return processor;
		} else {
			throw new IllegalArgumentException(
					"Provided class does not implement ModProcessor");
		}
	}
	
	/**
	 * Add a mod processor identified by its class name, but only if the test
	 * class is available.
	 *
	 * @param testClass
	 *            class to test
	 * @param className
	 *            mod processor class to create an instance of
	 * @return the instance of the mod processor, or null if the test class is
	 *         unavailable.
	 */
	public static ModProcessor addModProcessor(String testClass,
			String className) {
		try {
			Class.forName(testClass);
			ModProcessor processor = addModProcessor(className);
			FMLLog.info("Loaded unification support for %s",
					processor.getModId());
			return processor;
		} catch(Exception e) {
			e.printStackTrace();
		}
		
		return null;
	}
	
	/**
	 * Find the first matching replacement item, based on the items ore
	 * dictionary registrations.
	 * The returned ItemStack is already copied.
	 * 
	 * @param stack
	 *            the item to replace
	 * @param replacements
	 *            the possible replacements
	 *            Map&lt;oreDictname, replacement&gt;
	 * @return the replacement, or null if none found.
	 */
	public static final ItemStack firstMatchingReplacement(ItemStack stack,
			Map<String, ItemStack> replacements) {
		if(stack == null) {
			return null;
		}
		
		int[] ores = OreDictionary.getOreIDs(stack);
		if(ores.length > 0) {
			for(int id : ores) {
				String oreName = OreDictionary.getOreName(id);
				if(oreName != null && replacements.containsKey(oreName)) {
					ItemStack replacement = replacements.get(oreName).copy();
					replacement.stackSize = stack.stackSize;
					
					if(!ItemStack.areItemStacksEqual(stack, replacement)) {
						return replacement;
					}
				}
			}
		}
		
		return null;
	}
	
	/**
	 * Find the first matching replacement item based on the items ore
	 * dictionary registrations. The replacements are provided by the values
	 * from the Unify instance.
	 * The returned ItemStack is already copied.
	 * 
	 * @param stack
	 *            the item to replace
	 * @return the replacement, or null if none found.
	 * @throws RuntimeException
	 *             if the Unify instance is null
	 */
	public static final ItemStack firstMatchingReplacement(ItemStack stack)
			throws RuntimeException {
		if(INSTANCE == null) {
			throw new RuntimeException("Unify has a null instance");
		}
		
		return firstMatchingReplacement(stack, INSTANCE.replacements);
	}
	
	@EventHandler
	public void preInit(FMLPreInitializationEvent event) {
		Path p = event.getSuggestedConfigurationFile().toPath();
		Path dir = p.toAbsolutePath().getParent();
		replacementPath = dir.resolve(CONFIG_FILE);
		
		if(Files.exists(replacementPath)) {
			GsonBuilder builder = new GsonBuilder();
			builder.registerTypeAdapter(Config.class,
					new ConfigSerializer());
			Gson gson = builder.create();
			
			try {
				config =
					gson.fromJson(Files.newBufferedReader(replacementPath,
							CONFIG_CHARSET), Config.class);
			} catch(JsonSyntaxException | JsonIOException | IOException e) {
				config = null;
				e.printStackTrace();
			}
		}
		
		/*
		 * Not all mods register their items in the preinit (THEY SHOULD).
		 * Therefore we can't search for the replacements yet, as they may not
		 * have been registered yet.
		 */
	}
	
	@EventHandler
	public void init(FMLInitializationEvent event) {
		boolean configChanged = config == null;
		if(configChanged) {
			// No config, generate a new one.
			config = new Config();
			
			dictLoop: for(String dictTag : OreDictionary.getOreNames()) {
				for(String prefix : DICT_PREFIXES) {
					if(dictTag.startsWith(prefix)) {
						// #ores == 0 Useless ore definition
						// #ores == 1 Nothing to replace as replacement=original
						if(OreDictionary.getOres(dictTag).size() > 1) {
							for(String blacklist : DICT_PREFIXES_BLACKLIST) {
								if(dictTag.startsWith(blacklist)) {
									continue dictLoop;
								}
							}
							
							config.addEmptyReplacements(dictTag);
							
							break;
						}
					}
				}
			}
		}
		
		replacements = new HashMap<String, ItemStack>();
		
		for(String dictTag : config) {
			List<ItemStack> dict = OreDictionary.getOres(dictTag);
			
			// Is creating replacements for this dictTag useful?
			if(dict.size() <= 1) {
				continue;
			}
			// Map<ItemId, Item>
			Map<String, ItemStack> knownItems =
				new TreeMap<String, ItemStack>();
			
			for(ItemStack item : dict) {
				knownItems.put(getItemStackId(item), item);
			}
			
			List<String> preferredItems =
				config.getReplacements(dictTag);
			
			// List of missing items in the config.
			List<String> missing =
				new ArrayList<String>(knownItems.keySet());
			missing.removeAll(preferredItems);
			
			if(missing.size() > 0) {
				/*
				 * Check with all mod processors to see if they recognize the
				 * items that are registered, but not in the config (yet).
				 * If the item is recognized we can add it to the config.
				 */
				for(ModProcessor processor : processors) {
					for(String missingItem : missing) {
						ItemStack stack = knownItems.get(missingItem);
						
						if(processor.isModItem(stack)) {
							preferredItems.add(getItemStackId(stack));
							
							configChanged = true;
						}
					}
				}
			}
			
			if(preferredItems.size() > 0) {
				/*
				 * An item that is in the config at this point may not be
				 * registered in the game. The first item in the config that is
				 * also registered in the game becomes the preferred item for
				 * this dictTag.
				 */
				for(String item : preferredItems) {
					if(knownItems.containsKey(item)) {
						FMLLog.info("Set replacement for %s: %s", dictTag,
								knownItems.get(item));
						replacements.put(dictTag, knownItems.get(item).copy());
						break;
					}
				}
			}
		}
		
		FMLLog.info("Starting replacement in init");
		processRecipes();
		
		FMLLog.info("config changed? %s", configChanged);
		if(configChanged) {
			writeReplacementConfig();
		}
		
		// Let the GC clean up the memory that we are not using anymore.
		config = null;
		replacementPath = null;
	}
	
	@EventHandler
	public void postInit(FMLPostInitializationEvent event) {
		FMLLog.info("Starting replacement in post init");
		processRecipes();
	}
	
	@EventHandler
	public void postInit(FMLLoadCompleteEvent event) {
		FMLLog.info("Starting replacement after load complete (%d)",
				replacements.size());
		processRecipes();
		
		// Let the GC clean up the memory that we are not using anymore.
		processors = null;
		replacements = null;
	}
	
	private void processRecipes() {
		// Process recipes.
		if(replacements.size() > 0) {
			for(ModProcessor processor : processors) {
				processor.replaceRecipes(replacements);
			}
		}
	}
	
	private String getItemStackId(ItemStack stack) {
		if(stack == null || stack.getItem() == null) {
			return "NULL";
		}
		return stack.getItem().delegate.name() + ":" + stack.getItemDamage();
	}
	
	/**
	 * Get the default item for the give ore dictionary tag.
	 * The returned ItemStack is shared for all calls of the same dictTag.
	 * Therefore if a mod wishes to use the ItemStack, it has to copy it.
	 * 
	 * @param dictTag
	 *            the ore dictionary name
	 * @return the ItemStack representing the default item, or null if none
	 *         configured
	 * @throws RuntimeException
	 *             if this call is made before post-init or after load-complete
	 */
	public ItemStack getDefaultItem(String dictTag) throws RuntimeException {
		if(replacements == null) {
			throw new RuntimeException("Default items for dictTag's can only be"
					+ " requested after FMLInitializationEvent and before "
					+ "FMLLoadCompleteEvent ends");
		}
		
		return replacements.get(dictTag);
	}
	
	private void writeReplacementConfig() {
		// Write the config to the config file.
		
		GsonBuilder builder = new GsonBuilder();
		builder.registerTypeAdapter(Config.class,
				new ConfigSerializer());
		// Create human readable JSON instead of a single line mess.
		builder.setPrettyPrinting();
		Gson gson = builder.create();
		try {
			Writer writer = Files.newBufferedWriter(replacementPath,
					CONFIG_CHARSET,
					StandardOpenOption.CREATE, StandardOpenOption.WRITE,
					StandardOpenOption.TRUNCATE_EXISTING);
			gson.toJson(config, writer);
			
			/*
			 * Flush is required! Without flush sometimes the final part of the
			 * config file is missing.
			 */
			writer.close();
		} catch(JsonIOException | IOException e) {
			e.printStackTrace();
		}
	}
}
