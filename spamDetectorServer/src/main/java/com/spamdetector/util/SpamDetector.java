package com.spamdetector.util;

import com.spamdetector.domain.TestFile;

import java.io.*;
import java.util.*;
import java.util.concurrent.atomic.AtomicInteger;


/**
 * TODO: This class will be implemented by you
 * You may create more methods to help you organize you strategy and make you code more readable
 */

public class SpamDetector {
    Map<String, Integer> trainHamFreq = new TreeMap<>();
    Map<String, Integer> trainSpamFreq = new TreeMap<>();
    Map<String, Double> probabilities = new TreeMap<>();
    AtomicInteger hamFiles = new AtomicInteger(0);
    AtomicInteger spamFiles = new AtomicInteger(0);

    public List<TestFile> trainAndTest(File mainDirectory) {
//      TODO: main method of loading the directories and files, training and testing the model
        String mainPath = mainDirectory.getAbsolutePath();
        System.out.println(mainPath);
        String trainingPath = mainPath + "/train/ham";
        Map<String, Integer> freq = trainHamFreq;
        AtomicInteger counter = hamFiles;
        
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
            double prWiS = (double) spamCount / spamFiles.get();
            double prWiH = (double) hamCount / hamFiles.get();
            double prSWi = prWiS / (prWiS + prWiH);
            probabilities.put(word, prSWi);
        }

        // Read all files from the testing directories and classify them as spam or ham
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
        // return new ArrayList<TestFile>();
    }

    private void trainingIterate(File folder, Map<String, Integer> freq, AtomicInteger counter) {
        File[] files = folder.listFiles();
        System.out.println(folder.getAbsolutePath());
        // Go through each file in the folder
        for(File file: files) {
            if(!file.getName().equals("cmds")) {
                try (BufferedReader reader = new BufferedReader(new FileReader(file))) {
                    counter.incrementAndGet();
                    String line;
                    ArrayList<String> inFile = new ArrayList<>();
    
                    // Read each line
                    while ((line = reader.readLine()) != null) {
                        // Split the line into words
                        String[] words = line.split("\\s+");
    
                        // Read each word
                        for (String word : words) {
                            // System.out.println(word);
                            word = word.replaceAll("\\p{Punct}", "").toLowerCase();
    
                            // Add word to map or add to counter
                            if(!word.isBlank() && !inFile.contains(word)) {
                                if(freq.containsKey(word)) {
                                    freq.replace(word, freq.get(word) + 1);
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
    }    

    private void testingIterate(File folder, List<TestFile> testFiles, String spam) {
        File[] files = folder.listFiles();
        double ypsilon = 0;
        System.out.println(folder.getAbsolutePath());
        // Go through each file in the folder
        for (File file: files) {
            if(!file.getName().equals("cmds")) {
                try (BufferedReader reader = new BufferedReader(new FileReader(file))) {
                    String line;

                    // Read each line
                    while ((line = reader.readLine()) != null) {
                        // Split line into words
                        String[] words = line.split("\\s+");

                        // Read each word
                        for (String word: words) {
                            // System.out.println(word);
                            word = word.replaceAll("\\p{Punct}", "").toLowerCase();

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

            // Determine spam probability and add TestFile to list
            double prSF = 1/(1+Math.exp(ypsilon));
            if (!Double.isInfinite(prSF) && !Double.isNaN(prSF)) {
                testFiles.add(new TestFile(file.getName(), prSF, spam));
            }
            ypsilon = 0;
            }
        }
    }
}

