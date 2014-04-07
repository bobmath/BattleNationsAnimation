package util;

import java.io.File;
import java.io.FilenameFilter;
import java.util.regex.Pattern;

public class GlobFilter implements FilenameFilter {
	private Pattern regex;

	public GlobFilter(String pattern) {
		regex = globToPattern(pattern);
	}

	public static Pattern globToPattern(String in) {
		StringBuilder out = new StringBuilder();
		out.append('^');
		for (int i = 0, len = in.length(); i < len; i++) {
			char c = in.charAt(i);
			switch (c) {
			case '*': out.append(".*"); break;
			case '.': out.append("\\."); break;
			case '?': out.append('.'); break;
			case ',': out.append("$|^"); break;
			default: out.append(c); break;
			}
		}
		out.append('$');
		return Pattern.compile(out.toString(), Pattern.CASE_INSENSITIVE);
	}

	@Override
	public boolean accept(File dir, String name) {
		return regex.matcher(name).matches();
	}

}