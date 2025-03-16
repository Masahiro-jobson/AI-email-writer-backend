package com.email.emai.writer;

import java.util.Map;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.web.reactive.function.client.WebClient;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;

@Service
public class EmailGeneratorService {

	// WebClient is an interface of springframework and it is injected in to
	// EmailGeneratorService
	// via Constructor. It makes HTTP requests such as sending requests to Gemini
	// and getting the response too.
	private final WebClient webClient;

	// Two of ${} are placeholders that refers to value
	// stored in application.properties or application.yml.
	// @Value annotation inject value into fields from external source.
	@Value("${gemini.api.url}")
	private String geminiApiUrl;
	@Value("${gemini.api.key}")
	private String geminiApiKey;

	// The build() method constructs a new WebClient instance.
	// The build WebClient is stored in this.webClient, which is
	// used to make HTTP requests.
	@Autowired
	public EmailGeneratorService(WebClient.Builder webClientBuilder) {
		this.webClient = webClientBuilder.build();
	}

	public String generateEmailReply(EmailRequest emailRequest) {
		// build the prompt(go to Gemini API)
		// This prompt stores the message that buildPrompt method stores
		String prompt = buildPrompt(emailRequest);

		// Craft a request
		// Map is interface of java.util, so we need to use HashMap<> but
		// we can use Map.of() instead too. Map.of has consistently key & value in the
		// parenthesis (which refers to Map <k, v>).
		// prompt is String data type but String extends Object class, so it can be
		// treated as
		// objects in the Map.of second parameter.
		// The code below fits in Gemini API prompt format.
		Map<String, Object> requestBody = Map.of("contents",
				new Object[] { Map.of("parts", new Object[] { Map.of("text", prompt) })

				});

		// Do request and get response
		String response = webClient.post().uri(geminiApiUrl + geminiApiKey).header("Content-Type", "application/json")
				.bodyValue(requestBody).retrieve().bodyToMono(String.class).block();

		// Extract response and Return
		return extractResponseContent(response);
	}

	private String extractResponseContent(String response) {
		try {
			// Create an ObjectMapper instance to parse the JSON response.
			ObjectMapper mapper = new ObjectMapper();
			// Convert the JSON string response into a JsonNode (root node).
			JsonNode rootNode = mapper.readTree(response);
			// Navigate through the JSON structure to retrieve the 'content' of the first
			// candidate. readTree(response) converts the JSON string response into a
			// JsonNode object, which is a tree-like representation of the JSON data.
			return rootNode.path("candidates")// Navigate to the 'candidates' array.
					.get(0)// Get the first candidate object (index 0).
					.path("content")// Navigate to the 'content' field within that candidate.
					.path("parts")// Get the 'parts' array inside the 'content' field.
					.get(0)// Get the first element in the 'parts' array.
					.path("text")// Get the 'text' field from the first part.
					.asText();// Convert that text field to a String.
			// Exception class e variable refers to the exception object that is inserted
			// when an error occurs in the try block.
			// Typically, e.getMessage returns the message as "Unrecognized tokens"
		} catch (Exception e) {
			return "Error processing request: " + e.getMessage();
		}
	}

	// make EmailRequest object in the arguments.prompt
	private String buildPrompt(EmailRequest emailRequest) {
		// Make StringBuilder object to store text.
		// StringBuilder class is used to create and manipulate mutable(modifiable)
		// strings
		// efficiently. However, String is immutable.
		// It means while the String object is immutable, its reference variable is not.
		StringBuilder prompt = new StringBuilder();
		prompt.append("Generate a professional email reply for the following email content"
				+ "Please don't generate a subject line");
		// Add base an instruction to prompt
		// Check if the tone field effxists in emailRequest variable and if so,
		// It add a tone instruction depending on .append(emailRequest.getTone())
		if (emailRequest.getTone() != null && !emailRequest.getTone().isEmpty()) {
			prompt.append("Use a ").append(emailRequest.getTone()).append("tone.");
		}
		prompt.append("\nOriginal email: \n").append(emailRequest.getEmailContent());
		// After getting email content from emairRequest class and return prompt after
		// converting StringBuilder object type to String type.
		return prompt.toString();

	}

}
