package bn;

import java.io.IOException;

public class FileFormatException extends IOException {

	private static final long serialVersionUID = 1L;

	public FileFormatException() {
	}

	public FileFormatException(String message) {
		super(message);
	}

	public FileFormatException(Throwable cause) {
		super(cause);
	}

	public FileFormatException(String message, Throwable cause) {
		super(message, cause);
	}

}
