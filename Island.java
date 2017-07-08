import java.io.FileNotFoundException;
import java.io.PrintWriter;
import java.util.concurrent.Semaphore;


public class Island {

    static final int MAX_GROUP = 3;

    private Person[]  persons;
    private Visitor[] visitors;
    private Guide[]   guides;

    private Visitor[] group = new Visitor[MAX_GROUP];

    private String  report = "";
    private Manager manager;

    Semaphore queue;

    private int cycles = 1;

    public Island(int nGuides, int nVisitors, int nCycles) {

        // Initializes the arrays for n length
        visitors = new Visitor[nVisitors];
        guides   = new Guide[nGuides];

        // Prepares all the Thread objects
        while (nVisitors-- > 0) getVisitors()[nVisitors] = new Visitor(this);
        while (nGuides-- > 0)   getGuides()[nGuides]     = new Guide(this);
        setManager(new Manager(this));

        setCycles(nCycles);

        // Create a general array with all the threads that inherit Person for easier general iterations. e.g threadWatcher and reportStatus methods
        persons= new Person[getVisitors().length + getGuides().length + 1];
        System.arraycopy(getVisitors(), 0, persons, 0, getVisitors().length);
        System.arraycopy(getGuides(),   0, persons, getVisitors().length, getGuides().length);
        persons[persons.length - 1] = getManager();

        // Queue semaphore initiated with fairness active, which means FIFO ordering is active
        queue = new Semaphore(0, true);

        // Starts and stops simulation
        startSimulation();
    }

    private void startSimulation() {

        // Start all Threads
        getManager().start();
        for (Person person : getVisitors()) person.start();
        for (Person person : getGuides())   person.start();

        // Thread watcher for debugging
        Thread tW = null;
        if(JurassicPark.debug) tW = startThreadWatcher();

        // Wait for Visitor threads to die and interrupt the others that are waiting
        try {
            for (Person person : getVisitors()) {
                person.join();
            }
            for (Person person : getGuides()) {
                person.interrupt();
            }

            manager.interrupt();

        } catch(InterruptedException e) {}
        finally {

            // Save results to the file
            try {
                PrintWriter out = new PrintWriter(JurassicPark.FILENAME);
                out.println(report);
                out.close();

                if(JurassicPark.debug) tW.interrupt();

            } catch(FileNotFoundException e) { } finally { }
        }

    }

    // Watcher thread for debugging purposes
    private Thread startThreadWatcher() {

        // Lambda expression to create a thread on the fly.
       Thread t = new Thread(() -> {
                try {

                    while (true) {

                        Thread.sleep(20);

                        for (Person person : getPersons()) JurassicPark.debug(person);

                        System.out.println();
                    }

                } catch (InterruptedException e) { }
        });

        t.start();

        return t;
    }

    // Visitors will call this method to wait in the queue for a visit to the park
    void waitInQueue(Visitor visitor) throws InterruptedException {

        queue.acquire();

        wakeUpManager(visitor);
    }

    // Guides will call this method when a trip to the park starts
    synchronized void loadVisitors(Guide guide) throws InterruptedException {

        for (Visitor visitor : getGroup()) {
            if(visitor != null) {
                visitor.setCurrentState(Visitor.BOARDING);
                visitor.setGuide(guide);
                visitor.ready.release();
            }
        }

    }

    // Guides will call this method when a trip to the park finishes
    synchronized void unloadVisitors(Guide guide) throws InterruptedException {

        // Wakes up all Visitors holding this Guide lock. All Visitors taking a trip with this particular Guide.
       synchronized (guide) {
           guide.notifyAll();
       }

       JurassicPark.debug("Trip with Guide " + guide.getId() + " is over. Waking up manager...");
       wakeUpManager(guide);
    }

    // Wakes up the manager when waiting for Visitors or Guide
    void wakeUpManager(Person person) {

        switch(person.getType()) {
            case "Guide"   : manager.guide.release();  break;
            case "Visitor" : manager.active.release(); break;
        }
    }

    // Method to report the status of each thread Person for the final report
    void reportStatus() {

        String line = "";

        synchronized(this) {

            // Report header
            if (report.isEmpty()) {
                for (Person person : getPersons()) {
                    line += String.format("%1$-13s", person.getType() + " " + person.getId());
                }
                line += "\r\n";
            }

            // Report body
            for (Person person : getPersons()) {
                line += String.format("%1$-13s", person.getStateDescription());
            }

            report += line + "\r\n";
        }
    }

    public Manager getManager() { return manager;  }

    public void setManager(Manager manager) { this.manager = manager;  }

    public Person[] getPersons() { return persons; }

    public Visitor[] getVisitors() { return visitors; }

    public Guide[] getGuides() { return guides; }

    public Visitor[] getGroup() { return group; }

    public void clearGroup() { this.group = new Visitor[MAX_GROUP]; }

    public int getCycles() { return cycles; }

    public void setCycles(int cycles) { this.cycles = cycles; }

}
