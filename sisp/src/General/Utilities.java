/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package General;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.FileReader;
import java.io.IOException;
import java.io.PrintWriter;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.LinkedList;
import java.util.List;
import java.util.concurrent.ThreadLocalRandom;
import java.util.logging.Level;
import java.util.logging.Logger;
import p11.hierarchicaloutranking.Alternative;
import p11.hierarchicaloutranking.Criterion;

/**
 *
 * @author efrai
 */
public class Utilities {
    public static final int REAL_NUMBER = 1;
    public static final int INTERVAL_NUMBER = 2;    
    public static final String ROOT_CRITERION = "g";//Common name of the root of criteria
    public static int SORTING_PROBLEM_nC = 1;
    public static int SORTING_PROBLEM_nB = 2;

    public static void main(String[]args){
//        String pathCredibilityMatrix = "G:\\Mi unidad\\Trabajo\\2020\\2020Enero-Junio\\Investigación\\Producción\\11. A hierarchical interval outranking -EJOR-\\"
//                + "Diseño del sistema\\Codigo\\P11.HierarchicalOutranking\\Files\\Project level\\Credibility matrices\\g-CredibilityMatrix-SupportedProject.txt";
        String pathCredibilityMatrix = "G:\\Mi unidad\\Trabajo\\2023\\2023Enero-Junio\\Investigación\\"
                + "29. Group decision with preference reinforcement, criteria interaction and force of discordant coalition\\Otros\\g-CredibilityMatrix-SupportedProject.txt";
        String[][] readingCredibilityMatrix = readFile(Paths.get(pathCredibilityMatrix), "\t");
        
        
//        String pathCredibilityMatrix = "G:\\Mi unidad\\Trabajo\\2020\\2020Enero-Junio\\Investigación\\Producción\\11. A hierarchical interval outranking -EJOR-\\"
//                + "Diseño del sistema\\Codigo\\P11.HierarchicalOutranking\\Files\\Project level\\Credibility matrices\\g-CredibilityMatrix-SupportedProject.txt";
//        String[][] readingCredibilityMatrix = readFile(pathCredibilityMatrix, "\t");
        netFlow(readingCredibilityMatrix, true, true);
        
        //String pathP13 = "G:\\Mi unidad\\Trabajo\\2020\\2020Julio-Diciembre\\Investigación\\"
        //        + "Producción\\13. R&D project portfolios (paper)\\Experimentos\\Files\\";
        //simulateProjects(pathP13);
        
        
//        String routeCredibility = "C:\\Users\\efrai\\Documents\\P9\\Clasificación.txt";
//        String[][] readingFile_credibility = readFile(routeCredibility, " ");
//        classifyObjects(readingFile_credibility);
                
//        String routeCredibility = "G:\\Mi unidad\\Trabajo\\2023\\2023Enero-Junio\\Investigación\\9. Turismo México\\Experimentación\\Resultados\\26-5-23\\ResultadosAG.txt";
//        String[][] readingFile_credibility = readFile(routeCredibility, " ");
//        fileToDot(readingFile_credibility);
                
        //String routeCredibility = "C:\\Users\\efrai\\OneDrive\\Trabajo\\2022\\Investigación\\25. Ordenamiento de ciudades capitales por seguridad\\Resultados\\7-4-22\\Credibility-g_0.txt";
        //String[][] readingFile_credibility = readFile(routeCredibility, " ");
        //String routeRanking = "C:\\Users\\efrai\\OneDrive\\Trabajo\\2022\\Investigación\\25. Ordenamiento de ciudades capitales por seguridad\\Resultados\\7-4-22\\RankingDestilacion-g_0.txt";
        //String[][] readingFile_ranking = readFile(routeRanking, " ");
        //countInconsCredMatrix_Ranking(readingFile_credibility, 0.59, readingFile_ranking);
    }

    public static String[][] readFile(Path path, String separator){
        String[][] data = null;

        FileReader fr;
        BufferedReader br;
        String line;
        File file;

        file = new File(path.toString());
        try {
            fr = new FileReader(file);
            br = new BufferedReader(fr);
            int count = 0;
            while ((br.readLine()) != null) {
                count++;
            }

            data = new String[count][];
            count = 0;
            fr = new FileReader(file);
            br = new BufferedReader(fr);
            while ((line = br.readLine()) != null) {
                data[count++] = line.split(separator);
            }
        } catch (NumberFormatException ex) {
            Logger.getLogger(Utilities.class.getName()).log(Level.SEVERE, "Problems with Number Format", ex);
        } catch (FileNotFoundException ex) {
            Logger.getLogger(Utilities.class.getName()).log(Level.SEVERE, "File Not Found: please provide the file "+path, ex);
        } catch (IOException ex) {
            Logger.getLogger(Utilities.class.getName()).log(Level.SEVERE, null, ex);
        }

        return data;
    }

    public static void saveFile(Path path, String[][] file, String separator){
        PrintWriter pw;
        try {
            pw = new PrintWriter(new FileOutputStream(new File(path.toString()), false /* append = false */));
            for (int i = 0; i < file.length; i++) {
                for (int j = 0; j < file[i].length; j++) {
                    pw.append(file[i][j]+separator);
                }
                pw.append("\n");
            }
            pw.flush();
            pw.close();
        } catch (FileNotFoundException ex) {
            Logger.getLogger(Utilities.class.getName()).log(Level.SEVERE, null, ex);
        }
        System.out.println("Sorting saved at " + path);
        
    }

    /**
     * Determines the line position where data ocurs for the first time in the first column of an array
     * @param data
     * @param matrix
     * @return -1 if data is not found
     */
    public static int findFirstocurrence(String data, String[] matrix){        
        for(int i = 0; i < matrix.length; i++){
            if (matrix[i].equals(data)) {
                return i;
            }
        }
        
        return -1;
    }

    /**
     * Determines the line position where data ocurs for the first time in ANY column of a matrix
     * @param data
     * @param matrix
     * @return -1 if data is not found
     */
    public static int findFirstocurrence_returnRow(String data, String[][] matrix){        
        for(int i = 0; i < matrix.length; i++){
            for (String item : matrix[i]) {
                if (item.equals(data)) {
                    return i;
                }
            }
        }
        
        return -1;
    }

    /**
     * Determines the line position where data ocurs for the first time in THE FIRST column of a matrix
     * @param data
     * @param matrix
     * @return -1 if data is not found
     */
    public static int findFirstocurrence_firstColumn_returnRow(String data, String[][] matrix){        
        for(int i = 0; i < matrix.length; i++){
            String item = matrix[i][0];
            if (item.equals(data)) {
                return i;
            }
        }
        
        return -1;
    }

    /**
     * Determines the line position where data ocurs for the first time in the first column of a matrix
     * @param data
     * @param matrix
     * @return -1 if data is not found
     */
    public static int findFirstocurrence_returnColumn(String data, String[][] matrix){        
        for (String[] row : matrix) {
            for (int j = 0; j < row.length; j++) {
                if (row[j].equals(data)) {
                    return j;
                }
            }
        }
        
        return -1;
    }

    /**
     * Determines the line position where data ocurs for the first time in the first column of a matrix
     * @param data
     * @param matrix
     * @return -1 if data is not found
     */
    public static int findFirstocurrence_firstRow_returnColumn(String data, String[][] matrix){
        for (int j = 0; j < matrix[0].length; j++) {
            if (matrix[0][j].equals(data)) {
                return j;
            }
        }
        
        return -1;
    }

    /**
     * Seeks criterion on the basis of the criteria's name
     * @param criterionName
     * @param criteria
     * @return 
     */
    public static Criterion findCriterion(String criterionName, Criterion[] criteria){
        for(Criterion criterion:criteria){
            if(criterion.getName().equals(criterionName)){
                return criterion;
            }
        }
        return new Criterion(criterionName);
    }

    public static int findCriterionPos(String criterionName, String[] criteriaNames) {
        int pos = -1;
        for (String name : criteriaNames) {
            pos++;
            if (name.equals(criterionName)) {
                return pos;
            }
        }

        return -1;
    }

    /**
     * Determines the maximum number in the SECOND POSITION of the input set
     * @param set
     * @return 
     */
    public static double findMax(double[][] set){
        double max = set[0][1];
        for (double[] set1 : set) {
            if (set1[1] > max) {
                max = set1[1];
            }
        }
        
        return max;
    }

    public static double P(double xmin, double xmax, double ymin, double ymax) {
        double D, p;

        if ((xmax == xmin) && (ymax == ymin)) {
            if (xmin >= ymin) {
                D = 1;
            } else {
                D = 0;
            }
        } else {
            D = (double) (xmax - ymin) / ((xmax - xmin) + (ymax - ymin));
        }

        // Cálculo de aPb
        if (D <= 0) {
            p = 0;
        } else {
            if (D >= 1) {
                p = 1;
            } else {
                p = D;
            }
        }
        return p;
    }

    /**
     * Orders a matrix on the basis of the second column
     * @param list
     * @param limite_izq
     * @param limite_der 
     */
    public static void quicksort(double list[][], int limite_izq, int limite_der) {
        int izq, der;
        double i, temporal, pivote;

        izq = limite_izq;
        der = limite_der;
        pivote = list[(izq + der) / 2][1];

        do {
            while (izq < limite_der && list[izq][1] > pivote) {
                izq++;
            }
            while (der > limite_izq && pivote > list[der][1]) {
                der--;
            }
            if (izq <= der) {
                i = list[izq][0];
                list[izq][0] = list[der][0];
                list[der][0] = i;

                temporal = list[izq][1];
                list[izq][1] = list[der][1];
                list[der][1] = temporal;

                izq++;
                der--;
            }

        } while (izq <= der);
        if (limite_izq < der) {
            quicksort(list, limite_izq, der);
        }
        if (limite_der > izq) {
            quicksort(list, izq, limite_der);
        }
    }

    /**
     * 
     * @param xmin
     * @param xmax
     * @param ymin
     * @param ymax
     * @return Posibilidad de que el número intervalo x sea al menos tan grande como el número intervalo y
     */
    public static double poss(double xmin, double xmax, double ymin, double ymax) {
        double D, p;

        if ((xmax == xmin) && (ymax == ymin)) {
            if(xmin >= ymin){
                D = 1;
            }else{
                D = 0;
            }
        } else {
            D = (double) (xmax - ymin) / ((xmax - xmin) + (ymax - ymin));
        }

        // Cálculo de aPb
        if (D <= 0) {
            p = 0;
        } else {
            if (D >= 1) {
                p = 1;
            } else {
                p = D;
            }
        }
        return p;
    }  

    /**
     * Compares the ranges of two classes following that Range1 (i, j) is not lower than Range2 (i', j') iff i >=i’ and j >=j’
     * @param low_range1
     * @param high_range1
     * @param low_range2
     * @param high_range2
     * @return 0: Weak inconsistency; 1: Strong inconsistency; -1: no inconsistency
     */
    public static int compareClasses_StrongWeakinconsistencies(int low_range1, int high_range1, int low_range2, int high_range2){
        int bestClass;
        if (low_range2 > high_range1){
            bestClass = 1;//Strong inconsistency
        }else if (low_range2 > low_range1 || high_range2 > high_range1){
            bestClass = 0;//Weak inconsistency
        }else{
            bestClass = -1;//No inconsistency
        }
        return bestClass;
    }

    /**
     * Adds a row to the input matrix
     * @param inputMatrix
     * @param row
     * @return 
     */
    public static String[][] addRow(String[][] inputMatrix, int[] row){
        String[][] outputMatrix = new String[inputMatrix.length+1][];
        
        for(int i = 0; i < inputMatrix.length; i++){
            outputMatrix[i] = new String[inputMatrix[0].length];
            for (int j = 0; j < inputMatrix[i].length; j++) {
                outputMatrix[i][j] = inputMatrix[i][j];
            }
        }
        outputMatrix[inputMatrix.length] = new String[row.length];
        for (int j = 0; j < row.length; j++) {
            outputMatrix[inputMatrix.length][j] = String.valueOf(row[j]);
        }
        
        return outputMatrix;
    }

    /**
     * Compares the ranges of two classes
     * @param low_range1
     * @param high_range1
     * @param low_range2
     * @param high_range2
     * @return 1: the first range is greater than the second one; 2: the second one is greater; 0: none is greater
     */
    public static int compareClasses(int low_range1, int high_range1, int low_range2, int high_range2){
        int bestClass = 0;
        if(low_range1 >= low_range2 && high_range1 >= high_range2){
            bestClass = 1;
        }else if(low_range2 >= low_range1 && high_range2 >= high_range1){
            bestClass = 2;
        }
        return bestClass;
    }

    public class Problem{
        private int NO_ALTERNATIVES;
        private final int no_criteria;
        private final int no_elementary_criteria;
        public String[] criteriaNames, elementaryCriteriaNames, classes;
        private final double[] lambda;
        private final double beta;
        private Alternative[] alternatives;
        private final Alternative[][] profiles;
        private final Criterion[] criteria;
        private final int typeOfProblem;
        
        /**
         * 
         * @param alternatives
         * @param profiles
         * @param criteria
         * @param no_elementary_criteria
         * @param criteriaNames
         * @param classes
         * @param elementaryCriteriaNames
         * @param lambda
         * @param beta
         * @param typeOfProblem 
         */
        public Problem(Alternative[] alternatives, Alternative[][] profiles, Criterion[] criteria, int no_elementary_criteria, 
                String[] criteriaNames, String[] classes, String[] elementaryCriteriaNames, double[] lambda, double beta, int typeOfProblem){
            this.NO_ALTERNATIVES = (alternatives == null ? 0 : alternatives.length);
            this.alternatives = alternatives;
            this.profiles = profiles;
            this.criteria = criteria;
            this.no_criteria = criteria.length;
            this.no_elementary_criteria = no_elementary_criteria;
            this.criteriaNames = criteriaNames;
            this.classes = classes;
            this.elementaryCriteriaNames = elementaryCriteriaNames;     
            this.lambda = lambda;
            this.beta = beta;
            this.typeOfProblem = typeOfProblem;
        }

//        public Problem(Alternative[][] profiles, int no_alternatives, int no_criteria, int no_elementary_criteria, int[] criteriaTypes, double[][] weights, 
//                double[] indiferenceThresholds, double[] preferenceThresholds, double[][] vetoes, 
//                String[] criteriaNames, String[] elementaryCriteriaNames, double[][][] performanceMatrix, double[] lambda, double beta, int typeOfProblem) {
//            this.profiles = profiles;
//            this.NO_ALTERNATIVES = no_alternatives;
//            alternatives = new Alternative[no_alternatives];
//            this.no_criteria = no_criteria;
//            this.no_elementary_criteria = no_criteria;
//            this.criteriaNames = criteriaNames;
//            this.elementaryCriteriaNames = elementaryCriteriaNames;
//            //this.criteriaTypes = criteriaTypes;
//            //this.weights = weights;
//            //this.indiferenceThresholds = indiferenceThresholds;
//            //this.preferenceThresholds = preferenceThresholds;
//            //this.vetoes = vetoes;
//            
//            for (int i = 0; i < no_alternatives; i++) {
//                alternatives[i] = new Alternative(no_criteria);
//                alternatives[i].setCrtieriaImpacts(performanceMatrix[i]);
//            }
//            this.criteria = null;  
//            this.lambda = lambda;
//            this.beta = beta;
//            this.typeOfProblem = typeOfProblem;
//        }

        public void setAlternatives(Alternative[] alternatives) {
            this.alternatives = alternatives;
            this.NO_ALTERNATIVES = alternatives.length;
        }
        public void setAlternatives(String[][] readingAlternatives) {
            for (int elementCritPos = 0; elementCritPos < no_elementary_criteria; elementCritPos++) {
                for (int alternativePos = 0; alternativePos < readingAlternatives.length; alternativePos++) {
                    String[] criterionValue = readingAlternatives[alternativePos + 1][elementCritPos].split(" ");
                    double lowestValue;
                    double highestValue;
                    if (criterionValue.length > 1) {
                        lowestValue = Double.parseDouble(criterionValue[0]);
                        highestValue = Double.parseDouble(criterionValue[1]);
                    } else {
                        lowestValue = highestValue = Double.parseDouble(criterionValue[0]);
                    }
                    if (lowestValue > highestValue) {
                        double temp = highestValue;
                        highestValue = lowestValue;
                        lowestValue = temp;
                    }
                    alternatives[alternativePos].setCrtieriaImpacts(elementCritPos, lowestValue, highestValue);
                }
            }
        }

        public int getNo_criteria() {
            return no_criteria;
        }

        public int getNo_elementary_criteria() {
            return no_elementary_criteria;
        }

        public int getNO_ALTERNATIVES() {
            return NO_ALTERNATIVES;
        }

        public double[] getLambda() {
            return lambda;
        }

        public double getBeta() {
            return beta;
        }

        public String[] getClasses() {
            return classes;
        }
        
//        public int[] getCriteriaTypes() {
//            return criteriaTypes;
//        }
//
//        public double[] getIndiferenceThresholds() {
//            return indiferenceThresholds;
//        }
//
//        public double[] getPreferenceThresholds() {
//            return preferenceThresholds;
//        }
//
//        public double[][] getWeights() {
//            return weights;
//        }
//
//        public double[][] getVetoes() {
//            return vetoes;
//        }

        public Alternative[] getAlternatives() {
            return alternatives;
        }    

        public Alternative[][] getProfiles() {
            return profiles;
        }    

        public Criterion[] getCriteria() {
            return criteria;
        }
    }

    public class Portfolio{
        private final List<Alternative> projects;
        private final String[] sortingCriteria;
        
        public Portfolio(String[][] readingProjects, String[][][] readingAssignments){
            projects = new LinkedList<>();
            int no_elementaryCriteria = readingProjects[0].length;
            for(int projectPos = 1; projectPos < readingProjects.length; projectPos++){
                Alternative alternative = new Alternative(no_elementaryCriteria);
                for (int elementCritPos = 0; elementCritPos < no_elementaryCriteria; elementCritPos++) {
                    double lowestValue, highestValue = 0d;
                    String[] criterionValue = readingProjects[projectPos][elementCritPos].split(" ");
                    lowestValue = Double.parseDouble(criterionValue[0]);
                    if(criterionValue.length>1)
                        highestValue = Double.parseDouble(criterionValue[1]);
                    if (lowestValue > highestValue) {
                        double temp = highestValue;
                        highestValue = lowestValue;
                        lowestValue = temp;
                    }
                    alternative.setElementaryCriteriaNames(readingProjects[0]);
                    alternative.setCrtieriaImpacts(elementCritPos, lowestValue, highestValue);
                }
                alternative.setAssignedClasses(readingAssignments[projectPos]);
                projects.add(alternative);
            }
            sortingCriteria = new String[readingAssignments[0].length];//Number of sorting criteria
            for(int i = 0; i < sortingCriteria.length; i++){
                sortingCriteria[i] = readingAssignments[0][i][0];
            }
        }

        public List<Alternative> getProjects() {
            return projects;
        }       

        public Alternative[] getProjects_array() {
            Alternative[] projects_array = new Alternative[projects.size()];
            for(int i = 0; i < projects.size(); i++){
                Alternative project = projects.get(i);
                projects_array[i] = project;
            }
            return projects_array;
        }       

        public String[] getSortingCriteria() {
            return sortingCriteria;
        }
    }

    /**
     * Finds the preference binary relations in a credibility matrix
     * @param readingFile credibility matrix
     * @param lambda cutting threshold to determine the crisp outranking
     */
    public static void findBinaryRelations(String readingFile[][], double lambda){
        int noRows = readingFile.length;
        int noColumns = readingFile[0].length;
        if(noRows != noColumns){
            System.out.println("The matrix is not squared");
            return;
        }
        double[][] sigmas = new double[noRows][noColumns];
        for (int row = 0; row < noRows; row++) {
            for (int column = 0; column < noColumns; column++) {
                sigmas[row][column] = Double.parseDouble(readingFile[row][column]);
            }
        }
        
        for (int row = 0; row < noRows; row++) {
            for (int column = 0; column < noColumns; column++) {
                if(sigmas[row][column] >= lambda && sigmas[column][row] >= lambda){
                    System.out.print("\tI");
                }else if(sigmas[row][column] >= lambda && sigmas[column][row] < lambda){
                    System.out.print("\tP");
                }else if(sigmas[row][column] < lambda && sigmas[column][row] >= lambda){
                    System.out.print("\tP-");
                }else if(sigmas[row][column] < lambda && sigmas[column][row] < lambda){
                    System.out.print("\tR");
                }
            }
            System.out.println();
        }
    }

    /**
     * Finds the preference binary relations in a credibility matrix
     * @param readingFile credibility matrix
     * @param lambda cutting threshold to determine the crisp outranking
     * @return 
     */
    public static String[][] returnBinaryRelations(String readingFile[][], double lambda){
        int noRows = readingFile.length;
        int noColumns = readingFile[0].length;
        if(noRows != noColumns){
            System.out.println("The matrix is not squared");
            return null;
        }
        String[][] matrix = new String[noRows][noColumns];
        double[][] sigmas = new double[noRows][noColumns];
        for (int row = 0; row < noRows; row++) {
            for (int column = 0; column < noColumns; column++) {
                sigmas[row][column] = Double.parseDouble(readingFile[row][column]);
            }
        }
        
        for (int row = 0; row < noRows; row++) {
            for (int column = 0; column < noColumns; column++) {
                if(sigmas[row][column] >= lambda && sigmas[column][row] >= lambda){
                    matrix[row][column] = "I";
                }else if(sigmas[row][column] >= lambda && sigmas[column][row] < lambda){
                    matrix[row][column] = "P";
                }else if(sigmas[row][column] < lambda && sigmas[column][row] >= lambda){
                    matrix[row][column] = "P-";
                }else if(sigmas[row][column] < lambda && sigmas[column][row] < lambda){
                    matrix[row][column] = "R";
                }
            }
            System.out.println();
        }
        
        return matrix;
    }

    /**
     * Counts the number of inconsistencies between i) the preferences of a credibility matrix with a given \lambda and ii) a ranking. NOTE: IT DOES NOT CONSIDER IMCOMPARABILITIES IN THE RANK
     * @param credibilityMatrixStringses
     * @param lambda
     * @param rankingStringses
     */
    public static void countInconsCredMatrix_Ranking(String credibilityMatrixStringses[][], double lambda, String rankingStringses[][]){
        int no_alternatives = credibilityMatrixStringses.length, no_inconsistencies = 0;
        int[] ranking = new int[no_alternatives];
        String[][] preferences = returnBinaryRelations(credibilityMatrixStringses, lambda);
        for (int i = 0; i < no_alternatives; i++) {
            ranking[Integer.parseInt(rankingStringses[i][0])-1] = Integer.parseInt(rankingStringses[i][1]);//Position 0 contains the alternative number, while position 1 contains the rank of the alternative
        }        
        for (int i = 0; i < no_alternatives; i++) {
            for (int j = 0; j < no_alternatives; j++) {
                if(ranking[i] < ranking[j] && !preferences[i][j].equals("P"))
                    no_inconsistencies++;
                else if(ranking[i] == ranking[j] && !preferences[i][j].equals("I"))
                    no_inconsistencies++;
                else if(ranking[i] > ranking[j] && !preferences[i][j].equals("P-"))
                    no_inconsistencies++;
            }
        }
        System.out.println("There are " + no_inconsistencies + " inconsistencies between the ranking and the binary preferences.");
    }

    public static double[][] StringMatrix_ToDouble(String[][] stringMatrix){
        int noRows = stringMatrix.length;
        int noColumns = stringMatrix[0].length;
        double[][] doubleMatrix = new double[noRows][noColumns];
        
        for(int i = 0; i < noRows; i++){
            for (int j = 0; j < noColumns; j++) {
                doubleMatrix[i][j] = Double.parseDouble(stringMatrix[i][j]);
            }
        }
        
        return doubleMatrix;
    }

    public static void fileToDot(String readingFile[][]){
        int noRows = readingFile.length;//-1 to discard header
        int noColumns = readingFile[0].length;//-1 to discard header
        System.out.println("digraph G {");
        for(int row = 0; row < noRows; row++){
            for (int column = 0; column < noColumns; column++) {
                int res = Integer.parseInt(readingFile[row][column]);
                if(row == 12 || row == 28 || column == 12 || column == 28)//IGNORING ALTERNATIVES A13 AND A29
                    continue;
                if(res==1){
                    System.out.println("C"+(row+1)+" -> C"+(column+1)+";");
                }
            }
            System.out.println();
        }
        System.out.print("}");
    }

    /**
     * Uses the position of an object to assign to a given class
     * @param readingFile 
     */
    public static void classifyObjects(String readingFile[][]){
        int noColumns = readingFile[1].length;
        int noClasses = Integer.parseInt(readingFile[0][0]);
        ArrayList<ArrayList> classes = new ArrayList<>();
        for(int thisClass = 0; thisClass < noClasses; thisClass++){
            ArrayList<Integer> class1 = new ArrayList<>();
            for (int column = 0; column < noColumns; column++) {
                int res = Integer.parseInt(readingFile[1][column]);
                if(res == thisClass)
                    class1.add(column);
            }
            classes.add(class1);
        }
        for (int thisClass = 0; thisClass < noClasses; thisClass++) {
            System.out.println(thisClass+1);
            ArrayList<Integer> class1 = classes.get(thisClass);
            class1.forEach((class11) -> {
                System.out.print((class11 + 1) + " ");
            });
            System.out.println("\n");
        }
    }

    /**
     * Converts a matrix of binary relations (P, P-, I, R) from a partial preorder to a matrix of zeros and ones between classes
     * All indifferent alternatives are within the same class
     * @param readingFile 
     * @param savingRoute 
     */
    public static void binaryRelTo_0_1(String readingFile[][], String savingRoute){
        int noRows = readingFile.length;//-1 to discard header
        int noColumns = readingFile[0].length;//-1 to discard header
        //ArrayList<ArrayList> classes = new ArrayList<>();
        //ArrayList<Integer> class1 = new ArrayList<>();
        //Finding classes
        //class1.add(0);
        //classes.add(class1);
        boolean added = true;
        for(int row = 0; row < noRows; row++){
            for (int column = 0; column < noColumns; column++) {
                if(row == column)
                    continue;
                String relation = readingFile[row][column];
                if(relation.equals("I")){
                    System.out.println("C"+(row+1)+" -> C"+(column+1)+";");
                }
            }
            if(!added){
                
            }
            added = false;
        }
    }

    public static void netFlow(String credibilityMatrix[][], boolean hasHeader_row, boolean hasHeader_column){
        int initialPos_row = (hasHeader_row == true ? 1 : 0);
        int initialPos_column = (hasHeader_column == true ? 1 : 0);
        int noAlternatives = (hasHeader_row == true ? credibilityMatrix.length-1 : credibilityMatrix.length);
        double[] netFlow = new double[noAlternatives];
        for(int i = initialPos_row; i < (noAlternatives+initialPos_row); i++){
            for (int j = initialPos_column; j < (noAlternatives+initialPos_column); j++) {
                if(i!=j){
                    netFlow[i-initialPos_row] += Double.parseDouble(credibilityMatrix[i][j]) - Double.parseDouble(credibilityMatrix[j][i]);
                }
            }
        }
        for (int i = 0; i < noAlternatives; i++) {
            System.out.println((i+1) + "\t" + netFlow[i]);
        }
    }

    public static void toXMCDA_credibilityMatrix(String IDs[], String credibilityMatrix[][]){
        int noAlternatives = credibilityMatrix.length;
        if (noAlternatives != credibilityMatrix[0].length) {
            System.out.println("The matrix is not squared");
            System.exit(1);
        }
        if (noAlternatives != IDs.length) {
            System.out.println("The arrays do not contain the correct number of alternatives");
            System.exit(1);
        }
        System.out.println("<ns0:XMCDA xmlns:ns0=\"http://www.decision-deck.org/2009/XMCDA-2.1.0\">\n" +
                            "  <alternativesComparisons>\n" +
                            "    <pairs>");
        for (int i = 0; i < noAlternatives; i++) {
            for (int j = 0; j < noAlternatives; j++) {
                System.out.println("      <pair>\n" +
                            "        <initial>\n" +
                            "          <alternativeID>" + IDs[i] + "</alternativeID>\n" +
                            "        </initial>\n" +
                            "        <terminal>\n" +
                            "          <alternativeID>" + IDs[j] + "</alternativeID>\n" +
                            "        </terminal>\n" +
                            "        <value>\n" +
                            "          <real>" + Double.parseDouble(credibilityMatrix[i][j]) + "</real>\n" +
                            "        </value>\n" +
                            "      </pair>");
            }
        }
        System.out.println("    </pairs>\n" +
                            "  </alternativesComparisons>\n" +
                            "</ns0:XMCDA>");
    }

    /**
     * Generation of the projects that will be used in P13
     * @param path to the files
     */
    public static void simulateProjects(Path path){
        double maxDeviation = 0.1;//Maximum deviation of a criterion to its upper boundary with respect to a correlated criterion's
        int no_alternatives = 1000;
        Path simprojDir = path.resolve("Project level").resolve("Simulating projects");
        String[][] criteriaCorrelation_file = readFile(simprojDir.resolve("Criteria correlation.txt"), "\t");
        String[][] criteriaDomains_file = readFile(simprojDir.resolve("Criteria Domains.txt"), "\t");
        String[][] intervalCriteria_file = readFile(simprojDir.resolve("Interval Criteria.txt"), "\t");
        int no_criteria = criteriaDomains_file.length;
        int[][] precise_performanceMatrix = new int[no_alternatives][no_criteria];//Contains precise performances 
        int[][][] interval_performanceMatrix = new int[no_alternatives][no_criteria][2];//Contains performances as interval numbers
        
        //Printing criteria names
        for(int criterionPos = 0; criterionPos < no_criteria-1; criterionPos++){
            System.out.print(criteriaDomains_file[criterionPos][0]+"\t");
            
        }
        System.out.println(criteriaDomains_file[no_criteria-1][0]);//To avoid last "\t"
        
        //Initialize performance matrix
        for (int[] alternative : precise_performanceMatrix) {
            for (int criterionPos=0; criterionPos < no_criteria; criterionPos++) {
                alternative[criterionPos] = -1;//Flag to indicate that the criterion has not received a score
            }
        }
        
        for(int alternativePos = 0; alternativePos < no_alternatives; alternativePos++){            
            //Fill the alternative's performance using lower and upper bounds of all criteria
            //System.out.println("Original criteria scores");
            for (int criterionPos = 0; criterionPos < no_criteria; criterionPos++) {
                int minValue = Integer.parseInt(criteriaDomains_file[criterionPos][1]);
                int maxValue = Integer.parseInt(criteriaDomains_file[criterionPos][2]);
                int random_value = ThreadLocalRandom.current().nextInt(minValue, maxValue + 1);
                precise_performanceMatrix[alternativePos][criterionPos] = random_value;
                //System.out.print(precise_performanceMatrix[alternativePos][criterionPos]+"\t");
            }
            //System.out.println("\n");
            
            /*Ensure that correlated criteria are proportionally similar to their respective upper bounds*/
            for (int i = 1; i < criteriaCorrelation_file.length; i++) {
                String[] correlation_row = criteriaCorrelation_file[i];
                String leadingCriterion = correlation_row[0];
                int posLeadingCriterion = findFirstocurrence_returnRow(leadingCriterion, criteriaDomains_file);//Position of leading criterion
                int leadingValue = precise_performanceMatrix[alternativePos][posLeadingCriterion];
                int maxLeadingValue = Integer.parseInt(criteriaDomains_file[posLeadingCriterion][2]);
                double leadingProportion = leadingValue/(double)maxLeadingValue;
                String correlatedCriterion = correlation_row[1];
                int posCorrelatedCriterion = findFirstocurrence_returnRow(correlatedCriterion, criteriaDomains_file);//Position of leading criterion
                int minCorrelatedValue = Integer.parseInt(criteriaDomains_file[posCorrelatedCriterion][1]);
                int maxCorrelatedValue = Integer.parseInt(criteriaDomains_file[posCorrelatedCriterion][2]);
                double deviation = ThreadLocalRandom.current().nextDouble(maxDeviation*2)-maxDeviation;//Randomly generate positive or negative deviation
                int newValue = (int)Math.round(maxCorrelatedValue*(leadingProportion+deviation));
                newValue = (newValue < minCorrelatedValue ? minCorrelatedValue : newValue);
                newValue = (newValue > maxCorrelatedValue ? maxCorrelatedValue : newValue);
                precise_performanceMatrix[alternativePos][posCorrelatedCriterion] = newValue;//Fulfill constraints                
            }
            
            /*When ALL the impacts in the scientific criteria involve a proportion greater than 50% of their respective higher values, 
            the economic impacts must be below 50% of theirs.*/
            boolean modify_scientificCriteria = true;
            boolean modify_economicCriteria = true;
            //Assessing economic criteria
            for (int criterionPos = 0; modify_scientificCriteria && criterionPos < no_criteria; criterionPos++) {
                String criterionName = criteriaDomains_file[criterionPos][0];
                if(criterionName.contains("g11")){//Economic impact
                    int maxValue = Integer.parseInt(criteriaDomains_file[criterionPos][2]);
                    int value = precise_performanceMatrix[alternativePos][criterionPos];
                    double proportion = value/(double)maxValue;
                    if(proportion < 0.9)//If there are economic criteria with no so-high scores, then it is not necessary to modify the scientific criteria
                        modify_scientificCriteria = false;
                }
            }
            //Assessing scientific criteria
            for (int criterionPos = 0; modify_economicCriteria && criterionPos < no_criteria; criterionPos++) {
                String criterionName = criteriaDomains_file[criterionPos][0];
                if(criterionName.contains("g12")){//Scientific impact
                    int maxValue = Integer.parseInt(criteriaDomains_file[criterionPos][2]);
                    int value = precise_performanceMatrix[alternativePos][criterionPos];
                    double proportion = value/(double)maxValue;
                    if(proportion < 0.9)
                        modify_economicCriteria = false;
                }
            }
            if(modify_scientificCriteria){//If all economic criteria have high scores
                for (int criterionPos = 0; criterionPos < no_criteria; criterionPos++) {
                    String criterionName = criteriaDomains_file[criterionPos][0];
                    if (criterionName.contains("g12")) {//Scientific impact
                        int minValue = Integer.parseInt(criteriaDomains_file[criterionPos][1]);
                        int maxValue = (int) 0.2*Integer.parseInt(criteriaDomains_file[criterionPos][2]);//Adjusted to be no more than 20% the maximum value
                        maxValue = (maxValue < minValue ? minValue : maxValue);
                        int random_value = ThreadLocalRandom.current().nextInt(minValue, maxValue + 1);
                        precise_performanceMatrix[alternativePos][criterionPos] = random_value;
                    }
                }
            }else if (modify_economicCriteria){//If all scientific criteria have high scores
                for (int criterionPos = 0; criterionPos < no_criteria; criterionPos++) {
                    String criterionName = criteriaDomains_file[criterionPos][0];
                    if (criterionName.contains("g11")) {//Economic impact
                        int minValue = Integer.parseInt(criteriaDomains_file[criterionPos][1]);
                        int maxValue = (int) 0.2 * Integer.parseInt(criteriaDomains_file[criterionPos][2]);//Adjusted to be no more than 20% the maximum value
                        maxValue = (maxValue < minValue ? minValue : maxValue);
                        int random_value = ThreadLocalRandom.current().nextInt(minValue, maxValue + 1);
                        precise_performanceMatrix[alternativePos][criterionPos] = random_value;
                    }
                }
            }
            
            //Finding interval criteria
            for (int criterionPos = 0; criterionPos < no_criteria; criterionPos++) {
                String criterionName = criteriaDomains_file[criterionPos][0];
                int value = precise_performanceMatrix[alternativePos][criterionPos];
                int foundValue = findFirstocurrence_returnRow(criterionName, intervalCriteria_file);
                if (foundValue>-1) {//Is this criterion an interval number?               
                    interval_performanceMatrix[alternativePos][criterionPos][0] = (int) (0.8 * (double) value);
                    interval_performanceMatrix[alternativePos][criterionPos][1] = (int) (1.2 * (double) value); 
                    System.out.print(interval_performanceMatrix[alternativePos][criterionPos][0] + " " + 
                            interval_performanceMatrix[alternativePos][criterionPos][1] + "\t");
                }else{
                    interval_performanceMatrix[alternativePos][criterionPos][0] = value;
                    interval_performanceMatrix[alternativePos][criterionPos][1] = value;
                    System.out.print(value + "\t");
                }
            }
            System.out.println();
        }
    }

    /**
     * Value function (normalized interval weighted sum) to calculate the credibility degree of the assertion “x is at least as good as y” 
     * @param x Impacts of alternative x
     * @param y Impacts of alternative y
     * @param criteria
     * @param elementaryCriteriaNames
     * @return 
     */
    public static double uModel(Alternative x, Alternative y, Criterion[] criteria, String[] elementaryCriteriaNames){
        int noCriteria = criteria.length;
        double maxVeto = 0d;
        double[] Ux = new double[2];
        double[] Uy = new double[2];
        double[][] xScores = new double[noCriteria][2];
        double[][] yScores = new double[noCriteria][2];
        double[][] w = new double[noCriteria][2];
        double[][] v = new double[noCriteria][2];
        double[][] fmin = new double[noCriteria][2];
        double[][] fmax = new double[noCriteria][2];

        for(int i = 0; i < noCriteria; i++){
            int posElementaryCriterion = Utilities.findCriterionPos(criteria[i].getName(), elementaryCriteriaNames);
            xScores[i] = x.getCrtieriaImpacts()[posElementaryCriterion];
            yScores[i] = y.getCrtieriaImpacts()[posElementaryCriterion];
            w[i] = criteria[i].getWeight();
            v[i] = criteria[i].getVeto();
            fmin[i] = criteria[i].getMinScore();
            fmax[i] = criteria[i].getMaxScore();
        }

        //Calculate Ux, Uy and the maximum possibility of veto
        for(int criterionPos = 0; criterionPos < noCriteria; criterionPos++){
            Ux[0] += w[criterionPos][0]*((xScores[criterionPos][0]-fmin[criterionPos][1])/(fmax[criterionPos][1]-fmin[criterionPos][0]));//Interval theory dictates that the denominator must be the smallest in the lower bound
            Ux[1] += w[criterionPos][1]*((xScores[criterionPos][1]-fmin[criterionPos][0])/(fmax[criterionPos][0]-fmin[criterionPos][1]));
            Uy[0] += w[criterionPos][0]*((yScores[criterionPos][0]-fmin[criterionPos][1])/(fmax[criterionPos][1]-fmin[criterionPos][0]));
            Uy[1] += w[criterionPos][1]*((yScores[criterionPos][1]-fmin[criterionPos][0])/(fmax[criterionPos][0]-fmin[criterionPos][1]));
            
            if(v[criterionPos][1] != 0d){//If the criterion has veto power
                double[] diff = new double[2];
                diff[0] = yScores[criterionPos][0] - xScores[criterionPos][1];
                diff[1] = yScores[criterionPos][1] - xScores[criterionPos][0];
                double veto = poss(diff[0], diff[1], v[criterionPos][0], v[criterionPos][1]);
                if(maxVeto < veto){
                    maxVeto = veto;
                }
            }
        }
        double possXY = poss(Ux[0], Ux[1], Uy[0], Uy[1]);
        double compVeto = 1d - maxVeto;

        double sigma = (possXY < compVeto ? possXY : compVeto);
        return sigma;
    }
}
