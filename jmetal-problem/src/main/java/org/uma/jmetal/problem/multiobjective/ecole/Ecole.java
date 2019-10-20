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

        lowerLimit.add(1);
        lowerLimit.add(100);
        lowerLimit.add(18);
        lowerLimit.add(100000);

        upperLimit.add(10);
        upperLimit.add(1000);
        upperLimit.add(90);
        upperLimit.add(1200000);

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

        
        //TODO parameterize the arguments later
        zClient = new ZMQClient("EAClient", "localhost", 5550);
    }

    public Ecole(int numberOfVariables, int numberOfObjectives) throws JMetalException {
        setNumberOfVariables(numberOfVariables);
        setNumberOfObjectives(numberOfObjectives);
        setName("Ecole");
    }


    public static boolean constraintSlideWindowAndLatency(int jobId, double batchInterval, double latency) {
        if (jobId >= 10 && jobId <= 15)
            return latency < 10 * 1000;
        if (jobId >= 16 && jobId <= 21)
            return latency < 10 * 1000;
        if (jobId >= 22 && jobId <= 23)
            return latency < 10 * 1000;
        if (jobId >= 24 && jobId <= 25)
            return latency < 10 * 1000;
        if (jobId >= 26 && jobId <= 31)
            return latency < batchInterval * 1000;
        if (jobId >= 32 && jobId <= 37)
            return latency < 10 * 1000;
        if (jobId >= 38 && jobId <= 43)
            return latency < 10 * 1000;
        if (jobId >= 44 && jobId <= 49)
            return latency < 10 * 1000;
        if (jobId >= 50 && jobId <= 55)
            return latency < 10 * 1000;
        if (jobId >= 56 && jobId <= 67)
            return latency < batchInterval * 1000;
        else {
            System.out.println("尚未涵盖该job");
            System.exit(-1);
            return false;
        }
    }

    public static double constraintBatchIntervalAndWindowSize(int jobId, double batchInterval) {
        if (jobId >= 10 && jobId <= 15)
            return 10.0 % batchInterval;
        if (jobId >= 16 && jobId <= 21)
            return 10.0 % batchInterval;
        if (jobId >= 22 && jobId <= 23)
            return 10.0 % batchInterval;
        if (jobId >= 24 && jobId <= 25)
            return 10.0 % batchInterval;
        if (jobId >= 26 && jobId <= 31)
            return 0;
        if (jobId >= 32 && jobId <= 37)
            return 10.0 % batchInterval;
        if (jobId >= 38 && jobId <= 43)
            return 10.0 % batchInterval;
        if (jobId >= 44 && jobId <= 49)
            return 10.0 % batchInterval;
        if (jobId >= 50 && jobId <= 55)
            return 10.0 % batchInterval;
        if (jobId >= 56 && jobId <= 67)
            return 0;
        else {
            System.out.println("尚未涵盖该job");
            System.exit(-1);
            return -1;
        }
    }

    public void evaluate(IntegerSolution solution) {
        int jobId = 14;

        int numberOfVariables = getNumberOfVariables();
        double[] x = new double[numberOfVariables] ;




        // since we only take continous chunk, we construct a mapping from a enumerated set to a continous chunk
        int[] batchIntervals = {1, 2, 5, 10};
        int[] maxSizeInFlightValues = {24, 48, 96};
        int[] bypassMergeThresholdValues = {10, 18, 200};
        int[] memoryFractionValues = {40, 60, 80};
        int[] executorMemoryValues = {512, 1024, 6144};


        Boolean rerunFlag = true;

        while(rerunFlag == true) {

            for (int i = 0; i < numberOfVariables; i++) {
                x[i] = solution.getVariableValue(i) ;
            }
            x[4] = maxSizeInFlightValues[(int)x[4]];
            x[5] = bypassMergeThresholdValues[(int)x[5]];
            x[6] = memoryFractionValues[(int)x[6]];
            x[7] = executorMemoryValues[(int)x[7]];

            StringBuilder configL = new StringBuilder("JobID" + ":" + jobId + ";" + "Objective:Latency;" + "batchInterval:" + x[0] + ";blockInterval:" + x[1] + ";parallelism:" + x[2] + ";inputRate:" + x[3] + ";maxSizeInFlightValues:" + x[4] + ";bypassMergeThresholdValues:" + x[5] + ";memoryFractionValues:" + x[6] + ";executorMemoryValues:" + x[7] + ";rddCompressValues:" + x[8] + ";broadcastCompressValues:" + x[9]);
            StringBuilder configT = new StringBuilder("JobID" + ":" + jobId + ";" + "Objective:Throughput;" + "batchInterval:" + x[0] + ";blockInterval:" + x[1] + ";parallelism:" + x[2] + ";inputRate:" + x[3] + ";maxSizeInFlightValues:" + x[4] + ";bypassMergeThresholdValues:" + x[5] + ";memoryFractionValues:" + x[6] + ";executorMemoryValues:" + x[7] + ";rddCompressValues:" + x[8] + ";broadcastCompressValues:" + x[9]);

            String configLatency = configL.toString();
            String configThruput = configT.toString();

            zClient.putMessage("JConfig", configLatency);
            String predictAnswer = zClient.getMessage();
            String predictAnsTopic = zClient.parseTopic(predictAnswer); // it should be PyPred
            String predictAnsMessage = zClient.parseMessage(predictAnswer);
            double targetLatency = Double.parseDouble(predictAnsMessage);

            if (constraintBatchIntervalAndWindowSize(jobId, x[0] * 1.0) != 0)
                continue;

            if (constraintSlideWindowAndLatency(jobId, x[0], targetLatency))
                continue;

            rerunFlag = false;
            zClient.putMessage("JConfig", configThruput);
            predictAnswer = zClient.getMessage();
            predictAnsTopic = zClient.parseTopic(predictAnswer); // it should be PyPred
            predictAnsMessage = zClient.parseMessage(predictAnswer);
            double targetThruput = Double.parseDouble(predictAnsMessage);

            solution.setObjective(0, targetLatency);
            solution.setObjective(1, -targetThruput);
        }
            
    }
}
