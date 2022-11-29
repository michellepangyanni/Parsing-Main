package main.rice.parse;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.*;
import main.rice.node.*;
import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.nio.file.*;
import java.io.IOException;

import org.json.JSONArray;
import org.json.JSONObject;
import java.util.Collections;
// TODO: implement the ConfigFileParser class here
public class ConfigFileParser{
    /**
     * fields, which are 5 keys of a single valid JSON object a config file must be comprised of.
     */
    private String fname;
    private JSONArray types;
    private JSONArray exDomain;
    private JSONArray randDomain;
    private Integer numRandom;
    /**
     * Reads and returns the contents of the file located at the input filepath;
     * throws an IOException if the file does n
     * @param filepath
     * @return the contents of the file located at the input filepath
     * @throws IOException if the file does n
     */
    public String readFile(String filepath) throws IOException{
        String contents = Files.readString(Paths.get(filepath));
        return contents;
    }

    /**
     * Helper function for parse, throw an exception when the given value has spurious colons
     * @param values a JSONArray of string values
     * @throws InvalidConfigException if contiguous colons or leading/ending colon
     */
    public void spuriousColons(String values) throws InvalidConfigException{
        values = values.strip();
        if (values.endsWith(":")){
            throw new InvalidConfigException("error: types ends with ':'!");
        }
        if (values.startsWith(":")){
            throw new InvalidConfigException("error: types starts with ':'!");
        }
        if (values.contains("::")){
            throw new InvalidConfigException(("error: spurious colon in types!"));
        }
    }
    /**
     * Helper function for parse, throw an exception when the given value has spurious parenthesis
     * @param values a JSONArray of string values
     * @throws InvalidConfigException if contiguous parenthesises or leading/ending parenthesis
     */
    public void spuriousParenthesis(String values) throws InvalidConfigException{
        values = values.strip();
        if (values.endsWith("(")){
            throw new InvalidConfigException("error: types ends with '('!");
        }
        if (values.startsWith("(")){
            throw new InvalidConfigException("error: types starts with '('!");
        }
        if (values.contains("((")){
            throw new InvalidConfigException(("error: spurious parenthesis in types!"));
        }
    }

    /**
     * Helper function to check that length of two JSONArrays are equal
     * @param array1 the first JSONArray
     * @param array2 the second JSONArray
     * @throws InvalidConfigException with a descriptive error message if arrays do not have the same length
     */
    public void checkLength(JSONArray array1, JSONArray array2) throws InvalidConfigException{
        if (array1.length() != array2.length()){
            throw new InvalidConfigException("The domain length are not the same!");
        }
    }
    /**
     * Helper function to check that each of the keys is an object of the correct type
     * @param json the json object to be checked with
     * @throws InvalidConfigException with a descriptive error message if invalid
     */
    public void checkKeyTypes(JSONObject json) throws InvalidConfigException{
        if (!(json.get("fname") instanceof String)){
            throw new InvalidConfigException("JSONObject 'fname' is not a string!");
        }
        if (!(json.get("types") instanceof JSONArray)){
            throw new InvalidConfigException("JSONObject 'types' is not a JSONArray!");
        }

        if (!(json.get("exhaustive domain") instanceof JSONArray)) {
            throw new InvalidConfigException("JSONObject 'exhaustive domain' is not a JSONArray!");
        }
        if (!(json.get("random domain") instanceof JSONArray)) {
            throw new InvalidConfigException("JSONObject 'random domain' is not a JSONArray!");
        }
        if (!(json.get("num random") instanceof Integer)) {
            throw new InvalidConfigException("JSONObject 'num random' is not an Integer!");
        }
    }

    /**
     * A helper function to check if the contents in a config file is valid
     * @param contents a string of possible config file
     * @throws InvalidConfigException with a descriptive error message if the contents is not valid
     */
    public void validConfigFile(String contents) throws InvalidConfigException{
        // a valid config file must start with "{" and ends with "}"
        if (! contents.startsWith("{")){
            throw new InvalidConfigException("JSONObject must begin with a '{' !");
        }
        if (! contents.endsWith("}")){
            throw new InvalidConfigException("JSONObject must end with a '}' !");
        }
        // a config file must be comprised of a single valid JSON object with the 5 keys
        if (! contents.contains("fname")){
            throw new InvalidConfigException("JSONObject key 'fname' not found!");
        }
        if (! contents.contains("types")){
            throw new InvalidConfigException("JSONObject key 'types' not found!");
        }
        if (! contents.contains("exhaustive domain")){
            throw new InvalidConfigException("JSONObject key 'exhaustive domain' not found!");
        }
        if (! contents.contains("random domain")){
            throw new InvalidConfigException("JSONObject key 'random domain' not found!");
        }
        if (! contents.contains("num random")){
            throw new InvalidConfigException("JSONObject key 'num random' not found!");
        }
    }

    /**
     * Recursive helper function to check the grammar of types in JSON file
     * @param types, the current type element
     * @return an APyNode representing the types in the input
     */
    public APyNode<?> typesGrammar(String types) throws InvalidConfigException{
        //Base: if <type> is <simple type>
        // either "int", "float", or "bool"
        types = types.strip();
        if (types.equals("int")){
            return new PyIntNode();
        }
        else if (types.equals("float")){
            return new PyFloatNode();
        }
        else if (types.equals("bool")){
            return new PyBoolNode();
        }
        //Base: if <type> is "str" "(" <strVal>
        // <strVal>::= any valid string
        if (types.startsWith("str")){
            int parentIndex = types.indexOf("(");
            return new PyStringNode(types.substring(parentIndex + 1));
        }
        //Recursion: if <type> is <iterableType> "(" <type>
        //<iterableType> ::= "list" | "tuple" | "set"
        //create a node of this iterable type
        if (types.startsWith("list")){
            if (! types.contains("(")){
                throw new InvalidConfigException("parenthesis is missing!");
            }
            int parentIndex = types.indexOf("(");
            String sub = types.substring(parentIndex + 1);
            return new PyListNode<>(typesGrammar(sub));
        }
        else if (types.startsWith("tuple")){
            if (! types.contains("(")){
                throw new InvalidConfigException("parenthesis is missing!");
            }
            int indexParen = types.indexOf("(");
            return new PyTupleNode<>(typesGrammar(types.substring(indexParen + 1)));
        }
        else if (types.startsWith("set")){
            if (! types.contains("(")){
                throw new InvalidConfigException("parenthesis is missing!");
            }
            int indexParen = types.indexOf("(");
            return new PySetNode<>(typesGrammar(types.substring(indexParen + 1)));
        }
        // Recursion: if <type> is "dict" "(" <type> ":" <type>
        //
        if (types.startsWith("dict")){
            spuriousColons(types);
            int indexParen = types.indexOf("(");
            int indexColon = types.indexOf(":");
            return new PyDictNode<>(typesGrammar(types.substring(indexParen + 1, indexColon).strip()), typesGrammar(types.substring(indexColon + 1).strip()));
        }
        throw new InvalidConfigException("Type is invalid!");
    }

    /**
     * Helper function for domainGrammar that parses domain based on grammar for both "~" and "[]" formats
     * @param dom a String representation of the domain
     * @return the List<Number> representation of the domain
     * @throws InvalidConfigException with descriptive exception if domain is invalid
     */
    private List<Number> domainList(String dom, APyNode<?> typesNodes) throws InvalidConfigException {
        List<Number> domList = new ArrayList<>();

        if (dom.contains("~")) {
            int indexCurly = dom.indexOf("~");
            try {
                int start = Integer.parseInt(dom.substring(0, indexCurly));
                int end = Integer.parseInt(dom.substring(indexCurly + 1));
                if (start > end) {
                    throw new InvalidConfigException("lower bound exceeds upper bound");
                }

                for (int i = start; i <= end; i++) {
                    domList.add(i);
                }
            } catch (NumberFormatException e) {
                throw new InvalidConfigException("invalid domain syntax");
            }


        } else if (dom.startsWith("[") && dom.endsWith("]")) {
            try {
                String[] temp = dom.substring(1, dom.length() - 1).split(", ");
                for (String elem : temp) {
//                    if (intVal(dom)){
                    System.out.println(elem);
                    int intElem = Integer.parseInt(elem);
                        //float floatElem = Float.parseFloat(elem);
                    domList.add(intElem);
                    }
//                    else if (floatVal(dom)){
//                        float floatElem = Float.parseFloat(elem);
//                        domList.add(floatElem);
//                    }

                    //domList.add(floatElem);
//                }
//                if (intVal(dom)){
//                    String[] temp = dom.substring(1, dom.length() - 1).split(", ");
//                    for (String elem : temp) {
//                        int intElem = Integer.parseInt(elem);
//                        domList.add(intElem);
//                    }
//                }
//                else if (floatVal(dom)){
//                    String[] temp = dom.substring(1, dom.length() - 1).split(", ");
//                    for (String elem : temp) {
//                        float floatElem = Float.parseFloat(elem);
//                        domList.add(floatElem);
//                    }
//                }
            }
            catch (Exception e) {
                throw new InvalidConfigException("invalid domain syntax");
            }
        }
        else {
            domList.add(Integer.parseInt(dom));
        }
//            if (intVal(dom)){
//                domList.add(Integer.parseInt(dom));
//            }
//            else if (floatVal(dom)){
//                domList.add(Float.parseFloat(dom));
//            }
//        }
        return domList;
    }

    /**
     * Helper function for domainGrammar that checks if <simpletype> "int"
     * @param domain a string of possible integers
     * @return true if the input is a string of an integer
     */
    private boolean intVal(String domain){
        if (domain.matches("-?[0-9]+")){
            return true;
        }
        return false;

    }
    /**
     * Helper function for domainGrammar that checks if <simpletype> "float"
     * @param domain a string of possible floats
     * @return true if the input is a string of an float
     */
    private boolean floatVal(String domain){
        if (domain.matches("-?[0-9]+\\.0")){
            return true;
        }
        return false;
    }
    /**
     * Helper function for domainGrammar that checks if <simpletype> "bool"
     * @param domain a string of possible booleans
     * @return true if the input is a string of a boolean
     */
    private boolean boolVal(String domain){
        if (domain.matches("[01]")){
            return true;
        }
        return false;
    }

    /**
     * Helper function for simpleType, checks if it's a valid <intDom>
     * @param domain current string domain
     * @return true if domain is a valid <intDom>
     * @throws InvalidConfigException if domain is invalid
     */
    private boolean intDom(String domain) throws InvalidConfigException{
        //<intDom> ::= <intVal> "~" <intVal>| <intArray>
        if (domain.contains("~")){
            int tildeIndex = domain.indexOf("~");
            if (intVal(domain.substring(0, tildeIndex)) && intVal(domain.substring(tildeIndex + 1))){
                return true;
            }
            else {
                throw new InvalidConfigException("invalid integer exhaustive domain");
            }
            }
        else if (numArray(domain, "int")){
            return true;
        }
        else {
            throw new InvalidConfigException("invalid integer exhaustive domain");
        }
    }

    /**
     * Helper function for simpleType, checks if it's a valid <floatDom>
     * @param domain current string domain
     * @return true if domain is a valid <floatDom>
     * @throws InvalidConfigException if domain is invalid
     */
    private boolean floatDom(String domain) throws InvalidConfigException{
        //<floatDom>::= <intVal> "~" <intVal> | <numArray>
        if (domain.contains("~")){
            int tildeIndex = domain.indexOf("~");
            if (floatVal(domain.substring(0, tildeIndex)) && floatVal(domain.substring(tildeIndex + 1))){
                return true;
            }
            else {
                throw new InvalidConfigException("invalid float exhaustive domain");
            }
        }
        else if (numArray(domain, "float")){
            return true;
        }
        else {
            throw new InvalidConfigException("invalid float exhaustive domain");
        }
    }
    /**
     * Helper function for simpleType, checks if it's a valid <boolDom>
     * @param domain current string domain
     * @return true if domain is a valid <boolDom>
     * @throws InvalidConfigException if domain is invalid
     */
    private boolean boolDom(String domain) throws InvalidConfigException{
        //<boolDom>::= <boolVal> "~" <boolVal> | <numArray>
        if (domain.contains("~")){
            int tildeIndex = domain.indexOf("~");
            if (boolVal(domain.substring(0, tildeIndex)) && boolVal(domain.substring(tildeIndex + 1))){
                return true;
            }
            else {
                throw new InvalidConfigException("invalid boolean exhaustive domain");
            }
        }
        else if (numArray(domain, "bool")){
            return true;
        }
        else {
            throw new InvalidConfigException("invalid boolean exhaustive domain");
        }
    }


    /**
     * Helper function for intDom, floatDom, and boolDom that checks if it's a valid numArray
     * @param domain current String domain
     * @param numVal a string comprised of any valid numeric value; this can be an integer or a real number
     * @return true if the domain is valid
     * @throws InvalidConfigException if domain is invalid
     */
    private boolean numArray(String domain, String numVal) throws InvalidConfigException{
        if (domain.startsWith("[") && domain.endsWith("]")){
            String temp_domainArray = domain.substring(1, domain.length() - 1);
            String[] temp = temp_domainArray.split(", ");
            if (numVal.equals("int")){
                for (String elem: temp){
                    // if numArray is not comprised of ints when it should be
                    if (!intVal(elem)){
                        throw new InvalidConfigException("exhaustive domain is not of type integer!");
                    }
                }
                return true;
            }
            else if (numVal.equals("float")){
                for (String elem: temp){
                    // if numArray is not comprised of floats when it should be
                    if (!floatVal(elem)){
                        throw new InvalidConfigException("exhaustive domain is not of type floats!");
                    }
                }
            }
            else if (numVal.equals("bool")){
                for (String elem: temp){
                    // if numArray is not comprised of booleans when it should be
                    if (!boolVal(elem)){
                        throw new InvalidConfigException("exhaustive domain is not of type boolean!");
                    }
                }
            }
            else if (numVal.equals("nonNeg")) {
                for (String elem: temp){
                    //if numArray is not composed of non-negatives when it should be
                    if (Integer.parseInt(elem) < 0){
                        //throw exception
                        throw new InvalidConfigException("exhaustive domain is not of iterable type! negative integer exists!");
                    }
                }
            }

        }
        return false;
    }
    /**
     * Helper function for checkGrammar to check if domain is an iterable domain
     * @param domain the current String domain
     * @return true if the domain is an iterable domain
     * @throws InvalidConfigException with descriptive exception if domain is invalid iterable domain
     */
    private boolean iterableString_domain(String domain) throws InvalidConfigException{
        domain = domain.strip();
        if (domain.contains("~")) {
            int tildeIndex = domain.indexOf("~");
            if (domain.contains("(")) {
                int parenthesisIndex = domain.indexOf("(");
                Integer before = Integer.parseInt(domain.substring(0, tildeIndex));
                Integer after = Integer.parseInt(domain.substring(tildeIndex + 1, parenthesisIndex));
                if (before >= 0 && after >= 0) {
                    return true;
                }
            } else {
                int before = Integer.parseInt(domain.substring(0, tildeIndex));
                int after = Integer.parseInt(domain.substring(tildeIndex + 1));
                if (before >= 0 && after >= 0) {
                    return true;
                }
            }
        }
        else if (numArray(domain, "nonNeg")){
            return true;
        }
        else {
            throw new InvalidConfigException("invalid exhaustive domain of type iterable type");
        }
        return false;
    }

    /**
     * Helper function for domainGrammar that checks an <iterableDom> does not have a negative domain value
     * @param domain List<Number> representation of domain
     * @throws InvalidConfigException with descriptive exception if a number is less than 0
     */
    private void iterableList_domain(List<Number> domain) throws InvalidConfigException{
        for (Number i: domain){
            if ((int) i < 0){
                throw new InvalidConfigException("invalid--negative iterable domain!");
            }
        }
    }

    /**
     * Helper function for parse that implements grammar for exhaustive domain and random domain
     *
     * @param domain String representation of domain
     * @param typesNodes the current APyNode representation of the domain
     * @param domainType the types of the current domain
     * @return the updated APyNode based on grammar that have domains applied to nodes and children
     * @throws InvalidConfigException with descriptive exception if invalid grammar exists
     */
    public APyNode<?> domainGrammar(String domain, APyNode<?> typesNodes, String domainType) throws InvalidConfigException {
        // Base: if <simpleDom>:
        spuriousParenthesis(domain);
        if (typesNodes instanceof PyIntNode || typesNodes instanceof PyFloatNode || typesNodes instanceof PyBoolNode ){
            if (domain.contains(".") && typesNodes instanceof PyIntNode){
                throw new InvalidConfigException("invalid domain type");
            }
            if (domain.contains(".") && domain.contains("~")){
                throw new InvalidConfigException("invalid domain type");
            }
            List<Number> domList = domainList(domain, typesNodes);
            if (typesNodes instanceof PyIntNode){
                for (Number num: domList){
                    if (!intVal(num.toString())){
                        throw new InvalidConfigException("invalid domain type");
                    }
                }
                if (domainType.equals("exhaustive")){
                    typesNodes.setExDomain(domainList(domain, typesNodes));
                }
                else if (domainType.equals("random")){
                    typesNodes.setRanDomain(domainList(domain, typesNodes));
                }
                return typesNodes;
            }
            else if (typesNodes instanceof PyFloatNode){
                List<Number> temp_floatDom = new ArrayList<>();
                for (Number num: domList){
                    temp_floatDom.add(num.doubleValue());
                }
                domList = temp_floatDom;
                for (Number num: domList){
                    if (!floatVal(num.toString())) throw new InvalidConfigException("invalid domain type");
                }
                if (domainType.equals("exhaustive")){
                    typesNodes.setExDomain(domList);
                }
                else if (domainType.equals("random")){
                    typesNodes.setRanDomain(domList);
                }
                return typesNodes;
            }
            else if (typesNodes instanceof PyBoolNode){
                for (Number num: domList){
                    if (!boolVal(num.toString())){
                        throw new InvalidConfigException("invalid domain type");
                    }
                }
                if (domainType.equals("exhaustive")){
                    typesNodes.setExDomain(domList);
                }
                else if (domainType.equals("random")){
                    typesNodes.setRanDomain(domList);
                }
                return typesNodes;
            }
        }
        // if of iterable domain:
        if (typesNodes instanceof PyStringNode && iterableString_domain(domain)){
            if (domainType.equals("exhaustive")){
                typesNodes.setExDomain(domainList(domain, typesNodes));
            }
            else if (domainType.equals("random")){
                typesNodes.setRanDomain(domainList(domain, typesNodes));
            }
            return typesNodes;
        }
        // Recursion
        if (domain.contains("(")){
            int parenthesisIndex = domain.indexOf("(");
            // corresponding type to domain is dict:
            if (typesNodes instanceof PyDictNode){
                int colonIndex = domain.indexOf(":");
                //assign dict's domain
                List<Number> temp_domain = domainList(domain.substring(0, parenthesisIndex).strip(), typesNodes);
                iterableList_domain(temp_domain);
                if (domainType.equals("exhaustive")){
                    typesNodes.setExDomain(temp_domain);
                    domainGrammar(domain.substring(parenthesisIndex + 1, colonIndex).strip(), typesNodes.getLeftChild(), domainType);
                    domainGrammar(domain.substring(colonIndex + 1).strip(), typesNodes.getRightChild(), domainType);
                }
                else if (domainType.equals("random")){
                    typesNodes.setRanDomain(temp_domain);
                    domainGrammar(domain.substring(parenthesisIndex + 1, colonIndex).strip(), typesNodes.getLeftChild(), domainType);
                    domainGrammar(domain.substring(colonIndex + 1).strip(), typesNodes.getRightChild(), domainType);
                }
            }
            else {
                List<Number> temp_domain = domainList(domain.substring(0, parenthesisIndex).strip(), typesNodes);
                iterableList_domain(temp_domain);
                if (domainType.equals("exhaustive")){
                    typesNodes.setExDomain(temp_domain);
                    domainGrammar(domain.substring(parenthesisIndex + 1).strip(), typesNodes.getLeftChild(), domainType);
                }
                else if (domainType.equals("random")){
                    typesNodes.setRanDomain(temp_domain);
                    domainGrammar(domain.substring(parenthesisIndex + 1).strip(), typesNodes.getLeftChild(), domainType);
                }
            }
        }
        else {
            throw new InvalidConfigException("missing parentheses");
        }
        return null;
    }
    /**
     * Parses the input string(the contents of a JSON file).
     * This should build an APyNode tree for each parameter, where each node's type,
     * exhaustive domain, and random domain should be set up to reflect the contents of the config file.
     * These nodes should be stored in a list in the order that they were specified in the config file.
     * That list, along with the parsed function name and number of random tests to generate, should be
     * placed in a new ConfigFile object and returned.
     * @param contents  contents of a JSON file
     * @return a new config file
     * @throws InvalidConfigException with a descriptive error message
     * if any part of the config file is missing or malformed
     */
 
    public ConfigFile parse(String contents) throws InvalidConfigException{
        // if the contents are valid, parse it
        validConfigFile(contents);
        // create JSONObject
        JSONObject jsonObj = new JSONObject(contents);
        // test that json key's values are of correct type
        if (!(jsonObj.get("fname") instanceof String)){
            throw new InvalidConfigException("fname is not a String!");
        }
        if (!(jsonObj.get("types") instanceof JSONArray)){
            throw new InvalidConfigException("types is not a JSONArray!");
        }
        if (!(jsonObj.get("exhaustive domain") instanceof JSONArray)){
            throw new InvalidConfigException("exhaustive domain is not a JSONArray!");
        }
        if (!(jsonObj.get("random domain") instanceof JSONArray)){
            throw new InvalidConfigException("random domain is not a JSONArray!");
        }
        if (!(jsonObj.get("num random") instanceof Integer)){
            throw new InvalidConfigException("num random is not a Integer!");
        }
        // assign appropriate String, JSONArray, and Integer to appropriate fields
        this.fname = jsonObj.getString("fname");
        this.types = jsonObj.getJSONArray("types");
        this.exDomain = jsonObj.getJSONArray("exhaustive domain");
        this.randDomain = jsonObj.getJSONArray("random domain");
        this.numRandom = jsonObj.getInt("num random");
        checkKeyTypes(jsonObj);

        System.out.println("fname: " + this.fname);
        System.out.println("types: " + this.types);
        System.out.println("exhaustive domain: " + this.exDomain);
        System.out.println("random domain: " + this.randDomain);
        System.out.println("num domain: " + this.numRandom);
        //check that the domain and type arrays have same number of elements, else throw exception
        checkLength(this.types, this.exDomain);
        checkLength(this.types, this.randDomain);
        // create the return list
        List<APyNode<?>> nodeList = new ArrayList<>();
        // iterate through each element in type array and check their grammar
        int index = 0;
        for (Object typeObj: this.types){
            String currentType = (String) typeObj;
            APyNode<?> currentTypeNode = typesGrammar(currentType.strip());
            nodeList.add(currentTypeNode);
            index ++;
        }
        // iterate through each element in domain arrays and call domainGrammar on them
        int count = 0;
        for (Object exDomObj: this.exDomain){
            String current_exDom = (String) exDomObj;
            domainGrammar(current_exDom, nodeList.get(count), "exhaustive");
            count ++;
        }
        int k = 0;
        for (Object randDomObj: this.randDomain){
            String current_randDom = (String) randDomObj;
            domainGrammar(current_randDom, nodeList.get(k), "random");
            k ++;
        }
        //create and return the ConfigFile with proper nodeList generated by recursive helpers and iteration
        return new ConfigFile(this.fname, nodeList,this.numRandom);

    }


}