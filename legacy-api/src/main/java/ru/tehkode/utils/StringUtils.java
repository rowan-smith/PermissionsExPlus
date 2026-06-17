/*
 * PermissionsEx - Permissions plugin for Bukkit
 * Copyright (C) 2011 t3hk0d3 http://www.tehkode.ru
 *
 * This program is free software; you can redistribute it and/or
 * modify it under the terms of the GNU General Public License
 * as published by the Free Software Foundation; either version 2
 * of the License, or (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program; if not, write to the Free Software
 * Foundation, Inc., 51 Franklin Street, Fifth Floor, Boston, MA  02110-1301, USA.
 */
package ru.tehkode.utils;

import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.Reader;
import java.util.List;

/**
 * General-purpose string manipulation utilities used by PermissionsEx.
 */
public class StringUtils {

	/**
	 * Returns the given array joined by a separator.
	 *
	 * @param array     an array of strings to join; must not be {@code null}
	 * @param separator a string to insert between the array elements; must not be {@code null}
	 * @return the joined string, or an empty string when {@code array} is empty
	 */
	public static String implode(String[] array, String separator) {
		if (array.length == 0) {
			return "";
		}

		StringBuilder buffer = new StringBuilder();

		for (String str : array) {
			buffer.append(separator);
			buffer.append(str);
		}

		return buffer.substring(separator.length()).trim();
	}

	/**
	 * Returns the elements joined by a separator.
	 *
	 * @param list      a list of objects to join; must not be {@code null}
	 * @param separator a string to insert between the list elements; must not be {@code null}
	 * @return the joined string, or an empty string when {@code list} is empty
	 */
	public static String implode(List<?> list, String separator) {
		if (list.isEmpty()) {
			return "";
		}

		StringBuilder buffer = new StringBuilder();

		int lastElement = list.size() - 1;
		for (int i = 0; i < list.size(); i++) {
			buffer.append(list.get(i).toString());

			if (i < lastElement) {
				buffer.append(separator);
			}
		}

		return buffer.toString();
	}

	/**
	 * Reads an input stream's complete contents as a UTF-8 string.
	 *
	 * <p>The stream is closed after reading, including when an I/O error occurs.</p>
	 *
	 * @param is an input stream to read from; may be {@code null}
	 * @return the full string content, or {@code null} if {@code is} is {@code null}
	 * @throws IOException if reading from the stream fails
	 */
	public static String readStream(InputStream is) throws IOException {
		if (is != null) {
			StringBuilder builder = new StringBuilder();

			try {
				Reader reader = new InputStreamReader(is, "UTF-8");

				char[] buffer = new char[128];
				int read;
				while ((read = reader.read(buffer)) > 0) {
					builder.append(buffer, 0, read);
				}
			} finally {
				is.close();
			}

			return builder.toString();
		}

		return null;
	}

	/**
	 * Repeats a string a given number of times.
	 *
	 * @param str   the string to repeat; must not be {@code null}
	 * @param times the number of repetitions; must be non-negative
	 * @return the concatenated result
	 */
	public static String repeat(String str, int times) {
		StringBuilder buffer = new StringBuilder(times * str.length());
		for (int i = 0; i < times; i++) {
			buffer.append(str);
		}
		return buffer.toString();

	}

	/**
	 * Parses a string to an integer value, returning a default on failure.
	 *
	 * @param value        a string to parse to an int; may be {@code null}
	 * @param defaultValue the value returned when {@code value} is not a valid integer
	 * @return the parsed integer, or {@code defaultValue} on parse failure
	 */
	public static int toInteger(String value, int defaultValue) {
		int ret = defaultValue;
		try {
			ret = Integer.parseInt(value);
		} catch (NumberFormatException e) {
		}
		return ret;
	}
}
