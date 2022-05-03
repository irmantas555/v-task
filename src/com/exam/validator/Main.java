package com.exam.validator;

import static java.lang.Math.abs;

import java.io.BufferedReader;
import java.io.FileReader;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
import java.util.stream.Collectors;

import org.apache.commons.lang3.StringUtils;

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

    private static Map<Integer, String> correctAnswers = new HashMap<>();

    private static Map<Integer, List<Character>> correctOptionsMap = new HashMap<>();

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

    public static void poupulateOptions() {
        correctAnswers.entrySet().forEach(answer -> {
            correctOptionsMap.put(answer.getKey(), answer.getValue().chars().mapToObj(c -> (char) c).toList());
        });
    }

    public static void main(String[] args) {
        populateAnswers();
        poupulateOptions();
        List<Student> students = CSVReader.parse();
        StudentUtils.addNeighbours(students);
        StudentUtils.collectNeighboursAnswers(students);
        StudentUtils.analyzeAnswers(students);
    }

    public static Map<Integer, String> getCorrectAnswers() {
        return correctAnswers;
    }

    public static Map<Integer, List<Character>> getCorrectOptionsMap() {
        return correctOptionsMap;
    }
}

class Student {

    private String name;
    private String sittingLocation;
    private Map<Integer, String> answers = new HashMap<Integer, String>();

    private List<Student> cheatingPossibilities = new ArrayList<>();

    Map<Integer, List<String>> neighboursAnswersMap = new HashMap<>();

    List<Student> identicalNeighbours = new ArrayList<>();

    Map<Integer, Set<Character>> commonNeighbourOptions = new HashMap<>();
    Map<Integer, Set<Character>> dominantNeighbourOptions = new HashMap<>();

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

    List<Student> getIdenticalNeighbours() {
        return identicalNeighbours;
    }

    void setIdenticalNeighbours(List<Student> identicalNeighbours) {
        this.identicalNeighbours = identicalNeighbours;
    }

    Map<Integer, Set<Character>> getCommonNeighbourOptions() {
        return commonNeighbourOptions;
    }

    void setCommonNeighbourOptions(Map<Integer, Set<Character>> commonNeighbourOptions) {
        this.commonNeighbourOptions = commonNeighbourOptions;
    }

    Map<Integer, Set<Character>> getDominantNeighbourOptions() {
        return dominantNeighbourOptions;
    }

    void setDominantNeighbourOptions(Map<Integer, Set<Character>> dominantNeighbourOptions) {
        this.dominantNeighbourOptions = dominantNeighbourOptions;
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
            student.identicalNeighbours.addAll(student.getCheatingPossibilities());
            Main.getCorrectAnswers().entrySet().forEach(answer -> {
                HashSet<Character> neighboursCommonOptionsSet = new HashSet<Character>();
                Map<Character, Integer> neighbourOptionsCount = new HashMap<>();
                List<String> answersList = new ArrayList<>();
                Integer answerNo = answer.getKey();
                answersList.add(getString(answer.getValue()));
                answersList.add(getString(student.getAnswers().get(answerNo)));
                student.getCheatingPossibilities().forEach(neighbouour -> {
                    List<Character> characters = neighbouour.getAnswers().get(answerNo).chars().mapToObj(e -> (char) e).toList();
                    answersList.add(getString(neighbouour.getAnswers().get(answerNo)));
                    if (student.identicalNeighbours.contains(neighbouour) && !neighbouour.getAnswers().get(answerNo).equals(answer.getValue())) {
                        student.identicalNeighbours.remove(neighbouour);
                    }
                    if (neighboursCommonOptionsSet.isEmpty()) {
                        neighboursCommonOptionsSet.addAll(characters);
                    } else {
                        neighboursCommonOptionsSet.retainAll(characters);
                    }
                    characters.forEach(character -> {
                        if (neighbourOptionsCount.containsKey(character)) {
                            neighbourOptionsCount.put(character, neighbourOptionsCount.get(character) + 1);
                        } else {
                            neighbourOptionsCount.put(character, 1);
                        }
                    });
                });
                student.getCommonNeighbourOptions().put(answerNo, neighboursCommonOptionsSet);
                student.getDominantNeighbourOptions().put(answerNo, neighbourOptionsCount.entrySet().stream().filter(
                                                                                                 it -> it.getValue().doubleValue() >= (it.getValue() != 1 ? ((double) student.getCheatingPossibilities().size()) / 2 : (double) student.getCheatingPossibilities().size()))
                                                                                         .map(Map.Entry::getKey).collect(Collectors.toSet()));
                student.neighboursAnswersMap.put(answerNo, answersList);
            });
            if (student.identicalNeighbours.size() > 0) {
                System.out.printf("%s has neighbours with identical answer: ");
                student.identicalNeighbours.forEach(student1 -> {
                    System.out.println(student1.getName());
                });
            }
        });
    }

    private static String getString(String value) {
        return StringUtils.isEmpty(value) ? "-" : value;
    }

    public static void analyzeAnswers(List<Student> students) {
        students.forEach(student -> {
            Set<Map.Entry<Integer, List<String>>> answersSet = student.getNeighboursAnswersMap().entrySet();
            long correctAnswersCount = answersSet.stream().filter(it -> StringUtils.equals(it.getValue().get(0), it.getValue().get(1))).count();
            long incorrectOrPartiallyCorrectAnswersCount = answersSet.stream().filter(
                    it -> !StringUtils.equals(it.getValue().get(0), it.getValue().get(1)) && !StringUtils.equals("-", it.getValue().get(1))).count();
            long correctOptionsCount = answersSet.stream().mapToInt(it -> (int) getCorrectOptionsCount(it.getValue().get(1), Main.getCorrectOptionsMap().get(it.getKey()))).sum();
            long incorrectOptionsCount = answersSet.stream().mapToInt(it -> (int) getIncorrectOptionsCount(it.getValue().get(1), Main.getCorrectOptionsMap().get(it.getKey()))).sum();
            long neighbourDominantCorrectAndNotChosenOptionsCount = answersSet.stream().mapToInt(
                                                                                      it -> (int) getDominantOptionsNotChosenAndCorrectCount(it.getValue().get(1), Main.getCorrectOptionsMap().get(it.getKey()), student.getDominantNeighbourOptions().get(it.getKey())))
                                                                              .sum();
            long correctAndNotChosenOptionsCount = answersSet.stream().mapToInt(it -> (int) getCorrectNotChosenOptionsCount(it.getValue().get(1), Main.getCorrectOptionsMap().get(it.getKey()))).sum();
            Map<Integer, String> neighboursDominantAnswerMap = getDominantAnswer(answersSet);
            long answerEqualsDominantAnswerCount = answersSet.stream().filter(it -> StringUtils.equals(neighboursDominantAnswerMap.get(it.getKey()), it.getValue().get(1))).count();
            long answerEqualsDominantAnswerThatIsIncorrectCount = answersSet.stream().filter(
                                                                                    it -> StringUtils.equals(neighboursDominantAnswerMap.get(it.getKey()), it.getValue().get(1)) && StringUtils.equals(it.getValue().get(1), Main.getCorrectAnswers().get(it.getKey())))
                                                                            .count();
            long answerOptionsEqualsDominantOptionsCount = answersSet.stream().filter(it -> StringUtils.equals(it.getValue().get(1), student.getDominantNeighbourOptions().get(it.getKey()).stream()
                                                                                                                                            .map(String::valueOf).collect(Collectors.joining())))
                                                                     .count();
            long timesPickedIncorrectOptionWhichWasNeighbourDominant = answersSet.stream().mapToInt(
                    it -> (int) getIncorrectAndDominantOptionsCount(it.getValue().get(1), Main.getCorrectOptionsMap().get(it.getKey()), student.getDominantNeighbourOptions().get(it.getKey()))).sum();
            System.out.printf("%s from 16 questions has:" + "%n%d times all options correct" + "%n%d incorrect or partially answers" + "%n%d times options chosen is correct"
                                      + "%n%d times option is correct but not chosen" + "%n%d times major count of neighbours had this option it is correct but was not chosen"
                                      + "%n%d times options chosen is incorrect" + "%n%d times option equals to most dominant neighbours options and is incorrect"
                                      + "%n%d answers options equals to most dominant neighbours options" + "%n%d answers equals to most dominant neighbours answer from which incorrect is %d%n%n",
                              student.getName(), correctAnswersCount, incorrectOrPartiallyCorrectAnswersCount, correctOptionsCount, correctAndNotChosenOptionsCount,
                              neighbourDominantCorrectAndNotChosenOptionsCount, incorrectOptionsCount, timesPickedIncorrectOptionWhichWasNeighbourDominant, answerOptionsEqualsDominantOptionsCount,
                              answerEqualsDominantAnswerCount, answerEqualsDominantAnswerThatIsIncorrectCount);
        });
    }

    private static long getIncorrectAndDominantOptionsCount(String s, List<Character> characters, Set<Character> dominantNeighboursOptions) {
        List<Character> answerOptions = s.chars().mapToObj(c -> (char) c).toList();
        return answerOptions.stream().filter(c -> !characters.contains(c) && dominantNeighboursOptions.contains(c)).count();
    }

    private static long getDominantOptionsNotChosenAndCorrectCount(String s, List<Character> correctCharacters, Set<Character> dominantNeighboursOptions) {
        List<Character> answerOptions = s.chars().mapToObj(c -> (char) c).toList();
        return dominantNeighboursOptions.stream().filter(c -> correctCharacters.contains(c) && !answerOptions.contains(c)).count();
    }

    private static long getCorrectOptionsCount(String s, List<Character> correctCharacters) {
        List<Character> answerOptions = s.chars().mapToObj(c -> (char) c).toList();
        long count = answerOptions.stream().filter(c -> correctCharacters.contains(c)).count();
        return count;
    }

    private static long getIncorrectOptionsCount(String s, List<Character> correctCharacters) {
        List<Character> answerOptions = s.chars().mapToObj(c -> (char) c).toList();
        long count = answerOptions.stream().filter(c -> !correctCharacters.contains(c)).count();
        return count;
    }

    private static long getCorrectNotChosenOptionsCount(String s, List<Character> correctCharacters) {
        List<Character> answerOptions = s.chars().mapToObj(c -> (char) c).toList();
        long count = correctCharacters.stream().filter(c -> !answerOptions.contains(c)).count();
        return count;
    }

    private static Map<Integer, String> getDominantAnswer(Set<Map.Entry<Integer, List<String>>> answersSet) {
        Map<Integer, String> dominantNeighbourAnswer = new HashMap<>();
        answersSet.forEach(answer -> {
            Map<String, Integer> answersCountMap = new HashMap<>();
            List<String> values = answer.getValue();
            for (int i = 2; i < values.size(); i++) {
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
