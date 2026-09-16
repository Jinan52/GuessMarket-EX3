package guessmarket.client;

public final class ClientSession {

    private static String currentUserName;


    private ClientSession() {
    }


    public static void setCurrentUserName(
            String userName) {

        currentUserName = userName;
    }


    public static String getCurrentUserName() {

        return currentUserName;
    }


    public static void clear() {

        currentUserName = null;
    }
}
