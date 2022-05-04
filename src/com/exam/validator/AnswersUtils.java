package com.exam.validator;

import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import org.apache.commons.lang3.StringUtils;

class AnswersUtils {

    AnswersUtils() {
    }

    public static List<Student> filterSudentsAndAnswers(List<Student> students) {
        return students.stream().peek(AnswersUtils::removeCorrectAnswers).filter(it -> !it.getAnswers().isEmpty()).toList();
    }

    private static void removeCorrectAnswers(Student student) {
        student.setAnswers(student.getAnswers().entrySet().stream().filter(AnswersUtils::answerIsIncorrect).collect(Collectors.toMap(Map.Entry::getKey, Map.Entry::getValue)));
    }

    private static boolean answerIsIncorrect(Map.Entry<Integer, String> it) {
        return !StringUtils.equals(Main.getCorrectAnswers().get(it.getKey()), it.getValue());
    }
}
