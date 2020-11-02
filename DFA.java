/**
 * This is the driver class used to run the FA class.
 * @author Alex Smith (alsmi14@ilstu.edu)
 */
public class DFA {
    public static void main(String[] args) {
        // if (args.length < 2) {
        //     return;
        // }
        // String faFile = args[0];
        // String stringsFile = args[1];
        String faFile = "dfaX";
        String stringsFile = "strings.txt";
        FA dfa = new FA(faFile);
        dfa.printFA();
        FA minDFA = dfa.minimizeDFA();
        // minDFA.printFA();
        // minDFA.areSentences(stringsFile, 30);
    }
}
