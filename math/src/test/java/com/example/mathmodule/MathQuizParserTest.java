package com.example.mathmodule;

import com.example.mathmodule.ai.MathQuizParser;

import org.junit.Test;

import static org.junit.Assert.assertEquals;

/*
 * Role: Test class for Math, used to verify key rules and behaviors after code changes.
 * Purpose: These tests verify key behaviors automatically instead of relying only on manual UI testing.
 */
public class MathQuizParserTest {
    @Test
    public void extractsJsonArrayFromGeminiMarkdownResponse() {
        String response = "Lời dẫn\n```json\n[{\"question\":\"2 + 2 = ?\"}]\n```\nKết thúc";
        assertEquals("[{\"question\":\"2 + 2 = ?\"}]",
                MathQuizParser.cleanJson(response));
    }

    /*
     * Responsibility: Performs the `invalid or missing response returns empty array` responsibility of `MathQuizParserTest` and returns or updates the result for the caller.
     * Purpose: A focused method improves readability, reuse, and step-by-step explanation.
     */
    @Test
    public void invalidOrMissingResponseReturnsEmptyArray() {
        assertEquals("[]", MathQuizParser.cleanJson(null));
        assertEquals("[]", MathQuizParser.cleanJson("Không có dữ liệu JSON"));
        assertEquals("[]", MathQuizParser.cleanJson("[thiếu dấu đóng"));
    }
}
