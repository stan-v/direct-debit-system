/*******************************************************************************
 * Copyright (c) 2021 Stan Verstappen. All Rights Reserved.
 *
 * This work is licensed under the terms of the MIT license.
 * For a copy, see <https://opensource.org/licenses/MIT>.
 ******************************************************************************/

package tools;

import java.text.DateFormat;
import java.text.DecimalFormat;
import java.text.NumberFormat;

/**
 * Converts various datatypes to strings and vice versa
 * @author Stan
 *
 */
public abstract class DataConverter {

	protected DateFormat dateTime;
	protected DateFormat date;
	protected DecimalFormat currency;
	protected NumberFormat number;
	
	public abstract Object fromString(String string);
	public abstract String toString(Object object);
	
}
