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
        String folderPath = "../../../../resources/data/train/ham";
        Map<String, Integer> freq = trainHamFreq;
        Integer counter = hamFiles;
        List<TestFile> testFiles = new ArrayList<>();
        
        for (int i = 0; i < 3; i++) {
            if(i == 1) {
                folderPath += "2";
            }
            else if(i == 2) {
                folderPath = "../../../../resources/data/train/spam";
                freq = trainSpamFreq;
                counter = spamFiles;
            }
                
            File folder = new File(folderPath);
            iterateFolder(folder, freq, counter);
        }

        for (String word : trainSpamFreq.keySet()) {
            int spamCount = trainSpamFreq.get(word);
            int hamCount = trainHamFreq.getOrDefault(word, 0);
            double prWiS = (double) spamCount / spamFiles;
            double prWiH = (double) hamCount / hamFiles;
            double prSWi = prWiS / (prWiS + prWiH);
            probabilities.put(word, prSWi);
        }

        // Read all files from the testing directories and classify them as spam or ham
        
        for (File dir : mainDirectory.listFiles()) {
            if (!dir.isDirectory()) continue;
            boolean isSpam = dir.getName().equals("testing/spam");
            for (File file : dir.listFiles()) {
                double probSpam = 1.0;
                // double probHam = 1.0;
                try (Scanner scanner = new Scanner(file)) {
                    while (scanner.hasNext()) {
                        String word = scanner.next().toLowerCase();
                        if (word.matches("\\W+")) continue;
                        if (probabilities.containsKey(word)) {
                            double prSiWi = probabilities.get(word);
                            probSpam = prSiWi;
                            // probHam = 1 - prSiWi;
                        }
                    }
                } catch (FileNotFoundException e) {
                    e.printStackTrace();
                }
                // double probTotal = probSpam + probHam;
                // boolean isSpamDetected = probSpam / probTotal >= 0.5;
                String classification;
                if(isSpam) {
                    classification = "spam";
                }
                else {
                    classification = "ham";
                }
                testFiles.add(new TestFile(file.getPath(), probSpam, classification));
            }
        }
        return testFiles;
        // return new ArrayList<TestFile>();
    }

    private void iterateFolder(File folder, Map<String, Integer> freq, int counter) {
        File[] files = folder.listFiles();

        for(File file: files) {
            try (BufferedReader reader = new BufferedReader(new FileReader(file))) {
                counter++;
                String line;
                ArrayList<String> inFile = new ArrayList<>();
                while ((line = reader.readLine()) != null) {
                    // split the line into words
                    String[] words = line.split("\\s+");
                    for (String word : words) {
                        // process the word here
                        System.out.println(word);
                        word = word.toLowerCase();
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
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }

}

