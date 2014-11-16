package com.nexuspad.service.util;

/**
 * Created by ren on 7/27/14.
 */
public class InputParser {
	public final static boolean isValidEmail(String email) {
		if (email == null) {
			return false;
		} else {
			return android.util.Patterns.EMAIL_ADDRESS.matcher(email).matches();
		}
	}
}
