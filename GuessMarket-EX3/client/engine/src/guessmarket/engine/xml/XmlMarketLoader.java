package guessmarket.engine.xml;

import guessmarket.engine.model.CommissionType;
import guessmarket.engine.model.MarketEvent;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.NodeList;
import org.xml.sax.SAXException;

import javax.xml.XMLConstants;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Locale;
import java.util.Set;

public final class XmlMarketLoader {
    private XmlMarketLoader() {
    }

    public static List<MarketEvent> loadAndValidate(Path xmlPath) throws IOException {
        validatePath(xmlPath);

        try {
            DocumentBuilderFactory factory = createSecureFactory();
            Document document = factory.newDocumentBuilder().parse(xmlPath.toFile());
            document.getDocumentElement().normalize();
            return parseEvents(document);
        } catch (ParserConfigurationException | SAXException exception) {
            throw new IOException("The XML document is malformed: " + readableMessage(exception), exception);
        }
    }

    private static DocumentBuilderFactory createSecureFactory() throws ParserConfigurationException {
        DocumentBuilderFactory factory = DocumentBuilderFactory.newInstance();
        factory.setFeature("http://apache.org/xml/features/disallow-doctype-decl", true);
        factory.setFeature("http://xml.org/sax/features/external-general-entities", false);
        factory.setFeature("http://xml.org/sax/features/external-parameter-entities", false);
        factory.setAttribute(XMLConstants.ACCESS_EXTERNAL_DTD, "");
        factory.setAttribute(XMLConstants.ACCESS_EXTERNAL_SCHEMA, "");
        factory.setXIncludeAware(false);
        factory.setExpandEntityReferences(false);
        return factory;
    }

    private static void validatePath(Path xmlPath) throws IOException {
        if (xmlPath == null) {
            throw new IOException("No file path was provided.");
        }
        if (!Files.exists(xmlPath)) {
            throw new IOException("The file does not exist: " + xmlPath);
        }
        if (!Files.isRegularFile(xmlPath)) {
            throw new IOException("The selected path is not a file: " + xmlPath);
        }
        if (!xmlPath.getFileName().toString().toLowerCase(Locale.ROOT).endsWith(".xml")) {
            throw new IOException("The selected file must have an .xml extension.");
        }
    }

    private static List<MarketEvent> parseEvents(Document document) throws IOException {
        Element root = document.getDocumentElement();
        if (root == null || !"Guess-Market".equals(root.getTagName())) {
            throw new IOException("The root element must be Guess-Market.");
        }

        NodeList eventNodes = document.getElementsByTagName("GM-event");
        if (eventNodes.getLength() == 0) {
            throw new IOException("The XML file does not contain any events.");
        }

        List<MarketEvent> events = new ArrayList<>();
        Set<Integer> eventIds = new HashSet<>();

        for (int index = 0; index < eventNodes.getLength(); index++) {
            Element eventElement = (Element) eventNodes.item(index);
            String eventPosition = "event number " + (index + 1);

            String name = eventElement.getAttribute("name").trim();
            if (name.isEmpty()) {
                throw new IOException("The name is missing in " + eventPosition + ".");
            }

            int id = parseInteger(requiredText(eventElement, "id", eventPosition), "id", eventPosition);
            if (!eventIds.add(id)) {
                throw new IOException("Duplicate event id " + id + " was found.");
            }

            String description = requiredText(eventElement, "description", eventPosition).trim();
            if (description.isEmpty()) {
                throw new IOException("The description is empty in event id " + id + ".");
            }

            Element commissionElement = requiredElement(eventElement, "comision", eventPosition);
            int commissionPercent = parseInteger(
                    commissionElement.getTextContent().trim(),
                    "commission",
                    "event id " + id);
            if (commissionPercent < 0 || commissionPercent > 90) {
                throw new IOException(
                        "Commission for event id " + id
                                + " must be between 0 and 90, but was " + commissionPercent + ".");
            }

            CommissionType commissionType;
            try {
                commissionType = CommissionType.fromXml(commissionElement.getAttribute("type"));
            } catch (IllegalArgumentException exception) {
                throw new IOException(exception.getMessage() + " in event id " + id + ".", exception);
            }

            Element optionsElement = requiredElement(eventElement, "GM-options", eventPosition);
            NodeList optionNodes = optionsElement.getElementsByTagName("GM-option");
            if (optionNodes.getLength() != 2) {
                throw new IOException(
                        "Event id " + id + " must contain exactly two options, but contains "
                                + optionNodes.getLength() + ".");
            }

            List<String> optionNames = new ArrayList<>();
            for (int optionIndex = 0; optionIndex < optionNodes.getLength(); optionIndex++) {
                String optionName = optionNodes.item(optionIndex).getTextContent().trim();
                if (optionName.isEmpty()) {
                    throw new IOException("Event id " + id + " contains an empty option.");
                }
                optionNames.add(optionName);
            }

            Element lmsrElement = requiredElement(eventElement, "GM-LMSR", eventPosition);
            int liquidityParameter = parseInteger(
                    requiredText(lmsrElement, "b", eventPosition),
                    "b",
                    "event id " + id);
            if (liquidityParameter <= 0) {
                throw new IOException("The LMSR b value for event id " + id + " must be greater than zero.");
            }

            events.add(new MarketEvent(
                    id,
                    name,
                    description,
                    commissionPercent,
                    commissionType,
                    optionNames,
                    liquidityParameter));
        }

        return events;
    }

    private static Element requiredElement(Element parent, String tagName, String context) throws IOException {
        NodeList nodes = parent.getElementsByTagName(tagName);
        if (nodes.getLength() == 0) {
            throw new IOException("The element " + tagName + " is missing in " + context + ".");
        }
        return (Element) nodes.item(0);
    }

    private static String requiredText(Element parent, String tagName, String context) throws IOException {
        return requiredElement(parent, tagName, context).getTextContent().trim();
    }

    private static int parseInteger(String value, String fieldName, String context) throws IOException {
        try {
            return Integer.parseInt(value);
        } catch (NumberFormatException exception) {
            throw new IOException(
                    "The " + fieldName + " value in " + context + " must be a whole number.",
                    exception);
        }
    }

    private static String readableMessage(Exception exception) {
        String message = exception.getMessage();
        return message == null || message.isBlank() ? exception.getClass().getSimpleName() : message;
    }
}
