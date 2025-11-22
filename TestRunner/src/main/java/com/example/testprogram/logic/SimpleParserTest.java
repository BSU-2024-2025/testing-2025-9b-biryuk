package com.example.testprogram.logic;

import org.testng.annotations.DataProvider;
import org.testng.annotations.Test;
import static org.testng.Assert.assertTrue;
import static org.testng.Assert.assertFalse;

public class SimpleParserTest {

    @DataProvider(name = "expressionProvider")
    public Object[][] expressionProvider() {
        return new Object[][]{
                // позитивные тесты
                {"1", true},
                {"1+2", true},
                {"1+2+3", true},
                {"1 //", true},
                {"// только комментарий", true},

                // негативные тесты
                {"+", true},
                {"1+", true},
                {"1+2+", true}

        };
    }

    @Test(dataProvider = "expressionProvider")
    public void testExpressions(String expression, boolean expectedResult) {
        SimpleParser parser = new SimpleParser(expression);
        boolean result = parser.parseExpression();

        if (expectedResult) {
            assertTrue(result, "Expression '" + expression + "' should be valid");
        } else {
            assertFalse(result, "Expression '" + expression + "' should be invalid");
        }
    }
}
