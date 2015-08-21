package extracells.api;

public class ECApi {
    private static IECApi instance = null;
    public static IECApi instance() {
        if (instance == null) {
            try {
                instance = (IECApi) Class.forName("extracells.ECApiInstance")
                        .getMethod("instance").invoke(null);
            } catch (Exception e) {
            }
        }
        return instance;
    }
}
