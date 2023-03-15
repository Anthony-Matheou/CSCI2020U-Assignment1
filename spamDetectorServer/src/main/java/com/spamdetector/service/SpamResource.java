package com.spamdetector.service;

import com.spamdetector.domain.TestFile;
import com.spamdetector.util.SpamDetector;
import jakarta.ws.rs.GET;
import jakarta.ws.rs.Path;
import jakarta.ws.rs.Produces;

import java.io.File;
import java.util.List;

import jakarta.ws.rs.core.Response;

@Path("/spam")
public class SpamResource {
    SpamDetector detector = new SpamDetector();
    List<TestFile> testFiles;
    int truePos = 0;
    int trueNeg = 0;
    int falsePos = 0;

    /*
     * Constructor
     */
    public SpamResource(){
        
        System.out.print("Training and testing the model, please wait");

        testFiles = this.trainAndTest();
    }

    /*
     * Returns the test results list of TestFile objects as a Response object
     */
    @GET
    @Produces("application/json")
    public Response getSpamResults() {
        Response.ResponseBuilder response = Response.ok(testFiles);
        response.header("Access-Control-Allow-Origin", "*");
        return response.build();
    }

    /*
     * Returns the accuracy of the model as a Response object
     */
    @GET
    @Path("/accuracy")
    @Produces("application/json")
    public Response getAccuracy() {

        // Calculate accuracy
        int numberOfFiles = testFiles.size();
        countResults();
        double accuracy = (double)(truePos+trueNeg)/numberOfFiles;

        // Build the JSON and return the response
        String jsonResponse = "{\"accuracy\":" + accuracy + "}";
        Response.ResponseBuilder response = Response.ok(jsonResponse);
        response.header("Access-Control-Allow-Origin", "*");
        return response.build();
    }

    /*
     * Returns the precision of the model as a response object
     */
    @GET
    @Path("/precision")
    @Produces("application/json")
    public Response getPrecision() {
        // Calculate precision
        countResults();
        double precision = (double)truePos/(double)(falsePos + truePos);

        // Build the JSON and return the response
        String jsonResponse = "{\"precision\":" + precision + "}";
        Response.ResponseBuilder response = Response.ok(jsonResponse);
        response.header("Access-Control-Allow-Origin", "*");
        return response.build();
    }

    /*
     * Private method that begins the training and testing process
     */
    private List<TestFile> trainAndTest()  {
        // Check that the detector object exists and create one if not
        if (this.detector == null){
            this.detector = new SpamDetector();
        }
    
        // Find the absolute path of the data folder in the resources directory
        File mainDirectory = new File(getClass().getClassLoader().getResource("data").getFile());

        // Return the output of the trainAndTest method
        return this.detector.trainAndTest(mainDirectory);
    }

    /*
     * Private method that determines the number of true positives, true negatives and false positives
     */
    private void countResults() {
        for (TestFile testFile : testFiles) {
            if(testFile.getSpamProbability() > 0.5 && testFile.getActualClass().equals("spam")) {
                truePos++;
            }
            else if(testFile.getSpamProbability() <= 0.5 && testFile.getActualClass().equals("ham")) {
                trueNeg++;
            }
            else if(testFile.getSpamProbability() > 0.5 && testFile.getActualClass().equals("ham")) {
                falsePos++;
            }
        }
    }
    
}