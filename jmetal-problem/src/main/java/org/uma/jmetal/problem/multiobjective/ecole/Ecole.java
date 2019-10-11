package org.uma.jmetal.problem.multiobjective.ecole;

import org.uma.jmetal.problem.impl.AbstractIntegerProblem;
import org.uma.jmetal.solution.IntegerSolution;
import org.uma.jmetal.util.JMetalException;

import org.uma.jmetal.util.ZMQClient;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class Ecole extends AbstractIntegerProblem {
	
	ZMQClient zClient;

    public Ecole() {
        // 10 knobs, 3 objectives
        //this(10, 3);
        this(10, 2);

        // for all knobs, we need to specify the lower and upper bound. It has to be continous integer range
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

        // name the jobId
        int jobId = 14;


        // read the prediction from cache; later may need ask directly from model
        String cachePath = "/mnt/disk8/fei/sigmod2019/data/catch-2d/";
        latencyMap = (HashMap<String, Double>) deser(cachePath + "latency_" + jobId + ".dat");
        throughputMap = (HashMap<String, Double>) deser(cachePath + "throughput_" + jobId + ".dat");
        
        //TODO parameterize the arguments later
        zClient = new ZMQClient("EAClient", "localhost", 5555);
    }

    public Ecole(int numberOfVariables, int numberOfObjectives) throws JMetalException {
        setNumberOfVariables(numberOfVariables);
        setNumberOfObjectives(numberOfObjectives);
        setName("Ecole");
    }

    // a helper function from moo project, for reading the cached result
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

        // since we only take continous chunk, we construct a mapping from a enumerated set to a continous chunk
        int[] batchIntervals = {1, 2, 5, 10};
        int[] maxSizeInFlightValues = {24, 48, 96};
        int[] bypassMergeThresholdValues = {10, 18, 200};
        int[] memoryFractionValues = {40, 60, 80};
        int[] executorMemoryValues = {512, 1024, 6144};

/*      
        // construct a concrete configuration to retrive the prediction (latency or throughput)
        String key = jobId+"_" + batchIntervals[solution.getVariableValue(0)] + "_" + solution.getVariableValue(1)*100 + "_" + solution.getVariableValue(2)*18 + "_" + solution.getVariableValue(3)*10000 + "_" + maxSizeInFlightValues[solution.getVariableValue(4)] + "_" + bypassMergeThresholdValues[solution.getVariableValue(5)] + "_" + memoryFractionValues[solution.getVariableValue(6)] + "_" + executorMemoryValues[solution.getVariableValue(7)] + "_" + solution.getVariableValue(8) + "_" + solution.getVariableValue(9);
        //System.out.println(key);
        latency = latencyMap.get(key);
        throughput = throughputMap.get(key);
        //double cost = 12.5 * batchIntervals[solution.getVariableValue(0)] + 48 * solution.getVariableValue(2)*18;
        solution.setObjective(0, latency);
        solution.setObjective(1, -throughput);
        //solution.setObjective(2, cost);
*/        
        
        StringBuilder config = new StringBuilder("JobID" + ":" + jobId + ";");
        
        String configLatency = (config.append(";Objective:Latency")).toString();
        String configThruput = (config.append(";Objective:Throughput")).toString();
        
        zClient.putMessage("JConfig", configLatency);
        String predictAnswer = zClient.getMessage();
        String predictAnsTopic = zClient.parseTopic(predictAnswer); // it should be PyPred
        String predictAnsMessage = zClient.parseMessage(predictAnswer);
        double targetLatency = Double.parseDouble(predictAnsMessage);
        solution.setObjective(0, targetLatency);
        
        zClient.putMessage("JConfig", configThruput);
        predictAnswer = zClient.getMessage();
        predictAnsTopic = zClient.parseTopic(predictAnswer); // it should be PyPred
        predictAnsMessage = zClient.parseMessage(predictAnswer);
        double targetThruput = Double.parseDouble(predictAnsMessage);
        solution.setObjective(1, targetThruput);
        
            
    }
}
