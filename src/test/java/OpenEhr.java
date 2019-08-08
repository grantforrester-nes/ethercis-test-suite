import io.restassured.RestAssured;

public class OpenEhr {

    public static final String DEFAULT_BASE_URL = "http://localhost:8084";

    private static String baseUrl = System.getProperty("OPENEHR_BASE_URL", DEFAULT_BASE_URL);

    static String login() {
        return RestAssured
            .post(baseUrl + "/rest/v1/session?username=guest&password=guest")
            .then().statusCode(200).extract().path("sessionId");
    }

    static void logout(String sessionId) {
        RestAssured
            .with().header("Ehr-Session", sessionId)
            .delete(baseUrl + "/rest/v1/session")
            .then().statusCode(200);
    }

    static String createEHR(String sessionId, String subjectId, String subjectNamespace) {
        return RestAssured
            .with().header("Ehr-Session", sessionId)
            .post(baseUrl + "/rest/v1/ehr?subjectId=" + subjectId + "&subjectNamespace=" + subjectNamespace)
            .then().statusCode(200).extract().path("ehrId");
    }

    static String createComposition(String sessionId, String ehrId, String templateId, String composition) {
        return RestAssured
            .with().header("Ehr-Session", sessionId)
            .body(composition)
            .post(baseUrl + "/rest/v1/composition?ehrId=" + ehrId + "&templateId=" + templateId + "&format=FLAT")
            .then().statusCode(200).extract().path("compositionUid");
    }

    static void readComposition(String sessionId, String compositionId) {
        RestAssured
            .with().header("Ehr-Session", sessionId)
            .get(baseUrl + "/rest/v1/composition/" + compositionId)
            .then().statusCode(200);
    }
}
