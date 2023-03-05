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
    int numberOfFiles = 0;


    public List<TestFile> trainAndTest(File mainDirectory) {
//      TODO: main method of loading the directories and files, training and testing the model
        String folderPath = "../../../../resources/data/train/ham";
        Map<String, Integer> freq = trainHamFreq;
        
        
        for (int i = 0; i < 3; i++) {
            if(i == 1) {
                folderPath += "2";
            }
            else if(i == 2) {
                folderPath = "../../../../resources/data/train/spam";
                freq = trainSpamFreq;
            }
                
            File folder = new File(folderPath);
            iterateFolder(folder, freq);
            
        }

        
        

        return new ArrayList<TestFile>();
    }

    private void iterateFolder(File folder, Map<String, Integer> freq) {
        ArrayList<String> inFile = new ArrayList<>();
        File[] files = folder.listFiles();

        for(File file: files) {
            try (BufferedReader reader = new BufferedReader(new FileReader(file))) {
                String line;
                while ((line = reader.readLine()) != null) {
                    // split the line into words
                    String[] words = line.split("\\s+");
                    for (String word : words) {
                        // process the word here
                        System.out.println(word);
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

