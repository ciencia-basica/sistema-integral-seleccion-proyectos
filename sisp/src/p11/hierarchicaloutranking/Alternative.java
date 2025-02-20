/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package p11.hierarchicaloutranking;

/**
 *
 * @author efrai
 */
public class Alternative {
    private String[] elementaryCriteriaNames;
    private double[][] elementaryCriteriaImpacts;
    private String[][] assignedClasses;

    /**
     * Does NOT initialize the assignedClasses array
     * @param numElementaryCriteria 
     */
    public Alternative(int numElementaryCriteria) {        
        elementaryCriteriaNames = new String[numElementaryCriteria];
        elementaryCriteriaImpacts = new double[numElementaryCriteria][2];//Interval numbers by default; degenerate interval numbers to represent real numbers
    }

    public void setElementaryCriteriaNames(String[] elementaryCriteriaNames) {
        this.elementaryCriteriaNames = elementaryCriteriaNames;
    }

    public void setCrtieriaImpacts(double[][] crtieriaImpacts) {
        this.elementaryCriteriaImpacts = crtieriaImpacts;
    }

    public void setAssignedClasses(int sortingCriterion_pos, String lowerClass, String upperClass) {
        this.assignedClasses[sortingCriterion_pos][0] = lowerClass;
        this.assignedClasses[sortingCriterion_pos][1] = upperClass;
    }

    public void setAssignedClasses(String[][] classes) {
        int noSortingCriteria = classes.length;
        this.assignedClasses = new String[noSortingCriteria][2];
        for(int i = 0; i < classes.length; i++){
            this.assignedClasses[i][0] = classes[i][0];
            this.assignedClasses[i][1] = classes[i][1];
        }
    }

    public void setCrtieriaImpacts(int criterionPos, double crtieriaImpact0, double crtieriaImpact1) {
        this.elementaryCriteriaImpacts[criterionPos][0] = crtieriaImpact0;
        this.elementaryCriteriaImpacts[criterionPos][1] = crtieriaImpact1;
    }

    public String[] getElementaryCriteriaNames() {
        return elementaryCriteriaNames;
    }

    public String[][] getAssignedClasses() {
        return assignedClasses;
    }

    public double[][] getCrtieriaImpacts() {
        return elementaryCriteriaImpacts;
    }
}
