package com.dercio.algonated_code_runner.runner;

import com.dercio.algonated_code_runner.runner.calculator.Calculator;
import com.dercio.algonated_code_runner.verifier.IllegalMethodVerifier;
import com.dercio.algonated_code_runner.verifier.ImportVerifier;
import com.dercio.algonated_code_runner.verifier.Verifier;
import com.google.common.base.Stopwatch;
import org.joor.Reflect;
import org.joor.ReflectException;

import java.util.Collections;
import java.util.List;
import java.util.Optional;
import java.util.concurrent.TimeUnit;

public class CodeRunner {

    private static final String PLEASE_REMOVE = "Please remove the following ";

    private String errorMessage;
    private boolean isSuccess;
    private Reflect compiledClass;
    private final CodeOptions options;
    private final CodeRunnerSummary runnerSummary;

    public CodeRunner(CodeOptions options) {
        this.options = options;
        this.isSuccess = false;
        this.errorMessage = "It seems compile was never called!";
        this.runnerSummary = new CodeRunnerSummary();
    }

    public boolean compile() {
        if (verifyError(new ImportVerifier(), "imports:\n")) {
            return false;
        }

        if (verifyError(new IllegalMethodVerifier(options.getIllegalMethods()), "illegal methods:\n")) {
            return false;
        }

        try {
            compiledClass = compileClass();
            isSuccess = true;
            errorMessage = "";
        } catch (ReflectException reflectException) {
            handleError(reflectException.getMessage());
            return false;
        }

        return true;
    }

    public List<Integer> execute() {

        if (!isSuccess) {
            return Collections.emptyList();
        }

        Object data = Optional.ofNullable(options.getModifier())
                .map(function -> function.apply(options.getData()))
                .orElse(options.getData());

        try {
            Stopwatch timer = Stopwatch.createStarted();
            final List<Integer> valueToReturn = compiledClass.call(options.getMethodToCall(), data, options.getIterations())
                    .get();
            timer.stop();
            runnerSummary.setTimeRun(timer.elapsed(TimeUnit.MILLISECONDS));
            runnerSummary.setIterations(options.getIterations());
            isSuccess = true;
            errorMessage = "Compile and Run was a success!";
            return valueToReturn;
        } catch (ReflectException reflectException) {
            handleError(reflectException.getMessage());
            return Collections.emptyList();
        }
    }

    public <T> CodeRunnerSummary getSummary(
            Calculator<T> fitnessCalculator,
            Calculator<T> efficiencyCalculator,
            T data, List<Integer> solution) {
        runnerSummary.setFitness(fitnessCalculator.calculate(data, solution));
        runnerSummary.setEfficacy(efficiencyCalculator.calculate(data, solution));
        return runnerSummary;
    }

    public String getErrorMessage() {
        return errorMessage;
    }

    public boolean isSuccess() {
        return isSuccess;
    }

    private void handleError(String errorMessage) {
        this.errorMessage = errorMessage;
        isSuccess = false;
    }

    private boolean verifyError(Verifier verifier, String errorMessage) {
        final List<String> errors = verifier.verify(options.getCode());
        if (!errors.isEmpty()) {
            handleError(PLEASE_REMOVE + errorMessage + String.join("\n", errors));
            return true;
        }
        return false;
    }

    private Reflect compileClass() {
        String packageName = "package " + options.getPackageName() + ";";
        String className = options.getPackageName() + "." + options.getClassName();
        return Reflect.compile(className, packageName +
                "\n" +
                String.join("\n", options.getImportsAllowed()) +
                "\n" +
                options.getCode()
        ).create();
    }
}
