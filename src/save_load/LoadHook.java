/*******************************************************************************
 * Copyright (c) 2021 Stan Verstappen. All Rights Reserved.
 *
 * This work is licensed under the terms of the MIT license.
 * For a copy, see <https://opensource.org/licenses/MIT>.
 ******************************************************************************/

package save_load;


/**
 * A LoadHook is created by a Saveable that is loaded but still has an unloaded reference.
 * The loadRef() function is implemented by the instantiation of the LoadHook as a kind of interface.
 * The SaveManager then holds an array of LoadHooks and goes through them whenever a new object is loaded.
 * It then passes this Saveable as an argument to the loadRef function. 
 * The savemanager should have a method addLoadHook() to append the hook to the hook-array
 * @author Stan
 *
 */
public abstract class LoadHook {

	private String targetSLC;
	
	protected abstract void loadRef(Saveable S);
	protected String getSLC(){
		return targetSLC;
	}
	
	protected LoadHook(String slc){
		if(slc.charAt(0) == SaveManager.SLC_PREFIX)targetSLC = slc.substring(1);
		else targetSLC = slc;
	}
}
