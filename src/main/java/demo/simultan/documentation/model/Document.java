package demo.simultan.documentation.model;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.ToString;

import java.io.Serializable;

@Getter
@ToString
@EqualsAndHashCode
@SuppressWarnings("serial")
public class Document implements Serializable {
	@JsonProperty("id")
	private String id;

	@JsonProperty("number")
	private Integer number;

	@JsonProperty("name")
	private String name;

	@JsonCreator
	@lombok.Builder(builderClassName = "Builder", toBuilder = true)
	Document(@JsonProperty("id") String id,
			 @JsonProperty("number") Integer number,
			 @JsonProperty("name") String name) {
		this.id = id;
		this.number = number;
		this.name = name;
	}
}