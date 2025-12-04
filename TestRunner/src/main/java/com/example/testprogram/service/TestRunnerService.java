package com.example.testprogram.service;

import com.example.testprogram.logic.Calculator;
import com.example.testprogram.logic.CalculatorTest;
import com.example.testprogram.logic.SimpleParserTest;
import org.springframework.stereotype.Service;
import org.testng.TestNG;
import org.testng.ITestListener;
import org.testng.ITestContext;
import org.testng.ITestResult;

import java.io.FileWriter;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.CopyOnWriteArrayList;

@Service
public class TestRunnerService {

	private final List<String> executionLogs = new CopyOnWriteArrayList<>();
	private volatile boolean isRunning = false;
	private volatile boolean isPaused = false;

	private final Calculator calculator = new Calculator();

	public void runTests() {
		if (isRunning) return;

		isRunning = true;
		isPaused = false;
		executionLogs.clear();
		executionLogs.add("INFO: Инициализация тестового прогона...");

		new Thread(() -> {
			TestNG testng = new TestNG();
			testng.setTestClasses(new Class[] {
					CalculatorTest.class,
					SimpleParserTest.class,
			});

			testng.addListener(new CustomTestListener());

			try {
				testng.run();
			} catch (Exception e) {
				executionLogs.add("ERROR: Критическая ошибка: " + e.getMessage());
			} finally {
				isRunning = false;
				executionLogs.add("INFO: Все тесты завершены.");
			}
		}).start();
	}

	public void stopTests() {
		if (isRunning) {
			executionLogs.add("WARNING: !!! ПРИНУДИТЕЛЬНАЯ ОСТАНОВКА ПОЛЬЗОВАТЕЛЕМ !!!");
			isRunning = false;
		}
	}

	public void togglePause() {
		isPaused = !isPaused;
		executionLogs.add(isPaused ? "INFO: Пауза..." : "INFO: Продолжаем...");
	}

	public String calculateManually(String expression) {
		try {
			double result = calculator.calculate(expression);
			return "Результат: " + result;
		} catch (Exception e) {
			return "Ошибка: " + e.getMessage();
		}
	}

	public List<String> getLogs() {
		return new ArrayList<>(executionLogs);
	}

	public void saveResults(String filename) throws IOException {
		try (FileWriter writer = new FileWriter(filename + ".txt")) {
			for (String log : executionLogs) {
				writer.write(log + "\n");
			}
		}
	}

	private class CustomTestListener implements ITestListener {
		@Override
		public void onTestStart(ITestResult result) {
			if (!isRunning) {
				throw new RuntimeException("Stop requested");
			}

			try {
				while (isPaused) {
					Thread.sleep(500);
				}
				Thread.sleep(800);
			} catch (InterruptedException e) {
				Thread.currentThread().interrupt();
			}

			executionLogs.add("START: " + result.getName());
		}

		@Override
		public void onTestSuccess(ITestResult result) {
			executionLogs.add("PASSED: " + result.getName());
		}

		@Override
		public void onTestFailure(ITestResult result) {
			executionLogs.add("FAILED: " + result.getName() + " | " + result.getThrowable().getMessage());
		}
	}
}