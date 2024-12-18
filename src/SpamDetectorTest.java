import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.*;

import java.util.*;

class SpamDetectorTest {

    @Test
    void testEmptyEmailBodies() {
        List<String> emails = Collections.emptyList();
        Map<String, Double> result = App.calculateSpamProbabilities(emails);
        assertTrue(result.isEmpty(), "Empty email bodies should return an empty result map.");
    }

    @Test
    void testSingleEmail() {
        List<String> emails = Arrays.asList("Hello world!");
        Map<String, Double> result = App.calculateSpamProbabilities(emails);
        assertEquals(1, result.size(), "Single email should result in a map with one entry.");
        assertEquals(0.0, result.get("Hello world!"), 0.0001, "Single email should have a spam probability of 0.0.");
    }

    @Test
    void testIdenticalEmails() {
        List<String> emails = Arrays.asList("This is a spam mail.", "This is a spam mail.", "This is a spam mail.");
        Map<String, Double> result = App.calculateSpamProbabilities(emails);
        for (double prob : result.values()) {
            assertEquals(1.0, prob, 0.0001, "Identical emails should have a spam probability of 1.0.");
        }
    }

    @Test
    void testDifferentLengths() {
        List<String> emails = Arrays.asList("Short", "This is a much longer email body to test length handling.");
        Map<String, Double> result = App.calculateSpamProbabilities(emails);
        assertEquals(2, result.size(), "Two emails should result in a map with two entries.");
    }

    @Test
    void testCompletelyDifferentEmails() {
        List<String> emails = Arrays.asList(
                "This is an email about your bank account.",
                "Here is a recipe for apple pie.",
                "The weather today is sunny."
        );
        Map<String, Double> result = App.calculateSpamProbabilities(emails);
        for (double prob : result.values()) {
            assertTrue(prob < 0.5, "Completely different emails should have a low spam probability.");
        }
    }
}
