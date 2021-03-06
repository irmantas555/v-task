package com.exam.validator;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Scanner;
import java.util.logging.Logger;

/**
 * Students were taking an exam which was the a multichoice test. We know the location in auditorium where each student was sitting and their answers.
 * Student's sitting location is in format x.y, where x is the row and the y is the sitting place in the row. Rows are numbered from front to back.
 * Each student had a chance to cheat and write answers down from:
 * <p>
 * a) his neighbours in the same row,
 * b) the 3 guys sitting in the row in front of him
 * <p>
 * e.g. auditorium could look like that
 * (.....back........)
 * (x,x,x,x,x,x,x,x,x)
 * (x,x,x,x,x,y,s,y,x)
 * (x,x,x,x,x,y,y,y,x)
 * (....front........), where s is the student, and y are his neighbours
 * <p>
 * The task is to identify cheating students in the class.
 * <p>
 * If you find that you are missing requirements, take your own judgment.
 * <p>
 * Data could be found in results.csv file, but CSV parsing is already done for you and results are mapped to a
 * Student objects list.
 */
public class Main {

    static Scanner scanner = new Scanner(System.in);

    private static final Map<Integer, String> correctAnswers = new HashMap<>();

    private static final Map<Integer, List<Character>> correctOptionsMap = new HashMap<>();

    private static final Map<String, List<String>> analyzedPairs = new HashMap<>();

    private static final Map<Integer, Map<Character, Integer>> incorrectChosenOptionsCountMap = new HashMap<>();

    public static void populateAnswers() {
        correctAnswers.put(1, "a");
        correctAnswers.put(2, "bd");
        correctAnswers.put(3, "abef");
        correctAnswers.put(4, "f");
        correctAnswers.put(5, "f");
        correctAnswers.put(6, "d");
        correctAnswers.put(7, "abe");
        correctAnswers.put(8, "abcde");
        correctAnswers.put(9, "abe");
        correctAnswers.put(10, "abd");
        correctAnswers.put(11, "b");
        correctAnswers.put(12, "af");
        correctAnswers.put(13, "ce");
        correctAnswers.put(14, "be");
        correctAnswers.put(15, "bdf");
        correctAnswers.put(16, "a");
    }

    public static void main(String[] args) {
        populateAnswers();
        poupulateOptions();
        List<Student> students = CSVReader.parse();
        populateIncorectOptionsCount(students);
        StudentUtils.addNeighbours(students);
        StudentUtils.collectNeighboursAnswers(students);
        Map<String, List<String>> badAnswerOptionsMap = StudentUtils.makeBadAnswersMap(students);
        Map<String, Integer> possibleCheatingBadOptionsTimesMap = StudentUtils.calculateBadOptionsAccumulatedPlan(badAnswerOptionsMap, students);
        StudentUtils.analyzeAnswers(students);
        analysisOptions(badAnswerOptionsMap, possibleCheatingBadOptionsTimesMap, students);
    }

    private static void analysisOptions(Map<String, List<String>> badAnswerOptionsMap, Map<String, Integer> badAnswerOptionsTimesMap, List<Student> students) {
        char optionChosen;

        while (true) {
            System.out.println("**************************************************************");
            System.out.println("Enter option for possible cheating analysis");
            System.out.println("1 - Print preface");
            System.out.println("2 - Print distributions plans for each wrong answers option");
            System.out.println("3 - Print wrong answers option repetition in neighbours plan");
            System.out.println("4 - Print analysis of student pairs possible cheaters");
            System.out.println("5 - Print analysis of students answers");
            System.out.println("6 - Print analysis of individual student answers");
            System.out.println("7 - Print personal conclusion");
            System.out.println("8 - Exit");
            System.out.println("**************************************************************");
            optionChosen = scanner.nextLine().charAt(0);
            switch (optionChosen) {
                case '1' -> AnswersUtils.printPreface();
                case '2' -> StudentUtils.drawBadOptionsPlan(badAnswerOptionsMap, students);
                case '3' -> StudentUtils.drawBadOptionsAccumulatedPlan(badAnswerOptionsTimesMap, students);
                case '4' -> StudentUtils.analyzePossibleCheatingNeighbours(students, null);
                case '5' -> StudentUtils.printFullStudentsAnalysis(students, null);
                case '6' -> printindividualAnalysis(students);
                case '7' -> AnswersUtils.printConclusion(students);
                case '8' -> {return;}
                default -> System.out.println("Tokio pasisrinkimo n??ra");
            }
        }
    }

    private static void printindividualAnalysis(List<Student> students) {
        String optionChosen;
        int intOption = 0;
        while (true) {
            System.out.println("**************************************************************");
            System.out.printf("Enter student number 1 to %d%n", students.size());
            System.out.println("0 - Exit");
            System.out.println("**************************************************************");
            optionChosen = scanner.nextLine();
            try {
                intOption = Integer.parseInt(String.valueOf(optionChosen));
            } catch (Exception e) {
                System.out.println("Number must be entered");
                optionChosen = null;
            }
            if ("0".equals(optionChosen)) {
                return;
            } else if (optionChosen != null){
                StudentUtils.printFullStudentsAnalysis(students, intOption);
            }
        }
    }

    private static void populateIncorectOptionsCount(List<Student> students) {
        students.forEach(student -> student.getAnswers().entrySet().forEach(answer -> {
            List<Character> characters = answer.getValue().chars().mapToObj(c -> (char) c).toList();
            characters.forEach(character -> {
                if (!(correctOptionsMap.get(answer.getKey()).contains(character))) {
                    if (incorrectChosenOptionsCountMap.containsKey(answer.getKey())) {
                        if (incorrectChosenOptionsCountMap.get(answer.getKey()).containsKey(character)) {
                            incorrectChosenOptionsCountMap.get(answer.getKey()).put(character, incorrectChosenOptionsCountMap.get(answer.getKey()).get(character) + 1);
                        } else {
                            incorrectChosenOptionsCountMap.get(answer.getKey()).put(character, 1);
                        }
                    } else {
                        Map<Character, Integer> characterIntegerMap = new HashMap<>();
                        characterIntegerMap.put(character, 1);
                        incorrectChosenOptionsCountMap.put(answer.getKey(), characterIntegerMap);
                    }
                }
            });
        }));
    }

    public static void poupulateOptions() {
        correctAnswers.entrySet().forEach(answer -> correctOptionsMap.put(answer.getKey(), answer.getValue().chars().mapToObj(c -> (char) c).toList()));
    }

    public static Map<Integer, String> getCorrectAnswers() {
        return correctAnswers;
    }

    public static Map<Integer, List<Character>> getCorrectOptionsMap() {
        return correctOptionsMap;
    }

    public static Map<String, List<String>> getAnalyzedPairs() {
        return analyzedPairs;
    }

    public static Map<Integer, Map<Character, Integer>> getIncorrectChosenOptionsCountMap() {
        return incorrectChosenOptionsCountMap;
    }
}

