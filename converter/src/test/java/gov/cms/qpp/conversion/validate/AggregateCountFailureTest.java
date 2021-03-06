package gov.cms.qpp.conversion.validate;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.ObjectWriter;
import gov.cms.qpp.conversion.Converter;
import gov.cms.qpp.conversion.PathQrdaSource;
import gov.cms.qpp.conversion.model.error.AllErrors;
import gov.cms.qpp.conversion.model.error.TransformException;
import org.junit.Test;

import java.io.IOException;
import java.nio.file.Paths;

import static com.google.common.truth.Truth.assertWithMessage;
import static org.junit.Assert.fail;

public class AggregateCountFailureTest {

	@Test
	public void testInvalidAggregateCounts() throws IOException {
		//execute
		Converter converter = new Converter(new PathQrdaSource(Paths.get("src/test/resources/negative/angerTheConverter.xml")));

		String errorContent = "";
		try {
			converter.transform();
			fail("A transformation exception must have been thrown!");
		} catch(TransformException exception) {
			AllErrors errors = exception.getDetails();
			ObjectWriter jsonObjectWriter = new ObjectMapper()
				.setSerializationInclusion(JsonInclude.Include.NON_NULL)
				.writer()
				.withDefaultPrettyPrinter();
			errorContent = jsonObjectWriter.writeValueAsString(errors);
		}

		//assert
		assertWithMessage("The error file flags a aggregate count type error")
				.that(errorContent)
				.contains(String.format(CommonNumeratorDenominatorValidator.NOT_AN_INTEGER_VALUE, "Numerator"));

		assertWithMessage("The error file flags a aggregate count value error")
				.that(errorContent)
				.contains(String.format(CommonNumeratorDenominatorValidator.INVALID_VALUE, "Denominator"));
	}

}
