package guessmarket.engine.dto;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public class OrderBookParticipantInfo {

    private final String userName;

    private final List<Integer> sharesPerOption;

    private final List<Double> valuePerOption;


    public OrderBookParticipantInfo(
            String userName,
            List<Integer> sharesPerOption,
            List<Double> valuePerOption) {

        if (userName == null
                ||
                userName.trim().isEmpty()) {

            throw new IllegalArgumentException(
                    "User name cannot be empty."
            );
        }


        if (sharesPerOption == null
                ||
                valuePerOption == null) {

            throw new IllegalArgumentException(
                    "Participant information cannot be null."
            );
        }


        if (sharesPerOption.size()
                != valuePerOption.size()) {

            throw new IllegalArgumentException(
                    "Shares and values must contain the same number of options."
            );
        }


        this.userName =
                userName;


        this.sharesPerOption =
                new ArrayList<>(
                        sharesPerOption
                );


        this.valuePerOption =
                new ArrayList<>(
                        valuePerOption
                );
    }


    public String getUserName() {

        return userName;
    }


    public List<Integer> getSharesPerOption() {

        return Collections.unmodifiableList(
                sharesPerOption
        );
    }


    public List<Double> getValuePerOption() {

        return Collections.unmodifiableList(
                valuePerOption
        );
    }
}