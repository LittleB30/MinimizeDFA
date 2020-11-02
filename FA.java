import java.io.File;
import java.util.ArrayList;
import java.util.NoSuchElementException;
import java.util.Scanner;
import java.util.Set;

/**
 * This class defines a Finite Automata of either nondeterministic or deterministic capabilities.
 * @author Alex Smith (alsmi14@ilstu.edu)
 */
public class FA {
    private int numStates;
    private ArrayList<Character> alphabets;
    private ArrayList<ArrayList<ArrayList<Integer>>> transitions;
    private int initial;
    private ArrayList<Integer> accepting;
    private boolean isNondeterministic;

    /*********** CONSTRUCTORS ***********/
    /**
     * Constructs an empty finite automata which is assumed to be deterministic.
     */
    public FA() {
        numStates = 0;
        alphabets = new ArrayList<>();
        transitions = new ArrayList<>();
        initial = -1;
        accepting = new ArrayList<>();
        isNondeterministic = false;
    }

    /**
     * Constructs a finite automata from information stored in a file.
     * 
     * @param fileName file to be read from
     */
    public FA(String fileName) {
        alphabets = new ArrayList<>();
        transitions = new ArrayList<>();
        accepting = new ArrayList<>();
        readFA(fileName);
    }

    /**
     * Constructs a finite atomata with given arguments.
     * 
     * @param num    number of states
     * @param alpha  finite list of alphabets
     * @param t      transition function
     * @param i      initial state
     * @param accept list of accepting states
     */
    public FA(int num, ArrayList<Character> alpha, ArrayList<ArrayList<ArrayList<Integer>>> t, int i, ArrayList<Integer> accept) {
        numStates = num;
        alphabets = alpha;
        transitions = t;
        initial = i;
        accepting = accept;
        if (alphabets.size() == transitions.get(0).size()) {// if there is not a lambda closure then it is a DFA.
            isNondeterministic = false;
        } else {
            isNondeterministic = true;
        }
    }

    /*********** PUBLIC METHODS ***********/
    public FA minimizeDFA(){
        FA minDFA = new FA();

        //find partitions and build the minimized transition table
        int[][] distTable = buildDistinguishabilityTable();
        ArrayList<Set<Integer>> partitions = partitionTransitions(distTable);
        ArrayList<ArrayList<ArrayList<Integer>>> minTransitions = minimizePartitions(partitions);

        //initialize the new FA with the minimized transition table
        minDFA.numStates = minTransitions.size();
        minDFA.alphabets = new ArrayList<>(this.alphabets);
        minDFA.transitions = minTransitions;
        minDFA.initial = findInitialState(partitions);
        minDFA.accepting = findAcceptingStates(partitions);

        return minDFA;
    }

    /**
     * Determines if a given string is a sentence of this DFA.
     * 
     * @param str the string to be evaluated
     * @return true if the string is a sentence of this DFA, false otherwise
     */
    public boolean isSentence(String str) {
        boolean isSentence = false;
        if (!isNondeterministic) {
            int state = initial;
            ArrayList<ArrayList<Integer>> cur = transitions.get(state);
            boolean invalidAlphabet = false;
            for (char a : str.toCharArray()) {
                try {
                    state = cur.get(alphabets.indexOf(a)).get(0);
                } catch (IndexOutOfBoundsException e) {
                    if (!(a == '\0')) {
                        invalidAlphabet = true;
                        break;
                    }
                }
                cur = transitions.get(state);
            }
            if (!invalidAlphabet && accepting.contains(state)) {
                isSentence = true;
            }
        }
        return isSentence;
    }

    /**
     * Reads strings from a file and outputs if they are a sentence of this DFA. Note: all false if NFA
     * @param fileName the file to be read from
     */
    public void areSentences(String fileName, int numStrings) {
        if (!isNondeterministic) {
            ArrayList<Boolean> areSentences = new ArrayList<>();
        Scanner scan = null;
		try {
			scan = new Scanner(new File(fileName));
        } catch (Exception e) {
            System.out.println(e.getStackTrace());
            return;
        }
        
        try {
            String curLine;
            for (int i = 0; i < numStrings; i++) { 
                curLine = scan.nextLine().trim();
                areSentences.add(isSentence(curLine));
            }
        } catch (NoSuchElementException e) {}

        System.out.println("Parsing results of strings in " + fileName + " on DFA:");
        int count = 1;
        for (boolean cur : areSentences) {
            System.out.print((cur?"Yes ":"No  "));
            if (count%15 == 0) {
                System.out.println();
                count = 1;
            } else {
                count++;
            }
        }
        scan.close();
        }
    }

    /**
     * Prints the FA based on whether or not it is deterministic.
     */
    public void printFA() {
        if (isNondeterministic) {
            System.out.println("=NFA=");
            System.out.print("Sigma: ");
            for (Character c : alphabets) System.out.print(c + " ");
            System.out.print("\n------");
            for (int i = 0; i < alphabets.size(); i++) System.out.print("--");
            System.out.println();
            for (int i = 0; i < numStates; i++) {
                System.out.print((i < 10000?" ":"") + (i < 1000?" ":"") + (i < 100?" ":"") + (i < 10?" ":"") + i + ": ");
                for (int j = 0; j < transitions.get(0).size()-1; j++) {
                    System.out.print("(" + alphabets.get(j) + "," + transitionToString(i, j) + ") ");
                }
                System.out.println("( " + "," + transitionToString(i, transitions.get(0).size()-1) + ")");
            }
            System.out.print("------");
            for (int i = 0; i < alphabets.size(); i++) System.out.print("--");
        } else {
            System.out.println("=DFA=");
            System.out.print("Sigma:\t");
            for (Character c : alphabets) System.out.print(c + "\t");
            System.out.print("\n---------");
            for (int i = 1; i < alphabets.size(); i++) System.out.print("--------");
            System.out.println();
            for (int i = 0; i < numStates; i++) {
                System.out.print((i < 10000?" ":"") + (i < 1000?" ":"") + (i < 100?" ":"") + (i < 10?" ":"") + i + ":\t");
                for (ArrayList<Integer> transition : transitions.get(i)) {
                    for (int t : transition) {
                        System.out.print(t + "\t");
                    }
                }
                System.out.println();
            }
            System.out.print("---------");
            for (int i = 1; i < alphabets.size(); i++) System.out.print("--------");
        }
        System.out.println("\n" + initial + ": Initial State");
        System.out.println(acceptingToString() + ": Accepting State" + (accepting.size() > 1 ? "s":"") + "\n");
    }

    public String toString() {
        return "M:(Q=" + numStates + 
                ", Σ=" + alphabets.size() + 
                ", δ=" + transitions.get(0).size() + 
                ", q=\"" + initial + "\"" +
                ", A=" + accepting.size() + ")";
    }

    /***********PRIVATE METHODS***********/
    /**
     * 
     * @param partitions
     * @return
     */
    private ArrayList<Integer> findAcceptingStates(ArrayList<Set<Integer>> partitions) { //TODO implement findAcceptingStates

        return null;
    }

    /**
     * 
     * @param partitions
     * @return
     */
    private int findInitialState(ArrayList<Set<Integer>> partitions) {//TODO implement findInitialState
        
        return 0;
    }

    /**
     * 
     * @param partitions
     * @return
     */
    private ArrayList<ArrayList<ArrayList<Integer>>> minimizePartitions(ArrayList<Set<Integer>> partitions) { // TODO implement minimizePartitions
        
        return null;
    }

    /**
     * 
     * @param distTable
     * @return
     */
    private ArrayList<Set<Integer>> partitionTransitions(int[][] distTable) { //TODO implement partitionTransitions
        
        return null;
    }

    /**
     * Builds a distinguishability table between the current states represented by a matrix where 1 indicates indistinguishable and 0 indicates distinguishable.
     * 
     * @return a matrix where 1 indicates indistinguishable and 0 indicates distinguishable
     */
    private int[][] buildDistinguishabilityTable() {
        int[][] distTable = new int[transitions.size()][transitions.size()];

        //fill 1s for states that are simply indistiguishable
        for (int i = 0; i < distTable.length; i++) {
            for (int j = i + 1; j < distTable.length; j++) {
                if (accepting.contains(i) ^ accepting.contains(j)) { //XOR
                    distTable[i][j] = 1;
                    distTable[j][i] = 1;
                }
            }
        }
        
        //contiuously fill 1s for states that are adjacent to indistiguishable states
        boolean madeChange;
        do {
            madeChange = false;
            for (int i = 0; i < distTable.length; i++) {
                for (int j = i + 1; j < distTable.length; j++) {
                    if (distTable[i][j] != 1 && isIndistinguishable(i, j, distTable)) {
                        madeChange = true;
                        distTable[i][j] = 1;
                        distTable[j][i] = 1;
                    }
                }
            }
        } while (madeChange);

        return distTable;
    }

    /**
     * 
     * @param s1
     * @param s2
     * @param distTable
     * @return
     */
    private boolean isIndistinguishable(int s1, int s2, int[][] distTable) {
        int adjToS1;
        int adjToS2;
        boolean isIndistinguishable = false;
        for (int i = 0; i < alphabets.size() && !isIndistinguishable; i++) {
            adjToS1 = transitions.get(s1).get(i).get(0);
            adjToS2 = transitions.get(s2).get(i).get(0);
            if (distTable[adjToS1][adjToS2] == 1) {
                isIndistinguishable = true;
            }
        }
        return isIndistinguishable;
    }
    
    /**
     * Reads an FA from a properly formated file.
     */
    private void readFA(String fileName) {
        Scanner scan = null;
		try {
			scan = new Scanner(new File(fileName));
        } catch (Exception e) {
            System.out.println(e.getStackTrace());
            return;
        }
        
        numStates = Integer.parseInt(scan.nextLine());

        String curLine = scan.nextLine();
        char[] alpha = curLine.substring(curLine.indexOf(":")+1).replaceAll("\\s", "").toCharArray();
        for (char a : alpha) alphabets.add(a);

        scan.nextLine();
        for (int i = 0; i < numStates; i++) { //for each state
            curLine = scan.nextLine();
            char[] t = curLine.substring(curLine.indexOf(":")+1).replaceAll("\\s", "").toCharArray();
            
            ArrayList<ArrayList<Integer>> trans = new ArrayList<>();
            for (char s : t) {
                ArrayList<Integer> state = new ArrayList<>();
                state.add(Character.getNumericValue(s));
                trans.add(state);
            }

            transitions.add(trans); //add this state's transitions to the total transition function
        }

        //read an assign initial state
        scan.nextLine();
        curLine = scan.nextLine().trim();
        initial = Integer.parseInt(curLine.substring(0, curLine.indexOf(":")));

        //read and assign accepting state(s)
        curLine = scan.nextLine().replaceAll("\\s", "");
        String[] a = curLine.substring(0, curLine.indexOf(":")).split(",");
        for (String state : a) {
            accepting.add(Integer.valueOf(state));
        }

        if (alphabets.size() == transitions.get(0).size()) {//if there is not a lambda closure then it is a DFA.
            isNondeterministic = false;
        } else {
            isNondeterministic = true;
        }
        scan.close();
    }

    /**
     * @param state the desired state
     * @param alpha the desired alphabet
     * @return a string form of the transition function for a given state and alphabet
     */
    private String transitionToString(int state, int alpha) {
        String temp = "{";
        if (transitions.get(state).get(alpha).size() > 0) {
            for (int num : transitions.get(state).get(alpha)) {
                temp += num + ",";
            }
            temp += "\b";
        }
        temp += "}";
        return temp;
    }

    /**
     * @return a string form of the list of accepting states
     */
    private String acceptingToString() {
        String temp = "";
        for (int accept : accepting) {
            temp += accept + ",";
        }
        temp += "\b";
        return temp;
    }

    /***********GETTERS***********/
    public int getNumStates() {
        return numStates;
    }

    public ArrayList<Character> getAlphabets() {
        return alphabets;
    }

    public ArrayList<ArrayList<ArrayList<Integer>>> getTransitions() {
        return transitions;
    }

    public int getInitial() {
        return initial;
    }

    public ArrayList<Integer> getAccepting() {
        return accepting;
    }

    public boolean isNondeterministic() {
        return isNondeterministic;
    }
}