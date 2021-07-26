/*******************************************************************************
 * Copyright (c) 2021 Stan Verstappen. All Rights Reserved.
 *
 * This work is licensed under the terms of the MIT license.
 * For a copy, see <https://opensource.org/licenses/MIT>.
 ******************************************************************************/

package save_load;

import java.io.File;

public interface SaveableSystem {

	Saveable[] getAllSaveables();
	
	File getDirectory();
	
	public void add(Saveable S);
	
	public SaveManager getSaveManager();
	
	public void execute(String command);
}
