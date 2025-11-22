package com.example.testprogram.service;

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
	private boolean isRunning = false;

	public void runTests() {
		if (isRunning) return;

		isRunning = true;
		executionLogs.clear();
		executionLogs.add("INFO: Запуск тестов...");

		new Thread(() -> {
			TestNG testng = new TestNG();

			testng.setTestClasses(new Class[] { CalculatorTest.class, SimpleParserTest.class });

			testng.addListener(new CustomTestListener());

			try {
				testng.run();
			} catch (Exception e) {
				executionLogs.add("ERROR: Ошибка при выполнении: " + e.getMessage());
			} finally {
				isRunning = false;
				executionLogs.add("INFO: Тестирование завершено.");
			}
		}).start();
	}

	public void stopTests() {
		if (isRunning) {
			executionLogs.add("WARNING: Поступила команда остановки. Ожидание завершения текущего теста...");
			isRunning = false;
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
			executionLogs.add("START: " + result.getName());
		}

		@Override
		public void onTestSuccess(ITestResult result) {
			executionLogs.add("PASSED: " + result.getName());
		}

		@Override
		public void onTestFailure(ITestResult result) {
			executionLogs.add("FAILED: " + result.getName() + " | Ошибка: " + result.getThrowable().getMessage());
		}

		@Override
		public void onStart(ITestContext context) {
			executionLogs.add("SUITE START: " + context.getName());
		}
	}
}
