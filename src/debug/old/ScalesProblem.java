package old.scales;

import java.util.ArrayList;
import java.util.List;
import java.util.Random;

public class ScalesProblem {
    private List<String> solutions;

    public String runScales(List<Double> weights, int iterations) {

        Algorithm<Solution> algorithm = new SimulatedAnnealingAlgorithm();
        Solution solution = algorithm
                .run(weights, iterations);
        solutions = algorithm.getSolutions();
        return solution
                .getSolution();
    }
}

interface Solution {
    double calculateFitness(List<Double> weights);

    void makeSmallChange();

    String getSolution();

    Solution copy();
}

class ScalesSolution implements Solution {
    private static final UniformRandomGenerator randomGenerator = new UniformRandomGenerator();
    private String solution;

    public ScalesSolution(int length) {
        this(generateRandomBinaryString(length));
    }

    private ScalesSolution(String solution) {
        this.solution = solution;
    }

    @Override
    public double calculateFitness(List<Double> weights) {
        if (solution.length() > weights.size()) return (-1);
        double leftHandSide = 0.0;
        double rightHandSide = 0.0;
        int n = solution.length();

        for (int i = 0; i < n; i++) {
            if (solution.charAt(i) == '0') {
                leftHandSide += weights.get(i);
            } else
                rightHandSide += weights.get(i);
        }

        return (Math.abs(leftHandSide - rightHandSide));
    }

    @Override
    public void makeSmallChange() {
        int length = solution.length();

        int randomInt = randomGenerator.generateInteger(0, length - 1);
        char targetChar = solution.charAt(randomInt);
        StringBuilder strBuilder = new StringBuilder(solution);

        if (targetChar == '1') {
            strBuilder.replace(randomInt, randomInt + 1, "0");
        } else {
            strBuilder.replace(randomInt, randomInt + 1, "1");
        }

        solution = strBuilder.toString();
    }

    @Override
    public String getSolution() {
        return solution;
    }

    @Override
    public ScalesSolution copy() {
        return new ScalesSolution(this.solution);
    }

    private static String generateRandomBinaryString(int n) {
        String s = "";
        for (int i = 0; i < n; i++) {
            String randomChar = String.valueOf(randomGenerator.generateInteger(0, 1));
            if (randomChar.equals("0")) {
                s = s.concat(randomChar);
            } else {
                s = randomChar.concat(s);
            }
        }

        return (s);
    }
}

class UniformRandomGenerator {
    private final Random random;

    public UniformRandomGenerator() {
        random = new Random();
        random.setSeed(System.nanoTime());
    }

    public int generateInteger(int lower, int upper) {
        int bound = upper - lower + 1;
        return (random.nextInt(bound) + lower);
    }

    public double generateDouble(double lower, double upper) {
        return ((upper - lower) * random.nextDouble() + lower);
    }
}

interface Algorithm<T> {
    T run(List<Double> weights, int iterations);

    List<String> getSolutions();
}

class SimulatedAnnealingAlgorithm implements Algorithm<Solution> {

    private final UniformRandomGenerator randomGenerator = new UniformRandomGenerator();
    private final List<String> solutions = new ArrayList<>();

    @Override
    public Solution run(List<Double> weights, int iterations) {
        System.out.println("Running SA");

        double temperature = 1000;
        double coolingRate = calcCR(temperature, iterations);

        Solution finalSolution = new ScalesSolution(weights.size());

        for (int i = 0; i < iterations; i++) {
            finalSolution = calculateNewSolution(weights, temperature, finalSolution);
            temperature = coolingRate * temperature;
            solutions.add(finalSolution.getSolution());
        }


        return finalSolution;
    }

    @Override
    public List<String> getSolutions() {
        return solutions;
    }

    private Solution calculateNewSolution(List<Double> weights, double temperature, Solution finalSolution) {
        Solution temporarySolution = finalSolution.copy();
        temporarySolution.makeSmallChange();
        double temporarySolutionFitness = temporarySolution.calculateFitness(weights);

        double finalSolutionFitness = finalSolution.calculateFitness(weights);

        if (temporarySolutionFitness > finalSolutionFitness) {
            double changeProbability = acceptanceFunction(temporarySolutionFitness, finalSolutionFitness, temperature);

            if (changeProbability > randomGenerator.generateDouble(0, 1)) {
                finalSolution = temporarySolution.copy();
            }

        } else {
            finalSolution = temporarySolution.copy();
        }

        return finalSolution;
    }

    private double acceptanceFunction(double f2, double f1, double temp) {
        double delta = Math.abs(f1 - f2);
        delta = -1 * delta;
        return Math.exp(delta / temp);
    }

    private double calcCR(double temperature, int nIterations) {
        double tIter = 0.001;
        double power = 1.0 / nIterations;
        double tValue = tIter / temperature;

        return Math.pow(tValue, power);
    }

}

class RandomHillClimbingAlgorithm implements Algorithm<Solution> {

    private final List<String> solutions = new ArrayList<>();

    @Override
    public Solution run(List<Double> weights, int iterations) {
        System.out.println("Running RMHC");

        Solution currentSolution = new ScalesSolution(weights.size());

        for (int i = 0; i < iterations; i++) {
            Solution newSolution = currentSolution.copy();
            newSolution.makeSmallChange();

            double newFitness = newSolution.calculateFitness(weights);
            double currentFitness = currentSolution.calculateFitness(weights);

            if (newFitness < currentFitness) {
                currentSolution = newSolution.copy();
            }

            if (newFitness == 0) {
                currentSolution = newSolution.copy();
                break;
            }

            solutions.add(currentSolution.getSolution());

        }

        return currentSolution;
    }

    @Override
    public List<String> getSolutions() {
        return solutions;
    }

}