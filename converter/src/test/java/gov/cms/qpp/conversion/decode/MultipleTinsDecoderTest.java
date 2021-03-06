package gov.cms.qpp.conversion.decode;

import gov.cms.qpp.TestHelper;
import gov.cms.qpp.conversion.Context;
import gov.cms.qpp.conversion.model.Node;
import gov.cms.qpp.conversion.model.TemplateId;
import gov.cms.qpp.conversion.xml.XmlException;
import gov.cms.qpp.conversion.xml.XmlUtils;
import org.jdom2.Element;
import org.jdom2.xpath.XPathHelper;
import org.junit.Before;
import org.junit.Test;

import java.io.IOException;
import java.util.List;

import static com.google.common.truth.Truth.assertThat;
import static com.google.common.truth.Truth.assertWithMessage;

/**
 * Test class for MultipleTinsDecoder
 */
public class MultipleTinsDecoderTest {

	private static final String TEST_NPI1 = "NPI-1";
	private static final String TEST_NPI2 = "NPI-2";
	private static final String TEST_NPI3 = "NPI-3";
	private static final String TEST_TIN1 = "TIN-1";
	private static final String TEST_TIN2 = "TIN-2";
	private static final String TEST_TIN3 = "TIN-3";

	private Context context;

	@Before
	public void setup() {
		context = new Context();
	}

	@Test
	public void internalDecode() throws Exception {
		Element multipleTinsElement = makeTestElement();
		List<Node> children = getTestChildren(multipleTinsElement);
		assertThat(children).hasSize(4);
		int matches = countMatches(children);

		// Assert that one child TIN is TIN-1 and NPI is NPI-1
		// Assert that one child TIN is TIN-2 and NPI is NPI-2
		// Assert that one child TIN is TIN-3 and NPI is NPI-3
		// Assert that one child is ClinicalDocument
		assertThat(matches).isEqualTo(4);
	}

	private int countMatches(List<Node> children) {
		int matches = 0;
		String tin = null;
		String npi = null;
		for (Node child : children) {
			if (child.getType() == TemplateId.NPI_TIN_ID) {
				npi = child.getValue(MultipleTinsDecoder.NATIONAL_PROVIDER_IDENTIFIER);
				tin = child.getValue(MultipleTinsDecoder.TAX_PAYER_IDENTIFICATION_NUMBER);
				matches += testChildExistence(npi, tin);
			} else if (child.getType() == TemplateId.CLINICAL_DOCUMENT) {
				matches++;
			}
		}
		return matches;
	}

	@Test
	public void testNullNPI() throws Exception {

		Element multipleTinsElement = makeTestElementMissingNPI();
		List<Node> children = getTestChildren(multipleTinsElement);
		assertWithMessage("Expect that there are three children")
				.that(children)
				.hasSize(3);
		int matches = countMatches(children);
		assertWithMessage("The correct children were decoded")
				.that(matches)
				.isEqualTo(3);
	}

	@Test
	public void testNullTIN() throws Exception {
		Element multipleTinsElement = makeTestElementMissingTIN();
		List<Node> children = getTestChildren(multipleTinsElement);
		assertWithMessage("Expect that there are three children")
				.that(children)
				.hasSize(3);
		int matches = countMatches(children);
		assertWithMessage("The correct children were decoded")
				.that(matches)
				.isEqualTo(3);
	}

	@Test
	public void testNullTINID() throws Exception {
		Element multipleTinsElement = makeTestElementMissingTINID();
		List<Node> children = getTestChildren(multipleTinsElement);
		assertWithMessage("Expect that there are four children")
				.that(children)
				.hasSize(4);
	}

	@Test
	public void testNullNPIID() throws Exception {
		Element multipleTinsElement = makeTestElementMissingNPIID();
		List<Node> children = getTestChildren(multipleTinsElement);
		assertWithMessage("Expect that there are four children")
				.that(children)
				.hasSize(4);
	}

	@Test
	public void testNullTINIDAndNPIID() throws Exception {
		Element multipleTinsElement = makeTestElementMissingTINIDAndNPIID();
		List<Node> children = getTestChildren(multipleTinsElement);
		assertWithMessage("Expect that there are three children")
				.that(children)
				.hasSize(3);
	}

	@Test
	public void testNullTaxEl() throws Exception {
		Element multipleTinsElement = makeTestElementMissingTaxEl();
		List<Node> children = getTestChildren(multipleTinsElement);
		assertWithMessage("Expect that there are three children")
				.that(children)
				.hasSize(3);
	}

	@Test
	public void testPathIsSet() throws Exception {
		Element multipleTinsElement = makeTestElementMissingTaxEl();
		Node child = getTestChildren(multipleTinsElement)
				.stream()
				.filter(node -> node.getType() == TemplateId.CLINICAL_DOCUMENT)
				.findFirst()
				.get();
		assertWithMessage("Expect that child has an xpath")
				.that(child.getPath())
				.isEqualTo(XPathHelper.getAbsolutePath(multipleTinsElement));
	}

	private int testChildExistence(String npi, String tin) {
		int match = 0;
		if (TEST_NPI1.equals(npi) && TEST_TIN1.equals(tin)) {
			match = 1;
		} else if (TEST_NPI2.equals(npi) && TEST_TIN2.equals(tin)) {
			match = 1;
		} else if (TEST_NPI3.equals(npi) && TEST_TIN3.equals(tin)) {
			match = 1;
		}
		return match;
	}

	private List<Node> getTestChildren(Element multipleTinsElement) {
		Node mulipleTinsNode = new Node();
		MultipleTinsDecoder decoder = new MultipleTinsDecoder(context);
		decoder.setNamespace(multipleTinsElement, decoder);
		decoder.internalDecode(multipleTinsElement, mulipleTinsNode);
		return mulipleTinsNode.getChildNodes();
	}

	private Element makeTestElement() throws IOException, XmlException {
		String xmlFragment = TestHelper.getFixture("multiTinMinimal.xml");
		Element ele = XmlUtils.stringToDom(xmlFragment);
		return ele;
	}

	private Element makeTestElementMissingNPI() throws IOException, XmlException {
		String xmlFragment = TestHelper.getFixture("multiTinMinimal.xml");
		xmlFragment = xmlFragment.replace("<id root=\"2.16.840.1.113883.4.6\" extension=\"NPI-1\"/>", "");
		Element ele = XmlUtils.stringToDom(xmlFragment);
		return ele;
	}

	private Element makeTestElementMissingTIN() throws IOException, XmlException {
		String xmlFragment = TestHelper.getFixture("multiTinMinimal.xml");
		xmlFragment = xmlFragment.replace("<id root=\"2.16.840.1.113883.4.2\" extension=\"TIN-1\"/>", "");
		Element ele = XmlUtils.stringToDom(xmlFragment);
		return ele;
	}

	private Element makeTestElementMissingTaxEl() throws IOException, XmlException {
		String xmlFragment = TestHelper.getFixture("multiTinMinimal.xml");
		xmlFragment = xmlFragment.replaceFirst("<representedOrganization>", "")
				.replaceFirst("</representedOrganization>", "");
		Element ele = XmlUtils.stringToDom(xmlFragment);
		return ele;
	}

	private Element makeTestElementMissingNPIID() throws IOException, XmlException {
		String xmlFragment = TestHelper.getFixture("multiTinMinimal.xml");
		xmlFragment = xmlFragment.replace("extension=\"NPI-1\"", "");
		Element ele = XmlUtils.stringToDom(xmlFragment);
		return ele;
	}

	private Element makeTestElementMissingTINID() throws IOException, XmlException {
		String xmlFragment = TestHelper.getFixture("multiTinMinimal.xml");
		xmlFragment = xmlFragment.replace("extension=\"TIN-1\"", "");
		Element ele = XmlUtils.stringToDom(xmlFragment);
		return ele;
	}

	private Element makeTestElementMissingTINIDAndNPIID() throws IOException, XmlException {
		String xmlFragment = TestHelper.getFixture("multiTinMinimal.xml");
		xmlFragment = xmlFragment.replace("extension=\"NPI-1\"", "");
		xmlFragment = xmlFragment.replace("extension=\"TIN-1\"", "");
		Element ele = XmlUtils.stringToDom(xmlFragment);
		return ele;
	}
}
