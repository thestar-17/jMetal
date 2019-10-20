package org.uma.jmetal.problem.multiobjective.ecole;

import org.uma.jmetal.problem.impl.AbstractIntegerProblem;
import org.uma.jmetal.solution.IntegerSolution;
import org.uma.jmetal.util.JMetalException;
import org.uma.jmetal.util.SocClient;

import java.util.ArrayList;
import java.util.List;

public class Erc27 extends AbstractIntegerProblem {

    SocClient socClient;

    public Erc27() {
        // 10 knobs, 3 objectives
        //this(10, 3);
        this(12, 2);

        // for all knobs, we need to specify the lower and upper bound. It has to be continous integer range
        List<Integer> lowerLimit = new ArrayList<>(getNumberOfVariables()) ;
        List<Integer> upperLimit = new ArrayList<>(getNumberOfVariables()) ;

        // for job10, the only valid batchIntevals are 1, 2, 5, 10

        lowerLimit.add(8);
        lowerLimit.add(2);
        lowerLimit.add(2);
        lowerLimit.add(4);
        lowerLimit.add(12);
        lowerLimit.add(7);
        lowerLimit.add(0);
        lowerLimit.add(50);
        lowerLimit.add(1000);
        lowerLimit.add(32);
        lowerLimit.add(10);
        lowerLimit.add(8);

        upperLimit.add(216);
        upperLimit.add(36);
        upperLimit.add(4);
        upperLimit.add(8);
        upperLimit.add(480);
        upperLimit.add(217);
        upperLimit.add(1);
        upperLimit.add(75);
        upperLimit.add(100000);
        upperLimit.add(512);
        upperLimit.add(500);
        upperLimit.add(2001);


        setLowerLimit(lowerLimit);
        setUpperLimit(upperLimit);

        // name the jobId
        //int jobId = 14;

        //TODO parameterize the arguments later
        socClient = new SocClient("EAClient", "localhost", 5565);
    }

    public Erc27(int numberOfVariables, int numberOfObjectives) throws JMetalException {
        setNumberOfVariables(numberOfVariables);
        setNumberOfObjectives(numberOfObjectives);
        setName("Erc27");
    }


    public void evaluate(IntegerSolution solution) {
        String jobId = "27-7";

        int numberOfVariables = getNumberOfVariables();
        double[] x = new double[numberOfVariables] ;

        for (int i = 0; i < numberOfVariables; i++) {
            x[i] = solution.getVariableValue(i) ;
        }

        StringBuilder config1 = new StringBuilder("JobID" + ":" + jobId + ";" + "Objective:latency;" + "k1:" + x[0] + ";k2:" + x[1] + ";k3:" + x[2] + ";k4:" + x[3] + ";k5:" + x[4] + ";k6:" + x[5] + ";k7:" + x[6] + ";k8:" + x[7] + ";s1:" + x[8] + ";s2:" + x[9] + ";s3:" + x[10] + ";s4:" + x[11]);
        StringBuilder config2 = new StringBuilder("JobID" + ":" + jobId + ";" + "Objective:cores;" + "k1:" + x[0] + ";k2:" + x[1] + ";k3:" + x[2] + ";k4:" + x[3] + ";k5:" + x[4] + ";k6:" + x[5] + ";k7:" + x[6] + ";k8:" + x[7] + ";s1:" + x[8] + ";s2:" + x[9] + ";s3:" + x[10] + ";s4:" + x[11]);

        String configLatency = config1.toString();
        String configCores = config2.toString();

        //System.out.println(configLatency);
        
        socClient.putMessage("JConfig", configLatency);
        String predictAnswer = socClient.getMessage();
        String predictAnsTopic = socClient.parseTopic(predictAnswer); // it should be PyPred
        String predictAnsMessage = socClient.parseMessage(predictAnswer);
        double targetLatency = Double.parseDouble(predictAnsMessage);
        solution.setObjective(0, targetLatency);
        
        socClient.putMessage("JConfig", configCores);
        predictAnswer = socClient.getMessage();
        predictAnsTopic = socClient.parseTopic(predictAnswer); // it should be PyPred
        predictAnsMessage = socClient.parseMessage(predictAnswer);
        double targetCores = Double.parseDouble(predictAnsMessage);
        solution.setObjective(1, targetCores);

        //solution.setObjective(1, solution.getVariableValue(1) * solution.getVariableValue(2));

    }
}
