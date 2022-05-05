package com.exam.validator;

import java.util.List;
import java.util.Map;
import java.util.logging.Logger;
import java.util.stream.Collectors;

import org.apache.commons.lang3.StringUtils;

class AnswersUtils {

    private AnswersUtils() {
    }

    private static final Logger logger = Logger.getLogger(AnswersUtils.class.getName());

    public static List<Student> filterSudentsAndAnswers(List<Student> students) {
        return students.stream().peek(AnswersUtils::removeCorrectAnswers).filter(it -> !it.getAnswers().isEmpty()).toList();
    }

    private static void removeCorrectAnswers(Student student) {
        student.setAnswers(student.getAnswers().entrySet().stream().filter(AnswersUtils::answerIsIncorrect).collect(Collectors.toMap(Map.Entry::getKey, Map.Entry::getValue)));
    }

    private static boolean answerIsIncorrect(Map.Entry<Integer, String> it) {
        return !StringUtils.equals(Main.getCorrectAnswers().get(it.getKey()), it.getValue());
    }

    public static void printPreface() {
        System.out.println("This is possible cheaters between students analyzer program");
        System.out.println("");
        System.out.println(" This analysis is based on these assumptions: ");
        System.out.println("a) student can copy all answers or answers he is not sure about from one neighbour person or from statistical average of all students he has availability to copy from.");
        System.out.println(
                "b) using wrong answer options analysis have far more possibility to find cheater then good answers since it statistically more rear so is possibility to same wrong answer for neighbour students.");
        System.out.println(
                " So there is made three type of reports in this program one is graphical representation of student auditorium and wrong option distribution for each wrong option as for times for each student ");
        System.out.println(
                "having the same wrong answer as one its neighbour, there is analyis of pairs of student having more than two answer options in common, and there is analysis of each student with similarities ");
        System.out.println("to most common answer of his neighbours analysis.");
        System.out.println();
        System.out.println();
    }

    public static void printConclusion(List<Student> students) {
        System.out.println("MY PERSONAL CONCLUSION:");
        System.out.println(
                "There is no clear cheaters which would have 100% identical answer to one of his neighbour or to statistical average. So we can find only possible cheaters with higher possibilities than others");
        System.out.println("From my personal observation based on highest probability possible cheaters is:");
        System.out.println();
        StudentUtils.printPersonalPreference(students);
        System.out.println();
    }
}
