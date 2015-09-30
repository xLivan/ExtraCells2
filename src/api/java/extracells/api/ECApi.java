package extracells.api;

import org.apache.logging.log4j.LogManager;

final public class ECApi {
    private static IECApi instance = null;
    public static IECApi instance() {
        if (instance == null) {
            try {
                instance = (IECApi) Class.forName("extracells.core.ECApiInstance$")
                        .getField("MODULE$").get(null);
            } catch (Exception e) {
                LogManager.getLogger("ExtraCells|API")
                        .error("Unable to get API instance!", e);
            }
        }
        return instance;
    }
}
