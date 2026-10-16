package com.example.mathmodule.ai;

/*
 * Role: AI/API class for Math, responsible for preparing requests or prompts, calling the service, and converting response data.
 * Purpose: Separating AI communication from the UI makes error handling, model or endpoint changes, and logic testing easier.
 */
public final class MathQuizParser {
    private MathQuizParser(){}
    public static String cleanJson(String response){
        if(response==null) return "[]";
        String text=response.replace("```json","").replace("```","").trim();
        int start=text.indexOf("[");
        int end=text.lastIndexOf("]");
        return start>=0 && end>start ? text.substring(start,end+1) : "[]";
    }
}
