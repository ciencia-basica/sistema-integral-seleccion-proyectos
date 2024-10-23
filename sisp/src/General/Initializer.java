package General;

import P13.GeneticAlgorithm;
import java.nio.file.Path;
import java.nio.file.Paths;
import p11.hierarchicaloutranking.Alternative;
import p11.hierarchicaloutranking.Criterion;
import p11.hierarchicaloutranking.h_I_outranking;

/**
 *
 * @author iSistemas
 * Para:
 * Responsable Técnico: Dr. Efraín Solares Lachica
 * Proyecto CONACyT de Ciencia Básica y/o de Frontera No. 321028
 * “Hibridación de Apoyo a la Toma de Decisiones Multicriterio y Metaheurísticas Multiobjetivo para la selección de proyectos”
 * Facultad de Contaduría y Administración, unidad Torreón
 * Universidad Autónoma de Coahuila
 * México
 * 
 */
public class Initializer {
    Criterion criteria[];
    Alternative alternatives[];
    Alternative profiles[][][];
    int no_alternatives;
    int no_criteria;
    int no_elementary_criteria;
    int typeOfProblem;
    String[] criteriaNames;
    String[] elementaryCriteriaNames;
    String[][] classes;
    String[] credibilityCriteria;
    String[] sortingCriteria;
    double[] lambda;
    double beta;
    double[][][] performanceMatrix;
    
    public static void main(String[] args) {
        Initializer application = new Initializer();
        int activityID;

        if(args.length != 2){
            application.printUsage();
            System.exit(1);
        }

        try{
            activityID = Integer.parseInt(args[0]);
        }catch(NumberFormatException ex){
            application.printUsage();
            System.exit(1);
            return;
        }

        Path filesDir = Paths.get(args[1]);
        String separator = "\t";
        
        System.out.println("Reading files from: " + filesDir.toString());
        switch (activityID) {
            case 1:
                application.projectLevel(filesDir, separator);
                break;
            case 2:
                application.portfolioLevel(filesDir, separator);
                break;
            case 3:
                application.projectLevel(filesDir, separator);
                application.portfolioLevel(filesDir, separator);
                break;
            case 4:
                application.calculateCredibilityMatrix(filesDir, separator);
                break;
            case 5:
                application.calculateSorting(filesDir, separator);
                break;
            default:
                System.out.println("Usage: <P11.HierarchicalOutranking.jar Activity ID(1:Project level; 2:Portfolio level; 3:Both levels;"
                        + "4:Calculate credibility matrix; 5:Calculate sorting)>");
                System.exit(1);
            break;
        }
    }
    
    void printUsage(){
        System.out.println("Usage: java -jar executable.jar [activityId]");
        System.out.println("  [activityId]  An integer representing the activity ID:");
        System.out.println("    1  Project level");
        System.out.println("    2  Portfolio level");
        System.out.println("    3  Both levels");
        System.out.println("    4  Credibility matrix");
        System.out.println("    5  Sorting");
        System.out.println("  [folderPath]  The path to the folder containing the files.");
    }

    void projectLevel(Path filesDir, String separator){
        Path projectDir = filesDir.resolve("Project level");
        String readingAditionals[][] = Utilities.readFile(projectDir.resolve("Additional criteria parameters.txt"), separator);
        String readingInteractions[][] = Utilities.readFile(projectDir.resolve("Criteria interactions.txt"), separator);
        String readingWeights[][] = Utilities.readFile(projectDir.resolve("Weights.txt"), separator);
        String readingPreferenceParameters[][] = Utilities.readFile(projectDir.resolve("Criteria parameters.txt"), separator);
        String readingDirections[][] = Utilities.readFile(projectDir.resolve("Criteria directions.txt"), separator);
        String readingPerformanceMatrix[][] = Utilities.readFile(projectDir.resolve("Performance matrix.txt"), separator);
        String readingCardinalCriteria[][] = Utilities.readFile(projectDir.resolve("Use value function.txt"), separator);//Super criteria whose elemtary criteria are cardional, and where a function value will be used to assess its credibility degree
        String readingDescendantsWithVeto[][] = Utilities.readFile(projectDir.resolve("Veto thresholds for supercriteria.txt"), separator);//Super criteria whose elemtary criteria are cardional, and where a function value will be used to assess its credibility degree
        String readingCredibilityCriteria[][] = Utilities.readFile(projectDir.resolve("Credibility criteria.txt"), separator);//Contains the non-elementary criteria where a credibility matrix must be built
        String readingSortingCriteria[][] = Utilities.readFile(projectDir.resolve("Sorting criteria.txt"), separator);//Contains the non-elementary criteria where sorting must be performed
        initializeParameters(readingPerformanceMatrix, readingDirections, readingPreferenceParameters,
                readingWeights, readingInteractions, readingAditionals, readingCardinalCriteria, readingDescendantsWithVeto,
                readingCredibilityCriteria, readingSortingCriteria, projectDir.resolve("Sorting"), separator);
        execute(projectDir);
    }
    
    void portfolioLevel(Path filesDir, String separator){
        Path projectDir = filesDir.resolve("Project level");
        Path portfolioDir = filesDir.resolve("Portfolio level");
        String readingPerformanceMatrix[][] = Utilities.readFile(projectDir.resolve("Performance matrix.txt"), separator);
        String readingSortingCriteria[][] = Utilities.readFile(projectDir.resolve("Sorting criteria.txt"), separator);
        String readingThresholdsAcceptedProjects[][] = Utilities.readFile(portfolioDir.resolve("Thresholds to accepted projects.txt"), separator);//Indicates the minimum class to which a project must be assigned to be accepted
        Object[] objects = filterProjects(readingPerformanceMatrix, readingThresholdsAcceptedProjects,
                readingSortingCriteria, projectDir.resolve("Sorting"), separator);
        String[][] supportedProjects = (String[][]) objects[0];
        String[][][] readingAssignmentes_supportedProjects = (String[][][]) objects[1];
        String[][] discardedProjects = (String[][]) objects[2];
        String[][][] readingAssignments_discardedProjects = (String[][][]) objects[3];
//        Utilities.saveFile(route+ "Portfolio level\\Accepted projects.txt", acceptedProjects, separator);
//        Utilities.saveFile(filesDir+ "Portfolio level\\Discarded projects.txt", discardedProjects, separator);
        String readingConstraints[][] = Utilities.readFile(portfolioDir.resolve("Constraints.txt"), separator);
        String GA_parameters[][] = Utilities.readFile(portfolioDir.resolve("GA parameters.txt"), separator);
        String readingElementaryCriteria[][] = Utilities.readFile(portfolioDir.resolve("Elementary criteria.txt"), separator);
        String readingDirectionsPortfolio[][] = Utilities.readFile(portfolioDir.resolve("Criteria directions.txt"), separator);
        String readingAggregationBySum[][] = Utilities.readFile(portfolioDir.resolve("Aggregation by sum.txt"), separator);
        String readingAggregationByCount[][] = Utilities.readFile(portfolioDir.resolve("Aggregation by count.txt"), separator);
        String readingAggregationBySorting[][] = Utilities.readFile(portfolioDir.resolve("Aggregation by sorting.txt"), separator);
        String readingAggregationByInconsistencies[][] = Utilities.readFile(portfolioDir.resolve("Aggregation by inconsistencies.txt"), separator);
        String seeds[][] = Utilities.readFile(portfolioDir.resolve("Seeds.txt"), separator);
        GeneticAlgorithm GA = new GeneticAlgorithm(supportedProjects, readingAssignmentes_supportedProjects, readingConstraints, GA_parameters,
                readingElementaryCriteria, readingDirectionsPortfolio, readingAggregationBySum, readingAggregationByCount, readingAggregationBySorting,
                readingAggregationByInconsistencies, seeds, filesDir);
        GA.launchGA_PortfolioLevel(filesDir, separator);
        GA.optimise();
    }
    
    void calculateCredibilityMatrix(Path filesDir, String separator){
        Path credMatrixDir = filesDir.resolve("Calculate credibility matrix");
        String readingAditionals[][] = Utilities.readFile(credMatrixDir.resolve("Additional criteria parameters.txt"), separator);
        String readingDirections[][] = Utilities.readFile(credMatrixDir.resolve("Criteria directions.txt"), separator);
        String readingInteractions[][] = Utilities.readFile(credMatrixDir.resolve("Criteria interactions.txt"), separator);
        String readingPreferenceParameters[][] = Utilities.readFile(credMatrixDir.resolve("Criteria parameters.txt"), separator);
        String readingPerformanceMatrix[][] = Utilities.readFile(credMatrixDir.resolve("Performance matrix.txt"), separator);
        String readingCardinalCriteria[][] = Utilities.readFile(credMatrixDir.resolve("Use value function.txt"), separator);//Super criteria whose elemtary criteria are cardional, and where a function value will be used to assess its credibility degree
        String readingDescendantsWithVeto[][] = Utilities.readFile(credMatrixDir.resolve("Veto thresholds for supercriteria.txt"), separator);//Super criteria whose elemtary criteria are cardional, and where a function value will be used to assess its credibility degree
        String readingCredibilityCriteria[][] = Utilities.readFile(credMatrixDir.resolve("Credibility criteria.txt"), separator);//Contains the non-elementary criteria where a credibility matrix must be built
        String readingSortingCriteria[][] = new String[0][0];
        String readingWeights[][] = Utilities.readFile(credMatrixDir.resolve("Weights.txt"), separator);
        initializeParameters(readingPerformanceMatrix, readingDirections, readingPreferenceParameters,
                readingWeights, readingInteractions, readingAditionals, readingCardinalCriteria, readingDescendantsWithVeto,
                readingCredibilityCriteria, readingSortingCriteria, credMatrixDir.resolve("Sorting"), separator);
        execute(credMatrixDir);
    }
    
    void calculateSorting(Path filesDir, String separator){
        Path sortingDir = filesDir.resolve("Calculate sorting");
        String readingAditionals[][] = Utilities.readFile(sortingDir.resolve("Additional criteria parameters.txt"), separator);
        String readingDirections[][] = Utilities.readFile(sortingDir.resolve("Criteria directions.txt"), separator);
        String readingInteractions[][] = Utilities.readFile(sortingDir.resolve("Criteria interactions.txt"), separator);
        String readingPreferenceParameters[][] = Utilities.readFile(sortingDir.resolve("Criteria parameters.txt"), separator);
        String readingPerformanceMatrix[][] = Utilities.readFile(sortingDir.resolve("Performance matrix.txt"), separator);
        String readingCardinalCriteria[][] = Utilities.readFile(sortingDir.resolve("Use value function.txt"), separator);//Super criteria whose elemtary criteria are cardional, and where a function value will be used to assess its credibility degree
        String readingDescendantsWithVeto[][] = Utilities.readFile(sortingDir.resolve("Veto thresholds for supercriteria.txt"), separator);//Super criteria whose elemtary criteria are cardional, and where a function value will be used to assess its credibility degree
        String readingSortingCriteria[][] = Utilities.readFile(sortingDir.resolve("Sorting criteria.txt"), separator);//Contains the non-elementary criteria where a credibility matrix must be built
        String readingCredibilityCriteria[][] = new String[0][0];
        String readingWeights[][] = Utilities.readFile(sortingDir.resolve("Weights.txt"), separator);
        initializeParameters(readingPerformanceMatrix, readingDirections, readingPreferenceParameters,
                readingWeights, readingInteractions, readingAditionals, readingCardinalCriteria, readingDescendantsWithVeto,
                readingCredibilityCriteria, readingSortingCriteria, sortingDir.resolve("Sorting"), separator);
        execute(sortingDir);
    }

    void execute(Path path) {
        /*Credibility matrices*/
        Utilities.Problem problem = new Utilities().new Problem(alternatives, null, criteria, no_elementary_criteria,
                criteriaNames, null, elementaryCriteriaNames, lambda, beta, typeOfProblem);//Profiles and Classes are null because there will not be sorting here
        for(String credibilityCriterion:credibilityCriteria){
            System.out.println("Calculating credibility matrix for "+credibilityCriterion);
            double[][] credibilityMatrix = h_I_outranking.calculateCredibilityMatrix(problem, path, credibilityCriterion);
            h_I_outranking.saveCredibilityMatrix(credibilityMatrix, credibilityCriterion, path);
        }
        
        /*Sorting*/
        for(int i = 0; i < sortingCriteria.length; i++){
            problem = new Utilities().new Problem(alternatives, profiles[i], criteria, no_elementary_criteria,
                    criteriaNames, classes[i], elementaryCriteriaNames, lambda, beta, typeOfProblem);//Profiles is null because there will not be sorting here
            if (typeOfProblem == Utilities.SORTING_PROBLEM_nC) {
                System.out.println("Sorting alternatives for "+sortingCriteria[i]);
                String[][] sorting = h_I_outranking.sortAlternatives_HI_Interclass_nC(problem, path, sortingCriteria[i]);
                h_I_outranking.saveSorting(sorting, sortingCriteria[i], path);
            }   
        }
    }
    
    /**
     * Defines the number of criteria and alternatives, as well as the criteria types out of the performance matrix
     * @param readingPerformanceMatrix
     * @param readingDirections
     * @param readingPreferenceParameters
     * @param readingWeights
     * @param readingInteractions
     * @param readingAditionals
     * @param readingCardinalCriteria
     * @param readingDescendantsWithVeto
     * @param readingCredibilityCriteria
     * @param sortingPath
     * @param readingSortingCriteria
     * @param separator
     */
    public void initializeParameters(String readingPerformanceMatrix[][], String readingDirections[][], String readingPreferenceParameters[][],
            String readingWeights[][], String readingInteractions[][], String readingAditionals[][], String readingCardinalCriteria[][],
            String readingDescendantsWithVeto[][], String readingCredibilityCriteria[][], String readingSortingCriteria[][],
            Path sortingPath, String separator){
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
        
        beta = Double.parseDouble(readingAditionals[1][0]);
        typeOfProblem = Integer.parseInt(readingAditionals[2][0]);  
        
        /*ALL CRITERIA*/
        /*Creting all criteria*/
        for(int criterionPos = 0; criterionPos < no_criteria; criterionPos++){
            criteria[criterionPos] = new Criterion();
            criteria[criterionPos].setName(readingWeights[criterionPos][0]);
            criteriaNames[criterionPos] = criteria[criterionPos].getName();
            String[] weightValues = readingWeights[criterionPos][1].split(" ");
            criteria[criterionPos].setWeight(Double.parseDouble(weightValues[0]), Double.parseDouble(weightValues[1]));
            if (Utilities.findFirstocurrence_returnRow(criteria[criterionPos].getName(), readingCardinalCriteria)>-1){//Which criteria will aggregate descendant's scores using value function
                criteria[criterionPos].setUseValueFunction(true);
            }
            int pos;
            if ((pos = Utilities.findFirstocurrence_firstColumn_returnRow(criteria[criterionPos].getName(), readingDescendantsWithVeto))>-1){//Which descendant (not necessarily immediate) criteria will exert veto power
                String[] descendantWithVeto = readingDescendantsWithVeto[pos][0].split("\t");
                criteria[criterionPos].setDescendantsWithVeto(descendantWithVeto);
            }
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
                g.setVeto(0,0);//The criterion does not have veto power
            }
        }        
        /*Performance matrix*/
        double[][] minScores = new double[no_elementary_criteria][2];
        double[][] maxScores = new double[no_elementary_criteria][2];
        for(int alternativePos = 0; alternativePos < no_alternatives; alternativePos++){
            alternatives[alternativePos] = new Alternative(no_elementary_criteria);
            for (int elementCritPos = 0; elementCritPos < no_elementary_criteria; elementCritPos++) {
                int direction = Integer.parseInt(readingDirections[1][elementCritPos]);
                if(criteriaTypes[elementCritPos] == Utilities.REAL_NUMBER){
                    double value = Double.parseDouble(readingPerformanceMatrix[alternativePos+1][elementCritPos]);
                    alternatives[alternativePos].setCrtieriaImpacts(elementCritPos, (value*direction), (value*direction));
                    minScores[elementCritPos][0] = (value < minScores[elementCritPos][0] ? value : minScores[elementCritPos][0]);
                    maxScores[elementCritPos][1] = (value > maxScores[elementCritPos][0] ? value : maxScores[elementCritPos][0]);
                    minScores[elementCritPos][1] = minScores[elementCritPos][0];
                    maxScores[elementCritPos][0] = maxScores[elementCritPos][1];
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
                    minScores[elementCritPos][0] = minScores[elementCritPos][1] = (lowestValue < minScores[elementCritPos][0] ? lowestValue : minScores[elementCritPos][0]);
                    maxScores[elementCritPos][0] = maxScores[elementCritPos][1] = (highestValue > maxScores[elementCritPos][1] ? highestValue : maxScores[elementCritPos][1]);
                }
            }
        }
        for (int elementCritPos = 0; elementCritPos < no_elementary_criteria; elementCritPos++) {
            int criterionPos = Utilities.findFirstocurrence(elementaryCriteriaNames[elementCritPos], criteriaNames);
            Criterion g = criteria[criterionPos];
            g.setMinScore(minScores[elementCritPos]);
            g.setMaxScore(maxScores[elementCritPos]);
        }
        
        /*Credibility matrices*/
        credibilityCriteria = new String[readingCredibilityCriteria.length];
        for(int i = 0; i < credibilityCriteria.length; i++){
            credibilityCriteria[i] = readingCredibilityCriteria[i][0];
        }
        
        /*Sorting*/
        int noSortingCriteria = readingSortingCriteria.length;
        sortingCriteria = new String[noSortingCriteria];
        profiles = new Alternative[noSortingCriteria][][];
        classes = new String[noSortingCriteria][];
        for(int i = 0; i < noSortingCriteria; i ++){
            String sortingCriterion = sortingCriteria[i] = readingSortingCriteria[i][0];
            
            /*Profiles*/
            String[][] readingProfiles; 
            if (typeOfProblem == Utilities.SORTING_PROBLEM_nC) {
                readingProfiles = Utilities.readFile(sortingPath.resolve(sortingCriterion).resolve("Characteristic profiles.txt"), separator);
            }else{
                readingProfiles = null;
            }
            int noSetsOfProfiles = 0;
            if (readingProfiles != null) {
                noSetsOfProfiles = Integer.parseInt(readingProfiles[0][0]);
                profiles[i] = new Alternative[noSetsOfProfiles][];
            } else {
                profiles[i] = new Alternative[0][];
                classes[i] = new String[0];
                continue;
            }
            int posInFile = noSetsOfProfiles + 3;//To skip headers (noSetsOfProfiles + # of profiles per set, names of criteria, anti-ideal solution)
            for (int classPos = 0; classPos < noSetsOfProfiles; classPos++) {//Discard header
                int noProfiles = Integer.parseInt(readingProfiles[classPos + 1][0]);
                profiles[i][classPos] = new Alternative[noProfiles];
                for (int profilePos = 0; profilePos < noProfiles; profilePos++) {
                    profiles[i][classPos][profilePos] = new Alternative(no_elementary_criteria);
                    for (int criterionPos = 0; criterionPos < no_elementary_criteria; criterionPos++) {
                        int direction = Integer.parseInt(readingDirections[1][criterionPos]);
                        String[] profileValues = readingProfiles[posInFile][criterionPos].split(" ");
                        if (criteriaTypes[criterionPos] == Utilities.REAL_NUMBER) {
                            int value = Integer.parseInt(profileValues[0]);
                            profiles[i][classPos][profilePos].setCrtieriaImpacts(criterionPos, (value * direction), (value * direction));
                        } else {
                            int lowestValue = Integer.parseInt(profileValues[0]) * direction;
                            int highestValue = Integer.parseInt(profileValues[1]) * direction;
                            if (lowestValue > highestValue) {
                                int temp = highestValue;
                                highestValue = lowestValue;
                                lowestValue = temp;
                            }
                            profiles[i][classPos][profilePos].setCrtieriaImpacts(criterionPos, lowestValue, highestValue);
                        }
                    }
                    posInFile++;//Each new profile is in the immediate following row of the file
                }
            }

            /*Classes*/
            String[][] readingClasses;            
            if (typeOfProblem == Utilities.SORTING_PROBLEM_nC) {
                readingClasses = Utilities.readFile(sortingPath.resolve(sortingCriterion).resolve("Classes.txt"), separator);
                classes[i] = new String[readingClasses.length];
            } else {
                readingClasses = null;
            }
            for (int classPos = 0; classPos < classes[i].length; classPos++) {
                classes[i][classPos] = readingClasses[classPos][0];
            }
        }
    }
    
    /**
     * Determines which will be the accepted projects discarding those that do not fulfill the minimum class
     * @param readingProjects
     * @param readingThresholdsAcceptedProjects 
     * @param readingSortingCriteria 
     * @param path 
     * @param separator 
     * @return  the accepted projects
     */
    public Object[] filterProjects(String readingProjects[][], String readingThresholdsAcceptedProjects[][], 
            String[][]readingSortingCriteria, Path path, String separator){
        int noConstraints = readingThresholdsAcceptedProjects.length;
        int noProjects = readingProjects.length-1;//Remove header
        int noDiscardedProjects = 0;
        int[] discardedPositions = new int[noProjects];
        for(int i = 0; i < noConstraints; i++){
            String criterionName = readingThresholdsAcceptedProjects[i][0];
            String str = readingThresholdsAcceptedProjects[i][1].replaceAll("[^0-9]+", "");
            int constraint = Integer.parseInt(str);
            Path fileName = path.resolve(criterionName).resolve("Assignments.txt");
            String[][] readingAssignments = Utilities.readFile(fileName, separator);
            for(int j = 0; j < noProjects; j++){
                int lowerAssignedClass = Integer.parseInt(readingAssignments[j][1].replaceAll("[^0-9]+", ""));
                if(lowerAssignedClass < constraint){
                    if(discardedPositions[j] != 1)
                        noDiscardedProjects++;
                    discardedPositions[j] = 1;
                }
            }
        }
        int noAcceptedProjects = noProjects-noDiscardedProjects;
        String[][] acceptedProjects = new String[noAcceptedProjects+1][];
        acceptedProjects[0]=readingProjects[0];//Header
        String[][] discardedProjects = new String[noDiscardedProjects+1][];
        discardedProjects[0]=readingProjects[0];//Header
        for (int j = 0, posAccepted = 1, posDiscarded = 1; j < noProjects;) {
            if(discardedPositions[j++] != 1){
                acceptedProjects[posAccepted++]=readingProjects[j];//Remove header
            }else{
                discardedProjects[posDiscarded++]=readingProjects[j];//Remove header
            }
        }
        
        //Read the assignments for ALL THE SORTING CRITERIA (i.e., non-elementary criteria where sorting must be performed)
        int noSortingCriteria = readingSortingCriteria.length;
        String[][][] readingAssignments_acceptedProjects = new String[noAcceptedProjects+1][noSortingCriteria][2];
        String[][][] readingAssignments_discardedProjects = new String[noDiscardedProjects+1][noSortingCriteria][2];
        for (int sortCritPos = 0; sortCritPos < noSortingCriteria; sortCritPos++) {
            int acceptedProjPos = 0;
            int discardedProjPos = 0;
            String criterionName = readingSortingCriteria[sortCritPos][0];
            Path fileName = path.resolve(criterionName).resolve("Assignments.txt");
            String[][] readingAssignments_thisCriterion = Utilities.readFile(fileName, separator);
            readingAssignments_acceptedProjects[acceptedProjPos][sortCritPos][0] = 
                    readingAssignments_acceptedProjects[acceptedProjPos][sortCritPos][1] = criterionName;
            readingAssignments_discardedProjects[discardedProjPos][sortCritPos][0] = 
                    readingAssignments_discardedProjects[discardedProjPos][sortCritPos][1] = criterionName;
            acceptedProjPos++;
            discardedProjPos++;
            for (int projectPos = 0; projectPos < noProjects; projectPos++) {
                int lowerClass = Integer.parseInt(readingAssignments_thisCriterion[projectPos][1].replaceAll("[^0-9]+", ""));
                int upperClass = Integer.parseInt(readingAssignments_thisCriterion[projectPos][2].replaceAll("[^0-9]+", ""));
                if (discardedPositions[projectPos] != 1) {
                    readingAssignments_acceptedProjects[acceptedProjPos][sortCritPos][0] = String.valueOf(lowerClass);
                    readingAssignments_acceptedProjects[acceptedProjPos][sortCritPos][1] = String.valueOf(upperClass);
                    acceptedProjPos++;
                }else{
                    readingAssignments_discardedProjects[discardedProjPos][sortCritPos][0] = String.valueOf(lowerClass);
                    readingAssignments_discardedProjects[discardedProjPos][sortCritPos][1] = String.valueOf(upperClass);
                    discardedProjPos++;
                }
            }
        }
        
        return new Object[] {acceptedProjects, readingAssignments_acceptedProjects, discardedProjects, readingAssignments_discardedProjects};
    }

    /**
     * Determines the classes to which ALL the projects in the given filesDir were allocated
     * @param readingSortingCriteria
     * @param path
     * @param separator
     * @return 
     */
    public String[][][] obtainAssignments(String[][]readingSortingCriteria, Path path, String separator){
        String[][][] readingAssignments;
        //Initialize array
        String criterionName = readingSortingCriteria[0][0];
        Path fileName = path.resolve(criterionName).resolve("Assignments.txt");
        String[][] readingAssignments_thisCriterion = Utilities.readFile(fileName, separator);
        int noProjects = readingAssignments_thisCriterion.length;
        int noSortingCriteria = readingSortingCriteria.length;
        readingAssignments = new String[noProjects][noSortingCriteria][2];
        
        for (int sortCritPos = 0; sortCritPos < noSortingCriteria; ) {
            for (int projectPos = 0; projectPos < noProjects; projectPos++) {
                int lowerClass = Integer.parseInt(readingAssignments_thisCriterion[projectPos][1].replaceAll("[^0-9]+", ""));
                int upperClass = Integer.parseInt(readingAssignments_thisCriterion[projectPos][2].replaceAll("[^0-9]+", ""));
                readingAssignments[projectPos][sortCritPos][0] = String.valueOf(lowerClass);
                readingAssignments[projectPos][sortCritPos][1] = String.valueOf(upperClass);
            }   
            if(++sortCritPos < noSortingCriteria){
                criterionName = readingSortingCriteria[sortCritPos][0];
                fileName = path.resolve(criterionName).resolve("Assignments.txt");
                readingAssignments_thisCriterion = Utilities.readFile(fileName, separator);
            }
        }
        return readingAssignments;
    }
}
