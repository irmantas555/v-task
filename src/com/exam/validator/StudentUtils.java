package com.exam.validator;

import static java.lang.Math.abs;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;
import java.util.Set;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.function.Predicate;
import java.util.logging.Logger;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import org.apache.commons.collections4.CollectionUtils;
import org.apache.commons.lang3.StringUtils;

final class StudentUtils {

    StudentUtils() {
    }

    public static void addNeighbours(List<Student> students) {
        students.forEach(student -> {
            Integer studentRow = getRow(student);
            Integer seat = getSeat(student);
            students.forEach(possibleNeighbour -> {
                if (!possibleNeighbour.equals(student) && (getRow(possibleNeighbour).equals(studentRow) && abs(getSeat(possibleNeighbour) - seat) == 1
                        || (studentRow - getRow(possibleNeighbour) == 1 && abs(getSeat(possibleNeighbour) - seat) <= 1))) {
                    student.getCheatingPossibilities().add(possibleNeighbour);
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
                HashSet<Character> neighboursCommonOptionsSet = new HashSet<>();
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
            if (!student.identicalNeighbours.isEmpty()) {
                System.out.printf("%s has following neighbours with identical answer: ", student.getName());
                student.identicalNeighbours.forEach(student1 -> System.out.println(student1.getName()));
            }
        });
    }

    private static String getString(String value) {
        return StringUtils.isEmpty(value) ? "-" : value;
    }

    public static void analyzeAnswers(List<Student> students) {
        students.forEach(student -> {
            Set<Map.Entry<Integer, List<String>>> answersSet = student.getNeighboursAnswersMap().entrySet();
            Map<Integer, String> neighboursDominantAnswerMap = getDominantAnswer(answersSet);
            Map<AnswerTypes, Integer> analysisMap = student.getStudentAnalysisMap();

            analysisMap.put(AnswerTypes.CORRECT_ANSWER_COUNT,
                            (int) answersSet.stream()
                                            .filter(it -> StringUtils.equals(it.getValue().get(0), it.getValue().get(1)))
                                            .count());
            analysisMap.put(AnswerTypes.INCORRECT_OR_PARTIAL_ANSWER_COUNT,
                            (int) answersSet.stream()
                                            .filter(it -> !StringUtils.equals(it.getValue().get(0), it.getValue().get(1)) && !StringUtils.equals("-", it.getValue().get(1)))
                                            .count());
            analysisMap.put(AnswerTypes.CORRECT_OPTION_COUNT,
                            answersSet.stream()
                                      .mapToInt(it -> (int) getCorrectOptionsCount(it.getValue().get(1), Main.getCorrectOptionsMap().get(it.getKey())))
                                      .sum());
            analysisMap.put(AnswerTypes.INCORRECT_OPTION_COUNT,
                            answersSet.stream()
                                      .mapToInt(it -> (int) getIncorrectOptionsCount(it.getValue().get(1), Main.getCorrectOptionsMap().get(it.getKey())))
                                      .sum());
            analysisMap.put(AnswerTypes.CORRECT_NOT_CHOSEN_OPTION_COUNT,
                            answersSet.stream()
                                      .mapToInt(it -> (int) getCorrectNotChosenOptionsCount(it.getValue().get(1), Main.getCorrectOptionsMap().get(it.getKey())))
                                      .sum());
            analysisMap.put(AnswerTypes.EQUAL_NEIGHBOURS_DOMINANT_ANSWER_COUNT,
                            (int) answersSet.stream()
                                            .filter(it -> StringUtils.equals(neighboursDominantAnswerMap.get(it.getKey()), it.getValue().get(1)))
                                            .count());
            analysisMap.put(AnswerTypes.EQUAL_NEIGHBOURS_DOMINANT_INCORRECT_ANSWER_COUNT,
                            (int) answersSet.stream()
                                            .filter(it -> StringUtils.equals(neighboursDominantAnswerMap.get(it.getKey()), it.getValue().get(1))
                                                    && StringUtils.equals(it.getValue().get(1), Main.getCorrectAnswers().get(it.getKey()))).count());
            analysisMap.put(AnswerTypes.EQUAL_NEIGHBOURS_DOMINANT_OPTION_COUNT,
                            answersSet.stream()
                                      .mapToInt(it -> (int) getChosenOptionsEqualsDominantCount(it.getValue().get(1), student.getDominantNeighbourOptions().get(it.getKey())))
                                      .sum());
            analysisMap.put(AnswerTypes.NEIGHBOURS_CORRECT_NOT_CHOSEN_OPTION_COUNT,
                            answersSet.stream()
                                      .mapToInt(it -> (int) getDominantOptionsNotChosenAndCorrectCount(it.getValue().get(1), Main.getCorrectOptionsMap().get(it.getKey()),
                                                                                                       student.getDominantNeighbourOptions().get(it.getKey())))
                                      .sum());
            analysisMap.put(AnswerTypes.NEIGHBOURS_INCORRECT_NOT_CHOSEN_OPTION_COUNT,
                            answersSet.stream()
                                      .mapToInt(it -> (int) getIncorrectAndDominantOptionsCount(it.getValue().get(1), Main.getCorrectOptionsMap().get(it.getKey()),
                                                                                                student.getDominantNeighbourOptions().get(it.getKey())))
                                      .sum());
        });
    }

    public static void printFullStudentsAnalysis(List<Student> students, Integer studentNr) {
        for (int i = 0; i < students.size(); i++) {
            Map<AnswerTypes, Integer> analysisMap = students.get(i).getStudentAnalysisMap();
            if (studentNr == null || (studentNr != null && i + 1 == studentNr)) {
                System.out.printf("%s from 16 questions has:" + "%n%d times all options correct" + "%n%d incorrect or partially correct answers" + "%n%d times option chosen is correct"
                                          + "%n%d times option is correct but not chosen" + "%n%d times options chosen is incorrect"
                                          + "%n%d times major count of neighbours had option marked it is correct but was not chosen (less means higher cheating possibility)"
                                          + "%n%d times major count of neighbours had option marked it is incorrect but was not chosen(less means higher cheating possibility)"
                                          + "%n%d times answers options equals to most dominant neighbours options (more means higher cheating possibility)"
                                          + "%n%d all answer options equals to most dominant neighbours answer option set from which incorrect is %d (more means higher cheating possibility)%n%n",
                                  students.get(i).getName(),
                                  analysisMap.get(AnswerTypes.CORRECT_ANSWER_COUNT),
                                  analysisMap.get(AnswerTypes.INCORRECT_OR_PARTIAL_ANSWER_COUNT),
                                  analysisMap.get(AnswerTypes.CORRECT_OPTION_COUNT),
                                  analysisMap.get(AnswerTypes.CORRECT_NOT_CHOSEN_OPTION_COUNT),
                                  analysisMap.get(AnswerTypes.INCORRECT_OPTION_COUNT),
                                  analysisMap.get(AnswerTypes.NEIGHBOURS_CORRECT_NOT_CHOSEN_OPTION_COUNT),
                                  analysisMap.get(AnswerTypes.NEIGHBOURS_INCORRECT_NOT_CHOSEN_OPTION_COUNT),
                                  analysisMap.get(AnswerTypes.EQUAL_NEIGHBOURS_DOMINANT_OPTION_COUNT),
                                  analysisMap.get(AnswerTypes.EQUAL_NEIGHBOURS_DOMINANT_ANSWER_COUNT),
                                  analysisMap.get(AnswerTypes.EQUAL_NEIGHBOURS_DOMINANT_INCORRECT_ANSWER_COUNT));
            }
        }
    }

    private static long getChosenOptionsEqualsDominantCount(String s, Set<Character> dominantNeighboursOptions) {
        List<Character> answerOptions = s.chars().mapToObj(c -> (char) c).toList();
        return answerOptions.stream().filter(dominantNeighboursOptions::contains).count();
    }

    private static long getIncorrectAndDominantOptionsCount(String s, List<Character> correctCharacters, Set<Character> dominantNeighboursOptions) {
        List<Character> answerOptions = s.chars().mapToObj(c -> (char) c).toList();
        return answerOptions.stream().filter(c -> !correctCharacters.contains(c) && dominantNeighboursOptions.contains(c)).count();
    }

    private static long getDominantOptionsNotChosenAndCorrectCount(String s, List<Character> correctCharacters, Set<Character> dominantNeighboursOptions) {
        List<Character> answerOptions = s.chars().mapToObj(c -> (char) c).toList();
        return dominantNeighboursOptions.stream().filter(c -> correctCharacters.contains(c) && !answerOptions.contains(c)).count();
    }

    private static long getCorrectOptionsCount(String s, List<Character> correctCharacters) {
        List<Character> answerOptions = s.chars().mapToObj(c -> (char) c).toList();
        return answerOptions.stream().filter(correctCharacters::contains).count();
    }

    private static long getIncorrectOptionsCount(String s, List<Character> correctCharacters) {
        List<Character> answerOptions = s.chars().mapToObj(c -> (char) c).toList();
        return answerOptions.stream().filter(Predicate.not(correctCharacters::contains)).count();
    }

    private static long getCorrectNotChosenOptionsCount(String s, List<Character> correctCharacters) {
        List<Character> answerOptions = s.chars().mapToObj(c -> (char) c).toList();
        return correctCharacters.stream().filter(Predicate.not(answerOptions::contains)).count();
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

    public static Map<String, List<String>> makeBadAnswersMap(List<Student> students) {
        List<Character> allAnswerOPtions = List.of('a', 'b', 'c', 'd', 'e', 'f');
        Map<String, List<String>> incorectOptionsAndSeatUsedMap = new HashMap<>();
        for (int i = 1; i < 16; i++) {
            List<Character> correctOptionsList = Main.getCorrectOptionsMap().get(i);
            List<Character> incorrectPossibleOptionsList = allAnswerOPtions.stream().filter(it -> !correctOptionsList.contains(it)).toList();
            int finalI = i;
            students.forEach(student -> {
                List<Character> anserOptions = student.getAnswers().get(finalI).chars().mapToObj(c -> (char) c).toList();
                anserOptions.forEach(ch -> {
                    if (incorrectPossibleOptionsList.contains(ch)) {
                        String answerString = String.valueOf(finalI) + "-" + ch;
                        if (incorectOptionsAndSeatUsedMap.get(answerString) != null) {
                            incorectOptionsAndSeatUsedMap.get(answerString).add(student.getSittingLocation());
                        } else {
                            List<String> seatList = new ArrayList<>();
                            seatList.add(student.getSittingLocation());
                            incorectOptionsAndSeatUsedMap.put(answerString, seatList);
                        }
                    }
                });
            });
        }
        return incorectOptionsAndSeatUsedMap;
    }

    public static void drawBadOptionsPlan(Map<String, List<String>> badAnswerOptionsMap, List<Student> students) {

        badAnswerOptionsMap.entrySet().forEach(entry -> {
            if (entry.getValue().size() > 1) {
                System.out.println("Plan of students with option marked for Answer-Option: " + entry.getKey());
                printSeatsPlan(students, entry, null);
                String question = entry.getKey().split("-")[0];
                String option = entry.getKey().split("-")[1];
                System.out.println("Legend:");
                System.out.println("'n'r - n row number");
                System.out.println("'n's - n seat number");
                System.out.println("'-'- Incorrect option '" + option + "' for question " + question + " in this seat is not marked");
                System.out.println("'B' - Incorrect option '" + option + "' for question " + question + " in this seat is marked");
                System.out.println("'X' - This seat is not occupied");
                System.out.println("");
            }
        });
    }

    private static void printSeatsPlan(List<Student> students, Map.Entry<String, List<String>> entry, Map<String, Integer> timesMap) {
        List<String> occupiedSeats = students.stream().map(Student::getSittingLocation).toList();
        for (int i = 9; i >= 1; i--) {
            if (i != 9) {
                System.out.print(i + "r ");
            } else {
                System.out.print("   ");
            }
            for (int j = 1; j <= 8; j++) {
                if (i == 9) {
                    System.out.print(j + "s ");
                }
                String seatString = i + "." + j;
                if (occupiedSeats.contains(seatString)) {
                    if (entry != null && entry.getValue().contains(seatString)) {
                        System.out.print("B  ");
                    } else if (entry != null) {
                        System.out.print("-  ");
                    }
                    if (timesMap != null && timesMap.containsKey(seatString)) {
                        System.out.printf(timesMap.get(seatString).toString() + (timesMap.get(seatString) < 10 ? "  " : " "));
                    } else if (timesMap != null) {
                        System.out.print("-  ");
                    }
                } else if (i != 9) {
                    System.out.print("X  ");
                }
            }
            System.out.printf("%n");
        }
    }

    public static Map<String, Integer> calculateBadOptionsAccumulatedPlan(Map<String, List<String>> badAnswerOptionsMap, List<Student> students) {
        Map<String, Integer> badOptionWhenNeighbourHaveTimes = new HashMap<>();
        badAnswerOptionsMap.entrySet().forEach(answerOption -> answerOption.getValue().forEach(seat -> {
            Optional<Student> seatOwner = students.stream().filter(it -> it.getSittingLocation().equals(seat)).findFirst();
            if (seatOwner.isPresent()) {
                Student studentOwner = seatOwner.get();
                List<String> neighbourSeats = Stream.of(studentOwner).flatMap(student -> student.getCheatingPossibilities().stream()).map(Student::getSittingLocation).toList();
                Optional<String> firstNeighbour = neighbourSeats.stream().filter(s -> answerOption.getValue().contains(s)).findFirst();
                List<String> similarNeighbourSeats = neighbourSeats.stream().filter(s -> answerOption.getValue().contains(s)).toList();
                if (firstNeighbour.isPresent()) {
                    if (badOptionWhenNeighbourHaveTimes.get(seat) != null) {
                        badOptionWhenNeighbourHaveTimes.put(seat, badOptionWhenNeighbourHaveTimes.get(seat) + 1);
                    } else {
                        badOptionWhenNeighbourHaveTimes.put(seat, 1);
                    }
                }
                if (CollectionUtils.isNotEmpty(similarNeighbourSeats)) {
                    similarNeighbourSeats.forEach(s -> {
                        if (studentOwner.getNeighboursWithSimmilarBadAnswersCountMap().get(s) != null) {
                            studentOwner.getNeighboursWithSimmilarBadAnswersCountMap().put(s, studentOwner.getNeighboursWithSimmilarBadAnswersCountMap().get(s) + 1);
                        } else {
                            studentOwner.getNeighboursWithSimmilarBadAnswersCountMap().put(s, 1);
                        }
                    });
                }
            }
        }));
        return badOptionWhenNeighbourHaveTimes;
    }

    public static void drawBadOptionsAccumulatedPlan(Map<String, Integer> badAnswerOptionsTimesMap, List<Student> students) {
        System.out.println("Plan of students with repetition times of wrong option marked when one of the neighbours have same:");
        printSeatsPlan(students, null, badAnswerOptionsTimesMap);
        System.out.println("Legend:");
        System.out.println("'n'r - n row number");
        System.out.println("'n's - n seat number");
        System.out.println("'n' - Incorrect option used n times when this student neighbour have same");
        System.out.println("'-' - Incorrect option not used when this student neighbour have same");
        System.out.println("'X' - This seat is not occupied");
        System.out.println("");
    }

    public static void analyzePossibleCheatingNeighbours(List<Student> students, Integer minPrecentage) {
        students.forEach(student -> student.getNeighboursWithSimmilarBadAnswersCountMap().entrySet().stream().filter(entry -> entry.getValue() > 2).map(Map.Entry::getKey).map(
                                                   s -> students.stream().filter(student1 -> student1.getSittingLocation().equals(s)).findFirst().orElse(null)).filter(Objects::nonNull)
                                           .peek(neighbourWithSimilarIncorectAnswers -> analyzeAnswerSimmiliarity(neighbourWithSimilarIncorectAnswers, student, minPrecentage)).count()
        );
    }

    private static void analyzeAnswerSimmiliarity(Student neighbour, Student student, Integer minPrecentage) {
        String neighbourSeat = neighbour.getSittingLocation();
        String studentSeat = student.getSittingLocation();
        Map<OptionSimmiliaritiesTypes, Integer> aggregateOptionMap = new HashMap<>();
        if (Main.getAnalyzedPairs().get(neighbourSeat) != null && !(Main.getAnalyzedPairs().get(neighbourSeat).contains(studentSeat) || (Main.getAnalyzedPairs().get(studentSeat) != null
                && Main.getAnalyzedPairs().get(studentSeat).contains(neighbourSeat)))) {
            return;
        } else if (Main.getAnalyzedPairs().get(neighbourSeat) == null) {
            List<String> analyzedStudents = new ArrayList<>();
            analyzedStudents.add(studentSeat);
            Main.getAnalyzedPairs().put(neighbourSeat, analyzedStudents);
        } else {
            Main.getAnalyzedPairs().get(neighbourSeat).add(studentSeat);
        }
        Map<Integer, String> studentAnswers = student.getAnswers();
        Map<Integer, String> neighbourAnswers = neighbour.getAnswers();
        studentAnswers.entrySet().forEach(it -> {
            Map<Character, Integer> totalIncorrectOccurrencesMap = Main.getIncorrectChosenOptionsCountMap().get(it.getKey());
            Map<OptionSimmiliaritiesTypes, Integer> optionMap = analyzeOneAnswer(it.getValue(), neighbourAnswers.get(it.getKey()), Main.getCorrectOptionsMap().get(it.getKey()),
                                                                                 totalIncorrectOccurrencesMap);
            addValues(optionMap, aggregateOptionMap);
        });
        AtomicBoolean titlePrinted = new AtomicBoolean(false);
        aggregateOptionMap.entrySet().forEach(optionType -> {

            Integer totalCorrectOptionsCount = aggregateOptionMap.get(OptionSimmiliaritiesTypes.CORRECT_AND_EQUAL) + aggregateOptionMap.get(OptionSimmiliaritiesTypes.CORRECT_AND_ABSENT);
            Integer totalIncorrectOptionsCount = aggregateOptionMap.get(OptionSimmiliaritiesTypes.INCORRECT_AND_EQUAL) + aggregateOptionMap.get(OptionSimmiliaritiesTypes.INCORRECT_AND_ABSENT);
            Integer correctProcentage = (int) (optionType.getValue().doubleValue() / totalCorrectOptionsCount.doubleValue() * 100);
            Integer incorrectProcentage = (int) (optionType.getValue().doubleValue() / totalIncorrectOptionsCount.doubleValue() * 100);
            if (minPrecentage == null || (minPrecentage != null && (optionType.getKey() == OptionSimmiliaritiesTypes.CORRECT_AND_EQUAL && correctProcentage > minPrecentage
                    || optionType.getKey() == OptionSimmiliaritiesTypes.INCORRECT_AND_EQUAL && incorrectProcentage > minPrecentage))) {
                if (!titlePrinted.get()) {
                    System.out.println("Similiarities statistics for " + student.getName() + " and " + neighbour.getName() + ":");
                    titlePrinted.set(true);
                }
                switch (optionType.getKey()) {
                    case CORRECT_AND_EQUAL -> System.out.println(optionType.getValue().toString() + " times correct answers between students are equal which is " + (int) (
                            optionType.getValue().doubleValue() / totalCorrectOptionsCount.doubleValue() * 100) + "% of all correct options (more means higher cheating possibility)");
                    case INCORRECT_AND_EQUAL -> System.out.println(optionType.getValue().toString() + " times incorrect answers between students are equal which is " + (int) (
                            optionType.getValue().doubleValue() / totalIncorrectOptionsCount.doubleValue() * 100) + "% of all incorrect options (more means higher cheating possibility)");
                    case INCORRECT_AND_EQUAL_AND_RARE -> System.out.println(
                            optionType.getValue().toString() + " times incorrect answers between students are equal and this option is chosen less than 4 time by all other students");
                }
                if (minPrecentage != null) {
                    Integer studentRow = null;
                    Integer neighbourRow = null;
                    String cheat = "both";

                    try {
                        studentRow = Integer.parseInt(student.getSittingLocation().split("\\.")[0]);
                        neighbourRow = Integer.parseInt(neighbour.getSittingLocation().split("\\.")[0]);
                    } catch (Exception e) {
                        System.out.println("Students seating places could not be parsed");
                    }
                    if (studentRow != null && neighbourRow != null) {
                        Integer diff = studentRow - neighbourRow;
                        if (diff > 0) {cheat = neighbour.getName();} else if (diff > 0) {cheat = student.getName();}
                        System.out.printf("Cheater" + (cheat == "both" ? "s" : "") + " could be %s,", cheat);
                    }
                    System.out.println("since student row is " + studentRow + " and neighbour row is " + neighbourRow);
                    System.out.println();
                }
            }
        });
        if (minPrecentage == null) {
            System.out.println();
        }
    }

    private static void addValues(Map<OptionSimmiliaritiesTypes, Integer> map, Map<OptionSimmiliaritiesTypes, Integer> aggregateOptionMap) {
        map.entrySet().forEach(entry -> {
            if (aggregateOptionMap.get(entry.getKey()) != null) {
                aggregateOptionMap.put(entry.getKey(), aggregateOptionMap.get(entry.getKey()) + 1);
            } else {
                aggregateOptionMap.put(entry.getKey(), 1);
            }
        });
    }

    private static Map<OptionSimmiliaritiesTypes, Integer> analyzeOneAnswer(String studentAnswer, String neighbourAnswer, List<Character> correctOptions,
            Map<Character, Integer> totalIncorrectOccurrencesMap) {
        List<Character> studentOptions = studentAnswer.chars().mapToObj(c -> (char) c).toList();
        List<Character> neighbourOptions = neighbourAnswer.chars().mapToObj(c -> (char) c).toList();
        Map<OptionSimmiliaritiesTypes, Integer> optionMap = new HashMap<>();
        studentOptions.forEach(option -> {
            if (neighbourOptions.contains(option) && correctOptions.contains(option)) {
                optionMap.merge(OptionSimmiliaritiesTypes.CORRECT_AND_EQUAL, 1, Integer::sum);
            }

            if (neighbourOptions.contains(option) && !correctOptions.contains(option)) {
                optionMap.merge(OptionSimmiliaritiesTypes.INCORRECT_AND_EQUAL, 1, Integer::sum);
                Integer totalTimesChosen = totalIncorrectOccurrencesMap.get(option);
                if (totalTimesChosen < 5) {
                    optionMap.merge(OptionSimmiliaritiesTypes.INCORRECT_AND_EQUAL_AND_RARE, 1, Integer::sum);
                }
            }

            if (!neighbourOptions.contains(option) && correctOptions.contains(option)) {
                optionMap.merge(OptionSimmiliaritiesTypes.CORRECT_AND_ABSENT, 1, Integer::sum);
            }

            if (!neighbourOptions.contains(option) && !correctOptions.contains(option)) {
                optionMap.merge(OptionSimmiliaritiesTypes.INCORRECT_AND_ABSENT, 1, Integer::sum);
            }
            optionMap.merge(OptionSimmiliaritiesTypes.TOTAL_COUNT, 1, Integer::sum);
        });
        return optionMap;
    }

    public static void printPersonalPreference(List<Student> students) {
        students.forEach(student -> {
            if (student.getStudentAnalysisMap().get(AnswerTypes.NEIGHBOURS_CORRECT_NOT_CHOSEN_OPTION_COUNT) < 4 &&
                    student.getStudentAnalysisMap().get(AnswerTypes.EQUAL_NEIGHBOURS_DOMINANT_INCORRECT_ANSWER_COUNT) < 3 &&
                    student.getStudentAnalysisMap().get(AnswerTypes.EQUAL_NEIGHBOURS_DOMINANT_ANSWER_COUNT) >= 1) {
                printFullStudentsAnalysis(students, students.indexOf(student) + 1);
            }
        });
        analyzePossibleCheatingNeighbours(students, 80);
    }
}
