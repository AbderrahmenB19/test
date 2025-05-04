package com.user19.pfe_testing.util;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.expression.ExpressionParser;
import org.springframework.expression.spel.standard.SpelExpressionParser;
import org.springframework.expression.spel.support.StandardEvaluationContext;
import org.springframework.stereotype.Component;

import java.sql.Time;
import java.time.LocalDateTime;
import java.util.Date;
import java.util.Map;

@Component
public class ConditionEvaluator {

    private final ExpressionParser parser = new SpelExpressionParser();
    private final ObjectMapper objectMapper = new ObjectMapper();


    public  boolean evaluateCondition(String jsonData, String condition) {

        System.out.println("DEBUG - Input jsonData: " + jsonData);  // First thing in the method
        System.out.println("DEBUG - Input condition: " + condition);

        if (jsonData == null) {
            System.out.println("DEBUG - Null jsonData detected! Stack trace:");
            new Exception().printStackTrace();  // Shows who called this with null
            throw new IllegalArgumentException("JSON data cannot be null");
        }

        try {
            Map<String, Object> dataMap = objectMapper.readValue(jsonData, Map.class);
            StandardEvaluationContext context = new StandardEvaluationContext();
            dataMap.forEach(context::setVariable);

            // Evaluate the condition
            Boolean result = parser.parseExpression(condition).getValue(context, Boolean.class);

            // Handle case where expression doesn't evaluate to a boolean
            if (result == null) {
                throw new RuntimeException("Condition did not evaluate to a boolean value");
            }

            return result;
        } catch (JsonProcessingException e) {
            throw new RuntimeException("Error parsing JSON data: " + e.getMessage(), e);
        } catch (Exception e) {
            throw new RuntimeException("Error evaluating condition: " + e.getMessage(), e);
        }
    }
    }

