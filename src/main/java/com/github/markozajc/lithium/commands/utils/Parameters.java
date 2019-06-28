package com.github.markozajc.lithium.commands.utils;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Iterator;
import java.util.List;

import com.github.markozajc.lithium.commands.exceptions.runtime.NumberOverflowException;
import com.github.markozajc.lithium.commands.exceptions.startup.MissingParametersException;
import com.github.markozajc.lithium.utilities.Parser;

public class Parameters implements Iterable<String> {

	private String[] parametersArray;
	private int parametersDesiredQuantity = 0;

	/**
	 * Formats a string with a command call into array of parameters.
	 * 
	 * @param input
	 *            command call
	 * @param limit
	 *            splitter limit, 0 for no limit
	 * @param trim
	 *            whether to trim the results
	 * @param omitName
	 *            whether to omit command's name
	 * @return an array of input parameters
	 */
	public static String[] formatParams(String input, int limit, boolean trim, boolean omitName) {
		String[] splitted = input.split("\n| ", limit);

		if (splitted[0].startsWith("<@") && splitted[0].endsWith(">"))
			splitted = input.split("\n| ", limit > 0 ? limit + 1 : 0);

		List<String> result = new ArrayList<>(Arrays.asList(splitted));

		if (omitName) {

			if (result.get(0).startsWith("<@") && result.get(0).endsWith(">"))
				result.remove(1);

			result.remove(0);
		}

		result.removeIf(t -> t.trim().equals(""));

		if (trim) {
			result.replaceAll(String::trim);
		}

		return result.toArray(new String[result.size()]);
	}

	/**
	 * Creates parameter system.
	 * 
	 * @param quantity
	 *            desired quantity of parameters. If there are more, they will be merged
	 *            with the last parameter
	 * @param command
	 *            full command
	 */
	public Parameters(int quantity, String command) {
		String[] parameters = formatParams(command, quantity + 1, true, true);

		this.parametersArray = parameters;
		this.parametersDesiredQuantity = quantity;
	}

	/**
	 * Gets parameter at index as a string
	 * 
	 * @param index
	 *            index of parameter to get
	 * @return parameter at index as an string
	 * @throws MissingParametersException
	 *             if no parameter is at that index
	 */
	public String get(int index) {
		try {
			if (this.parametersArray == null || this.parametersArray[index] == null) {
				return null;
			}
		} catch (ArrayIndexOutOfBoundsException e) {
			throw new MissingParametersException();
		}
		// If requested parameter is null or all the parameters are null

		if (this.parametersArray[index] == null) {
			return null;
		}
		return this.parametersArray[index].trim();
	}

	/**
	 * Gets parameter at index as a primitive type int.
	 * 
	 * @param index
	 *            index of parameter to get
	 * @return parameter at index as an int
	 * @throws NumberFormatException
	 *             if index is not an integer
	 * @throws MissingParametersException
	 *             if no parameter is at that index
	 * @throws NumberOverflowException
	 *             if the number is too big
	 */
	public int getAsInt(int index) {
		return Parser.parseInt(this.get(index));
	}

	/**
	 * Gets parameter at index as a primitive type long.
	 * 
	 * @param index
	 *            index of parameter to get
	 * @return parameter at index as an long
	 * @throws NumberFormatException
	 *             if index is not an integer
	 * @throws MissingParametersException
	 *             if no parameter is at that index
	 * @throws NumberOverflowException
	 *             if the number is too big
	 */
	public long getAsLong(int index) {
		return Parser.parseLong(this.get(index));
	}

	/**
	 * Checks if parameters are null, if at least one parameter is an empty string or if
	 * there are not enough of them (less than provided parametersDesiredQuantity)
	 * 
	 * @return true if everything is OK, false if not
	 */
	public boolean check() {
		return this.check(this.parametersDesiredQuantity);
	}

	/**
	 * Checks if parameters are null, if at least one parameter is an empty string or if
	 * there are not enough of them (less than min)
	 * 
	 * @param min
	 *            - minimum number of parameters
	 * @return true if everything is OK, false if not
	 */
	public boolean check(int min) {
		for (int i = 0; i < this.parametersArray.length; i++) {
			if (this.parametersArray[i].length() < 1 && i <= min) {
				return false;
			}
		}

		return min <= this.parametersArray.length;
	}

	/**
	 * Returns length of current parameters object
	 * 
	 * @return quantity of parameters
	 */
	public int size() {
		return this.parametersArray.length;
	}

	/**
	 * @return parameters as an array
	 */
	public String[] asArray() {
		return this.parametersArray.clone();
	}

	@Override
	public Iterator<String> iterator() {
		return Arrays.asList(this.parametersArray).iterator();
	}
}
