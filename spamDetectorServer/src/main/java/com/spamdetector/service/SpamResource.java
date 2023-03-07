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

//    your SpamDetector Class responsible for all the SpamDetecting logic
    SpamDetector detector = new SpamDetector();
    List<TestFile> testFiles;
    int truePos = 0;
    int trueNeg = 0;
    int falsePos = 0;

    public SpamResource(){
//        TODO: load resources, train and test to improve performance on the endpoint calls
        System.out.print("Training and testing the model, please wait");

//      TODO: call  this.trainAndTest();
        testFiles = this.trainAndTest();
        System.out.println(testFiles.size());
    }
    @GET
    @Produces("application/json")
    public Response getSpamResults() {
//       TODO: return the test results list of TestFile, return in a Response object
        Response.ResponseBuilder response = Response.ok(testFiles);
        return response.build();
    }

    @GET
    @Path("/accuracy")
    @Produces("application/json")
    public Response getAccuracy() {
//      TODO: return the accuracy of the detector, return in a Response object
        int numberOfFiles = testFiles.size();
        countResults();
        double accuracy = (double)(truePos+trueNeg)/numberOfFiles;
        Response.ResponseBuilder response = Response.ok(accuracy);
        return response.build();
    }

    @GET
    @Path("/precision")
    @Produces("application/json")
    public Response getPrecision() {
       //      TODO: return the precision of the detector, return in a Response object
        countResults();
        double precision = (double)truePos/(double)(falsePos + truePos);
        Response.ResponseBuilder response = Response.ok(precision);
        return response.build();
    }

    private List<TestFile> trainAndTest()  {
        if (this.detector==null){
            this.detector = new SpamDetector();
        }
    
        // Obtain the absolute path of the "data" folder in the resources directory
        File mainDirectory = new File(getClass().getClassLoader().getResource("data").getFile());
        return this.detector.trainAndTest(mainDirectory);
    }

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