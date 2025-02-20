/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package p11.hierarchicaloutranking;
import General.Utilities;
import java.util.ArrayList;
import java.util.logging.Level;
import java.util.logging.Logger;
/**
 *
 * @author efrai
 */
public class Criterion {

    private String name;
    private int type;
    private Criterion superCriterion;
    private Criterion[] children;
    private int no_children;
    private boolean isElementary;
    private boolean useValueFunction;
    private double[][] Gamma;
    private double[] weight;
    private double indifferenceThreshold;
    private double preferenceThreshold;
    private double[] veto;
    private double[] minScore;
    private double[] maxScore;
    private ArrayList<String[]> descendantsWithVeto;
    public WeakeningInteraction weakeningInteraction;
    public StrengtheningInteraction strengtheningInteraction;
    public AntagonistInteraction antagonistInteraction;

    public Criterion() {
        isElementary = true;
        useValueFunction = false;
        weight = new double[2];
        veto = new double[2];
        minScore = new double[2];
        maxScore = new double[2];
        descendantsWithVeto = new ArrayList<>();
        weakeningInteraction = new WeakeningInteraction();
        strengtheningInteraction = new StrengtheningInteraction();
        antagonistInteraction = new AntagonistInteraction();
    }

    public Criterion(String name) {
        this.name = name;
        isElementary = true;
        useValueFunction = false;
        weight = new double[2];
        veto = new double[2];
        minScore = new double[2];
        maxScore = new double[2];
        weakeningInteraction = new WeakeningInteraction();
        strengtheningInteraction = new StrengtheningInteraction();
        antagonistInteraction = new AntagonistInteraction();
    }

    public void initializeChildren(String[] childrenNames, Criterion[] criteria) {
        no_children = childrenNames.length;
        children = new Criterion[no_children];

        for (int i = 0; i < no_children; i++) {
            children[i] = Utilities.findCriterion(childrenNames[i], criteria);
        }
        Gamma = new double[no_children][2];
    }

    public void setName(String name) {
        this.name = name;
    }

    public void setWeight(double[] weight) {
        this.weight = weight;
    }

    public void setWeight(double weight0, double weight1) {
        this.weight[0] = weight0;
        this.weight[1] = weight1;
    }

    public void setVeto(double[] veto) {
        this.veto = veto;
    }

    public void setDescendantsWithVeto(String[] descendantsWithVeto) {
        this.descendantsWithVeto.add(descendantsWithVeto);
    }
    
    public void setVeto(double veto0, double veto1) {
        this.veto[0] = veto0;
        this.veto[1] = veto1;
    }

    public void setMinScore(double[] minScore) {
        this.minScore = minScore;
    }
    
    public void setMinScore(double minScore0, double minScore1) {
        this.minScore[0] = minScore0;
        this.minScore[1] = minScore1;
    }

    public void setMaxScore(double[] maxScore) {
        this.maxScore = maxScore;
    }
    
    public void setMaxScore(double maxScore0, double maxScore1) {
        this.maxScore[0] = maxScore0;
        this.maxScore[1] = maxScore1;
    }

    public void setPreferenceThreshold(double preferenceThreshold) {
        this.preferenceThreshold = preferenceThreshold;
    }

    public void setIndifferenceThreshold(double indifferenceThreshold) {
        this.indifferenceThreshold = indifferenceThreshold;
    }

    public void setType(int type) {
        this.type = type;
    }

    public void setIsElementary(boolean isElementary) {
        this.isElementary = isElementary;
    }

    public void setUseValueFunction(boolean useValueFunction) {
        this.useValueFunction = useValueFunction;
    }    

    public void setSuperCriterion(Criterion superCriterion) {
        this.superCriterion = superCriterion;
    }

    public void setChildren(Criterion[] children) {
        this.children = children;
    }

    public void setGamma(double[][] Gamma) {
        this.Gamma = Gamma;
    }

    public void setGamma(int index, double gammaValue, int criterionPosition) {
        this.Gamma[index][0] = criterionPosition;
        this.Gamma[index][1] = gammaValue;
    }

    public String getName() {
        return name;
    }

    public int getType() {
        return type;
    }

    public double[] getWeight() {
        return weight;
    }

    public double[] getVeto() {
        return veto;
    }

    public String[][] getDescendantsWithVeto() {
        int noDescendants = descendantsWithVeto.size();
        String[][] descendants = new String[noDescendants][];
        for(int i = 0; i < noDescendants; i++){
            descendants[i] = descendantsWithVeto.get(i);
        }
        return descendants;
    }

    public double[] getMinScore() {
        return minScore;
    }

    public double[] getMaxScore() {
        return maxScore;
    }

    public double getPreferenceThreshold() {
        return preferenceThreshold;
    }

    public double getIndifferenceThreshold() {
        return indifferenceThreshold;
    }

    public boolean getIsElementary() {
        return isElementary;
    }

    public boolean getUseValueFunction() {
        return useValueFunction;
    }

    public Criterion getSuperCriterion() {
        return superCriterion;
    }

    public int getNo_children() {
        return no_children;
    }

    public Criterion[] getChildren() {
        return children;
    }

    public double[][] getGamma() {
        return Gamma;
    }    
    
    /**
     * Returns the set of elementary criteria that descend from the criterion
     * @return 
     */
    Criterion[] getElementaryCriteria() {
        int no_elementaryCriteria = 0;
        int pos = 0;
        Criterion[] elementaryCriteria;
        
        if(isElementary)
            return new Criterion[]{this};
        for(Criterion child:getChildren()){
            no_elementaryCriteria += child.getElementaryCriteria().length;
        }
        elementaryCriteria = new Criterion[no_elementaryCriteria];
        for (Criterion child : getChildren()) {
            for (Criterion elementaryCriterion : child.getElementaryCriteria()) {
                elementaryCriteria[pos++] = elementaryCriterion;
            }
        }
        return elementaryCriteria;
    }
    
    /**
     * Returns the set of IMMEDIATE elementary criteria that descend from the criterion
     * @return 
     */
    Criterion[] getImmediateElementaryCriteria() {
        int no_elementaryCriteria = 0;
        int pos = 0;
        Criterion[] descendants = getChildren();
        if(descendants == null){
            return new Criterion[0];
        }
        
        Criterion[] elementaryCriteria;
        
        for(Criterion child:descendants){
            if (child.isElementary) {
                no_elementaryCriteria++;//How many immediate elementary criteria?
            }
        }
        elementaryCriteria = new Criterion[no_elementaryCriteria];//Create the array
        for (Criterion child:descendants) {
            if (child.isElementary) {
                elementaryCriteria[pos++] = child;
            }
        }
        return elementaryCriteria;
    }
    
    public class WeakeningInteraction{
        private final ArrayList<String> interactingCriteria;
        private final ArrayList<double[]> interactionValues;
        
        WeakeningInteraction(){
            interactingCriteria = new ArrayList();
            interactionValues = new ArrayList();
        }

        public void setInteraction(String interactingCriterion, double[] interactionValue){
            interactingCriteria.add(interactingCriterion);
            interactionValues.add(interactionValue);
        }
        
        public Object[] getInteraction(int position){
            return new Object[]{interactingCriteria.get(position), interactionValues.get(position)};
        }
        
        int getNumberOfInteractions(){
            return interactingCriteria.size();
        }
    }
    
    public class StrengtheningInteraction{
        private final ArrayList<String> interactingCriteria;
        private final ArrayList<double[]> interactionValues;
        
        StrengtheningInteraction(){
            interactingCriteria = new ArrayList();
            interactionValues = new ArrayList();
        }

        public void setInteraction(String interactingCriterion, double[] interactionValue){
            interactingCriteria.add(interactingCriterion);
            interactionValues.add(interactionValue);
        }
        
        public Object[] getInteraction(int position){
            return new Object[]{interactingCriteria.get(position), interactionValues.get(position)};
        }
        
        int getNumberOfInteractions(){
            return interactingCriteria.size();
        }
    }
    
    public class AntagonistInteraction{
        private final ArrayList<String> interactingCriteria;
        private final ArrayList<double[]> interactionValues;
        
        AntagonistInteraction(){
            interactingCriteria = new ArrayList();
            interactionValues = new ArrayList();
        }

        public void setInteraction(String interactingCriterion, double[] interactionValue){
            interactingCriteria.add(interactingCriterion);
            interactionValues.add(interactionValue);
        }
        
        public Object[] getInteraction(int position){
            return new Object[]{interactingCriteria.get(position), interactionValues.get(position)};
        }
        
        int getNumberOfInteractions(){
            return interactingCriteria.size();
        }
    }
}
