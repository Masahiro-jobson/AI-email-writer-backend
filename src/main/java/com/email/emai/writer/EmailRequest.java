package com.email.emai.writer;

import lombok.Data;

//@Data makes it easier to write getter, setter & constructors
@Data
public class EmailRequest {
	private String emailContent;
	// Tone is how reply to the mail such as professional sarcastic, casual.
	private String tone;

}
