package com.spamdetector.util;

import com.spamdetector.domain.TestFile;

import java.io.*;
import java.util.*;
import java.util.concurrent.atomic.AtomicInteger;

public class SpamDetector {
    // Maps which contain the word and the number of ham/files it appears in
    Map<String, Integer> trainHamFreq = new TreeMap<>();
    Map<String, Integer> trainSpamFreq = new TreeMap<>();

    // Map to store the words and their respective spam probabilities
    Map<String, Double> probabilities = new TreeMap<>();

    // AtomicIntegers were used as they can be set/passed by reference, which made the code a little simpler
    AtomicInteger hamFiles = new AtomicInteger(0);
    AtomicInteger spamFiles = new AtomicInteger(0);

    /*
     * The central method of this class, which administers the training and testing
     */
    public List<TestFile> trainAndTest(File mainDirectory) {
        String mainPath = mainDirectory.getAbsolutePath();
        String trainingPath = mainPath + "/train/ham";
        Map<String, Integer> freq = trainHamFreq;
        AtomicInteger counter = hamFiles;
        
        // Run the training method for each folder
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
            double prWiS = (double) spamCount / spamFiles.get();
            double prWiH = (double) hamCount / hamFiles.get();
            double prSWi = prWiS / (prWiS + prWiH);
            probabilities.put(word, prSWi);
        }

        // Create a list to store the test files
        List<TestFile> testFiles = new ArrayList<>();

        // Test ham files
        String testingPath = mainPath + "/test/ham";
        File folder = new File(testingPath);
        testingIterate(folder, testFiles, "ham");

        // Test spam files
        testingPath = mainPath + "/test/spam";
        folder = new File(testingPath);
        testingIterate(folder, testFiles, "spam");
        
        return testFiles;
    }

    /*
     * Executes the training
     */
    private void trainingIterate(File folder, Map<String, Integer> freq, AtomicInteger counter) {
        File[] files = folder.listFiles();
        System.out.println(folder.getAbsolutePath());

        // Go through each file in the folder
        for(File file: files) {
            // Avoid cmds files
            if(!file.getName().equals("cmds")) {
                try (BufferedReader reader = new BufferedReader(new FileReader(file))) {
                    counter.incrementAndGet();

                    // Array that keeps track of whether the word has already appeared in the file (to avoid overcounting)
                    ArrayList<String> inFile = new ArrayList<>();
    
                    String line;

                    // Read each line
                    while ((line = reader.readLine()) != null) {
                        // Split the line into words
                        String[] words = line.split("\\s+");
    
                        // Read each word
                        for (String word : words) {
                            // Remove all punctuation
                            word = word.replaceAll("\\p{Punct}", "").toLowerCase();
    
                            // Check if the word is empty or if has already been seen in the file
                            if(!word.isBlank() && !inFile.contains(word)) {
                                // If the word has been seen in previous files, increment the frequency
                                if(freq.containsKey(word)) {
                                    freq.replace(word, freq.get(word) + 1);
                                }
                                // If not, add it to the map
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
    }    

    /*
     * Executes the testing
     */
    private void testingIterate(File folder, List<TestFile> testFiles, String spam) {
        File[] files = folder.listFiles();
        double ypsilon = 0;
        System.out.println(folder.getAbsolutePath());

        // Go through each file in the folder
        for (File file: files) {
            // Avoid cmds files
            if(!file.getName().equals("cmds")) {
                try (BufferedReader reader = new BufferedReader(new FileReader(file))) {
                    String line;

                    // Read each line
                    while ((line = reader.readLine()) != null) {
                        // Split line into words
                        String[] words = line.split("\\s+");

                        // Read each word
                        for (String word: words) {
                            // Use regex to remove anything that is not a letter
                            word = word.replaceAll("[^a-zA-Z]", "").toLowerCase();

                            // Retrive probability and determine ypsilon
                            if(!word.isBlank() && probabilities.containsKey(word)) {
                                double prSWi = probabilities.get(word);
                                ypsilon += Math.log(1-prSWi) - Math.log(prSWi);
                            }
                        }
                    }
                } 
                catch (IOException e) {
                    e.printStackTrace();
                }

            // Determine spam probability
            double prSF = 1/(1+Math.exp(ypsilon));

            // Ensure no infinite or NaN probabilities are passed
            if(Double.isInfinite(prSF)) {
                prSF = 1.0;
            }
            else if(Double.isNaN(prSF)) {
                prSF = 0.0;
            }

            // Add test file to list
            testFiles.add(new TestFile(file.getName(), prSF, spam));
            ypsilon = 0;
            }
        }
    }
}

