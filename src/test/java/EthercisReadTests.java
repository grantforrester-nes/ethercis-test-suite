import io.restassured.RestAssured;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicInteger;

public class EthercisReadTests {

    private static final String COMPOSITION_ID = "4fb6d040-2dce-447d-844e-a3d16ef9147a::ethercis::1";
    public static final String BASE_URL = "http://localhost:8084";

    private AtomicInteger failureCount;

    @BeforeEach
    public void setup() {
        failureCount = new AtomicInteger(0);
    }

    @Test
    public void readComposition100Times()  {
        String sessionId = login();
        try {
            for (int i = 0; i < 100; i++) {
                System.out.println("(" + Thread.currentThread().getName() + ") test: " + i);
                try {
                    readComposition(sessionId, COMPOSITION_ID);
                } catch (Error e) {
                    e.printStackTrace();
                    failureCount.incrementAndGet();
                }
            }
        } finally {
            logout(sessionId);
        }

        Assertions.assertEquals(0, failureCount.get());
    }

    @Test
    public void parallelTest() throws Exception {

        ExecutorService executor = Executors.newFixedThreadPool(10);
        executor.submit(() -> {
            readComposition100Times();
        });
        executor.submit(() -> {
            readComposition100Times();
        });
        executor.awaitTermination(30, TimeUnit.SECONDS);

        Assertions.assertEquals(0, failureCount);
    }

    private String login() {
        return RestAssured
            .post(BASE_URL + "/rest/v1/session?username=guest&password=guest")
            .then().statusCode(200).extract().path("sessionId");
    }


    private void logout(String sessionId) {
        RestAssured
            .with().header("Ehr-Session", sessionId)
            .delete(BASE_URL + "/rest/v1/session")
            .then().statusCode(200);
    }

    private void readComposition(String sessionId, String compositionId) {
        RestAssured
            .with().header("Ehr-Session", sessionId)
            .get(BASE_URL + "/rest/v1/composition/" + compositionId)
            .then().statusCode(200);
    }
}
