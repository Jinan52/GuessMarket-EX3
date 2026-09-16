package guessmarket.engine;

import guessmarket.generated.GuessMarket;

import java.io.File;

public class XmlLoaderTest {

    public static void main(String[] args) {
        try {
            XmlMarketLoaderEx2 loader = new XmlMarketLoaderEx2();

            File file = new File(
                    "course-materials/EX3-small.xml"
            );

            GuessMarket market = loader.load(file);

            System.out.println("XML loaded successfully");
            System.out.println("Events: " + market.getGMEvents().getGMEvent().size());

        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}
