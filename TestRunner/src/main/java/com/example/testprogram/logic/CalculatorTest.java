package com.example.testprogram.logic;

import org.testng.annotations.DataProvider;
import org.testng.annotations.Test;
import java.util.HashMap; // <--- ДОБАВЛЕН ИМПОРТ
import java.util.Map;

import static org.testng.Assert.assertEquals;

public class CalculatorTest {

    @DataProvider(name = "expressionData")
    public Object[][] provideExpressionData() {
        return new Object[][] {
                {"-5 + 3 ", -2.0},
                {"10 - 2", 8.0},
                {"4 * 5", 20.0},
                {"20 / 4", 5.0},
                {"10 / 0", Double.POSITIVE_INFINITY},
                {"2 * 5", 15.0},  // негативный тест (намеренная ошибка в ожидаемом результате?)

                {"3 * 5 - 7", 8.0},
                {"10 + 2 * 3", 16.0},
                {"10 - 2 * 3", 4.0},
                {"8 / 2 + 3", 7.0},
                {"(5 + 3)", 8.0},
                {"(10 - 2) * 3", 24.0},
                {"2 * (3 + 4)", 14.0},
                {"(2 + 3) * (4 - 1)", 15.0},
                {"((2 + 3) * 4) / 2", 10.0},
                {"3 + 4 * 2 / (1 - 5)", 2.0},

                {"x=5;y=10;z=x+y", 15.0},
                {"a=2;b=3;c=a*b;d=c+5", 11.0},
                {"x=10;y=x*2;z=x+y", 30.0},
                {"x=5;y=3;x+y*2", 11.0},
                {"a=4;b=2;(a+b)*3", 20.0},

        };
    }

    @Test(dataProvider = "expressionData")
    public void testExpressions(String expression, double expected) {
        Map<String, Double> scalars = new HashMap<>();
        Map<String, double[]> arrays = new HashMap<>();
        Calculator calculator = new Calculator(scalars, arrays);
        double result = calculator.calculate(expression);
        assertEquals(result, expected, 0.001,
                "Ошибка в выражении: " + expression);
    }
}