/*******************************************************************************
 * Copyright (c) 2021 Stan Verstappen. All Rights Reserved.
 *
 * This work is licensed under the terms of the MIT license.
 * For a copy, see <https://opensource.org/licenses/MIT>.
 ******************************************************************************/

package global;

import save_load.Dictionary;
import tools.DataConverter;

/**
 * This class manages global configuration loading and adding. This object cannot be instantiated.
 * It encapsulates a config dictionary and manages the actions that can be done to the configuration object.
 * @author Stan
 *
 */
public final class GlobalConfig {
	
	private static Dictionary config;
	
	public DataConverter dataConverter;
	
	/**
	 * Empty and private constructor to prevent instances being made.
	 */
	private GlobalConfig() {}
	
	/**
	 * Loads in the configuration rules from the configurationDictionary.
	 * @param configurationDictionary
	 */
	public static void load(Dictionary configurationDictionary) {
		if(config == null) config = configurationDictionary;
		else config.add(configurationDictionary);
	}
	
	public static String get(String key) {
		return config.get(key);
	}
	
	public static void add(String key, String value) {
		config.add(key,value);
	}
	
	public static void remove(String key) {
		config.remove(key);
	}
	
}
