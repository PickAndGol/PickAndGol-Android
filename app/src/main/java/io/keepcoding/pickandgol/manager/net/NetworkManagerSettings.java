package io.keepcoding.pickandgol.manager.net;

/**
 * This class contains static settings to be used by the NewtworkManager class.
 */
public class NetworkManagerSettings {

    // Possible 'result' values for JSON responses
    public static final String JSON_RESULT_OK = "OK";
    public static final String JSON_RESULT_ERROR = "ERROR";

    // Remote URLs and endpoints
    public static final String URL_LOGIN = "http://10.0.2.2:3000/api/v1/users/login";
    public static final String URL_REGISTER_USER = "http://www.mocky.io/v2/58a547fb290000fb076d69c6";
    public static final String URL_USER_DETAIL = "http://10.0.2.2:3000/api/v1/users";
    public static final String URL_EVENTS = "http://10.0.2.2:3000/api/v1/events";

    // Available JSON response types expected from the server
    public static enum JsonResponseType {

        LOGIN,      // LoginResponse
        USER,       // UserResponse
        EVENTS      // EventsResponse
    }
}
