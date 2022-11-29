package main.rice.parse;
import java.util.*;
import main.rice.node.APyNode;

// TODO: implement the ConfigFile class here
public class ConfigFile{
    /**
     * The name of the function under test.
     */
    private String testFuncName;
    /**
     * A List of PyNodes that will be used to generate TestCases for the function under test.
     */
    private List<APyNode<?>> pyNodeList;
    /**
     * The number of random test cases to be generated.
     */
    private int numRandTest;

    /**
     * Constructor for a ConfigFile object, which takes in three pieces of data:
     * @param funcName: The name of the function under test.
     * @param nodes: A List of PyNodes that will be used to generate TestCases for the function under test.
     * @param numRand: The number of random test cases to be generated.
     */
    public ConfigFile(String funcName, List<APyNode<?>> nodes, int numRand){
        this.testFuncName = funcName;
        this.pyNodeList = nodes;
        this.numRandTest = numRand;
    }
    /**
     *
     * @return the name of the function under test.
     */
    public String getFuncName(){
        return this.testFuncName;
    }
    /**
     * @return the List of PyNodes that will be used to generate TestCases for the function under test.
     */
    public List<APyNode<?>> getNodes(){
        return this.pyNodeList;
    }

    /**
     *
     * @return the number of random test cases to be generated.
     */
    public int getNumRand(){
        return this.numRandTest;

    }



}