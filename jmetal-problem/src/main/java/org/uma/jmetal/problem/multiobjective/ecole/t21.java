package org.uma.jmetal.problem.multiobjective.ecole;

import org.uma.jmetal.problem.impl.AbstractIntegerProblem;
import org.uma.jmetal.solution.IntegerSolution;
import org.uma.jmetal.util.JMetalException;
import org.uma.jmetal.util.SocClient;
import org.uma.jmetal.util.solutionattribute.impl.NumberOfViolatedConstraints;
import org.uma.jmetal.util.solutionattribute.impl.OverallConstraintViolation;

import java.util.ArrayList;
import java.util.List;

public class t21 extends AbstractIntegerProblem {

    public OverallConstraintViolation<IntegerSolution> overallConstraintViolationDegree ;
    public NumberOfViolatedConstraints<IntegerSolution> numberOfViolatedConstraints ;

    SocClient socClient;

    public t21() {
        // 10 knobs, 3 objectives
        //this(10, 3);
        this(10, 3);

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

        for (int i = 4; i <= 5; i++) {
            lowerLimit.add(0);
            upperLimit.add(1);
        }

        for (int i = 6; i <= 9; i++) {
            lowerLimit.add(0);
            upperLimit.add(2);
        }


        setLowerLimit(lowerLimit);
        setUpperLimit(upperLimit);

        overallConstraintViolationDegree = new OverallConstraintViolation<IntegerSolution>() ;
        numberOfViolatedConstraints = new NumberOfViolatedConstraints<IntegerSolution>() ;

        //TODO parameterize the arguments later
        socClient = new SocClient("EAClient", "localhost", 5590);
    }

    public t21(int numberOfVariables, int numberOfObjectives) throws JMetalException {
        setNumberOfVariables(numberOfVariables);
        setNumberOfObjectives(numberOfObjectives);
        setNumberOfConstraints(2);
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
        int jobId = 21;

        int numberOfVariables = getNumberOfVariables();
        double[] x = new double[numberOfVariables] ;

        // since we only take continous chunk, we construct a mapping from a enumerated set to a continous chunk
        int[] batchIntervals = {1, 2, 5, 10};
        int[] maxSizeInFlightValues = {24, 48, 96};
        int[] bypassMergeThresholdValues = {10, 18, 200};
        double[] memoryFractionValues = {0.4, 0.6, 0.8};
        int[] executorMemoryValues = {512, 1024, 6144};

        for (int i = 0; i < numberOfVariables; i++) {
            x[i] = solution.getVariableValue(i) ;
        }
        x[6] = maxSizeInFlightValues[(int)x[6]];
        x[7] = bypassMergeThresholdValues[(int)x[7]];
        x[8] = memoryFractionValues[(int)x[8]];
        x[9] = executorMemoryValues[(int)x[9]];

        StringBuilder configL = new StringBuilder("JobID" + ":" + jobId + ";" + "Objective:latency;" + "batchInterval:" + x[0] + ";blockInterval:" + x[1] + ";parallelism:" + x[2] + ";inputRate:" + x[3] + ";broadcastCompressValues:" + x[4] + ";rddCompressValues:" + x[5] + ";maxSizeInFlightValues:" + x[6] + ";bypassMergeThresholdValues:" + x[7] + ";memoryFractionValues:" + x[8] + ";executorMemoryValues:" + x[9]);
        StringBuilder configT = new StringBuilder("JobID" + ":" + jobId + ";" + "Objective:throughput;" + "batchInterval:" + x[0] + ";blockInterval:" + x[1] + ";parallelism:" + x[2] + ";inputRate:" + x[3] + ";broadcastCompressValues:" + x[4] + ";rddCompressValues:" + x[5] + ";maxSizeInFlightValues:" + x[6] + ";bypassMergeThresholdValues:" + x[7] + ";memoryFractionValues:" + x[8] + ";executorMemoryValues:" + x[9]);

        String configLatency = configL.toString();
        String configThruput = configT.toString();

        socClient.putMessage("JConfig", configLatency);
        String predictAnswer = socClient.getMessage();
        String predictAnsTopic = socClient.parseTopic(predictAnswer); // it should be PyPred
        String predictAnsMessage = socClient.parseMessage(predictAnswer);
        double targetLatency = Double.parseDouble(predictAnsMessage);
        solution.setObjective(0, targetLatency);

        socClient.putMessage("JConfig", configThruput);
        predictAnswer = socClient.getMessage();
        predictAnsTopic = socClient.parseTopic(predictAnswer); // it should be PyPred
        predictAnsMessage = socClient.parseMessage(predictAnswer);
        double targetThruput = Double.parseDouble(predictAnsMessage);
        solution.setObjective(1, -targetThruput);

        solution.setObjective(2, 21.5 * x[0] + 30 * x[2]);

        this.evaluateConstraints(solution);

    }

    private void evaluateConstraints(IntegerSolution solution)  {

        int jobId = 21;
        double[] constraint = new double[this.getNumberOfConstraints()];

        double batchInterval = solution.getVariableValue(0) ;
        double targetLatency = solution.getObjective(0);

        int violatedConstraints = 0;
        double overallConstraintViolation = 0.0;

        if (constraintBatchIntervalAndWindowSize(jobId, batchInterval * 1.0) != 0) {
            violatedConstraints++;
            overallConstraintViolation = overallConstraintViolation + 100;
        }
        if (constraintSlideWindowAndLatency(jobId, batchInterval, targetLatency)) {
            violatedConstraints++;
            overallConstraintViolation = overallConstraintViolation + 100;
        }

        overallConstraintViolationDegree.setAttribute(solution, overallConstraintViolation);
        numberOfViolatedConstraints.setAttribute(solution, violatedConstraints);
    }
}
