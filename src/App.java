import java.util.Arrays;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class App {
    public static void main(String[] args) throws Exception {
        List<String> emailBodies = Arrays.asList(
                "Congratulations! You have won a free iPhone!",
                "Your bank account has been flagged for suspicious activity.",
                "You are a winner! Get your free iPhone now!",
                "Important update: Your account requires verification.",
                "Don't miss this chance to claim your prize!",
                "This is a final reminder: Your subscription is about to expire.",
                "Earn money from home with just a few clicks!",
                "Your Amazon order #12345 has been shipped. Track it here.",
                "Upgrade your account today to enjoy premium features.",
                "Get the latest iPhone for just $1! Limited time offer."
        );

        Map<String, Double> spamProbMap = calculateSpamProbabilities(emailBodies);
        // Print the spam probability for each email
        spamProbMap.forEach((email, probability) -> {
            System.out.println("Email: \"" + email + "\"\nSpam Probability: " + probability + "\n");
        });

        // Find and print the most and least spammy emails
        String mostSpammy = Collections.max(spamProbMap.entrySet(), Map.Entry.comparingByValue()).getKey();
        String leastSpammy = Collections.min(spamProbMap.entrySet(), Map.Entry.comparingByValue()).getKey();

        System.out.println("Most Spammy Email: \"" + mostSpammy + "\"");
        System.out.println("Least Spammy Email: \"" + leastSpammy + "\"");
    }

    /**
     * Builds a frequency map of words from the given email body.
     * The method removes punctuation, converts all words to lowercase,
     * and counts the occurrences of each word.
     *
     * @param emailBody The email body as a string. It should be non-null and can include punctuation or mixed case.
     * @return A map where the keys are unique words (in lowercase, with punctuation removed),
     *         and the values are the frequency of each word in the email body.
     */
    public static Map<String, Integer> buildWordFrequencyMap(String emailBody) {
        Map<String, Integer> wordFrequencyMap = new HashMap<>();
        String[] words = emailBody.toLowerCase().replaceAll("[^a-z0-9 ]","").split(" ");
        for (String word : words) {
            if (wordFrequencyMap.containsKey(word)) {
                wordFrequencyMap.put(word, wordFrequencyMap.get(word) + 1);
            } else {
                wordFrequencyMap.put(word, 1);
            }
        }
        return wordFrequencyMap;
    }

    /**
     * Calculates the cosine similarity between two email bodies based on their word frequency maps.
     * Cosine similarity measures the similarity between two texts by comparing the frequency
     * of words in each text, resulting in a value between 0 and 1.
     *
     * @param emailbody1 The body of the first email to be compared. It should be a non-null string.
     * @param emailbody2 The body of the second email to be compared. It should be a non-null string.
     * @return A double value between 0 and 1 representing the cosine similarity.
     *         - 1 indicates identical word frequency distributions.
     *         - 0 indicates no common words or vectors with zero magnitude.
     */
    public static double calculateCosineSimilarity(String emailbody1, String emailbody2) {
        Map<String, Integer> wordFrequencyMap1 = buildWordFrequencyMap(emailbody1);
        Map<String, Integer> wordFrequencyMap2 = buildWordFrequencyMap(emailbody2);

        double dotProduct = 0.0;
        double magnitude1 = 0.0;
        double magnitude2 = 0.0;

        for (Map.Entry<String, Integer> entry : wordFrequencyMap1.entrySet()) {
            String word = entry.getKey();
            int frequency1 = entry.getValue();
            magnitude1 += frequency1 * frequency1;
            if (wordFrequencyMap2.containsKey(word)) {
                dotProduct += frequency1 * wordFrequencyMap2.get(word);
            }
        }

        for (Map.Entry<String, Integer> entry : wordFrequencyMap2.entrySet()) {
            int frequency2 = entry.getValue();
            magnitude2 += frequency2 * frequency2;
        }

        magnitude1 = Math.sqrt(magnitude1);
        magnitude2 = Math.sqrt(magnitude2);

        return (magnitude1 == 0 || magnitude2 == 0) ? 0.0 : dotProduct / (magnitude1 * magnitude2);
    }

    // Method for creating cosine similarity matrix

    /**
     * Generates a cosine similarity matrix for a list of email bodies.
     * The matrix contains pairwise cosine similarity values for the given email bodies,
     * where each value indicates the similarity between two emails.
     *
     * @param emailBodies A list of email body strings to compare. Each string represents the content of an email.
     *                    The list must not be null, and it can have zero or more email bodies.
     * @return A 2D array (double[][]) representing the cosine similarity matrix.
     *         - The matrix is of size n x n, where n is the number of email bodies.
     *         - Each cell [i][j] contains the cosine similarity between emailBodies.get(i) and emailBodies.get(j).
     *         - Diagonal elements [i][i] will always be 1.0, as each email is perfectly similar to itself.
     */
    public static double[][] createCosineSimilarityMatrix(List<String> emailBodies) {
        int n = emailBodies.size();
        double[][] cosineSimilarityMatrix = new double[n][n];
        for (int i = 0; i < n; i++) {
            String emailBody1 = emailBodies.get(i);
            for (int j = 0; j < n; j++) {
                String emailBody2 = emailBodies.get(j);
                if (i == j) {
                    cosineSimilarityMatrix[i][j] = 1.0;
                    continue;
                }
                cosineSimilarityMatrix[i][j] = calculateCosineSimilarity(emailBody1, emailBody2);
            }
        }
        return cosineSimilarityMatrix;
    }

    /**
     * Calculates spam probabilities for a list of email bodies based on their cosine similarity.
     * Each email is assigned a spam probability that reflects its similarity to other emails in the list.
     * Higher probabilities indicate greater similarity to other emails, suggesting potential spam.
     *
     * @param emailBodies A list of email body strings to analyze for spam probability.
     *                    - If the list is empty, an empty map is returned.
     *                    - If the list contains a single email, its spam probability is set to 0.0.
     * @return A map where:
     *         - Keys are the email body strings.
     *         - Values are the corresponding spam probabilities (as normalized cosine similarity values).
     *         - For multiple emails, probabilities are normalized sums of cosine similarities with all other emails.
     */
    public static Map<String, Double> calculateSpamProbabilities(List<String> emailBodies) {
        Map<String, Double> spamProbMap = new HashMap<>();
        if(emailBodies.isEmpty()) {
            return spamProbMap;
        }
        else if(emailBodies.size() == 1){
            spamProbMap.put(emailBodies.getFirst(), 0.0);
            return spamProbMap;
        }
        else {
            double[][] cosineSimilarityMatrix = createCosineSimilarityMatrix(emailBodies);

            // Sum all the cosine similarity values in the row of the matrix and normalize it
            int len = cosineSimilarityMatrix.length;
            for (int i = 0; i < len; i++) {
                double sum = 0.0;
                for (int j = 0; j < len; j++) {
                    if (i != j) {
                        sum += cosineSimilarityMatrix[i][j];
                    }
                }
                // Calculate the normalized spam probability and store it in the map
                double spamProbability = sum / (len - 1);
                spamProbMap.put(emailBodies.get(i), spamProbability);
            }
        }

        return spamProbMap;
    }
}
