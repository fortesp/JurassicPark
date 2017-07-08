import java.util.Scanner;

public class JurassicPark {

    // Filename and path of the output file that is created with all the results.
    static final String FILENAME = "output.txt";

    // Defines whether a user wants to run the simulation in debug mode.
    static boolean debug = true;
    static Scanner n = new Scanner(System.in);

    // Java main method
    public static void main(String[] args) throws InterruptedException {

        System.out.println("Welcome to the Jurassic Park simulation!");

        int nGuides   = promptInt("How many Guides? ");
        int nVisitors = promptInt("How many Visitors? ");
        int nCycles   = promptInt("How many cycles? ");

        System.out.println("Do you want debug mode? (yes/no) ");
        debug = (n.next().toLowerCase().trim().equals("yes"))?true:false;

        if(!debug) System.out.println("\r\nRunning... Please wait.");

        Island island = new Island(nGuides, nVisitors, nCycles);

        System.out.println("\r\nEnd of simulation. Final results in output.txt file.");
    }

    // ------------------------------------------------------------------------------

    static int promptInt(String str) {

        String r = "1";
        do {
            System.out.println(str);
             r = n.next();
        } while(!isNumeric(r));

        return Integer.parseInt(r);
    }

    static void debug(String msg) {

        if(debug) System.out.println(msg);
    }

    static void debug(Person person) {

        if(debug) System.out.println(person);
    }

    static void debug(Person person, String msg) {

        debug(person + " >> " + msg);
    }

    static int getRandomInt(int min, int max)
    {
        int range = (max - min) + 1;
        return (int)(Math.random() * range) + min;
    }

    static int getRandomTime() {

        return getRandomInt(100, 1000);
    }

    static boolean isNumeric(String s) {
        return s != null && s.matches("[-+]?\\d*\\.?\\d+");
    }

}
