/**
 THE DIFFERENCE BETWEEN THE PROJECT IN P13 AND THE ONE IN P11 IS THAT THE FORMER
 * NOW ALLOWS TO INCORPORATE THE U-MODEL (i.e., there may be sub-trees with
 * only cardinal elementary criteria that require to use a function-utility model)
 */

package P13;

import General.Utilities;
import java.nio.file.Path;
import java.nio.file.Paths;

/**
 *
 * @author Dr. Efraín Solares
 * Facultad de Contaduría y Administración
 * Universidad Autónoma de Coahuila
 * México
 * 
 */

public class ProjectPortfolios {
   public static void main(String[] args) {
       if (args.length != 1 && args.length != 2 && args.length != 3) {
           System.out.println("Usage:<route-to-files>"
                   + " <Type of problem: 1 is sorting with HI-INTERCLASS-nC; 2 with HI-INTERCLASS-nB> [Criterion name where the sorting must be carried on]");
           System.exit(1);
       }
       String separator = "\t";

       Path route = Paths.get(args[0]);
       String readingProjects[][] = Utilities.readFile(route.resolve("Accepted projects.txt"), separator);
       String GA_parameters[][] = Utilities.readFile(route.resolve("GA parameters.txt"), separator);
       String readingElementaryCriteria[][] = Utilities.readFile(route.resolve("Elementary criteria.txt"), separator);
       String readingAggregationBySum[][] = Utilities.readFile(route.resolve("Aggregation by sum.txt"), separator);
       String readingAggregationByCount[][] = Utilities.readFile(route.resolve("Aggregation by count.txt"), separator);
       String readingAggregationBySorting[][] = Utilities.readFile(route.resolve("Aggregation by sorting.txt"), separator);
       String readingAggregationByInconsistencies[][] = Utilities.readFile(route.resolve("Aggregation by inconsistencies.txt"), separator);

//       GeneticAlgorithm GA = new GeneticAlgorithm(readingProjects, null, GA_parameters, readingElementaryCriteria, 
//               readingAggregationBySum, readingAggregationByCount, readingAggregationBySorting, readingAggregationByInconsistencies);
//       GA.optimise();
    }
}
