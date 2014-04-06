package util;

import java.io.File;
import java.io.FilenameFilter;
import java.util.regex.Pattern;

import javax.swing.filechooser.FileFilter;

public class GlobFilter extends FileFilter implements FilenameFilter {
	private String pattern;
	private Pattern regex;
	private boolean matchDirs;

	public GlobFilter(String pattern) {
		this.pattern = pattern;
		regex = globToRegex(pattern);
	}

	public GlobFilter(String pattern, boolean dirs) {
		this.pattern = pattern;
		regex = globToRegex(pattern);
		matchDirs = dirs;
	}
	
	public static Pattern globToRegex(String in) {
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
		if (matchDirs && new File(dir,name).isDirectory())
			return true;
		return accept(name);
	}

	@Override
	public boolean accept(File f) {
		if (matchDirs && f.isDirectory())
			return true;
		return accept(f.getName());
	}
	
	public boolean accept(String name) {
		return regex.matcher(name).matches();
	}

	@Override
	public String getDescription() {
		return pattern;
	}
}