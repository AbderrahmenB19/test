package com.user19.pfe_testing.util;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.expression.ExpressionParser;
import org.springframework.expression.spel.standard.SpelExpressionParser;
import org.springframework.expression.spel.support.StandardEvaluationContext;
import java.util.Map;

public class ConditionEvaluator {

    private static final ObjectMapper objectMapper = new ObjectMapper();
    private static final SpelExpressionParser parser = new SpelExpressionParser();

    public static boolean evaluateCondition(String formData, String condition) {
        try {
            // 1. Parse JSON to Map
            Map<String, Object> dataMap = objectMapper.readValue(
                    formData,
                    new TypeReference<Map<String, Object>>() {}
            );

            // 2. Create evaluation context and set variables
            StandardEvaluationContext context = new StandardEvaluationContext();
            dataMap.forEach(context::setVariable);

            // 3. Convert condition to use bracket notation
            String normalizedCondition = convertToBracketNotation(condition);

            System.out.println("Evaluating: " + normalizedCondition);
            System.out.println("With data: " + dataMap);

            // 4. Evaluate the condition
            Boolean result = parser.parseExpression(normalizedCondition)
                    .getValue(context, Boolean.class);

            return Boolean.TRUE.equals(result);

        } catch (JsonProcessingException e) {
            throw new RuntimeException("Failed to evaluate condition: " + condition, e);
        }
    }

    private static String convertToBracketNotation(String condition) {
        // Convert #age > 18 to #age > 18 (no change for direct variables)
        // Or convert age > 18 to #age > 18 if using # prefix
        return condition;

        // Alternative if the above doesn't work:
        // Convert dot notation to bracket notation
        // return condition.replaceAll("(\\w+)", "#['$1']");
    }
}