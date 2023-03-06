package com.spamdetector.util;

import com.spamdetector.domain.TestFile;

import java.io.*;
import java.util.*;


/**
 * TODO: This class will be implemented by you
 * You may create more methods to help you organize you strategy and make you code more readable
 */

public class SpamDetector {
    Map<String, Integer> trainHamFreq = new TreeMap<>();
    Map<String, Integer> trainSpamFreq = new TreeMap<>();
    Map<String, Double> probabilities = new TreeMap<>();
    Integer hamFiles = Integer.valueOf(0);
    Integer spamFiles = Integer.valueOf(0);

    public List<TestFile> trainAndTest(File mainDirectory) {
//      TODO: main method of loading the directories and files, training and testing the model
        String mainPath = mainDirectory.getAbsolutePath();
        String trainingPath = mainPath + "/train/ham";
        Map<String, Integer> freq = trainHamFreq;
        Integer counter = hamFiles;
        
        // Train
        for (int i = 0; i < 3; i++) {
            if(i == 1) {
                trainingPath += "2";
            }
            else if(i == 2) {
                trainingPath = mainPath + "/train/spam";
                freq = trainSpamFreq;
                counter = spamFiles;
            }
                
            File folder = new File(trainingPath);
            trainingIterate(folder, freq, counter);
        }

        // Determine probabilities for each word
        for (String word : trainSpamFreq.keySet()) {
            int spamCount = trainSpamFreq.get(word);
            int hamCount = trainHamFreq.getOrDefault(word, 0);
            double prWiS = (double) spamCount / spamFiles;
            double prWiH = (double) hamCount / hamFiles;
            double prSWi = prWiS / (prWiS + prWiH);
            probabilities.put(word, prSWi);
        }

        // Read all files from the testing directories and classify them as spam or ham
        List<TestFile> testFiles = new ArrayList<>();

        // Test ham files
        String testingPath = mainPath + "/testing/ham";
        File folder = new File(testingPath);
        testingIterate(folder, testFiles, "ham");

        // Test spam files
        testingPath = mainPath + "testing/spam";
        folder = new File(testingPath);
        testingIterate(folder, testFiles, "spam");
        
        return testFiles;
        // return new ArrayList<TestFile>();
    }

    private void trainingIterate(File folder, Map<String, Integer> freq, int counter) {
        File[] files = folder.listFiles();

        // Go through each file in the folder
        for(File file: files) {
            try (BufferedReader reader = new BufferedReader(new FileReader(file))) {
                counter++;
                String line;
                ArrayList<String> inFile = new ArrayList<>();

                // Read each line
                while ((line = reader.readLine()) != null) {
                    // Split the line into words
                    String[] words = line.split("\\s+");

                    // Read each word
                    for (String word : words) {
                        System.out.println(word);
                        word = word.toLowerCase();

                        // Add word to map or add to counter
                        if(!inFile.contains(word)) {
                            if(freq.containsKey(word)) {
                                freq.replace(word, trainHamFreq.get(word) + 1);
                            }
                            else {
                                freq.put(word, 1);
                            }
                        }
                    }
                }
            } 
            catch (IOException e) {
                e.printStackTrace();
            }
        }
    }

    private void testingIterate(File folder, List<TestFile> testFiles, String spam) {
        File[] files = folder.listFiles();
        double ypsilon = 0;

        // Go through each file in the folder
        for (File file: files) {
            try (BufferedReader reader = new BufferedReader(new FileReader(file))) {
                String line;

                // Read each line
                while ((line = reader.readLine()) != null) {
                    // Split line into words
                    String[] words = line.split("\\s+");

                    // Read each word
                    for (String word: words) {
                        System.out.println(word);
                        word = word.toLowerCase();

                        // Retrive probability and determine ypsilon
                        if(probabilities.containsKey(word)) {
                            double prSWi = probabilities.get(word);
                            ypsilon += Math.log(1-prSWi) - Math.log(prSWi);
                        }
                    }
                }
            } 
            catch (IOException e) {
                e.printStackTrace();
            }

            // Determine spam probability and add TestFile to list
            double prSF = 1/(1+Math.exp(ypsilon));
            testFiles.add(new TestFile(file.getPath(), prSF, spam));

            ypsilon = 0;
        }
    }
}

