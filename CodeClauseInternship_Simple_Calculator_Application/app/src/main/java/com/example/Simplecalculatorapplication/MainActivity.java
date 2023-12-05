package com.example.Simplecalculatorapplication;

import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.ImageButton;
import android.widget.TextView;
import androidx.appcompat.app.AppCompatActivity;


import net.objecthunter.exp4j.Expression;
import net.objecthunter.exp4j.ExpressionBuilder;
import net.objecthunter.exp4j.ValidationResult;
import java.math.BigDecimal;
import java.util.ArrayList;

public class MainActivity extends AppCompatActivity {

    private TextView currentInput;

    private TextView answerScreen;
    private ArrayList<String> realTimeScreenValue;
    private boolean shouldAddCloseParentheses;
    private int openParenthesesCount;
    private TextView completeOperation;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        // Find the ImageButton for Backspace
        ImageButton buttonBackspace = findViewById(R.id.button_backspace);

        currentInput = findViewById(R.id.current_input);
        answerScreen = findViewById(R.id.answer_screen);
        realTimeScreenValue = new ArrayList<>();
        shouldAddCloseParentheses = false;
        openParenthesesCount = 0;
        completeOperation = findViewById(R.id.complete_operation);

        // Set OnClickListener for Backspace ImageButton
        buttonBackspace.setOnClickListener(v -> {
            handleBackspace(); // Call the method to handle backspace functionality
        });
    }

    public void onButtonClick(View view) {
        Button button = (Button) view;
        String buttonText = button.getText().toString();

        switch (buttonText) {
            case "C":
                clearAll();
                break;
            case "=":
                evaluateExpression();
                currentInput.setText("");
                break;
            case "⌫": // Backspace button
                handleBackspace();
                break;
            case "(":
                onOpenParenthesesButtonClick(view);
                break;
            case ")":
                onCloseParenthesesButtonClick(view);
                break;
            case "+":
            case "-":
            case "x":
            case "/":
                handleOperator(buttonText);
                break;
            case "%":
                handlePercentage();
                break;
            case "+/-":
                handleSignChange();
                break;
            case ".":
                handleDecimal();
                break;
            case "0":
            case "1":
            case "2":
            case "3":
            case "4":
            case "5":
            case "6":
            case "7":
            case "8":
            case "9":
                handleNumber(buttonText);
                break;
            default:
                // For other buttons or unknown input
                break;
        }
    }

    private void clearAll() {
        realTimeScreenValue.clear();
        updateDisplay();
        answerScreen.setText(getString(R.string.answer)); // Reset answer screen
        if (completeOperation != null) { // Ensure the TextView reference is not null
            completeOperation.setText(""); // Clear the completeOperation TextView
        }
    }

    public void onOpenParenthesesButtonClick(View view) {
        if (realTimeScreenValue.isEmpty() ||
                shouldAddCloseParentheses ||
                realTimeScreenValue.get(realTimeScreenValue.size() - 1).matches("[+\\-x/%]")) {
            realTimeScreenValue.add("(");
            updateDisplay();
        } else {
            realTimeScreenValue.add("*("); // Add a multiplication operator before the open parenthesis
            updateDisplay();
        }
        openParenthesesCount++;
        shouldAddCloseParentheses = false;
    }

    public void onCloseParenthesesButtonClick(View view) {
        if (openParenthesesCount > 0) {
            realTimeScreenValue.add(")");
            updateDisplay();
            openParenthesesCount--;
            shouldAddCloseParentheses = true;
        }  // Do nothing or display a message indicating an error

    }

    private void handleNumber(String number) {
        realTimeScreenValue.add(number);
        updateDisplay();
    }

    private void handleOperator(String operator) {
        if (realTimeScreenValue.size() > 0) {
            String lastChar = realTimeScreenValue.get(realTimeScreenValue.size() - 1);
            // Check if the last character is not an operator
            if (!lastChar.equals("+") && !lastChar.equals("-") &&
                    !lastChar.equals("x") && !lastChar.equals("/") && !lastChar.equals("%")) {
                if (operator.equals("x")) {
                    realTimeScreenValue.add("*");
                } else {
                    realTimeScreenValue.add(operator);
                }
                updateDisplay();
            }
        }
    }

    private void updateDisplay() {
        StringBuilder inputBuilder = new StringBuilder();
        for (String s : realTimeScreenValue) {
            inputBuilder.append(s);
        }
        currentInput.setText(inputBuilder.toString());
    }

    private void handleBackspace() {
        if (realTimeScreenValue.size() > 0) {
            realTimeScreenValue.remove(realTimeScreenValue.size() - 1);
            updateDisplay();
        }
    }

    private void evaluateExpression() {
        if (!realTimeScreenValue.isEmpty()) {
            StringBuilder expression = new StringBuilder();
            for (String element : realTimeScreenValue) {
                expression.append(element);
            }

            try {
                String currentExpression = expression.toString();
                double result;

                // Check for unbalanced parentheses in the expression
                int openParenthesesCount = currentExpression.length() - currentExpression.replace("(", "").length();
                int closeParenthesesCount = currentExpression.length() - currentExpression.replace(")", "").length();

                if (openParenthesesCount != closeParenthesesCount) {
                    answerScreen.setText(getString(R.string.error_unbalanced_parentheses));
                    return;
                }

                // Evaluate the expression
                ExpressionBuilder builder = new ExpressionBuilder(currentExpression);
                Expression e = builder.build();
                ValidationResult validationResult = e.validate();

                if (validationResult.isValid()) {
                    result = e.evaluate();
                } else {
                    answerScreen.setText(getString(R.string.error_invalid_expression));
                    return;
                }

                BigDecimal decimalResult = BigDecimal.valueOf(result);
                String resultString = decimalResult.stripTrailingZeros().toPlainString();

                answerScreen.setText(resultString);



                // Set the evaluated expression in the complete_operation TextView
                TextView completeOperation = findViewById(R.id.complete_operation);
                completeOperation.setText(currentExpression); // Set the currentExpression

                realTimeScreenValue.clear();
                realTimeScreenValue.add(resultString);
                updateDisplay();

            } catch (Exception ex) {
                answerScreen.setText(getString(R.string.error_exception_occurred));
                ex.printStackTrace();
            }
        }
    }

    private void handlePercentage() {
        if (!realTimeScreenValue.isEmpty() && !isLastCharacterOperator()) {
            StringBuilder lastNumber = new StringBuilder();
            int lastIndex = realTimeScreenValue.size() - 1;

            // Find the last number in the expression
            for (int i = lastIndex; i >= 0; i--) {
                String character = realTimeScreenValue.get(i);
                if (character.equals("+") || character.equals("-") ||
                        character.equals("x") || character.equals("/") || character.equals("%")) {
                    break;
                }
                lastNumber.insert(0, character);
            }

            // Calculate the percentage value
            if (lastNumber.length() > 0) {
                try {
                    double value = Double.parseDouble(lastNumber.toString());
                    value /= 100; // Convert to percentage
                    realTimeScreenValue.subList(lastIndex - lastNumber.length() + 1, lastIndex + 1).clear();
                    realTimeScreenValue.add(Double.toString(value));
                    updateDisplay();
                } catch (NumberFormatException e) {
                    e.printStackTrace();
                    answerScreen.setText(getString(R.string.error_invalid_number));
                }
            }
        }
    }
    private void handleDecimal() {
        // Check if there are no elements or the last element is not an operator
        if (realTimeScreenValue.isEmpty() || !isLastCharacterOperator()) {
            // Check if the last number in the expression already contains a decimal point
            if (!lastNumberContainsDecimal()) {
                realTimeScreenValue.add(".");
                updateDisplay();
            }
        }
    }

    private boolean isLastCharacterOperator() {
        if (!realTimeScreenValue.isEmpty()) {
            String lastChar = realTimeScreenValue.get(realTimeScreenValue.size() - 1);
            return lastChar.equals("+") || lastChar.equals("-") ||
                    lastChar.equals("x") || lastChar.equals("/") || lastChar.equals("%");
        }
        return false;
    }

    private boolean lastNumberContainsDecimal() {
        StringBuilder lastNumber = new StringBuilder();
        for (int i = realTimeScreenValue.size() - 1; i >= 0; i--) {
            String character = realTimeScreenValue.get(i);
            if (character.equals("+") || character.equals("-") ||
                    character.equals("x") || character.equals("/") || character.equals("%")) {
                break;
            }
            lastNumber.insert(0, character);
        }
        return lastNumber.toString().contains(".");
    }


    private void handleSignChange() {
        if (!realTimeScreenValue.isEmpty() && !isLastCharacterOperator()) {
            StringBuilder lastNumber = new StringBuilder();
            int lastIndex = realTimeScreenValue.size() - 1;

            // Find the last number in the expression
            for (int i = lastIndex; i >= 0; i--) {
                String character = realTimeScreenValue.get(i);
                if (character.equals("+") || character.equals("-") ||
                        character.equals("x") || character.equals("/") || character.equals("%")) {
                    break;
                }
                lastNumber.insert(0, character);
            }

            // Check if the last number is negative
            if (lastNumber.toString().startsWith("-")) {
                // Make the number positive by removing the negative sign
                realTimeScreenValue.subList(lastIndex - lastNumber.length() + 1, lastIndex + 1).clear();
            } else {
                // Make the number negative by adding a negative sign
                realTimeScreenValue.add("-");
            }
            updateDisplay();
        }
    }

}




