package com.example.springboot.common;

public class JudgeBedName {

    public static String getBedName(int num) {
        switch (num) {
            case 1:
                return "first_bed";
            case 2:
                return "second_bed";
            case 3:
                return "third_bed";
            case 4:
                return "fourth_bed";
            default:
                return null;
        }
    }
}
