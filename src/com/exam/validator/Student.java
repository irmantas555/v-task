package com.exam.validator;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

class Student {

    private String name;
    private String sittingLocation;
    private Map<Integer, String> answers = new HashMap<>();

    private List<Student> cheatingPossibilities = new ArrayList<>();

    Map<Integer, List<String>> neighboursAnswersMap = new HashMap<>();

    List<Student> identicalNeighbours = new ArrayList<>();

    Map<Integer, Set<Character>> commonNeighbourOptions = new HashMap<>();
    Map<Integer, Set<Character>> dominantNeighbourOptions = new HashMap<>();

    Map<String, Integer> neighboursWithSimmilarBadAnswersCountMap = new HashMap<>();

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

    Map<String, Integer> getNeighboursWithSimmilarBadAnswersCountMap() {
        return neighboursWithSimmilarBadAnswersCountMap;
    }

    void setNeighboursWithSimmilarBadAnswersCountMap(Map<String, Integer> neighboursWithSimmilarBadAnswersCountMap) {
        this.neighboursWithSimmilarBadAnswersCountMap = neighboursWithSimmilarBadAnswersCountMap;
    }

    @Override
    public String toString() {
        return "Student{" + "name='" + name + '\'' + ", sittingLocation='" + sittingLocation + '\'' + ", answers=" + answers + '}';
    }
}
