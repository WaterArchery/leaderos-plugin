package net.leaderos.shared;

import lombok.Getter;
import net.leaderos.shared.helpers.DebugAPI;

/**
 * @author poyrazinan
 * @since 1.0
 */
@Getter
public class Shared {

    /**
     * Shared instance
     */
    @Getter
    private static Shared instance;

    /**
     * Link of request
     */
    @Getter
    private static String link;

    /**
     * ApiKey for request
     */
    @Getter
    private static String apiKey;

    /**
     * DebugAPI for debug requests
     */
    @Getter
    private static DebugAPI debugAPI;

    /**
     * Constructor of shared
     *
     * @param link api link
     * @param apiKey api key
     * @param debugAPI for debug
     */
    public Shared(String link, String apiKey, DebugAPI debugAPI) {
        Shared.link = link;
        Shared.apiKey = apiKey;
        Shared.debugAPI = debugAPI;
        instance = this;
    }
}
