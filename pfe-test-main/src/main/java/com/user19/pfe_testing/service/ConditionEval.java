package com.user19.pfe_testing.service;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.expression.ExpressionParser;
import org.springframework.expression.spel.standard.SpelExpressionParser;
import org.springframework.expression.spel.support.StandardEvaluationContext;
import org.springframework.stereotype.Component;

import java.util.Map;

@Component
public class ConditionEval {

    private final ExpressionParser parser = new SpelExpressionParser();
    private final ObjectMapper objectMapper = new ObjectMapper();

    public  boolean evaluateCondition(String jsonData, String condition) {
        try {
            // Convert JSON to Map
            Map<String, Object> dataMap = objectMapper.readValue(jsonData, Map.class);

            // Create Evaluation Context
            StandardEvaluationContext context = new StandardEvaluationContext();
            dataMap.forEach(context::setVariable); // Set variables dynamically

            // Evaluate Condition
            System.out.println(context);
            System.out.println(condition);
            System.out.println(parser.parseExpression(condition).getValue(context, Boolean.class));
            return parser.parseExpression(condition).getValue(context, Boolean.class);
        } catch (JsonProcessingException e) {
            throw new RuntimeException("Error parsing JSON data", e);
        }
    }
}
