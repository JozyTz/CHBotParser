package org.example;

import com.google.gson.Gson;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;

import java.io.*;
import java.util.*;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class Main {
    public static void main(String[] args) {
        String inputFilePath = "input.txt"; // Replace with your input file path
        String outputFilePath = "output.csv"; // Replace with your desired CSV output file path
        String misspellingsFilePath = "misspellings.json";

        // Create a mapping of keywords to their point values
        Map<String, Integer> keywordPointValues = new HashMap<>();
        keywordPointValues.put("170.4", 4);
        keywordPointValues.put("170.5", 6);
        keywordPointValues.put("170.6", 8);
        keywordPointValues.put("180.4", 4);
        keywordPointValues.put("180.5", 6);
        keywordPointValues.put("180.6", 8);
        keywordPointValues.put("210.4", 6);
        keywordPointValues.put("210.5", 8);
        keywordPointValues.put("210.6", 10);
        keywordPointValues.put("215.4", 6);
        keywordPointValues.put("215.5", 8);
        keywordPointValues.put("215.6", 10);
        keywordPointValues.put("/rings1x5", 2);
        keywordPointValues.put("/ring1x5", 2);
        keywordPointValues.put("/rings2x5", 4);
        keywordPointValues.put("/ring2x5", 4);
        keywordPointValues.put("/rings3x5", 6);
        keywordPointValues.put("/ring3x5", 6);
        keywordPointValues.put("/rings4x5", 8);
        keywordPointValues.put("/ring4x5", 8);
        keywordPointValues.put("/ringa4x5", 8);
        keywordPointValues.put("/rings1x6", 8);
        keywordPointValues.put("/ring1x6", 8);
        keywordPointValues.put("/rings2x6", 16);
        keywordPointValues.put("/ring2x6", 16);
        keywordPointValues.put("/rings3x6", 24);
        keywordPointValues.put("/rings4x6", 32);
        keywordPointValues.put("/aggy", 5);
        keywordPointValues.put("/hrung", 5);
        keywordPointValues.put("/mordris", 10);
        keywordPointValues.put("/mordy", 10);
        keywordPointValues.put("/mordi", 10);
        keywordPointValues.put("/necro", 15);
        keywordPointValues.put("/base", 20);
        keywordPointValues.put("/prime", 25);
        keywordPointValues.put("/gele", 35);
        keywordPointValues.put("/gelebron", 35);
        keywordPointValues.put("/bt", 45);
        keywordPointValues.put("/bloodthorn", 45);
        keywordPointValues.put("/dino", 80);

        // Load misspellings from the JSON file
        Map<String, List<String>> nameMisspellings = loadMisspellingsFromJSON(misspellingsFilePath);

        try (BufferedReader br = new BufferedReader(new FileReader(inputFilePath));
             FileWriter outputFileWriter = new FileWriter(outputFilePath)) {

            // Write CSV header
            outputFileWriter.write("Name,Points\n");

            String line;
            Map<String, Integer> namePointsMap = new HashMap<>();

            while ((line = br.readLine()) != null) {
                // Skip lines containing the specified message
                if (line.contains("This message has been hidden by group admins.")) {
                    continue;
                }

                // Skip lines that start with a date pattern
                if (line.matches("\\d{2}/\\d{2}/\\d{4}.*")) {
                    continue;
                }

                // Use regex to find keywords and names on each line
                Pattern pattern = Pattern.compile("([0-9.]+|/\\w+)\\s+((\\S+\\s*)+)");
                Matcher matcher = pattern.matcher(line);

                while (matcher.find()) {
                    String keyword = matcher.group(1).toLowerCase(); // Convert to lowercase
                    String[] names = matcher.group(2).split("\\s+");

                    // Check if the keyword is in the mapping (case-insensitive)
                    int keywordValue = keywordPointValues.getOrDefault(keyword, 0);

                    // Process each name
                    for (String name : names) {
                        name = name.trim().toLowerCase(); // Convert to lowercase

                        // Check if the name matches any correct name or misspelling
                        for (Map.Entry<String, List<String>> entry : nameMisspellings.entrySet()) {
                            String correctName = entry.getKey();
                            List<String> misspellings = entry.getValue();

                            if (name.equals(correctName) || misspellings.contains(name)) {
                                int currentPoints = namePointsMap.getOrDefault(correctName, 0);
                                namePointsMap.put(correctName, currentPoints + keywordValue);
                                break; // Break loop once a match is found
                            }
                        }
                    }
                }
            }

            // Write the accumulated points to the CSV file
            for (Map.Entry<String, Integer> entry : namePointsMap.entrySet()) {
                String name = entry.getKey();
                int points = entry.getValue();
                outputFileWriter.write(name + "," + points + "\n");
            }

            System.out.println("CSV file generated successfully.");

        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private static Map<String, List<String>> loadMisspellingsFromJSON(String filePath) {
        Map<String, List<String>> misspellingsMap = new HashMap<>();
        try (Reader reader = new FileReader(filePath)) {
            JsonElement jsonElement = JsonParser.parseReader(reader);
            JsonObject jsonObject = jsonElement.getAsJsonObject();

            for (Map.Entry<String, JsonElement> entry : jsonObject.entrySet()) {
                String correctName = entry.getKey();
                List<String> misspellings = new ArrayList<>();
                JsonElement misspellingsArray = entry.getValue();

                if (misspellingsArray.isJsonArray()) {
                    for (JsonElement element : misspellingsArray.getAsJsonArray()) {
                        misspellings.add(element.getAsString());
                    }
                }

                misspellingsMap.put(correctName, misspellings);
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
        return misspellingsMap;
    }
}