/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package P13;

import General.Utilities;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.PrintWriter;
import java.nio.file.Path;
import java.util.LinkedList;
import java.util.List;
import java.util.concurrent.ThreadLocalRandom;
import java.util.logging.Level;
import java.util.logging.Logger;
import p11.hierarchicaloutranking.Alternative;
import p11.hierarchicaloutranking.Criterion;
import p11.hierarchicaloutranking.h_I_outranking;

/**
 *
 * @author efrai
 */
public class GeneticAlgorithm {
    final int NO_DIMENSIONS;
    List<Individual> population;//Population of individuals/agents
    final int POPULATION_SIZE;//Number of individuals/agents in the population  
    final double CROSSOVER_PROBABILITY; //Crossover probability [0,1]
    final double MUTATION_PROBABILITY;//Mutation probability [0,1]
    final int NO_ITERATIONS;//Number of iterations; to be used as stopping criterion
    final int NO_RESTARTS;//Number of generational restats; that is, the NO_ITERATIONS is repeated NO_RESTARTS times
    final int NO_ELEMENTARY_CRITERIA;//Number of elementary criteria at portfolio level
    final Utilities.Portfolio PROJECTS_SET;//The projects accepted by a previous screening phase
    final String[] elementaryCriteria_names;//Indicates which project scores will be used to buil the portfolio scores
    final String[][] directions;//If a criterion must be maximized (1) or minimized (-1)
    final String[][] aggregationBySum;//Indicates which project scores will have to be sumed to calculate the portfolio scores
    final String[][] aggregationByCount;//Indicates which project scores will have to be counted to calculate the portfolio scores
    final String[][] aggregationBySorting;//Indicates the criterion on which the projects will have to be sorted and the lowest class to calculate the portfolio scores
    final String[][] aggregationByInconsistencies;//Indicates the criterion on which the projects will have to be sorted
    final String[][] projects;
    final String[][][] assignments;
    final String[][] constraints;
    String[][] seeds;
    Utilities.Problem problem;
    final Path path;
    
    /**
     * 
     * @param supportedProjects
     * @param readingAssignments
     * @param readingConstraints
     * @param GA_parameters
     * @param readingElementaryCriteria
     * @param readingDirections
     * @param readingAggregationBySum
     * @param readingAggregationByCount
     * @param readingAggregationBySorting
     * @param readingAggregationByInconsistencies
     * @param seeds
     * @param path
     */
    public GeneticAlgorithm(String[][] supportedProjects, String[][][] readingAssignments, String[][] readingConstraints, String[][] GA_parameters, 
            String[][] readingElementaryCriteria, String[][] readingDirections, String[][] readingAggregationBySum, String[][] readingAggregationByCount, 
            String[][] readingAggregationBySorting, String[][] readingAggregationByInconsistencies, String[][] seeds, Path path) {
        int pos = 0;
        this.NO_DIMENSIONS = supportedProjects.length-1;//Header
        POPULATION_SIZE = Integer.parseInt(GA_parameters[pos++][1]);
        CROSSOVER_PROBABILITY = Double.parseDouble(GA_parameters[pos++][1]);
        MUTATION_PROBABILITY = Double.parseDouble(GA_parameters[pos++][1]);
        NO_ITERATIONS = Integer.parseInt(GA_parameters[pos++][1]);
        NO_RESTARTS = Integer.parseInt(GA_parameters[pos++][1]);
        PROJECTS_SET  = new Utilities().new Portfolio(supportedProjects, readingAssignments);
        this.aggregationBySum = readingAggregationBySum;
        this.aggregationByCount = readingAggregationByCount;
        this.aggregationBySorting = readingAggregationBySorting;
        elementaryCriteria_names = new String[readingElementaryCriteria.length];
        for(int i = 0; i < readingElementaryCriteria.length; i++)
            elementaryCriteria_names[i] = readingElementaryCriteria[i][0];
        this.directions = readingDirections;
        this.projects = supportedProjects;
        this.assignments = readingAssignments;
        this.NO_ELEMENTARY_CRITERIA = elementaryCriteria_names.length;
        this.aggregationByInconsistencies = readingAggregationByInconsistencies;
        this.seeds = seeds;
        this.path = path;
        this.constraints = readingConstraints;
    }
    
    public void optimise() {
        System.out.println("Optimizing");
        List<Individual> childPopulation;
        List<Individual> mixedPopulation;
        List<Individual> pool;
        double[][] credibilityMatrix;
        
        pool = new LinkedList<>();
        for(int noRestart = 0; noRestart < NO_RESTARTS; noRestart++){
            System.out.println("RESTART " + (noRestart+1));
            population = random_initialPopulation_seed(seeds);
            assessElementaryCriteria(population);
            credibilityMatrix = h_I_outranking.calculateCredibilityMatrix(problem, path.resolve("Portfolio level"), "");//It is necessary to first assess the elementary criteria impacts
            assessConstraints(population);
            assessFitness_discardInfeasible(population, credibilityMatrix);
            //printPopulation(population);
            //savePopulation(population, 0, path + "Portfolio level\\");

            /*Generational evolution*/
            System.out.print("Generation ");
            for (int currenntIteration = 0; currenntIteration < NO_ITERATIONS; currenntIteration++) {
                System.out.print((currenntIteration + 1) + " ");
                childPopulation = create_childPopulation();
                mutation(childPopulation);
                mixedPopulation = mixPopulation(population, childPopulation);
                assessElementaryCriteria(mixedPopulation);
                credibilityMatrix = h_I_outranking.calculateCredibilityMatrix(problem, path.resolve("Portfolio level"), "");//It is necessary to first assess the elementary criteria impacts
                assessConstraints(mixedPopulation);
                assessFitness_discardInfeasible(mixedPopulation, credibilityMatrix);
                population = new_ParentPopulation(mixedPopulation);
                //savePopulation(population, currenntIteration+1, path);
            }
            System.out.println();

            //Individual to be seed for the next restart
            Individual newBest = null;
            try {
                List<Individual> copyPop = new LinkedList();//So the original population is maintained
                for (int i = 0; i < population.size(); i++) {
                    copyPop.add((Individual) population.get(i).clone());
                }
                newBest = (Individual) select_bestIndividual(copyPop).clone();
            } catch (CloneNotSupportedException ex) {
                Logger.getLogger(GeneticAlgorithm.class.getName()).log(Level.SEVERE, null, ex);
            }
            
            System.out.println("\nNew best:\n" + newBest.toString());
            
            seeds = Utilities.addRow(seeds, newBest.getChromosome());
            pool.add(newBest);
        }
        
        savePopulation(population, NO_ITERATIONS, path.resolve("Portfolio level"));
        
        //Final best individual
        assessElementaryCriteria(pool);
        credibilityMatrix = h_I_outranking.calculateCredibilityMatrix(problem, path.resolve("Portfolio level"), "");//It is necessary to first assess the elementary criteria impacts
        assessConstraints(pool);
        assessFitness_discardInfeasible(pool, credibilityMatrix);
        Individual overallBest = null;
        try {
            List<Individual> copyPop = new LinkedList();//So the original population is maintained
            for (int i = 0; i < pool.size(); i++) {
                copyPop.add((Individual) pool.get(i).clone());
            }
            overallBest = (Individual) select_bestIndividual(copyPop).clone();
        } catch (CloneNotSupportedException ex) {
            Logger.getLogger(GeneticAlgorithm.class.getName()).log(Level.SEVERE, null, ex);
        }
        System.out.println("\nOverall best:\n" + overallBest.toString());
    }

    List<Individual> random_initialPopulation(){
        List<Individual> initialPopulation = new LinkedList<>();
        for (int i = 0; i < POPULATION_SIZE; i++) {
            Individual individual = new Individual();
            individual.initialize_random();
            initialPopulation.add(individual);
        }
        
        return initialPopulation;
    }

    /**
     * Uses a given number of individuals in the initial population; the rest
     * of individuals are randomly generated
     * @return 
     */
    List<Individual> random_initialPopulation_seed(String[][] seeds){
        List<Individual> initialPopulation = new LinkedList<>();
        int noSeeds = seeds.length;
        for(int i = 0; i < noSeeds; i++){
            String[] seed = seeds[i];
            Individual individual = new Individual();
            individual.initialize_seed(seed);
            initialPopulation.add(individual);
        }
        for (int i = noSeeds; i < POPULATION_SIZE; i++) {
            Individual individual = new Individual();
            individual.initialize_random();
            initialPopulation.add(individual);
        }
        
        return initialPopulation;
    }

    /**
     * Ensures that the random initial population is feasible
     * @return 
     */
    List<Individual> random_feasible_initialPopulation(){
        List<Individual> initialPopulation = new LinkedList<>();
        for (int i = 0; i < POPULATION_SIZE; i++) {
            Individual individual = new Individual();
            do{
                individual.initialize_random();
                assessConstraints_individual(individual);
            }while(individual.getNoConstraints()>0);
            initialPopulation.add(individual);
        }
        
        return initialPopulation;
    }

    /**
     * Performs binary tournament and one-point crossover to produce the child population
     * @return 
     */
    List<Individual>create_childPopulation(){
        List<Individual> childPopulation = new LinkedList<>();
        for (int i = 0; i < POPULATION_SIZE; i++) {
            Individual individual = new Individual();
            childPopulation.add(individual);
        }
        Individual parent1, parent2;
        int rand, temp;
        int[] a1, a2;
        
        /*Binary tournament*/
        a1 = new int[POPULATION_SIZE];
        a2 = new int[POPULATION_SIZE];        
        for (int i = 0; i < POPULATION_SIZE; i++) {
            a1[i] = a2[i] = i;
        }
        for (int i = 0; i < POPULATION_SIZE; i++) {//Randomly assign indices to create the tournament
            rand = ThreadLocalRandom.current().nextInt(0, POPULATION_SIZE);
            temp = a1[rand];
            a1[rand] = a1[i];
            a1[i] = temp;
            rand = ThreadLocalRandom.current().nextInt(0, POPULATION_SIZE);
            temp = a2[rand];
            a2[rand] = a2[i];
            a2[i] = temp;
        }
        for (int i = 0; i < POPULATION_SIZE; i += 4) {
            parent1 = binaryTournament(population.get(a1[i]), population.get(a1[i+1]));
            parent2 = binaryTournament(population.get(a1[i + 2]), population.get(a1[i + 3]));
            crossover(parent1, parent2, childPopulation, (i), (i + 1));
            
            parent1 = binaryTournament(population.get(a2[i]), population.get(a2[i + 1]));
            parent2 = binaryTournament(population.get(a2[i + 2]), population.get(a2[i + 3]));
            crossover(parent1, parent2, childPopulation, (i + 2), (i + 3));
        }
        return childPopulation;
    }

    Individual binaryTournament(Individual individual1, Individual individual2){
        int flag = check_optimality(individual1, individual2);
        if (flag == 1) {
            return (individual1);
        }
        if (flag == -1) {
            return (individual2);
        }

        if ((ThreadLocalRandom.current().nextDouble(0, 1)) <= 0.5) {
            return (individual1);
        } else {
            return (individual2);
        }
    }

    /**
     * Deb's (2002) selection is used: i) if exactly one indivual is feasible,
     * it is selected. ii) if both individuals are infeasible, the one with
     * the lowest number of constraints violated is selected. iii) otherwise, 
     * the one with the best O. F. is selected
     *
     * @param a
     * @param b
     * @return
     */
    int check_optimality(Individual a, Individual b){
        if (a.getNoConstraints() > 0 && b.getNoConstraints() > 0){//Both are infeasible
            if (a.getNoConstraints() < b.getNoConstraints()) {
                return (1);//a violates lesser constraints
            } else {
                if (a.getNoConstraints() > b.getNoConstraints()) {
                    return (-1);//b violates lesser constraints
                } else {//Both violate the same number of constraints           
                    if (a.getFitnessValue() > b.getFitnessValue()) {
                        return (1);//a is better in terms of FV
                    } else {
                        if (a.getFitnessValue() < b.getFitnessValue()) {
                            return (-1);//b is better in terms of FV
                        } else {//Same FV
                            return (0);
                        }
                    }
                }
            }
        } else {
            if (a.getNoConstraints() > 0 && b.getNoConstraints() == 0) {//b is feasible, but a is not
                return (-1);
            } else {
                if (a.getNoConstraints() == 0 && b.getNoConstraints() > 0) {//a is feasible, but b is not
                    return (1);
                } else{//Both are feasible
                    if (a.getFitnessValue() > b.getFitnessValue()) {
                        return (1);//a is better in terms of FV
                    } else {
                        if (a.getFitnessValue() < b.getFitnessValue()) {
                            return (-1);//b is better in terms of FV
                        } else {//Same value in O. F.
                            return (0);
                        }
                    }
                }
            }
        }
    }

    void crossover(Individual parent1, Individual parent2, List<Individual> childPopulation, int pos_child1, int pos_child2) {
        int i;
        double num;
        int crossPoint;

        num = ThreadLocalRandom.current().nextDouble();
        if (num <= CROSSOVER_PROBABILITY) {
            crossPoint = ThreadLocalRandom.current().nextInt(0, NO_DIMENSIONS);
            for (i = 0; i < crossPoint; i++) {
                childPopulation.get(pos_child1).getChromosome()[i] = parent1.getChromosome()[i];
                childPopulation.get(pos_child2).getChromosome()[i] = parent2.getChromosome()[i];
            }
            for (i = crossPoint; i < NO_DIMENSIONS; i++) {
                childPopulation.get(pos_child1).getChromosome()[i] = parent2.getChromosome()[i];
                childPopulation.get(pos_child2).getChromosome()[i] = parent1.getChromosome()[i];
            }
        } else {
            for (i = 0; i < NO_DIMENSIONS; i++) {
                childPopulation.get(pos_child1).getChromosome()[i] = parent2.getChromosome()[i];
                childPopulation.get(pos_child2).getChromosome()[i] = parent1.getChromosome()[i];
            }
        }
    }

    /**
     * Changes an allele (0 or 1) for the opposite
     * @param pop 
     */
    void mutation(List<Individual> pop){
        for(int i = 0; i < POPULATION_SIZE; i++){
            double num = ThreadLocalRandom.current().nextDouble();
            if (num <= MUTATION_PROBABILITY) {
                int gen;
                gen = ThreadLocalRandom.current().nextInt(0, NO_DIMENSIONS);
                int newValue = pop.get(i).getChromosome()[gen] - 1;//newValue will be in [-1,0]
                newValue /= (-1);//Now it will be in [0,1]
                pop.get(i).getChromosome()[gen] = newValue;
            }
        }
    }

    List<Individual> mixPopulation(List<Individual> old_parentPopulation, List<Individual> childPopulation){
        List<Individual> mixedPopulation = new LinkedList<>();
        
        for(int i = 0; i < POPULATION_SIZE; i++){
            mixedPopulation.add(old_parentPopulation.get(i));
            mixedPopulation.add(childPopulation.get(i));
        }
        
        return mixedPopulation;
    }

    List<Individual> new_ParentPopulation(List<Individual> mixedPopulation){
        List<Individual> new_parentPopulation = new LinkedList<>();
        
        for (int i = 0; i < POPULATION_SIZE; i++) {
            try {
                Individual newBest = (Individual)select_bestIndividual(mixedPopulation).clone();
                new_parentPopulation.add(newBest);
            } catch (CloneNotSupportedException ex) {
                Logger.getLogger(GeneticAlgorithm.class.getName()).log(Level.SEVERE, null, ex);
            }
        }
        
        return new_parentPopulation;
    }

    /**
     * NOTE: REMOVES ELEMENTS FROM THE INPUT SET
     * Uses the check_optimality function to compare by pairs of individual in a population and determine the best one
     * @param pop
     * @return 
     */
    Individual select_bestIndividual(List<Individual> pop){
        Individual bestIndividual = null;
        try {
            int bestPos = 0;
            bestIndividual = (Individual)pop.get(bestPos).clone();
            for (int i = bestPos+1; i < pop.size(); i++) {
                int flag = check_optimality(bestIndividual, pop.get(i));
                if (flag == -1) {
                    bestIndividual = (Individual)pop.get(i).clone();
                    bestPos = i;
                }
            }
            pop.remove(bestPos);
        } catch (CloneNotSupportedException ex) {
            Logger.getLogger(GeneticAlgorithm.class.getName()).log(Level.SEVERE, null, ex);
        }
        
        return bestIndividual;
    }

//    /**
//     * Uses the check_optimality function to compare by pairs of individual in a population and determine the best one
//     * @param pop
//     * @return 
//     */
//    Individual select_bestIndividual(List<Individual> pop){
//        Individual bestIndividual = null;
//        try {
//            int i = 0;
//            while(pop.get(i)==null){//Look for the first individual that is not null
//                i++;
//            }
//            bestIndividual = pop.get(i);
//            pop.set(i, null);
//            for (; i < pop.size(); i++) {
//                if(pop.get(i)==null){
//                    continue;
//                }
//                int flag = check_optimality(bestIndividual, pop.get(i));
//                if (flag == -1) {
//                    bestIndividual = (Individual)pop.get(i).clone();
//                    pop.set(i, null);
//                }
//            }
//        } catch (CloneNotSupportedException ex) {
//            Logger.getLogger(GeneticAlgorithm.class.getName()).log(Level.SEVERE, null, ex);
//        }
//        
//        return bestIndividual;
//    }

    void calculateElementaryCriteria_impacts(Individual individual){
        int noSupportedProjects;
        int noDiscardededProjects;
        int[] supportedProjects;
        int[] discardedProjects;
        String[] sortingCriteria;
//        String[] sortingCriteria = assignments[0][0];
        int[] chromosome;
        
        chromosome = individual.getChromosome();
        noSupportedProjects = 0;
        for (int projectPos = 0; projectPos < chromosome.length; projectPos++) {
            if (chromosome[projectPos] == 1) {
                noSupportedProjects++;
            }
        }
        noDiscardededProjects = chromosome.length-noSupportedProjects;
        supportedProjects = new int[noSupportedProjects];
        discardedProjects = new int[noDiscardededProjects];
        sortingCriteria = PROJECTS_SET.getSortingCriteria();
        for (int projectPos = 0, supportedProj_pos = 0, discardedProj_pos = 0; projectPos < NO_DIMENSIONS; projectPos++) {
            if (chromosome[projectPos] == 1) {
                supportedProjects[supportedProj_pos++] = projectPos;
            }else{                
                discardedProjects[discardedProj_pos++] = projectPos;
            }
        }
        for (int elementCriterion_pos = 0; elementCriterion_pos < NO_ELEMENTARY_CRITERIA; elementCriterion_pos++) {
            int pos;
            String name = elementaryCriteria_names[elementCriterion_pos];
            /*Aggregation by sum*/
            if ((pos = Utilities.findFirstocurrence_firstColumn_returnRow(name, aggregationBySum)) > -1) {//This criterion is of the type "Aggregation by Sum"
                double[] sum = new double[2];
                String relatedCriterion = aggregationBySum[pos][1];
                int criterionPos = Utilities.findFirstocurrence_firstRow_returnColumn(relatedCriterion, projects);
                if(criterionPos == -1){
                    System.out.println("Error in the file 'Aggregation by Sum.tx': " + relatedCriterion);
                    System.exit(1);
                }
                for (int i = 0; i < noSupportedProjects; i++) {
                    int supportedProj_pos = supportedProjects[i];
                    sum[0] += PROJECTS_SET.getProjects().get(supportedProj_pos).getCrtieriaImpacts()[criterionPos][0];
                    sum[1] += PROJECTS_SET.getProjects().get(supportedProj_pos).getCrtieriaImpacts()[criterionPos][1];
                }
                int direction = Integer.parseInt(directions[1][elementCriterion_pos]);
                if(direction == -1)
                    individual.setElementaryCriteriaImpact(elementCriterion_pos, sum[1]*direction, sum[0]*direction);
                else
                    individual.setElementaryCriteriaImpact(elementCriterion_pos, sum[0], sum[1]);
            }
            /*Aggregation by count*/
            if ((pos = Utilities.findFirstocurrence_firstColumn_returnRow(name, aggregationByCount)) > -1) {//This criterion is of the type "Aggregation by Count"
                int count = 0;
                String relatedCriterion = aggregationByCount[pos][1];
                int criterionPos = Utilities.findFirstocurrence_firstRow_returnColumn(relatedCriterion, projects);
                if (criterionPos == -1) {
                    System.out.println("Error in the file 'Aggregation by Count.tx': " + relatedCriterion);
                    System.exit(1);
                }
                for (int i = 0; i < noSupportedProjects; i++) {
                    int supportedProj_pos = supportedProjects[i];
                    count += PROJECTS_SET.getProjects().get(supportedProj_pos).getCrtieriaImpacts()[criterionPos][1];
                }
                int direction = Integer.parseInt(directions[1][elementCriterion_pos]);
                individual.setElementaryCriteriaImpact(elementCriterion_pos, count*direction, count*direction);
            }
        }//Stop looking for all criteria
        
        /*Aggregation by sorting: how many of the supported projects are assigned to, at least, the desired class*/
        for(int i = 1; i < aggregationBySorting.length; i++){
            int count = 0;
            String sorting_projectCriterion = aggregationBySorting[i][1];
            int minimumClass = Integer.parseInt(aggregationBySorting[i][2]);
            int sortingPos = Utilities.findFirstocurrence(sorting_projectCriterion, sortingCriteria);
            for (int j = 0; j < noSupportedProjects; j++) {
                int supportedProj_pos = supportedProjects[j];
                String[][] assignedClasses = assignments[supportedProj_pos+1];
//                String[][] assignedClasses2 = PROJECTS_SET.getProjects().get(projectPos).getAssignedClasses();                
                int lower_assignedClass = Integer.parseInt(assignedClasses[sortingPos][0]);
                if (lower_assignedClass >= minimumClass) {
                    count++;
                }
            }
            String portfolioCriterion = aggregationBySorting[i][0];
            int portfolioCriterion_pos = Utilities.findFirstocurrence(portfolioCriterion, elementaryCriteria_names);
            int direction = Integer.parseInt(directions[1][portfolioCriterion_pos]);
            individual.setElementaryCriteriaImpact(portfolioCriterion_pos, count*direction, count*direction);
        }
        /*Aggregation by inconsistencies: how many supported projects are worse than a non-supported project*/
        for (int i = 1; i < aggregationByInconsistencies.length; i++) {//For each non-elementary criterion where the number of inconsistencies must be calculated
            int count = 0;
            String portfolioCriterion = aggregationByInconsistencies[i][0];//Portfolio level criterion
            String sorting_projectCriterion = aggregationByInconsistencies[i][1];//Project level criterion
            int sortingPos = Utilities.findFirstocurrence(sorting_projectCriterion, sortingCriteria);
            for (int j = 0; j < noSupportedProjects; j++) {
                int supportedProj_pos = supportedProjects[j];
                int lower_assignedClass = Integer.parseInt(assignments[supportedProj_pos+1][sortingPos][0]);//Header
                int higher_assignedClass = Integer.parseInt(assignments[supportedProj_pos+1][sortingPos][1]);//Header
                for (int k = 0; k < noDiscardededProjects; k++) {
                    int discardedProj_pos = discardedProjects[k];
                    int lower_discardedClass = Integer.parseInt(assignments[discardedProj_pos+1][sortingPos][0]);//Header
                    int higher_discardedClass = Integer.parseInt(assignments[discardedProj_pos+1][sortingPos][1]);//Header
                    if (Utilities.compareClasses(lower_assignedClass, higher_assignedClass, lower_discardedClass, higher_discardedClass) == 2) {
                        count++;
                        break;
                    }
                }
            }
            int portfolioCriterion_pos = Utilities.findFirstocurrence(portfolioCriterion, elementaryCriteria_names);//Position of the portfolio criterion in the elementary criteria array
            int direction = Integer.parseInt(directions[1][portfolioCriterion_pos]);
            individual.setElementaryCriteriaImpact(portfolioCriterion_pos, count*direction, count*direction);
        }
    }

    void assessElementaryCriteria(List<Individual> pop){
        Alternative[] alternatives = new Alternative[pop.size()];
        //Aggregating project impacts and calculating comformity objectives
        for (int i = 0; i < pop.size(); i++) {
            alternatives[i] = new Alternative(NO_ELEMENTARY_CRITERIA);
            calculateElementaryCriteria_impacts(pop.get(i));
            alternatives[i].setCrtieriaImpacts(pop.get(i).getElementaryCriteriaImpacts());
        }
        problem.setAlternatives(alternatives);
    }

    void assessFitness(List<Individual> pop, double[][] credibilityMatrix){
        //Calculating fitness value using Eq. (4.3) of P12: "An Overall Characterization of the Project Portfolio Optimization Problem and an Approach Based on Evolutionary Algorithms to Address It"
        for (int xPos = 0; xPos < POPULATION_SIZE; xPos++) {            
            double fitnessX = 1d;
            for (int yPos = 0; yPos < POPULATION_SIZE; yPos++) {
                if (xPos == yPos) {
                    continue;
                }
                double sigmaXY = credibilityMatrix[xPos][yPos];
                double comp_sigmaYX = 1d - credibilityMatrix[yPos][xPos];
                fitnessX = fitnessX * (Math.sqrt(sigmaXY * comp_sigmaYX));
            }
            fitnessX = Math.pow(fitnessX, 1.0 / (POPULATION_SIZE - 1));

            pop.get(xPos).setFitnessValue(fitnessX);
        }
    }

    void assessFitness_discardInfeasible(List<Individual> pop, double[][] credibilityMatrix){
        for (int xPos = 0; xPos < pop.size(); xPos++) {    
            pop.get(xPos).setFitnessValue(0d);        
            double fitnessX = 1d;
//            if(pop.get(xPos).getNoConstraints() > 0)
//                continue;
            for (int yPos = 0; yPos < pop.size(); yPos++) {
                if (pop.get(yPos).getNoConstraints() > 0) {
                    continue;
                }
                if (xPos == yPos) {
                    continue;
                }
                double sigmaXY = credibilityMatrix[xPos][yPos];
                double comp_sigmaYX = 1d - credibilityMatrix[yPos][xPos];
                fitnessX = fitnessX * (Math.sqrt(sigmaXY * comp_sigmaYX));
                if(fitnessX == 0)
                    fitnessX = fitnessX;
            }
            fitnessX = Math.pow(fitnessX, 1.0 / (pop.size() - 1));

            pop.get(xPos).setFitnessValue(fitnessX);
        }
    }

    void assessConstraints(List<Individual> pop){
        for(int i = 1; i < constraints.length; i++){//Remove header
            String criterionName = constraints[i][0];
            int criterionPos = Utilities.findCriterionPos(criterionName, elementaryCriteria_names);
            if(criterionPos == -1){
                System.out.println("Error assessing the constraints. Please check the Constraints file");
                System.exit(0);
            }
            String constraintType = constraints[i][1];//Not lower than, Not greater than, or Equal to
            double constraintValue = Double.parseDouble(constraints[i][2]);
            int impactDirection = Integer.parseInt(directions[1][criterionPos]);
            for (Individual individual : pop) {
                double score;
                switch (constraintType) {
                    case "Not lower than":
                        score = impactDirection < 1 ? individual.elementaryCriteriaImpacts[criterionPos][1] * impactDirection : individual.elementaryCriteriaImpacts[criterionPos][0];
                        if (score < constraintValue) {
                            individual.increaseNoConstraints();
                        }   break;
                    case "Not greater than":
                        score = impactDirection < 1 ? individual.elementaryCriteriaImpacts[criterionPos][0] * impactDirection : individual.elementaryCriteriaImpacts[criterionPos][1];
                        if (score > constraintValue) {
                            individual.increaseNoConstraints();
                        }   break;
                    default://Equal to
                        if ((individual.elementaryCriteriaImpacts[criterionPos][1] * impactDirection) != constraintValue) {
                            individual.increaseNoConstraints();
                        }   break;
                }
            }
        }
    }

    void assessConstraints_individual(Individual individual){
        double score = individual.elementaryCriteriaImpacts[11][0] * -1;
        if (score > 35000000) {
            individual.increaseNoConstraints();
        }
    }

    void printPopulation(List<Individual> pop){
        int i;
        for (i = 0; i < elementaryCriteria_names.length - 1; i++) {
            System.out.print(elementaryCriteria_names[i] + "\t");
        }
        System.out.println(elementaryCriteria_names[i]);
        for (i = 0; i < pop.size(); i++) {
            Individual individual = pop.get(i);
            int j;
            for (j = 0; j < individual.elementaryCriteriaImpacts.length - 1; j++) {
                double lowerImpact = individual.elementaryCriteriaImpacts[j][0];
                double higherImpact = individual.elementaryCriteriaImpacts[j][1];
                System.out.print(" " + Double.toString(lowerImpact) + "\t" + Double.toString(higherImpact) + "\t");
            }
            System.out.println(Double.toString(individual.elementaryCriteriaImpacts[j][0]) + "\t" + Double.toString(individual.elementaryCriteriaImpacts[j][1]));
        }
    }

    class Individual implements Cloneable {
        private double fitnessValue;
        private double noConstraintsViolated;
        private final int[] chromosome;
        private int noSupportedProjects;
        private double[][] elementaryCriteriaImpacts;

        public Individual() {
            noSupportedProjects = 0;
            chromosome = new int[NO_DIMENSIONS];
            elementaryCriteriaImpacts = new double[NO_ELEMENTARY_CRITERIA][2];
        }
        
        /**
         * Define if each  project will be supported (1) or not (0)
         */
        public void initialize_random(){     
            noSupportedProjects = ThreadLocalRandom.current().nextInt(1, NO_DIMENSIONS+1);//Upper bound is exclusive
            int[] projectPositions = new int[NO_DIMENSIONS];
            for(int i = 0; i < NO_DIMENSIONS; i++){
                projectPositions[i] = i;
            }
            for (int i = 0; i < noSupportedProjects; i++) {
                int randomPos = ThreadLocalRandom.current().nextInt(0, NO_DIMENSIONS);
                int temp = projectPositions[randomPos];
                projectPositions[randomPos] = projectPositions[i];
                projectPositions[i] = temp;
            }
            for (int i = 0; i < noSupportedProjects; i++) {
                chromosome[projectPositions[i]] = 1;
            }
        }
        
        /**
         * Use the input to initialize_random an individual
         */
        public void initialize_seed(String[] seed){     
            if(seed.length != NO_DIMENSIONS){
                System.out.println("Error initializing with seed. Length does not match the number of supported projects");
                System.exit(1);
            }
            for(int i = 0; i < NO_DIMENSIONS; i++){
                chromosome[i] = Integer.parseInt(seed[i]);
                if(chromosome[i] == 1){
                    noSupportedProjects++;
                }
            }            
        }

        public void setFitnessValue(double fitnessValue) {
            this.fitnessValue = fitnessValue;
        }

        public void setNoConstraints(double noConstraints) {
            this.noConstraintsViolated = noConstraints;
        }

        public void increaseNoConstraints(){
            noConstraintsViolated++;
        }

        public void increaseNoSupportedProjects(){
            noSupportedProjects++;
        }

        public void setElementaryCriteriaImpacts(double[][] elementaryCriteriaImpacts) {
            this.elementaryCriteriaImpacts = elementaryCriteriaImpacts;
        }

        public void setElementaryCriteriaImpact(int posCriterion, double lower_impact, double upper_impact) {
            this.elementaryCriteriaImpacts[posCriterion][0] = lower_impact;
            this.elementaryCriteriaImpacts[posCriterion][1] = upper_impact;
        }

        public double getFitnessValue() {
            return fitnessValue;
        }

        public double getNoConstraints() {
            return noConstraintsViolated;
        }

        public int[] getChromosome() {
            return chromosome;
        }

        public int getNoSupportedProjects() {
            return noSupportedProjects;
        }

        public double[][] getElementaryCriteriaImpacts() {
            return elementaryCriteriaImpacts;
        }

        @Override
        public String toString() {
            int i;
            String string = "";
            string += "Fitness:\t"+fitnessValue+"\n";
            string += "Constraint violations:\t"+noConstraintsViolated+"\n";
            string += "Portfolio:\t";
            for (i = 0; i < chromosome.length-1; i++) {
                string += Integer.toString(chromosome[i])+",";
            }
            string += Integer.toString(chromosome[i])+"\n";
//            string += "Elementary criteria:\t";
//            for(i = 0; i < elementaryCriteria_names.length-1; i++){
//                string += elementaryCriteria_names[i]+",";
//            }
//            string += elementaryCriteria_names[i]+"\t";
            string += "Elementary criteria impacts:\t";
            for(i = 0; i < elementaryCriteriaImpacts.length-1; i++){
                string += Double.toString(elementaryCriteriaImpacts[i][0]) + " " + Double.toString(elementaryCriteriaImpacts[i][1]) +",";
            }
            string += Double.toString(elementaryCriteriaImpacts[i][0]) + " " + Double.toString(elementaryCriteriaImpacts[i][1])+"\n";
            return string;
        }

        @Override
        protected Object clone() throws CloneNotSupportedException {
            return super.clone();
        }
    }

    public void launchGA_PortfolioLevel(Path path, String separator) {
        Path portfolioDir = path.resolve("Portfolio level");
        String readingPreferenceParameters[][] = Utilities.readFile(portfolioDir.resolve("Criteria parameters.txt"), separator);
        String readingWeights[][] = Utilities.readFile(portfolioDir.resolve("Weights.txt"), separator);
        String readingInteractions[][] = Utilities.readFile(portfolioDir.resolve("Criteria interactions.txt"), separator);
        String readingAditionals[][] = Utilities.readFile(portfolioDir.resolve("Additional criteria parameters.txt"), separator);
        String readingCardinalCriteria[][] = Utilities.readFile(portfolioDir.resolve("Use value function.txt"), separator);//Super criteria whose elemtary criteria are cardional, and where a function value will be used to assess its credibility degree
        String readingDescendantsWithVeto[][] = Utilities.readFile(portfolioDir.resolve("Veto thresholds for supercriteria.txt"), separator);//Super criteria whose elemtary criteria are cardional, and where a function value will be used to assess its credibility degree
        createProblem(readingPreferenceParameters, readingWeights, readingInteractions,
                readingAditionals, readingCardinalCriteria, readingDescendantsWithVeto);
    }

    /**
     * Creates a Problem specifically for portfolios
     * @param readingPerformanceMatrix
     * @param readingPreferenceParameters
     * @param readingWeights
     * @param readingInteractions
     * @param readingAditionals
     * @param readingCardinalCriteria
     * @param readingDescendantsWithVeto
     * @return 
     */
    public void createProblem(String readingPreferenceParameters[][], String readingWeights[][], String readingInteractions[][], 
            String readingAditionals[][], String readingCardinalCriteria[][], String readingDescendantsWithVeto[][]){

        int no_criteria = readingWeights.length;
        String[] criteriaNames = new String[no_criteria];
        double[] lambda = new double[2];
        Criterion[] criteria = new Criterion[no_criteria];

        /*Aditionals*/
        lambda[0] = Double.parseDouble(readingAditionals[0][0]);
        lambda[1] = Double.parseDouble(readingAditionals[0][1]);
        double beta = Double.parseDouble(readingAditionals[1][0]);
        int typeOfProblem = Integer.parseInt(readingAditionals[2][0]);

        /*ALL CRITERIA*/
        /*Creting all criteria*/
        for (int criterionPos = 0; criterionPos < no_criteria; criterionPos++) {
            criteria[criterionPos] = new Criterion();
            criteria[criterionPos].setName(readingWeights[criterionPos][0]);
            criteriaNames[criterionPos] = criteria[criterionPos].getName();
            String[] weightValues = readingWeights[criterionPos][1].split(" ");
            criteria[criterionPos].setWeight(Double.parseDouble(weightValues[0]), Double.parseDouble(weightValues[1]));
            if (Utilities.findFirstocurrence_returnRow(criteria[criterionPos].getName(), readingCardinalCriteria) > -1) {//Which criteria will aggregate descendant's scores using value function
                criteria[criterionPos].setUseValueFunction(true);
            }
            int pos;
            if ((pos = Utilities.findFirstocurrence_firstColumn_returnRow(criteria[criterionPos].getName(), readingDescendantsWithVeto)) > -1) {//Which descendant (not necessarily immediate) criteria will exert veto power
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
            if (interaction.equals("weak")) {
                criterion.weakeningInteraction.setInteraction(interactingCriterionName, interactionValue);
            }
            if (interaction.equals("str")) {
                criterion.strengtheningInteraction.setInteraction(interactingCriterionName, interactionValue);
            }
            if (interaction.equals("ant")) {
                criterion.antagonistInteraction.setInteraction(interactingCriterionName, interactionValue);
            }
        }

        /*ELEMENTARY CRITERIA*/
        /*Thresholds*/
        for (int elementCritPos = 0; elementCritPos < NO_ELEMENTARY_CRITERIA; elementCritPos++) {
            int criterionPos = Utilities.findFirstocurrence(elementaryCriteria_names[elementCritPos], criteriaNames);
            Criterion g = criteria[criterionPos];

            g.setIsElementary(true);
            g.setType(Utilities.INTERVAL_NUMBER);//Here, it is assumed that all criteria are interval
            String[] vetoValues = readingPreferenceParameters[5][elementCritPos].split(" ");
            try {
                g.setVeto(Double.parseDouble(vetoValues[0]), Double.parseDouble(vetoValues[1]));
            } catch (NumberFormatException e) {
                g.setVeto(0, 0);//The criterion does not have veto power
            }
        }

        problem = new Utilities().new Problem(null, null, criteria, NO_ELEMENTARY_CRITERIA,
                criteriaNames, null, elementaryCriteria_names, lambda, beta, typeOfProblem);//Alternatives are null at portfolio level because they will be portfolios; Profiles and Classes are null because there will not be sorting here
    }

    void savePopulation(List<Individual> pop, int noGeneration, Path path) {
        PrintWriter pw;
        File directory = new File(path.toString());
        if (!directory.exists()) {
            directory.mkdirs();
            // If you require it to make only the final directory,
            // use directory.mkdir(); here instead.
            // If you require it to make the entire directory path including parents,
            // use directory.mkdirs(); here instead.
        }
        
        String fileName = path.resolve("Populations.txt").toString();
        try {
            pw = new PrintWriter(new FileOutputStream(new File(fileName), true /* append = false */));
            pw.append("GENERATION "+noGeneration+":\n");
            for (int i = 0; i < POPULATION_SIZE; i++) {
                pw.append(pop.get(i).toString());
            }
            pw.append("\n");
            pw.flush();
            pw.close();
        } catch (FileNotFoundException ex) {
            Logger.getLogger(Utilities.class.getName()).log(Level.SEVERE, null, ex);
        }
    }    
}
