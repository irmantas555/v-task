package com.exam.validator;

import java.io.BufferedReader;
import java.io.FileReader;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

class CSVReader {

    public static List<Student> parse() {

        String csvFile = "results.csv";
        String line = "";
        String cvsSplitBy = ",";

        List<Student> students = new ArrayList<>();

        try (BufferedReader br = new BufferedReader(new FileReader(csvFile))) {

            while ((line = br.readLine()) != null) {

                String[] studentResult = line.split(cvsSplitBy);
                Student student = new Student().setName(studentResult[0]).setSittingLocation(studentResult[1]).setAnswers(parseAnswers(studentResult));

                students.add(student);
            }
            return students;

        } catch (IOException e) {
            throw new RuntimeException("Error while parsing", e);
        }

    }

    private static Map<Integer, String> parseAnswers(String[] studentResult) {
        Map<Integer, String> answers = new HashMap<>();

        for (int i = 2; i < studentResult.length; i++) {answers.put(i - 1, studentResult[i]);}

        return answers;
    }

}
