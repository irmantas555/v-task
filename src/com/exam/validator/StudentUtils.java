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
import java.util.stream.Collectors;
import java.util.stream.Stream;

import org.apache.commons.collections4.CollectionUtils;
import org.apache.commons.lang3.StringUtils;

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
                System.out.printf("%s has neighbours with identical answer: ", student.getName());
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
            long correctAnswersCount = answersSet.stream().filter(it -> StringUtils.equals(it.getValue().get(0), it.getValue().get(1))).count();
            long incorrectOrPartiallyCorrectAnswersCount = answersSet.stream().filter(
                    it -> !StringUtils.equals(it.getValue().get(0), it.getValue().get(1)) && !StringUtils.equals("-", it.getValue().get(1))).count();
            long correctOptionsCount = answersSet.stream().mapToInt(it -> (int) getCorrectOptionsCount(it.getValue().get(1), Main.getCorrectOptionsMap().get(it.getKey()))).sum();
            long incorrectOptionsCount = answersSet.stream().mapToInt(it -> (int) getIncorrectOptionsCount(it.getValue().get(1), Main.getCorrectOptionsMap().get(it.getKey()))).sum();
            long correctAndNotChosenOptionsCount = answersSet.stream().mapToInt(it -> (int) getCorrectNotChosenOptionsCount(it.getValue().get(1), Main.getCorrectOptionsMap().get(it.getKey()))).sum();
            Map<Integer, String> neighboursDominantAnswerMap = getDominantAnswer(answersSet);
            long answerEqualsDominantAnswerCount = answersSet.stream().filter(it -> StringUtils.equals(neighboursDominantAnswerMap.get(it.getKey()), it.getValue().get(1))).count();
            long answerEqualsDominantAnswerThatIsIncorrectCount = answersSet.stream().filter(
                                                                                    it -> StringUtils.equals(neighboursDominantAnswerMap.get(it.getKey()), it.getValue().get(1)) && StringUtils.equals(it.getValue().get(1), Main.getCorrectAnswers().get(it.getKey())))
                                                                            .count();
            long answerOptionsEqualsDominantOptionsCount = answersSet.stream().mapToInt(
                    it -> (int) getChosenOptionsEqualsDominantCount(it.getValue().get(1), student.getDominantNeighbourOptions().get(it.getKey()))).sum();
            long neighbourDominantCorrectAndNotChosenOptionsCount = answersSet.stream().mapToInt(
                                                                                      it -> (int) getDominantOptionsNotChosenAndCorrectCount(it.getValue().get(1), Main.getCorrectOptionsMap().get(it.getKey()), student.getDominantNeighbourOptions().get(it.getKey())))
                                                                              .sum();
            long neighbourDominantIncorrectAndNotChosenOptionsCount = answersSet.stream().mapToInt(
                    it -> (int) getIncorrectAndDominantOptionsCount(it.getValue().get(1), Main.getCorrectOptionsMap().get(it.getKey()), student.getDominantNeighbourOptions().get(it.getKey()))).sum();
            System.out.printf("%s from 16 questions has:" + "%n%d times all options correct" + "%n%d incorrect or partially answers" + "%n%d times option chosen is correct"
                                      + "%n%d times option is correct but not chosen" + "%n%d times options chosen is incorrect"
                                      + "%n%d times major count of neighbours had this option it is correct but was not chosen"
                                      + "%n%d times major count of neighbours had this option it is incorrect but was not chosen"
                                      + "%n%d times answers options equals to most dominant neighbours options"
                                      + "%n%d answer options equals to most dominant neighbours answer option set from which incorrect is %d%n%n", student.getName(), correctAnswersCount,
                              incorrectOrPartiallyCorrectAnswersCount, correctOptionsCount, correctAndNotChosenOptionsCount, incorrectOptionsCount, neighbourDominantCorrectAndNotChosenOptionsCount,
                              neighbourDominantIncorrectAndNotChosenOptionsCount, answerOptionsEqualsDominantOptionsCount, answerEqualsDominantAnswerCount,
                              answerEqualsDominantAnswerThatIsIncorrectCount);
        });
    }

    private static long getChosenOptionsEqualsDominantCount(String s, Set<Character> dominantNeighboursOptions) {
        List<Character> answerOptions = s.chars().mapToObj(c -> (char) c).toList();
        return answerOptions.stream().filter(c -> dominantNeighboursOptions.contains(c)).count();
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
                System.out.println("Bad Answer-Option: " + entry.getKey());
                peintSeatsPlan(students, entry, null);
                String question = entry.getKey().split("-")[0];
                String option = entry.getKey().split("-")[1];
                System.out.println("Legend:");
                System.out.println("'n'r - n row number");
                System.out.println("'n's - n seat number");
                System.out.println("'-'- Incorrect option '" + option + "' for question " + question + " in this seat is not marked");
                System.out.println("'B' - Incorrect option '" + option + "' for question " + question + " in this seat is marked");
                System.out.println("'X' - This seat is not occupied");
                System.out.println();
            }
        });
    }

    private static void peintSeatsPlan(List<Student> students, Map.Entry<String, List<String>> entry, Map<String, Integer> timesMap) {
        List<String> occupiedSeats = students.stream().map(Student::getSittingLocation).toList();
        for (int i = 9; i >= 1; i--) {
            if (i != 9) {
                System.out.printf(i + "r ");
            } else {
                System.out.printf("   ");
            }
            for (int j = 1; j <= 8; j++) {
                if (i == 9) {
                    System.out.printf(j + "s ");
                }
                String seatString = i + "." + j;
                if (occupiedSeats.contains(seatString)) {
                    if (entry != null && entry.getValue().contains(seatString)) {
                        System.out.printf("B  ");
                    } else if (entry != null) {
                        System.out.printf("-  ");
                    }
                    if (timesMap != null && timesMap.containsKey(seatString)) {
                        System.out.printf(timesMap.get(seatString).toString() + (timesMap.get(seatString) < 10 ? "  " : " "));
                    } else if (timesMap != null) {
                        System.out.printf("-  ");
                    }
                } else if (i != 9) {
                    System.out.printf("X  ");
                }
            }
            System.out.printf("%n");
        }
    }

    public static Map<String, Integer> calculateBadOptionsAccumulatedPlan(Map<String, List<String>> badAnswerOptionsMap, List<Student> students) {
        Map<String, Integer> badOptionWhenNeighbourHaveTimes = new HashMap<>();
        badAnswerOptionsMap.entrySet().forEach(answerOption -> {
            answerOption.getValue().forEach(seat -> {
                Optional<Student> seatOwner = students.stream().filter(it -> it.getSittingLocation() == seat).findFirst();
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
            });
        });
        return badOptionWhenNeighbourHaveTimes;
    }

    public static void drawBadOptionsAccumulatedPlan(Map<String, Integer> badAnswerOptionsTimesMap, List<Student> students) {
        System.out.println("Times bad option used when one of the neighbour have same plan:");
        peintSeatsPlan(students, null, badAnswerOptionsTimesMap);
        System.out.println("Legend:");
        System.out.println("'n'r - n row number");
        System.out.println("'n's - n seat number");
        System.out.println("'n'- Incorrect option used n times when this student neighbour have same");
        System.out.println("'-' - Incorrect option not used when this student neighbour have same");
        System.out.println("'X' - This seat is not occupied");
        System.out.println();
    }

    public static void analyzePossibleCheatingNeighbours(List<Student> students) {
        students.forEach(student -> {
            student.getNeighboursWithSimmilarBadAnswersCountMap().entrySet().stream().filter(entry -> entry.getValue() > 2).map(Map.Entry::getKey).map(s -> students.stream().filter(
                    student1 -> student1.getSittingLocation() == s).findFirst().orElse(null)).filter(Objects::nonNull).peek(
                    neighbourWithSimilarIncorectAnswers -> analyzeAnswerSimmiliarity(neighbourWithSimilarIncorectAnswers, student)).count();
        });
    }

    private static void analyzeAnswerSimmiliarity(Student neighbour, Student student) {
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
        System.out.println("Similiarities statistics for " + student.getName() + " and " + neighbour.getName() + ":");
        aggregateOptionMap.entrySet().forEach(optionType -> {
            Integer totalMarkedOptions = aggregateOptionMap.get(OptionSimmiliaritiesTypes.TOTAL_COUNT);
            Integer totalCorrectOptionsCount = aggregateOptionMap.get(OptionSimmiliaritiesTypes.CORRECT_AND_EQUAL) + aggregateOptionMap.get(OptionSimmiliaritiesTypes.CORRECT_AND_ABSENT);
            Integer totalIncorrectOptionsCount = aggregateOptionMap.get(OptionSimmiliaritiesTypes.INCORRECT_AND_EQUAL) + aggregateOptionMap.get(OptionSimmiliaritiesTypes.INCORRECT_AND_ABSENT);
            switch (optionType.getKey()) {
                case CORRECT_AND_EQUAL -> System.out.println(optionType.getValue().toString() + " times correct answers between students are equal which is " + (int) (
                        optionType.getValue().doubleValue() / totalCorrectOptionsCount.doubleValue() * 100) + "% of all correct options");
                case INCORRECT_AND_EQUAL -> System.out.println(optionType.getValue().toString() + " times incorrect answers between students are equal which is " + (int) (
                        optionType.getValue().doubleValue() / totalIncorrectOptionsCount.doubleValue() * 100) + "% of all incorrect options");
                case INCORRECT_AND_EQUAL_AND_RARE -> System.out.println(
                        optionType.getValue().toString() + " times incorrect answers between students are equal and this option is chosen less than 4 time by all other students");
            }
        });
        System.out.println();
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
                if (optionMap.get(OptionSimmiliaritiesTypes.CORRECT_AND_EQUAL) != null) {
                    optionMap.put(OptionSimmiliaritiesTypes.CORRECT_AND_EQUAL, optionMap.get(OptionSimmiliaritiesTypes.CORRECT_AND_EQUAL) + 1);
                } else {
                    optionMap.put(OptionSimmiliaritiesTypes.CORRECT_AND_EQUAL, 1);
                }
            }

            if (neighbourOptions.contains(option) && !correctOptions.contains(option)) {
                if (optionMap.get(OptionSimmiliaritiesTypes.INCORRECT_AND_EQUAL) != null) {
                    optionMap.put(OptionSimmiliaritiesTypes.INCORRECT_AND_EQUAL, optionMap.get(OptionSimmiliaritiesTypes.INCORRECT_AND_EQUAL) + 1);
                } else {
                    optionMap.put(OptionSimmiliaritiesTypes.INCORRECT_AND_EQUAL, 1);
                }
                Integer totalTimesChosen = totalIncorrectOccurrencesMap.get(option);
                if (totalTimesChosen < 5) {
                    if (optionMap.get(OptionSimmiliaritiesTypes.INCORRECT_AND_EQUAL_AND_RARE) != null) {
                        optionMap.put(OptionSimmiliaritiesTypes.INCORRECT_AND_EQUAL_AND_RARE, optionMap.get(OptionSimmiliaritiesTypes.INCORRECT_AND_EQUAL_AND_RARE) + 1);
                    } else {
                        optionMap.put(OptionSimmiliaritiesTypes.INCORRECT_AND_EQUAL_AND_RARE, 1);
                    }
                }
            }

            if (!neighbourOptions.contains(option) && correctOptions.contains(option)) {
                if (optionMap.get(OptionSimmiliaritiesTypes.CORRECT_AND_ABSENT) != null) {
                    optionMap.put(OptionSimmiliaritiesTypes.CORRECT_AND_ABSENT, optionMap.get(OptionSimmiliaritiesTypes.CORRECT_AND_ABSENT) + 1);
                } else {
                    optionMap.put(OptionSimmiliaritiesTypes.CORRECT_AND_ABSENT, 1);
                }
            }

            if (!neighbourOptions.contains(option) && !correctOptions.contains(option)) {
                if (optionMap.get(OptionSimmiliaritiesTypes.INCORRECT_AND_ABSENT) != null) {
                    optionMap.put(OptionSimmiliaritiesTypes.INCORRECT_AND_ABSENT, optionMap.get(OptionSimmiliaritiesTypes.INCORRECT_AND_ABSENT) + 1);
                } else {
                    optionMap.put(OptionSimmiliaritiesTypes.INCORRECT_AND_ABSENT, 1);
                }
            }
            if (optionMap.get(OptionSimmiliaritiesTypes.TOTAL_COUNT) != null) {
                optionMap.put(OptionSimmiliaritiesTypes.TOTAL_COUNT, optionMap.get(OptionSimmiliaritiesTypes.TOTAL_COUNT) + 1);
            } else {
                optionMap.put(OptionSimmiliaritiesTypes.TOTAL_COUNT, 1);
            }
        });
        return optionMap;
    }
}
