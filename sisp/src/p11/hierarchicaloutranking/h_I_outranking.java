/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package p11.hierarchicaloutranking;

import General.Utilities;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.PrintWriter;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 *
 * @author efrai
 */
public class h_I_outranking {
    static Utilities.Problem problem;
    final static int aPb = 2, aQb = 3, aIb = 4, bPa = 5, bQa = 6, aRb = 7, aKb = 8, bKa = 9;

    public static String[][] sortAlternatives_HI_Interclass_nC(Utilities.Problem problem, Path path, String criterionName_sorting) {
        h_I_outranking.problem = problem;

        Criterion g = determineHierarchicalStructureUnder_gr(criterionName_sorting, path.resolve("Criteria hierarchy.txt"));//Utilities.ROOT_CRITERION
        Alternative[] alternatives = problem.getAlternatives();
        Alternative[][] profiles = problem.getProfiles();
        String[] classes = problem.getClasses();
        String[][] sorting = new String[alternatives.length][2];

        for (int i = 0; i < alternatives.length; i++) {
            Alternative x = alternatives[i];
            sorting[i] = HI_Interclass_nC(x, profiles, g, classes, criterionName_sorting);
        }
        return sorting;
    }

    public static String[] HI_Interclass_nC(Alternative x, Alternative[][] profiles, Criterion g, String[] classes, String criterionName_sorting) {
        int noClasses, descendantClass, ascendantClass, assigned;
        noClasses = classes.length;
        String[] sorting = new String[2];

        assigned = descendantClass = ascendantClass = -1;

        //Descending rule
        for (int classPos = (noClasses - 1); classPos >= 0 && assigned == -1; classPos--) {
            if (outranks_SetOfProfiles(x, profiles[classPos], g)) {
                if (classPos == (noClasses - 1)) {//k = M
                    descendantClass = classPos;
                    assigned = 0;
                } else {//0 < k < M
                    double fsK = selectionFunction(x, profiles[classPos], g);
                    double fsPlus1 = selectionFunction(x, profiles[classPos+1], g);
                    if (fsK >= fsPlus1) {
                        descendantClass = classPos;
                        assigned = 0;
                    } else {
                        descendantClass = classPos + 1;
                        assigned = 0;
                    }
                }
            }
        }
        if (assigned == -1) {//k = 0. Nota: el arrego <perfiles> no contiene la solución anti-ideal
            descendantClass = 0;
        }
        assigned = -1;
        //Ascending rule
        for (int classPos = 0; classPos < noClasses && assigned == -1; classPos++) {
            if (outranks_SetOfProfiles(profiles[classPos], x, g)) {
                if (classPos == 0) {//k = 1
                    ascendantClass = classPos;
                    assigned = 0;
                } else {//1 < k < M+1
                    double fsK = selectionFunction(x, profiles[classPos], g);
                    double fsKminus1 = selectionFunction(x, profiles[classPos - 1], g);
                    if (fsK >= fsKminus1) {
                        ascendantClass = classPos;
                        assigned = 0;
                    } else {
                        ascendantClass = classPos - 1;
                        assigned = 0;
                    }
                }
            }
        }
        if (assigned == -1) {//k = M+1. Nota: el arrego <perfiles> no contiene la solución anti-ideal
            ascendantClass = (noClasses - 1);
        }
        if (descendantClass > ascendantClass) {
            int temp = ascendantClass;
            ascendantClass = descendantClass;
            descendantClass = temp;
        }

        sorting[0] = classes[descendantClass]+"("+descendantClass+")";
        sorting[1] = classes[ascendantClass]+"("+ascendantClass+")";
        
        return sorting;
    }
    
    public static double[][] calculateCredibilityMatrix(Utilities.Problem problem, Path path, String criterion) {
        h_I_outranking.problem = problem;
        
        Criterion g = determineHierarchicalStructureUnder_gr(criterion, path.resolve("Criteria hierarchy.txt"));//Utilities.ROOT_CRITERION        
        Alternative[] alternatives = problem.getAlternatives();
        int noAlternatives = alternatives.length;
        double[][] credibilityMatrix = new double[noAlternatives][noAlternatives];
        
        int posX = 0, i = 0;
        for(Alternative x: alternatives){
            int posY = 0;
            for (Alternative y: alternatives) {
                if(x==y){
                    credibilityMatrix[posX][posY] = 1d;
                }else if(isDominance(y, x, g)){
                    credibilityMatrix[posX][posY] = 0d;//If y dominates x, the credibility of xSy = 0
                }else{
                    double sigma = sigmaValue(x, y, g);
                    credibilityMatrix[posX][posY] = sigma;
                }
                posY++;
            }
            posX++;
        }
        printCredibilityMatrix(credibilityMatrix);
//        saveCredibilityMatrix(credibilityMatrix, criterion, path);
        return credibilityMatrix;
    }
    
    /**
     * Does x dominates y?
     * @param x
     * @param y
     * @param g
     * @return 
     */
    static boolean isDominance(Alternative x, Alternative y, Criterion g){
        Criterion[] elementaryDescendants = g.getElementaryCriteria();
        for (Criterion elementaryDescendant : elementaryDescendants) {
            int posElementaryDescendant = Utilities.findCriterionPos(elementaryDescendant.getName(), problem.elementaryCriteriaNames);
            double[] gX = x.getCrtieriaImpacts()[posElementaryDescendant];
            double[] gY = y.getCrtieriaImpacts()[posElementaryDescendant];
            if (elementaryDescendant.getType() == Utilities.REAL_NUMBER) {
                if(gX[0] < gY[0]){
                    return false;
                }
            } else {//Interval number
                double poss = Utilities.poss(gX[0], gX[1], gY[0], gY[1]);
                if (poss < 0.5) {
                    return false;
                }
            }
        }
        
        return true;
    }
    
    static double sigmaValue(Alternative x, Alternative y, Criterion g){
        double sigma = 0d;
        if (g.getIsElementary()) {
            int posElementaryCriterion = Utilities.findCriterionPos(g.getName(), problem.elementaryCriteriaNames);
            double[] gX = x.getCrtieriaImpacts()[posElementaryCriterion];
            double[] gY = y.getCrtieriaImpacts()[posElementaryCriterion];
            if (g.getType() == Utilities.REAL_NUMBER) {
                double pj = g.getPreferenceThreshold();
                double qj = g.getIndifferenceThreshold();
                sigma = delta_j_Real(gX[0], gY[0], pj, qj);
            } else {
                sigma = delta_j_Interval(gX[0], gX[1], gY[0], gY[1]);
            }
            return sigma;            
        }else{
            if(g.getUseValueFunction()){//Determine this criterion's credibility degree through a value function
                sigma = Utilities.uModel(x, y, g.getElementaryCriteria(), problem.elementaryCriteriaNames);
            }else{//Outranking
                for (int posChild = 0; posChild < g.getNo_children(); posChild++) {
                    Criterion subcriterion = g.getChildren()[posChild];
                    double sigmaChild = sigmaValue(x, y, subcriterion);
                    g.setGamma(posChild, sigmaChild, posChild);
                }
                double maxGamma = Utilities.findMax(g.getGamma());
                if (maxGamma == 0) {
                    return 0d;
                }
                Utilities.quicksort(g.getGamma(), 0, g.getNo_children() - 1);//Orders the subcriterion's Gamma set on the basis of the gamma values
                double[] results = new double[g.getNo_children()];
                for (int i = 0; i < g.getNo_children(); i++) {
                    results[i] = marginalCredibility(g, x, y, i, problem.getLambda());
                }
                for (int i = 0; i < g.getNo_children(); i++) {
                    if (sigma < results[i]) {
                        sigma = results[i];
                    }
                }
            }
            return sigma;
        }
    }
    
    /**
     * Determines if an alternative x outranks a set of profiles. In that order
     * @param x
     * @param profiles
     * @param g
     * @param lambda
     * @return 
     */
    static boolean outranks_SetOfProfiles(Alternative x, Alternative[] profiles, Criterion g) {
        boolean conditionFulfilled = true, atLeastOneRank = false;

        for (Alternative profile : profiles) {
            double xSigmaProfile = sigmaValue(x, profile, g);
            double profileSigmaX = sigmaValue(profile, x, g);            
            int prefRelation = preferenceRelation(problem, xSigmaProfile, profileSigmaX);
            if (!(prefRelation == aRb || prefRelation == aPb || prefRelation == aQb || prefRelation == aIb)) {
                conditionFulfilled = false;
                break;
            }
            if (prefRelation == aPb || prefRelation == aQb || prefRelation == aIb) {
                atLeastOneRank = true;
            }
        }
        return conditionFulfilled && atLeastOneRank;
    }
        
    /**
     * Determines if a set of profiles outranks an alternative x. In that order
     * @param x
     * @param profiles
     * @param g
     * @param lambda
     * @return 
     */
    static boolean outranks_SetOfProfiles(Alternative[] profiles, Alternative x, Criterion g) {
        for (Alternative profile : profiles) {
            double profileSigmaX = sigmaValue(profile, x, g);
            double xSigmaProfile = sigmaValue(x, profile, g);
            int prefRelation = preferenceRelation(problem, profileSigmaX, xSigmaProfile);
            if (prefRelation == aPb || prefRelation == aQb || prefRelation == aIb) {
                return true;
            }
        }
        return false;
    }

    static double selectionFunction(Alternative x, Alternative[] profiles, Criterion g) {
        double maxCredibility, xSigmaProfiles, profilesSigmaX;

        xSigmaProfiles = outrankCredibility_SetOfProfiles(x, profiles, g);
        profilesSigmaX = outrankCredibility_SetOfProfiles(profiles, x, g);

        if (xSigmaProfiles < profilesSigmaX) {
            maxCredibility = xSigmaProfiles;
        } else {
            maxCredibility = profilesSigmaX;
        }
        return maxCredibility;
    }

    /**
     * Calculates the outrank credibility between an alternative and a set of profiles. In thar order
     * @param x
     * @param profiles
     * @param g
     * @param lambda
     * @return 
     */
    static double outrankCredibility_SetOfProfiles(Alternative x, Alternative[] profiles, Criterion g) {
        double maxCredibility = 0d;
        for (Alternative profile : profiles) {
            double sigma = sigmaValue(x, profile, g);
            if (maxCredibility < sigma) {
                maxCredibility = sigma;
            }
        }
        return maxCredibility;
    }

    /**
     * Calculates the outrank credibility between a set of profiles and an alternative. In thar order
     * @param perfilesEnClase
     * @param x
     * @param DM
     * @param cantCriterios
     * @param criteriaTypes
     * @param vetoesTypes
     * @return 
     */
    static double outrankCredibility_SetOfProfiles(Alternative[] profiles, Alternative x, Criterion g) {
        double maxCredibility = 0d;
        for (Alternative profile : profiles) {
            double sigma = sigmaValue(profile, x, g);
            if (maxCredibility < sigma) {
                maxCredibility = sigma;
            }
        }
        return maxCredibility;
    }
    
    static int preferenceRelation(Utilities.Problem problem, double Bab, double Bba) {
        int relation;
        
        relation = -1;

        if (Bab >= problem.getBeta() && Bba < 0.5){
            relation = aPb;
        } else if (Bab >= problem.getBeta() && Bba >= problem.getBeta()) {
            relation = aIb;
        } else if (Bab >= problem.getBeta() && Bab > Bba) {
            relation = aQb;
        } else if (Bab >= 0.5 && Bab < problem.getBeta() && Bba <= 0.5) {
            relation = aKb;
        } else if (Bba >= problem.getBeta() && Bab < 0.5){
            relation = bPa;
        } else if (Bba >= problem.getBeta() && Bba > Bab) {
            relation = bQa;
        } else if (Bba >= 0.5 && Bba < problem.getBeta() && Bab <= 0.5) {
            relation = bKa;
        } else if (Bba < 0.5 && Bab < 0.5) {
            relation = aRb;
        }
        return relation;
    }
    
    static Criterion determineHierarchicalStructureUnder_gr(String name_gr, Path path){
        int posGr;
        Criterion rootCriterion = new Criterion();
        
        String[][] data = Utilities.readFile(path, "=");
        if(name_gr.equals("")){
            posGr = 0;
        }else{
            posGr = Utilities.findFirstocurrence_returnRow(name_gr, data);
            if (posGr == -1) {
                System.out.println("The required criterion was not found in the file");
                System.exit(1);
            }
        }        
        rootCriterion = createNode(null, rootCriterion, data, posGr);
        
        return rootCriterion;
    }
    
    static Criterion createNode(Criterion superCriterion, Criterion g, String[][] data, int pos){
        String[] childrenNames;
        if(pos < data.length){//If there is still chance that g is a non-elmentary criterion
            childrenNames = data[pos][1].replace("{", "").replace("}", "").split(",");//String[] childrenNames = data[pos][1].split("=|\\{|\\,|\\}|\\\t|\\\n");
            g = Utilities.findCriterion(data[pos][0], problem.getCriteria());
            g.initializeChildren(childrenNames, problem.getCriteria());
            g.setIsElementary(false);
            for(Criterion subCriterion:g.getChildren()){
                createNode(g, subCriterion, data, ++pos);
            }
        }
        
        g.setSuperCriterion(superCriterion);
        return g;
    }
    
    static double delta_j_Real(double gX, double gY, double pj, double qj){
        if (gX - gY >= -qj) {
            return 1d;
        }else if(gY-gX >= pj){
            return 0d;
        } else{
            return (gX-gY+pj)/(pj-qj);
        }
    }
    
    static double delta_j_Interval(double xmin, double xmax, double ymin, double ymax) {
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
    
    static double marginalCredibility(Criterion g, Alternative x, Alternative y, int pos, double[] lambda) {
        double[] c, d; //Índices de concordancia y discordancia
        double result, p, gamma, pVetoMax;//Posibilidad máxima de veto
        double[][] Gamma, weightsF3;
        

        c = new double[2];
        d = new double[2];
        Gamma = g.getGamma();
        gamma = Gamma[pos][1]; //gamma a comparar

        getConcordance(g, c, pos);
        pVetoMax = getDiscordance(g, x, y, d, pos);
        double kLow = definition1_lowest(c[0], d);
        double kHigh = definition1_highest(c[1], d);

        p = Utilities.P(kLow, kHigh, lambda[0], lambda[1]);
        result = Math.min(p, 1-pVetoMax);
        result = Math.min(gamma, result);

        return result;
    }
    
    /**
     * Estimates the concordance index (without addressing the corresponding constraints)
     * @param g
     * @param c
     * @param Gamma
     * @param pos 
     */
    static void getConcordance(Criterion g, double[] c, int pos) {
        int posCriterion;
        double WCmax, WCmin;
        double[][] Gamma = g.getGamma();

        WCmax = WCmin = 0;

        /*Gamma is ordered from largest to smallest. The first "pos" gammas are in the Concordance coalition, 
        the rest of the gammas are in the Discorndace coalition*/
        for (int i = 0; i < (pos + 1); i++) {
            posCriterion = (int) Gamma[i][0]; //Criterion position before the ordering
            Criterion childCriterion = g.getChildren()[posCriterion];
            double[] weight = childCriterion.getWeight();
            WCmin += weight[0];
            WCmax += weight[1];
            for(int j = 0; j < childCriterion.strengtheningInteraction.getNumberOfInteractions(); j++){
                Object[] interactingCriteria = childCriterion.strengtheningInteraction.getInteraction(j);
                String criterionName = (String)interactingCriteria[0];
                if(isInConcordanceCoalition(criterionName, g, pos)){
                    double[]k = (double[])interactingCriteria[1];
                    WCmin += k[0];
                    WCmax += k[1];
                }
            }
            for(int j = 0; j < childCriterion.weakeningInteraction.getNumberOfInteractions(); j++){
                Object[] interactingCriteria = childCriterion.weakeningInteraction.getInteraction(j);
                String criterionName = (String)interactingCriteria[0];
                if(isInConcordanceCoalition(criterionName, g, pos)){
                    double[]k = (double[])interactingCriteria[1];
                    WCmin -= k[1];
                    WCmax -= k[0];
                }
            }
            for(int j = 0; j < childCriterion.antagonistInteraction.getNumberOfInteractions(); j++){
                Object[] interactingCriteria = childCriterion.antagonistInteraction.getInteraction(j);
                String criterionName = (String)interactingCriteria[0];
                if(isInDiscordanceCoalition(criterionName, g, pos)){
                    double[]k = (double[])interactingCriteria[1];
                    WCmin -= k[1];
                    WCmax -= k[0];
                }
            }
        }
        c[0] = WCmin;
        c[1] = WCmax;
    }
    

    static double getDiscordance(Criterion g, Alternative x, Alternative y, double[] d, int pos) {
        int j;
        double pVeto, pVetoMax, WDmax, WDmin;
        double[] dif;
        double[][] Gamma = g.getGamma();

        dif = new double[2];
        pVetoMax = WDmax = WDmin = 0d;

        /*Gamma está ordenado de mayor a menor. Las primeras "pos+1" gammas están en la coalición de concordancia, el resto de gammas
        están en la coalición de discordancia.*/
        for (int i = (pos + 1); i < Gamma.length; i++) {
            int posCriterion = (int) Gamma[i][0]; //Criterion position before the ordering
            Criterion childCriterion = g.getChildren()[posCriterion];
            
            //We only consider the weights of the direct descendants of childCriterion
            double[] weight = childCriterion.getWeight();
            WDmin += weight[0];
            WDmax += weight[1];
            if (!childCriterion.getIsElementary() || childCriterion.getVeto()[1] == 0) {//The criterion is not elementary or does not have veto power
                continue;
            }
            int posElementaryDescendant = Utilities.findCriterionPos(childCriterion.getName(), problem.elementaryCriteriaNames);
            double[] gX = x.getCrtieriaImpacts()[posElementaryDescendant];
            double[] gY = y.getCrtieriaImpacts()[posElementaryDescendant];

            dif[0] = gY[0] - gX[1]; //bmin-amax
            dif[1] = gY[1] - gX[0]; //bmax-amin  

            pVeto = Utilities.P(dif[0], dif[1], childCriterion.getVeto()[0], childCriterion.getVeto()[1]);

            if (pVeto > pVetoMax) {
                pVetoMax = pVeto;
            }
            
            /* USE (AND MODIFY) THE FOLOWING CODE TO CONSIDER VETO POWER OF "SUPER-DESCENDANTS"
            //We only consider the veto power of the elementary criteria descendant from childCriterion
            Criterion[] elementaryDescendants = childCriterion.getImmediateElementaryCriteria();//-------------------------------immediateElementaryDescendants----------------------------
            //Criterion[] elementaryDescendants = childCriterion.getElementaryCriteria();
            for(Criterion elementaryDescendant:elementaryDescendants){
                int posElementaryDescendant = Utilities.findCriterionPos(elementaryDescendant.getName(), problem.elementaryCriteriaNames);
                if(elementaryDescendant.getVeto()[1] == 0){//The criterion does not have veto power
                    continue;
                }
                double[] gX = x.getCrtieriaImpacts()[posElementaryDescendant];
                double[] gY = y.getCrtieriaImpacts()[posElementaryDescendant];

                dif[0] = gY[0] - gX[1]; //bmin-amax
                dif[1] = gY[1] - gX[0]; //bmax-amin  

                pVeto = Utilities.P(dif[0], dif[1], elementaryDescendant.getVeto()[0], elementaryDescendant.getVeto()[1]);

                if (pVeto > pVetoMax) {
                    pVetoMax = pVeto;
                }
            }
            */
        }
        d[0] = WDmin;
        d[1] = WDmax;

        return pVetoMax;
    }
    
    static boolean isInConcordanceCoalition(String criterionName, Criterion g, int pos){
        double[][] Gamma = g.getGamma();
        for (int i = 0; i < (pos + 1); i++) {
            int childPos = (int)Gamma[i][0];
            Criterion childCriterion = g.getChildren()[childPos];
            if(childCriterion.getName().equals(criterionName)){
                return true;
            }
        }
        return false;
    }
    
    static boolean isInDiscordanceCoalition(String criterionName, Criterion g, int pos){
        double[][] Gamma = g.getGamma();
        for (int i = pos + 1; i < Gamma.length; i++) {//This is wrong. It is looking in F_3, when it should be
            int childPos = (int)Gamma[i][0];
            Criterion childCriterion = g.getChildren()[childPos];
            if(childCriterion.getName().equals(criterionName)){
                return true;
            }
        }
        return false;
    }
    
    static double definition1_lowest(double cont_f1, double[] d){
        double sumKlowest = d[0];
        double sumKhighest = d[1];
        //Case 1.a
        if(cont_f1 + sumKlowest <= 1 && cont_f1 + sumKhighest >= 1 ){
            return cont_f1;
        }else if(cont_f1 + sumKlowest > 1){//Case 1.b
            return 1-sumKhighest;
        }else if(cont_f1 + sumKhighest < 1){//Case 1.c
            return 1-sumKhighest;
        }
        return -1;//Flag
    }
    
    static double definition1_highest(double cont_f1, double[] d){
        double sumKlowest = d[0];
        double sumKhighest = d[1];
        //Case 2.a
        if(cont_f1 + sumKlowest <= 1 && cont_f1 + sumKhighest >= 1 ){
            return cont_f1;
        }else if(cont_f1 + sumKlowest > 1){//Case 2.b
            return 1-sumKlowest;
        }else if(cont_f1 + sumKhighest < 1){//Case 2.c
            return 1-sumKlowest;
        }
        return -1;//Flag
    }
    
    static void printCredibilityMatrix(double[][] credibilityMatrix){
        int noAlternatives = credibilityMatrix.length;
        for(int i = 0; i < noAlternatives; i++){
            System.out.print("\t" + (i+1));
        }
        System.out.println();
        for (int i = 0; i < noAlternatives; i++) {
            System.out.print((i+1));
            for (int j = 0; j < noAlternatives; j++) {
                System.out.print("\t" + credibilityMatrix[i][j]);
            }
            System.out.println();
        }
    }
    
    public static void saveSorting(String[][] sorting, String criterion, Path path) {
        PrintWriter pw;
        String fileName = Paths.get(path.toString(), "Sorting", criterion, "Assignments.txt").toString();
        int noAlternatives = sorting.length;
        try {
            pw = new PrintWriter(new FileOutputStream(new File(fileName), false /* append = false */));
            for (int i = 0; i < noAlternatives; i++) {
                pw.append((i + 1) + "\t" + sorting[i][0] + "\t" + sorting[i][1] + "\n");
            }
            pw.flush();
            pw.close();
            //System.out.println("Sorting saved at " + fileName);
        } catch (FileNotFoundException ex) {
            Logger.getLogger(Utilities.class.getName()).log(Level.SEVERE, null, ex);
        }
    }
    
    public static void saveCredibilityMatrix(double[][] credibilityMatrix, String criterion, Path path) {
        PrintWriter pw;
        Path credMatrixDir = path.resolve("Credibility matrices");
        File directory = new File(credMatrixDir.toString());
        if (!directory.exists()) {
            directory.mkdirs();
            // If you require it to make only the final directory,
            // use directory.mkdir(); here instead.
            // If you require it to make the entire directory path including parents,
            // use directory.mkdirs(); here instead.
        }
        
        String fileName = credMatrixDir.resolve(criterion+"-CredibilityMatrix.txt").toString();
        int noAlternatives = credibilityMatrix.length;
        try {
            pw = new PrintWriter(new FileOutputStream(new File(fileName), false /* append = false */));
            for (int i = 0; i < noAlternatives; i++) {
                pw.append("\t" + (i + 1));
            }
            pw.append("\n");
            for (int i = 0; i < noAlternatives; i++) {
                pw.append((i + 1)+"");
                for (int j = 0; j < noAlternatives; j++) {
                    pw.append("\t" + Math.round(credibilityMatrix[i][j] * 100)/100d);
                }
                pw.append("\n");
            }
            pw.flush();
            pw.close();
            System.out.println("Credibility matrix saved at " + fileName);
        } catch (FileNotFoundException ex) {
            Logger.getLogger(Utilities.class.getName()).log(Level.SEVERE, null, ex);
        }
    }
}
