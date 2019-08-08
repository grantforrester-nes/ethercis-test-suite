import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.io.IOException;
import java.net.URISyntaxException;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.UUID;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicInteger;

public class EthercisConcurrencyTests {

    private static final String COMPOSITION_ID = "1daf820c-4982-4509-a059-fd7f464ea40d::local.ethercis.com::1";

    public static final String NAMESPACE = "uk.nhs.nhs_number";
    public static final String TEMPLATE = "RESPECT_NSS-v0";
    public static final String COMPOSITION_FILENAME = "respect.json";

    private AtomicInteger failureCount;

    @BeforeEach
    public void setup() {
        failureCount = new AtomicInteger(0);
    }


    @Test
    public void createComposition() throws Exception {
        String sessionId = OpenEhr.login();
        String subjectId = UUID.randomUUID().toString();
        String composition = readFileFromClasspath(COMPOSITION_FILENAME);

        String ehrId = OpenEhr.createEHR(sessionId, subjectId, NAMESPACE);
        String compositionId = OpenEhr.createComposition(sessionId, ehrId, TEMPLATE, composition);
        System.out.println(compositionId);
        OpenEhr.logout(sessionId);
    }

    @Test
    public void serialTest()  {
        String sessionId = OpenEhr.login();
        try {
            for (int i = 0; i < 100; i++) {
                System.out.println("(" + Thread.currentThread().getName() + ") test: " + i);
                try {
                    OpenEhr.readComposition(sessionId, COMPOSITION_ID);
                } catch (Error e) {
                    e.printStackTrace();
                    failureCount.incrementAndGet();
                }
            }
        } finally {
            OpenEhr.logout(sessionId);
        }

        Assertions.assertEquals(0, failureCount.get());
    }

    @Test
    public void parallelTest() throws Exception {
        ExecutorService executor = Executors.newFixedThreadPool(10);
        executor.submit(() -> {
            serialTest();
        });
        executor.submit(() -> {
            serialTest();
        });
        executor.awaitTermination(60, TimeUnit.SECONDS);

        Assertions.assertEquals(0, failureCount);
    }

    private String readFileFromClasspath(final String fileName) throws IOException, URISyntaxException {
        return new String(Files.readAllBytes(Paths.get(getClass().getClassLoader().getResource(fileName).toURI())));
    }
}
