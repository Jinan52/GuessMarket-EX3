package guessmarket.engine.model;

public enum CommissionType {
    ON_PURCHASE("on-purchase"),
    ON_CLOSE("on-close");

    private final String xmlValue;

    CommissionType(String xmlValue) {
        this.xmlValue = xmlValue;
    }

    public String xmlValue() {
        return xmlValue;
    }

    public static CommissionType fromXml(String value) {
        for (CommissionType type : values()) {
            if (type.xmlValue.equalsIgnoreCase(value)) {
                return type;
            }
        }
        throw new IllegalArgumentException("Unsupported commission type: " + value);
    }
}
