package example.document;

import org.springframework.stereotype.Component;

import com.github.resource4j.resources.references.ResourceFileReference;
import com.github.resource4j.spring.annotations.InjectResource;

@Component
public class DocumentTemplate {

	@InjectResource("/example/document/logo.png")
	private byte[] logo;
	
	@InjectResource(value = "*.txt", required = false)
	private ResourceFileReference content;

	public byte[] getLogo() {
		return logo;
	}

	public ResourceFileReference getContent() {
		return content;
	}
	
}
