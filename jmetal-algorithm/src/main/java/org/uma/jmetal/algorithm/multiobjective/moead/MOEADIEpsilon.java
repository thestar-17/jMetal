package org.uma.jmetal.algorithm.multiobjective.moead;

import org.uma.jmetal.algorithm.multiobjective.moead.util.MOEADUtils;
import org.uma.jmetal.operator.crossover.CrossoverOperator;
import org.uma.jmetal.operator.crossover.impl.DifferentialEvolutionCrossover;
import org.uma.jmetal.operator.mutation.MutationOperator;
import org.uma.jmetal.problem.ConstrainedProblem;
import org.uma.jmetal.problem.Problem;
import org.uma.jmetal.solution.doublesolution.DoubleSolution;
import org.uma.jmetal.util.SolutionListUtils;

import java.util.Arrays;
import java.util.List;

import static org.uma.jmetal.problem.ConstrainedProblem.Attributes.OVERALL_CONSTRAINT_VIOLATION_DEGREE;

/**
 * This class implements the MOEA/D-IEpsilon algorithm based on the one presented in the paper: "Z.
 * Fan, W. Li, X. Cai, H. Huang, Y. Fang, Y. You, J. Mo, C Wei, and E. D. Goodman, “An improved
 * epsilon constraint-handling method in MOEA/D for CMOPs with large infeasible regions,” Soft
 * Computing, https://doi.org/10.1007/s00500-019-03794-x
 *
 * @author Antonio J. Nebro
 * @version 1.0
 */
@SuppressWarnings("serial")
public class MOEADIEpsilon extends AbstractMOEAD<DoubleSolution> {

  private DifferentialEvolutionCrossover differentialEvolutionCrossover;
  private double epsilonK;
  private double phiMax = -1e30;

  public MOEADIEpsilon(
      ConstrainedProblem<DoubleSolution> problem,
      int populationSize,
      int resultPopulationSize,
      int maxEvaluations,
      MutationOperator<DoubleSolution> mutation,
      CrossoverOperator<DoubleSolution> crossover,
      FunctionType functionType,
      String dataDirectory,
      double neighborhoodSelectionProbability,
      int maximumNumberOfReplacedSolutions,
      int neighborSize) {
    super(
        problem,
        populationSize,
        resultPopulationSize,
        maxEvaluations,
        crossover,
        mutation,
        functionType,
        dataDirectory,
        neighborhoodSelectionProbability,
        maximumNumberOfReplacedSolutions,
        neighborSize);

    differentialEvolutionCrossover = (DifferentialEvolutionCrossover) crossoverOperator;
  }

  @Override
  public void run() {
    initializeUniformWeight();
    initializeNeighborhood();
    initializePopulation();
    idealPoint.update(population);

    double[] constraints = new double[populationSize];
    for (int i = 0; i < populationSize; i++) {
      constraints[i] = (double)population.get(i).getAttribute(OVERALL_CONSTRAINT_VIOLATION_DEGREE) ;
    }
    Arrays.sort(constraints);
    double epsilonZero = Math.abs(constraints[(int) Math.ceil(0.05 * populationSize)]);

    if (phiMax < Math.abs(constraints[0])) {
      phiMax = Math.abs(constraints[0]);
    }

    int tc = (int) (0.8 * maxEvaluations / populationSize);
    tc = 800 ;
    double tao = 0.05;
    double rk = SolutionListUtils.getFeasibilityRatio(population);

    evaluations = populationSize;
    int generationCounter = 0 ;
    epsilonK = epsilonZero;
    do {
      // Update the epsilon level
      if (generationCounter >= tc) {
        epsilonK = 0;
      } else {
        if (rk < 0.95) {
          epsilonK = (1 - tao) * epsilonK;
        } else {
          epsilonK = phiMax * (1 + tao);
        }
      }

      int[] permutation = new int[populationSize];
      MOEADUtils.randomPermutation(permutation, populationSize);

      for (int i = 0; i < populationSize; i++) {
        int subProblemId = permutation[i];

        NeighborType neighborType = chooseNeighborType();
        List<DoubleSolution> parents = parentSelection(subProblemId, neighborType);

        differentialEvolutionCrossover.setCurrentSolution(population.get(subProblemId));
        List<DoubleSolution> children = differentialEvolutionCrossover.execute(parents);

        DoubleSolution child = children.get(0);
        mutationOperator.execute(child);
        problem.evaluate(child);
        ((ConstrainedProblem)problem).evaluateConstraints(child) ;

        evaluations++;

        // Update PhiMax
        if (phiMax < Math.abs((double) child.getAttribute(OVERALL_CONSTRAINT_VIOLATION_DEGREE))) {
          phiMax = (double) child.getAttribute(OVERALL_CONSTRAINT_VIOLATION_DEGREE);
        }

        idealPoint.update(child.getObjectives());
        updateNeighborhood(child, subProblemId, neighborType);
      }
      rk = SolutionListUtils.getFeasibilityRatio(population);

      generationCounter++ ;
    } while (evaluations < maxEvaluations);
  }

  public void initializePopulation() {
    for (int i = 0; i < populationSize; i++) {
      DoubleSolution newSolution = (DoubleSolution) problem.createSolution();

      problem.evaluate(newSolution);
      ((ConstrainedProblem)problem).evaluateConstraints(newSolution) ;
      population.add(newSolution);
    }
  }

  @Override
  protected void updateNeighborhood(
      DoubleSolution individual, int subproblemId, NeighborType neighborType) {
    int size;
    int numberOfReplaceSolutions;

    numberOfReplaceSolutions = 0;

    if (neighborType == NeighborType.NEIGHBOR) {
      size = neighborhood[subproblemId].length;
    } else {
      size = population.size();
    }
    int[] perm = new int[size];

    MOEADUtils.randomPermutation(perm, size);

    for (int i = 0; i < size; i++) {
      int k;
      if (neighborType == NeighborType.NEIGHBOR) {
        k = neighborhood[subproblemId][perm[i]];
      } else {
        k = perm[i];
      }

      double f1, f2;
      f1 = fitnessFunction(population.get(k), lambda[k]);
      f2 = fitnessFunction(individual, lambda[k]);

      double cons1 =
          Math.abs((double) population.get(k).getAttribute(OVERALL_CONSTRAINT_VIOLATION_DEGREE));
      double cons2 =
          Math.abs((double) individual.getAttribute(OVERALL_CONSTRAINT_VIOLATION_DEGREE));

      if (cons1 < epsilonK && cons2 <= epsilonK) {
        if (f2 < f1) {
          population.set(k, (DoubleSolution) individual.copy());
          numberOfReplaceSolutions++;
        }
      } else if (cons1 == cons2) {
        if (f2 < f1) {
          population.set(k, (DoubleSolution) individual.copy());
          numberOfReplaceSolutions++;
        }
      } else if (cons2 < cons1) {
        population.set(k, (DoubleSolution) individual.copy());
        numberOfReplaceSolutions++;
      }

      if (numberOfReplaceSolutions >= maximumNumberOfReplacedSolutions) {
        return;
      }
    }
  }

  @Override
  public String getName() {
    return "MOEA/D IEpsilon";
  }

  @Override
  public String getDescription() {
    return "MOEA/D with improved epsilon constraint handling method";
  }
}
