package org.uma.jmetal.util.comparator;

import org.uma.jmetal.solution.Solution;
import org.uma.jmetal.util.JMetalException;
import org.uma.jmetal.util.VectorUtil;
import org.uma.jmetal.util.checking.Checker;
import org.uma.jmetal.util.comparator.impl.OverallConstraintViolationComparator;

import java.io.Serializable;
import java.util.Comparator;

/**
 * This class implements a solution comparator for dominance checking
 *
 * @author Antonio J. Nebro <antonio@lcc.uma.es>
 */
@SuppressWarnings("serial")
public class DominanceComparatorV2<S extends Solution<?>> implements Comparator<S>, Serializable {

  /**
   * Compares two solutions.
   *
   * @param solution1 Object representing the first <code>Solution</code>.
   * @param solution2 Object representing the second <code>Solution</code>.
   * @return -1, or 0, or 1 if solution1 dominates solution2, both are non-dominated, or solution1
   *     is dominated by solution2, respectively.
   */
  @Override
  public int compare(S solution1, S solution2) {
    Checker.isNotNull(solution1);
    Checker.isNotNull(solution2);
    Checker.isTrue(
        solution1.getNumberOfObjectives() == solution2.getNumberOfObjectives(),
        "Cannot compare because solution1 has "
            + solution1.getNumberOfObjectives()
            + " objectives and solution2 has "
            + solution2.getNumberOfObjectives());

    return VectorUtil.dominanceTest(solution1.getObjectives(), solution2.getObjectives()) ;
  }
}
