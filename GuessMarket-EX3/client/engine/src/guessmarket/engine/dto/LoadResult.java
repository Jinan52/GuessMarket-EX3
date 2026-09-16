package guessmarket.engine.dto;

public record LoadResult(boolean success, String message, int loadedEventsCount) {
    public static LoadResult success(int loadedEventsCount) {
        return new LoadResult(
                true,
                "The XML file is valid and was loaded successfully.",
                loadedEventsCount);
    }

    public static LoadResult failure(String message) {
        return new LoadResult(false, message, 0);
    }
}
