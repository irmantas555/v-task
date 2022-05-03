package com.exam.validator;

import static java.lang.Math.abs;

import java.io.BufferedReader;
import java.io.FileReader;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
import java.util.stream.Collectors;

import org.apache.commons.lang3.StringUtils;

/**
 *
 *
 * Students were taking an exam which was the a multichoice test. We know the location in auditorium where each student was sitting and their answers.
 * Student's sitting location is in format x.y, where x is the row and the y is the sitting place in the row. Rows are numbered from front to back.
 * Each student had a chance to cheat and write answers down from:
 *
 * a) his neighbours in the same row,
 * b) the 3 guys sitting in the row in front of him
 *
 * e.g. auditorium could look like that
 *     (.....back........)
 *     (x,x,x,x,x,x,x,x,x)
 *     (x,x,x,x,x,y,s,y,x)
 *     (x,x,x,x,x,y,y,y,x)
 *     (....front........), where s is the student, and y are his neighbours
 *
 * The task is to identify cheating students in the class.
 *
 * If you find that you are missing requirements, take your own judgment.
 *
 * Data could be found in results.csv file, but CSV parsing is already done for you and results are mapped to a
 * Student objects list.
 *
 *
 */
public class Main {

    private static Map<Integer, String> correctAnswers = new HashMap<>();

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
        List<Student> students = CSVReader.parse();
        StudentUtils.addNeighbours(students);
        StudentUtils.collectNeighboursAnswers(students);
        StudentUtils.analyzeAnswers(students);
    }

    public static Map<Integer, String> getCorrectAnswers() {
        return correctAnswers;
    }
}

class Student {

    private String name;
    private String sittingLocation;
    private Map<Integer, String> answers = new HashMap<Integer, String>();

    private List<Student> cheatingPossibilities = new ArrayList<>();

    Map<Integer, List<String>> neighboursAnswersMap = new HashMap<>();

    public String getName() {
        return name;
    }

    public Student setName(String name) {
        this.name = name;
        return this;
    }

    public String getSittingLocation() {
        return sittingLocation;
    }

    public Student setSittingLocation(String sittingLocation) {
        this.sittingLocation = sittingLocation;
        return this;
    }

    public Map<Integer, String> getAnswers() {
        return answers;
    }

    public Student setAnswers(Map<Integer, String> answers) {
        this.answers = answers;
        return this;
    }

    List<Student> getCheatingPossibilities() {
        return cheatingPossibilities;
    }

    void setCheatingPossibilities(List<Student> cheatingPossibilities) {
        this.cheatingPossibilities = cheatingPossibilities;
    }

    Map<Integer, List<String>> getNeighboursAnswersMap() {
        return neighboursAnswersMap;
    }

    void setNeighboursAnswersMap(Map<Integer, List<String>> neighboursAnswersMap) {
        this.neighboursAnswersMap = neighboursAnswersMap;
    }

    @Override
    public String toString() {
        return "Student{" + "name='" + name + '\'' + ", sittingLocation='" + sittingLocation + '\'' + ", answers=" + answers + '}';
    }
}

class CSVReader {

    public static List<Student> parse() {

        String csvFile = "results.csv";
        String line = "";
        String cvsSplitBy = ",";

        List<Student> students = new ArrayList<>();

        try (BufferedReader br = new BufferedReader(new FileReader(csvFile))) {

            while ((line = br.readLine()) != null) {

                String[] studentResult = line.split(cvsSplitBy);
                Student student = new Student()
                        .setName(studentResult[0])
                        .setSittingLocation(studentResult[1])
                        .setAnswers(parseAnswers(studentResult));

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

class AnswersUtils {

    public static List<Student> filterSudentsAndAnswers(List<Student> students) {
        return students.stream().peek(it -> removeCorrectAnswers(it)).filter(it -> !it.getAnswers().isEmpty()).toList();
    }

    private static void removeCorrectAnswers(Student student) {
        student.setAnswers(student.getAnswers().entrySet().stream().filter(it -> answerIsIncorrect(it)).collect(Collectors.toMap(Map.Entry::getKey, Map.Entry::getValue)));
    }

    private static boolean answerIsIncorrect(Map.Entry<Integer, String> it) {
        return !StringUtils.equals(Main.getCorrectAnswers().get(it.getKey()), it.getValue());
    }
}

class StudentUtils {
    public static void addNeighbours(List<Student> students) {
        students.forEach(student -> {
            Integer studentRow = getRow(student);
            Integer seat = getSeat(student);
            students.forEach(possibleNeighbour -> {
                if (!possibleNeighbour.equals(student)) {
                    if (getRow(possibleNeighbour).equals(studentRow) && abs(getSeat(possibleNeighbour) - seat) == 1) {
                        student.getCheatingPossibilities().add(possibleNeighbour);
                    } else if (studentRow - getRow(possibleNeighbour) == 1 && abs(getSeat(possibleNeighbour) - seat) <= 1) {
                        student.getCheatingPossibilities().add(possibleNeighbour);
                    }
                }
            });
        });
    }

    private static Integer getRow(Student student) {
        return Integer.parseInt(student.getSittingLocation().split("\\.")[0]);
    }

    private static Integer getSeat(Student student) {
        return Integer.parseInt(student.getSittingLocation().split("\\.")[1]);
    }

    public static void collectNeighboursAnswers(List<Student> students) {
        students.forEach(student -> {
            Main.getCorrectAnswers().entrySet().forEach(answer -> {
                List<String> answersList = new ArrayList<>();
                Integer answerNo = answer.getKey();
                answersList.add(getString(answer.getValue()));
                answersList.add(getString(student.getAnswers().get(answerNo)));
                student.getCheatingPossibilities().forEach(neighbouour -> {
                    answersList.add(getString(neighbouour.getAnswers().get(answerNo)));
                });
                student.neighboursAnswersMap.put(answerNo, answersList);
            });
        });
    }

    private static String getString(String value) {
        return StringUtils.isEmpty(value) ? "-" : value;
    }

    public static void analyzeAnswers(List<Student> students) {
        students.forEach(student -> {
            Set<Map.Entry<Integer, List<String>>> answersSet = student.getNeighboursAnswersMap().entrySet();
            long correctAnswersCount = answersSet.stream().filter(it -> StringUtils.equals(it.getValue().get(0), it.getValue().get(1))).count();
            long inCorrectAnswersCount = answersSet.stream().filter(it -> !StringUtils.equals(it.getValue().get(0), it.getValue().get(1))
            && !StringUtils.equals("-", it.getValue().get(1))).count();
            long noAnswersCount = answersSet.stream().filter(it -> StringUtils.equals("-", it.getValue().get(1))).count();
            Map<Integer, String> neighboursDominantAnswerMap = getDominantAnswer(answersSet);
            long answerEqualsDominantAnswerCount = answersSet.stream().filter(it -> StringUtils.equals(neighboursDominantAnswerMap.get(it.getKey()), it.getValue().get(1))).count();
            long noAnswerWhenNeighboursHaveAnswerCount = answersSet.stream().filter(it -> StringUtils.equals("-", it.getValue().get(1)) && it.getValue().size() > 2).count();
            System.out.printf("%s from 16 questions has:%n%d correct answers%n%d incorect answers%n%d non answered questions%n"
                                      + "%d answers equals to most dominant neighbours answer%n%d times was now answer when neighbours have one%n"
            , student.getName(), correctAnswersCount, inCorrectAnswersCount, noAnswersCount, answerEqualsDominantAnswerCount, noAnswerWhenNeighboursHaveAnswerCount);
        });
    }

    private static Map<Integer, String> getDominantAnswer(Set<Map.Entry<Integer, List<String>>> answersSet) {
        Map<Integer, String> dominantNeighbourAnswer = new HashMap<>();
        answersSet.forEach(answer -> {
            Map<String, Integer> answersCountMap = new HashMap<>();
            List<String> values = answer.getValue();
            for (int i = 2; i <values.size(); i++) {
                if (answersCountMap.containsKey(values.get(i))) {
                    answersCountMap.put(values.get(i), answersCountMap.get(values.get(i)) + 1);
                } else {
                    answersCountMap.put(values.get(i), 1);
                }
            }
            Optional<String> s = answersCountMap.entrySet().stream().sorted(Map.Entry.comparingByValue()).findFirst().map(Map.Entry::getKey);
            if (s.isPresent()) {
                dominantNeighbourAnswer.put(answer.getKey(), s.orElse("-"));
            }
        });
        return dominantNeighbourAnswer;
    }
}
