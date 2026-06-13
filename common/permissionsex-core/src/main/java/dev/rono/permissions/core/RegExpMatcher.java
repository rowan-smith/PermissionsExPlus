package dev.rono.permissions.core;

import com.google.common.cache.CacheBuilder;
import com.google.common.cache.CacheLoader;
import com.google.common.cache.LoadingCache;
import ru.tehkode.permissions.PermissionMatcher;

import java.util.concurrent.ExecutionException;
import java.util.logging.Level;
import java.util.logging.Logger;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.regex.PatternSyntaxException;

public class RegExpMatcher implements PermissionMatcher {
	public static final String RAW_REGEX_CHAR = "$";
	private static final Logger LOGGER = Logger.getLogger(RegExpMatcher.class.getName());
	protected static Pattern rangeExpression = Pattern.compile("(\\d+)-(\\d+)");

	private final LoadingCache<String, Pattern> patternCache = CacheBuilder.newBuilder().maximumSize(500).build(new CacheLoader<String, Pattern>() {
		@Override
		public Pattern load(String permission) throws Exception {
			return createPattern(permission);
		}
	});

	@Override
	public boolean isMatches(String expression, String permission) {
		try {
			Pattern permissionMatcher = patternCache.get(expression);
			return permissionMatcher.matcher(permission).matches();
		} catch (ExecutionException e) {
			LOGGER.log(Level.WARNING, "While checking regex match for " + permission + " against " + expression, e);
			return false;
		}


	}

	protected static Pattern createPattern(String expression) {
        try {
		    return Pattern.compile(prepareRegexp(expression), Pattern.CASE_INSENSITIVE);
        } catch (PatternSyntaxException e) {
            return Pattern.compile(Pattern.quote(expression), Pattern.CASE_INSENSITIVE);
        }
	}

	public static String prepareRegexp(String expression) {
		if (expression.startsWith("-")) {
			expression = expression.substring(1);
		}

		if (expression.startsWith("#")) {
			expression = expression.substring(1);
		}

		boolean rawRegexp = expression.startsWith(RAW_REGEX_CHAR);
		if (rawRegexp) {
			expression = expression.substring(1);
		}

		String regexp = rawRegexp ? expression : expression.replace(".", "\\.").replace("*", "(.*)");

		try {
			Matcher rangeMatcher = rangeExpression.matcher(regexp);
			StringBuffer expanded = new StringBuffer();
			while (rangeMatcher.find()) {
				StringBuilder range = new StringBuilder();
				int from = Integer.parseInt(rangeMatcher.group(1));
				int to = Integer.parseInt(rangeMatcher.group(2));

				if (from > to) {
					int temp = from;
					from = to;
					to = temp;
				}

				range.append("(");
				for (int i = from; i <= to; i++) {
					range.append(i);
					if (i < to) {
						range.append("|");
					}
				}
				range.append(")");
				rangeMatcher.appendReplacement(expanded, Matcher.quoteReplacement(range.toString()));
			}
			rangeMatcher.appendTail(expanded);
			regexp = expanded.toString();
		} catch (Throwable ignore) {
			// Keep regexp unchanged if range expansion fails
		}

		return regexp;
	}
}
