package org.uma.jmetal.problem.multiobjective.ecole;

import org.uma.jmetal.problem.impl.AbstractIntegerProblem;
import org.uma.jmetal.solution.IntegerSolution;
import org.uma.jmetal.util.JMetalException;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class Ecole extends AbstractIntegerProblem {

    public Ecole() {
        this(10, 3);
        List<Integer> lowerLimit = new ArrayList<>(getNumberOfVariables()) ;
        List<Integer> upperLimit = new ArrayList<>(getNumberOfVariables()) ;

        // for job10, the only valid batchIntevals are 1, 2, 5, 10

        lowerLimit.add(0);
        lowerLimit.add(1);
        lowerLimit.add(1);
        lowerLimit.add(10);

        upperLimit.add(3);
        upperLimit.add(10);
        upperLimit.add(5);
        upperLimit.add(120);

        for (int i = 4; i <= 7; i++) {
            lowerLimit.add(0);
            upperLimit.add(2);
        }

        for (int i = 8; i <= 9; i++) {
            lowerLimit.add(0);
            upperLimit.add(1);
        }


        setLowerLimit(lowerLimit);
        setUpperLimit(upperLimit);

        int jobId = 14;
        String cachePath = "/Users/feisong/test_space/pareto/data/catch-2d/";
        latencyMap = (HashMap<String, Double>) deser(cachePath + "latency_" + jobId + ".dat");
        //String key = jobId+"_" + 1 + "_" + 1*100 + "_" + 1*18 + "_" + 10*10000 + "_" + 24 + "_" + 18 + "_" + 60 + "_" + 1024 + "_" + 0 + "_" + 1;
        //System.out.println(key);
        //System.out.println(latencyMap.get(key));

        throughputMap = (HashMap<String, Double>) deser(cachePath + "throughput_" + jobId + ".dat");

    }

    public Ecole(int numberOfVariables, int numberOfObjectives) throws JMetalException {
        setNumberOfVariables(numberOfVariables);
        setNumberOfObjectives(numberOfObjectives);
        setName("Ecole");
    }

    // a helper function from moo project, for reading the cached result
    //public static int[] lowerBounds = {10, 1, 1, 1};
    //public static int[] UpperBounds = {120, 10, 10, 5};
    private Map<String, Double> latencyMap;
    private Map<String, Double> throughputMap;
    public static Object deser(String path) {
        Object obj = null;
        File file = new File(path);
        FileInputStream in;
        try {
            in = new FileInputStream(file);
            ObjectInputStream objIn = new ObjectInputStream(in);
            obj = objIn.readObject();
            objIn.close();
            System.out.println("read object success!");
        } catch (IOException e) {
            System.out.println("read object failed");
            e.printStackTrace();
        } catch (ClassNotFoundException e) {
            e.printStackTrace();
        }
        return obj;
    }

    public void evaluate(IntegerSolution solution) {
        int jobId = 14;
        double latency;
        double throughput;

        int[] batchIntervals = {1, 2, 5, 10};
        int[] maxSizeInFlightValues = {24, 48, 96};
        int[] bypassMergeThresholdValues = {10, 18, 200};
        int[] memoryFractionValues = {40, 60, 80};
        int[] executorMemoryValues = {512, 1024, 6144};

/*        int batchInterval = solution.getVariableValue(0);
        if (batchInterval == 3)
            batchInterval = 5;
        if (batchInterval == 4)
            batchInterval = 10;*/

        String key = jobId+"_" + batchIntervals[solution.getVariableValue(0)] + "_" + solution.getVariableValue(1)*100 + "_" + solution.getVariableValue(2)*18 + "_" + solution.getVariableValue(3)*10000 + "_" + maxSizeInFlightValues[solution.getVariableValue(4)] + "_" + bypassMergeThresholdValues[solution.getVariableValue(5)] + "_" + memoryFractionValues[solution.getVariableValue(6)] + "_" + executorMemoryValues[solution.getVariableValue(7)] + "_" + solution.getVariableValue(8) + "_" + solution.getVariableValue(9);
        /*
        for (int i = 0; i < solution.getNumberOfVariables(); i++) {
            int value = solution.getVariableValue(i) ;
            key = key + value + "_" ;
        }
        */
        //key = key + 24 + "_" + 18 + "_" + 60 + "_" + 1024 + "_" + 0 + "_" + 1;
        System.out.println(key);
        latency = latencyMap.get(key);
        throughput = throughputMap.get(key);
        double cost = 12.5 * batchIntervals[solution.getVariableValue(0)] + 48 * solution.getVariableValue(2)*18;
        solution.setObjective(0, latency);
        solution.setObjective(1, -throughput);
        solution.setObjective(2, cost);
    }
}