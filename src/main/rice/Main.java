package main.rice;
import main.rice.basegen.BaseSetGenerator;
import main.rice.concisegen.ConciseSetGenerator;
import main.rice.node.APyNode;
//import main.rice.parse.InvalidConfigException;
//import main.rice.parse.ConfigFile;
import main.rice.parse.*;
//import main.rice.parse.ConfigFileParser;
//import main.rice.test.TestCase;
import main.rice.test.*;

import java.awt.*;
import java.util.*;
import java.io.IOException;
import java.util.List;

public class Main{

    /**
     * Using these arguments, main() should delegate to generateTests()
     * in order to compute the concise test set. It should then print the result of calling generateTests()
     * to the console, along with an appropriate message explaining what's being printed.
     * @param args This method takes as its input a String[] that should contain three arguments:
     *              1. A String containing the path to the config file.
     *              2. A String containing the path to the directory containing the buggy implementations.
     *              3. A String containing the path to the reference solution.
     * @throws IOException
     * @throws InvalidConfigException
     * @throws InterruptedException
     */
    public static void main(String[] args) throws IOException, InvalidConfigException, InterruptedException{
        Set<TestCase> generateTestsRes = generateTests(args);
        System.out.println("The result of generating tests: " + generateTestsRes);
    }

    /**
     * This method is a helper for main(), and should take the exact same array of arguments.
     * It should utilize the components that you built in homeworks 1-6 in order to perform
     * end-to-end test case generation, returning the concise test set.
     * @param args same 3 arguments as main() above
     * @return the concise test set
     * @throws IOException
     * @throws InvalidConfigException
     * @throws InterruptedException
     */
    public static Set<TestCase> generateTests(String[] args) throws IOException, InvalidConfigException, InterruptedException{
        // variables to contain file paths
        String configFilePath = args[0];
        String buggyDirPath = args[1];
        String solutionPath = args[2];

        //create parser
        ConfigFileParser parser = new ConfigFileParser();
        //parse and store stuff
        String fileStuff = parser.readFile(configFilePath);
        ConfigFile parseConfig = parser.parse(fileStuff);

        //get the nodes for later function name and number random tests
        List<APyNode<?>> nodes = parseConfig.getNodes();
        String funcName = parseConfig.getFuncName();
        int numRanTests = parseConfig.getNumRand();

        // base set generator
        BaseSetGenerator baseSetGen = new BaseSetGenerator(nodes, numRanTests);
        //store the output of generated base set
        List<TestCase> baseSet = baseSetGen.genBaseSet();
        //construct tester
        Tester tester = new Tester(funcName, solutionPath,buggyDirPath,baseSet);
        //compute expected result
        tester.computeExpectedResults();
        //store results of calling runTests
        TestResults testResults = tester.runTests();
        //find concise test set and return it
        Set<TestCase> conciseTestSet = ConciseSetGenerator.setCover(testResults);
        return conciseTestSet;



    }






}
