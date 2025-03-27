package com.itsallbinary.tutorial.ai.spring_ai_app.common;

public class CommonHelper {

    public static final String URL_PREFIX_FOR_EXPERIMENTS = "/ai/exp/";

    public static final String URL_PREFIX_FOR_SPRING = "/ai/spring/";

    public static final String URL_PREFIX_FOR_LANGCHAIN4J = "/ai/langchain4j/";
    public static final String OUTPUT_NEW_LINE = "\n\n<br><br>";
    public static final String HORIZONTAL_LINE = " --------------------------------------------------------- ";

    public static String surroundMessage(Class aClass, String input, String output){
        return surroundMessageSection( " TUTORIAL " , aClass.getSimpleName() )+
                surroundMessageSection( " INPUT " , input )+
                surroundMessageSection( " OUTPUT " , output );
    }

    public static String surroundMessageSection(String title, String sectionData){
        return   HORIZONTAL_LINE + title + HORIZONTAL_LINE
                + OUTPUT_NEW_LINE
                + "<span style=\"white-space: pre-line\">"+sectionData+"</span>"
                + OUTPUT_NEW_LINE;
    }
}
