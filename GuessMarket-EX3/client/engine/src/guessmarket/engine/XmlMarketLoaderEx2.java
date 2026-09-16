package guessmarket.engine;

import guessmarket.engine.model.CommissionType;
import guessmarket.generated.GMEvent;
import guessmarket.generated.GMLMSR;
import guessmarket.generated.GMOrderBook;
import guessmarket.generated.GuessMarket;
import jakarta.xml.bind.JAXBContext;
import jakarta.xml.bind.JAXBException;
import jakarta.xml.bind.Unmarshaller;

import java.io.File;
import java.io.InputStream;
import java.util.List;

public class XmlMarketLoaderEx2 {

    public GuessMarket load(File xmlFile) throws JAXBException {

        validateFile(xmlFile);

        JAXBContext context =
                JAXBContext.newInstance(GuessMarket.class);

        Unmarshaller unmarshaller =
                context.createUnmarshaller();

        GuessMarket market =
                (GuessMarket) unmarshaller.unmarshal(xmlFile);

        return finishLoad(market);
    }


    public GuessMarket load(InputStream xmlSource) throws JAXBException {

        if (xmlSource == null) {
            throw new IllegalArgumentException(
                    "No XML content was provided."
            );
        }

        JAXBContext context =
                JAXBContext.newInstance(GuessMarket.class);

        Unmarshaller unmarshaller =
                context.createUnmarshaller();

        GuessMarket market =
                (GuessMarket) unmarshaller.unmarshal(xmlSource);

        return finishLoad(market);
    }


    private GuessMarket finishLoad(GuessMarket market) {

        if (market.getGMEvents() == null
                || market.getGMEvents().getGMEvent() == null
                || market.getGMEvents().getGMEvent().isEmpty()) {
            throw new IllegalArgumentException(
                    "The XML file does not contain any events."
            );
        }

        trimXmlStrings(market);

        validateUniqueEventNames(market);
        validateCommission(market);
        validateEventNameAndDescription(market);
        validateOptionNames(market);
        validateMarketMethods(market);

        return market;
    }

    private void trimXmlStrings(GuessMarket market) {

        for (GMEvent event :
                market.getGMEvents().getGMEvent()) {

            if (event.getName() != null) {
                event.setName(event.getName().trim());
            }

            if (event.getDescription() != null) {
                event.setDescription(event.getDescription().trim());
            }

            if (event.getCommission() != null
                    && event.getCommission().getType() != null) {
                event.getCommission().setType(
                        event.getCommission().getType().trim()
                );
            }

            if (event.getGMOptions() != null) {
                List<String> options =
                        event.getGMOptions().getGMOption();
                for (int i = 0; i < options.size(); i++) {
                    String option = options.get(i);
                    if (option != null) {
                        options.set(i, option.trim());
                    }
                }
            }

            if (event.getGMMethod() != null
                    && event.getGMMethod().getGMOrderBook() != null) {
                GMOrderBook orderBook =
                        event.getGMMethod().getGMOrderBook();
                if (orderBook.getAllowMint() != null) {
                    orderBook.setAllowMint(
                            orderBook.getAllowMint().trim()
                    );
                }
            }
        }
    }

    private void validateFile(File xmlFile) {

        if (xmlFile == null || !xmlFile.exists()) {
            throw new IllegalArgumentException(
                    "The selected file does not exist."
            );
        }

        if (!xmlFile.getName().toLowerCase().endsWith(".xml")) {
            throw new IllegalArgumentException(
                    "The selected file must be an XML file."
            );
        }
    }

    private void validateUniqueEventNames(GuessMarket market) {

        List<GMEvent> events =
                market.getGMEvents().getGMEvent();

        for (int i = 0; i < events.size(); i++) {

            GMEvent event1 = events.get(i);

            if (event1.getName() == null) {
                continue;
            }

            for (int j = i + 1; j < events.size(); j++) {

                GMEvent event2 = events.get(j);

                if (event1.getName().equals(event2.getName())) {
                    throw new IllegalArgumentException(
                            "Duplicate event name: " + event1.getName()
                    );
                }
            }
        }
    }

    private void validateCommission(GuessMarket market) {

        for (GMEvent event :
                market.getGMEvents().getGMEvent()) {

            if (event.getCommission() == null) {
                throw new IllegalArgumentException(
                        "Commission is missing in event "
                                + eventLabel(event)
                );
            }

            int commission =
                    event.getCommission().getValue();

            if (commission < 0 || commission > 90) {
                throw new IllegalArgumentException(
                        "Commission must be between 0 and 90 for event "
                                + eventLabel(event)
                );
            }

            try {
                CommissionType.fromXml(
                        event.getCommission().getType()
                );
            } catch (IllegalArgumentException exception) {
                throw new IllegalArgumentException(
                        exception.getMessage()
                                + " in event "
                                + eventLabel(event)
                );
            }
        }
    }

    private void validateEventNameAndDescription(
            GuessMarket market) {

        for (GMEvent event :
                market.getGMEvents().getGMEvent()) {

            if (event.getName() == null
                    || event.getName().isEmpty()) {
                throw new IllegalArgumentException(
                        "The name is missing in an event."
                );
            }

            if (event.getDescription() == null
                    || event.getDescription().isEmpty()) {
                throw new IllegalArgumentException(
                        "The description is empty in event "
                                + event.getName()
                );
            }
        }
    }

    private void validateOptionNames(GuessMarket market) {

        for (GMEvent event :
                market.getGMEvents().getGMEvent()) {

            if (event.getGMOptions() == null
                    || event.getGMOptions().getGMOption() == null
                    || event.getGMOptions().getGMOption().isEmpty()) {
                throw new IllegalArgumentException(
                        "Event "
                                + eventLabel(event)
                                + " does not contain any options."
                );
            }

            List<String> options =
                    event.getGMOptions().getGMOption();

            for (int i = 0; i < options.size(); i++) {
                String optionName = options.get(i);
                if (optionName == null || optionName.isEmpty()) {
                    throw new IllegalArgumentException(
                            "Event "
                                    + eventLabel(event)
                                    + " contains an empty option."
                    );
                }
            }
        }
    }

    private void validateMarketMethods(GuessMarket market) {

        for (GMEvent event :
                market.getGMEvents().getGMEvent()) {

            if (event.getGMMethod() == null) {
                throw new IllegalArgumentException(
                        "Event "
                                + eventLabel(event)
                                + " does not have a market method."
                );
            }

            GMLMSR lmsr =
                    event.getGMMethod().getGMLMSR();

            GMOrderBook orderBook =
                    event.getGMMethod().getGMOrderBook();

            if (lmsr != null) {

                if (lmsr.getB() <= 0) {
                    throw new IllegalArgumentException(
                            "The LMSR b value for event "
                                    + eventLabel(event)
                                    + " must be greater than zero."
                    );
                }

                continue;
            }

            if (orderBook != null) {

                if (orderBook.getInitial() < 0) {
                    throw new IllegalArgumentException(
                            "Order Book initial value cannot be negative in event "
                                    + eventLabel(event)
                    );
                }

                if (orderBook.getD() <= 0) {
                    throw new IllegalArgumentException(
                            "Order Book d value must be greater than zero in event "
                                    + eventLabel(event)
                    );
                }

                String allowMint =
                        orderBook.getAllowMint();

                if (allowMint == null
                        ||
                        !(allowMint.equalsIgnoreCase("true")
                                || allowMint.equalsIgnoreCase("false"))) {
                    throw new IllegalArgumentException(
                            "Order Book allow-mint must be true or false in event "
                                    + eventLabel(event)
                    );
                }

                continue;
            }

            throw new IllegalArgumentException(
                    "Event "
                            + eventLabel(event)
                            + " does not have a market method."
            );
        }
    }

    private String eventLabel(GMEvent event) {

        if (event.getName() == null || event.getName().isEmpty()) {
            return "(unnamed)";
        }

        return event.getName();
    }
}
