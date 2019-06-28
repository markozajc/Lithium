package com.github.markozajc.lithium.utilities;

import java.math.BigInteger;

import com.github.markozajc.lithium.commands.exceptions.runtime.NumberOverflowException;

public class Parser {

	private Parser() {}

	/**
	 * Safely parses a primitive type {@code int} from a {@link String}.
	 *
	 * @param string
	 *            The {@link String} to parse.
	 * @return The parsed {@code int}.
	 * @throws NumberOverflowException
	 *             If the abs of the parsed {@link Integer} exceeds the value of
	 *             {@value Integer#MAX_VALUE}.
	 * @throws NumberFormatException
	 *             If the provided {@link String} can not be parsed into a number.
	 */
	@SuppressWarnings("unused")
	public static int parseInt(String string) {
		try {
			return Integer.parseInt(string);

		} catch (NumberFormatException e) {
			new BigInteger(string);

			throw new NumberOverflowException();
		}

	}

	/**
	 * Safely parses a primitive type {@code long} from a {@link String}.
	 *
	 * @param string
	 *            The {@link String} to parse.
	 * @return The parsed {@code long}.
	 * @throws NumberOverflowException
	 *             If the abs of the parsed {@link Long} exceeds the value of
	 *             {@value Long#MAX_VALUE}.
	 * @throws NumberFormatException
	 *             If the provided {@link String} can not be parsed into a number.
	 */
	@SuppressWarnings("unused")
	public static long parseLong(String string) {
		try {
			return Long.parseLong(string);

		} catch (NumberFormatException e) {
			new BigInteger(string);

			throw new NumberOverflowException();
		}

	}

}
