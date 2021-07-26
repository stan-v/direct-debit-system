/*******************************************************************************
 * Copyright (c) 2021 Stan Verstappen. All Rights Reserved.
 *
 * This work is licensed under the terms of the MIT license.
 * For a copy, see <https://opensource.org/licenses/MIT>.
 ******************************************************************************/

package assets;

@SuppressWarnings("unused")
public class Post {

	
	private String name;
	private String accountNumber;
	private String description;
	
	private Post parent;
	private Post subposts;
	
	private double balance;
	
	private Transaction[] transactions;
	
	
	
}
