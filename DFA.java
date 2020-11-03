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
        String faFile = "dfaA";
        String stringsFile = "strings.txt";
        FA dfa = new FA(faFile);
        FA minDFA = dfa.minimizeDFA();
        System.out.println("=Minimized DFA from " + faFile + "=");
        minDFA.printFA();
        minDFA.areSentences(stringsFile, 30);
    }
}
