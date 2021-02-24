package eci.arsw.covidanalyzer;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.mashape.unirest.http.HttpResponse;
import com.mashape.unirest.http.Unirest;
import com.mashape.unirest.http.exceptions.UnirestException;
import org.json.JSONObject;

import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * The type Test reporter.
 */
public class TestReporter {

    /**
     * The constant TRUE_POSITIVE.
     */
    public static final int TRUE_POSITIVE = 0;
    /**
     * The constant FALSE_POSITIVE.
     */
    public static final int FALSE_POSITIVE = 1;
    /**
     * The constant TRUE_NEGATIVE.
     */
    public static final int TRUE_NEGATIVE = 2;
    /**
     * The constant FALSE_NEGATIVE.
     */
    public static final int FALSE_NEGATIVE = 3;

    private static final String SERVER_URL = "http://localhost:8080/covid/result";

    private static ObjectMapper mapper = new ObjectMapper();

    /**
     * Report.
     *
     * @param result the result
     * @param type   the type
     */
    public static void report(Result result, int type) {

        String complement = "/";
        switch (type) {
            case TRUE_POSITIVE:
                complement += "true-positive";
                break;
            case FALSE_POSITIVE:
                complement += "false-positive";
                break;
            case TRUE_NEGATIVE:
                complement += "true-negative";
                break;
            case FALSE_NEGATIVE:
                complement += "false-negative";
                break;
        }

        try {
            String jsonString = mapper.writeValueAsString(result);
            Unirest.post(SERVER_URL + complement)
                    .body(jsonString)
                    .asString();
        } catch (JsonProcessingException | UnirestException e) {
            Logger.getLogger(TestReporter.class.getName()).log(Level.SEVERE, "Unable to report covid test result", e);
        }
    }

}