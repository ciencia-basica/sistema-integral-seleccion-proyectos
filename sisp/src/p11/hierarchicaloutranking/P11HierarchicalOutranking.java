/**
 * @author Dr. Efraín Solares
 * Facultad de Contaduría y Administración
 * Universidad Autónoma de Coahuila
 * México
 */
package p11.hierarchicaloutranking;

import General.Utilities;
import java.nio.file.Path;
import java.nio.file.Paths;

public class P11HierarchicalOutranking {
    Criterion criteria[];
    Alternative alternatives[];
    int no_alternatives;
    int no_criteria;
    int no_elementary_criteria;
    String[] criteriaNames;
    String[] elementaryCriteriaNames;
    double[] lambda;
    double[][][] performanceMatrix ;

    public static void main(String[] args) {
        P11HierarchicalOutranking application = new P11HierarchicalOutranking();
        
        if(args.length != 1){
            System.out.println("Usage:<route-to-files>");
            System.exit(1);
        }
        
        Path route = Paths.get(args[0]);
        String separator = "\t";
        
        String readingAditionals[][] = Utilities.readFile(route.resolve("Parámetros de criterios adicionales.txt"), separator);
        String readingInteractions[][] = Utilities.readFile(route.resolve("Interacciones.txt"), separator);
        String readingWeights[][] = Utilities.readFile(route.resolve("Pesos.txt"), separator);
        String readingPreferenceParameters[][] = Utilities.readFile(route.resolve("Parámetros de criterios.txt"), separator);
        String readingDirections[][] = Utilities.readFile(route.resolve("Direcciones de los criterios.txt"), separator);
        String readingPerformanceMatrix[][] = Utilities.readFile(route.resolve("Matriz de desempeños.txt"), separator);
        application.initializeParameters(readingPerformanceMatrix, readingDirections, readingPreferenceParameters, 
                readingWeights, readingInteractions, readingAditionals);
        application.initializeProcedure(route);
    }
    
    void initializeProcedure(Path route){
        // THIS HAS CHANGED!!!! SEE INITIALIZER.java
        Utilities.Problem problem = new Utilities().new Problem(alternatives, null, criteria, no_elementary_criteria, null, 
                criteriaNames, elementaryCriteriaNames, lambda, 0, Utilities.SORTING_PROBLEM_nC);
        //Utilities.Problem problem = utilities.new Problem(no_alternatives, no_elementary_criteria, criteriaTypes, weights,
        //        indiferenceThresholds, preferenceThresholds, vetoes,
        //        elementaryCriteriaNames, performanceMatrix);
        h_I_outranking.calculateCredibilityMatrix(problem, route, Utilities.ROOT_CRITERION);
    }
    
    /**
     * Defines the number of criteria and alternatives, as well as the criteria types out of the performance matrix
     * @param readingPerformanceMatrix 
     */
    public void initializeParameters(String readingPerformanceMatrix[][], String readingDirections[][], String readingPreferenceParameters[][], 
            String readingWeights[][], String readingInteractions[][], String readingAditionals[][]){
        no_alternatives = readingPerformanceMatrix.length - 1;//-1 to remove header
        no_criteria = readingWeights.length;
        no_elementary_criteria = readingPerformanceMatrix[0].length;
        performanceMatrix = new double[no_alternatives][no_elementary_criteria][2];
        criteriaNames = new String[no_criteria];
        elementaryCriteriaNames = new String[no_elementary_criteria];
        lambda = new double[2];
        
        criteria = new Criterion[no_criteria];
        alternatives = new Alternative[no_alternatives];
        
        lambda[0] = Double.parseDouble(readingAditionals[0][0]);
        lambda[1] = Double.parseDouble(readingAditionals[0][1]);
        
        /*ALL CRITERIA*/
        /*Creting all criteria*/
        for(int criterionPos = 0; criterionPos < no_criteria; criterionPos++){
            criteria[criterionPos] = new Criterion();
            criteria[criterionPos].setName(readingWeights[criterionPos][0]);
            criteriaNames[criterionPos] = criteria[criterionPos].getName();
            String[] weightValues = readingWeights[criterionPos][1].split(" ");
            criteria[criterionPos].setWeight(Double.parseDouble(weightValues[0]), Double.parseDouble(weightValues[1]));
        }
        /*Interactions*/
        for (String[] readingInteraction : readingInteractions) {
            String currentCriterionName = readingInteraction[0];
            String interactingCriterionName = readingInteraction[1];
            String interaction = readingInteraction[2];
            String[] stringValues = readingInteraction[3].split(" ");
            double[] interactionValue = new double[]{Double.parseDouble(stringValues[0]), Double.parseDouble(stringValues[1])};
            Criterion criterion = Utilities.findCriterion(currentCriterionName, criteria);
            if(interaction.equals("weak")){
                criterion.weakeningInteraction.setInteraction(interactingCriterionName, interactionValue);
            }
            if(interaction.equals("str")){
                criterion.strengtheningInteraction.setInteraction(interactingCriterionName, interactionValue);
            }
            if(interaction.equals("ant")){
                criterion.antagonistInteraction.setInteraction(interactingCriterionName, interactionValue);
            }
        }
        
        /*ELEMENTARY CRITERIA*/
        /*Thresholds*/
        int[] criteriaTypes = new int[no_elementary_criteria];
        for(int elementCritPos = 0; elementCritPos < no_elementary_criteria; elementCritPos++){
            elementaryCriteriaNames[elementCritPos] = readingPerformanceMatrix[0][elementCritPos];            
            int criterionPos = Utilities.findFirstocurrence(elementaryCriteriaNames[elementCritPos], criteriaNames);
            Criterion g = criteria[criterionPos];
            
            g.setIsElementary(true);
            String[] criterionValue = readingPerformanceMatrix[1][elementCritPos].split(" ");
            if(criterionValue.length==1){
                criteriaTypes[elementCritPos] = Utilities.REAL_NUMBER;//Control for creating Alternatives below
                g.setType(Utilities.REAL_NUMBER);
                g.setIndifferenceThreshold(Double.parseDouble(readingPreferenceParameters[3][elementCritPos]));
                g.setPreferenceThreshold(Double.parseDouble(readingPreferenceParameters[4][elementCritPos]));
            }else{
                criteriaTypes[elementCritPos] = Utilities.INTERVAL_NUMBER;
                g.setType(Utilities.INTERVAL_NUMBER);
            }
            String[] vetoValues = readingPreferenceParameters[5][elementCritPos].split(" ");
            try {
                g.setVeto(Double.parseDouble(vetoValues[0]), Double.parseDouble(vetoValues[1]));
            } catch (NumberFormatException e) {
                g.setVeto(0, 0);//The criterion does not have veto power
            }
        }        
        /*Performance matrix*/
        for(int alternativePos = 0; alternativePos < no_alternatives; alternativePos++){
            alternatives[alternativePos] = new Alternative(no_elementary_criteria);
            for (int elementCritPos = 0; elementCritPos < no_elementary_criteria; elementCritPos++) {
                int direction = Integer.parseInt(readingDirections[1][elementCritPos]);
                if(criteriaTypes[elementCritPos] == Utilities.REAL_NUMBER){
                    double value = Double.parseDouble(readingPerformanceMatrix[alternativePos+1][elementCritPos]);
                    alternatives[alternativePos].setCrtieriaImpacts(elementCritPos, (value*direction), (value*direction));
                    //performanceMatrix[alternativePos][elementCritPos][0] = performanceMatrix[alternativePos][elementCritPos][1] = (value*direction);
                }else{
                    String[] criterionValue = readingPerformanceMatrix[alternativePos+1][elementCritPos].split(" ");
                    double lowestValue = Double.parseDouble(criterionValue[0])*direction;
                    double highestValue = Double.parseDouble(criterionValue[1])*direction;
                    if(lowestValue > highestValue){
                        double temp = highestValue;
                        highestValue = lowestValue;
                        lowestValue = temp;
                    }
                    alternatives[alternativePos].setCrtieriaImpacts(elementCritPos, lowestValue, highestValue);
                    //performanceMatrix[alternativePos][elementCritPos][0] = lowestValue;
                    //performanceMatrix[alternativePos][elementCritPos][1] = highestValue;
                }
            }
        }
    }
}
