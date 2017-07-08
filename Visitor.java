import java.util.concurrent.Semaphore;

public class Visitor extends Person {


    public final static int ARRIVING  = 0,
                            DECIDING  = 1,
                            VISITINGM = 2,
                            WAITING   = 3,
                            BOARDING  = 4,
                            VISITINGI = 5,
                            LEAVING   = 6;

    // Guide assigned to the Visitor for a specific trip to the park. Also a lock, released by the Guide.
    private Guide guide;

    // Semaphore that makes a Visitor wait to initiate a visit to the park. Released by a Guide.
    Semaphore ready = new Semaphore(0);

    public Visitor(Island island) {

        super(island);
    }

    public void run() {

        try {

            arrive();

            decide();

            int cycles = island.getCycles();

            while (cycles-- > 0) {

                visitMuseum();

                waiting();

                boarding();

                visitingPark();
            }

        } catch(InterruptedException e) {
        } finally {

            leave();
        }

    }

    private void arrive() throws InterruptedException {

        setCurrentState(ARRIVING);
        sleep();
    }

    private void decide() throws InterruptedException  {

        setCurrentState(DECIDING);
        sleep();
    }

    private void visitMuseum() throws InterruptedException {

        setCurrentState(VISITINGM);
        sleep();
    }

    private void waiting() throws InterruptedException {

        setCurrentState(Visitor.WAITING);
        island.waitInQueue(this);
    }

    private void boarding() throws InterruptedException {

        JurassicPark.debug("Visitor " + getId() + " is ready.");
        ready.acquire();
    }

    private void visitingPark() throws InterruptedException {

        setCurrentState(VISITINGI);

        synchronized (guide) {
            guide.wait();
        }

        guide = null;
        ready.drainPermits();
    }

    private void leave() {

        setCurrentState(LEAVING);
    }

    public Guide getGuide() {
        return guide;
    }

    public void setGuide(Guide guide) {
        this.guide = guide;
    }

    public String getStateDescription(int state) {

        switch(state) {

            case ARRIVING  : return "ARRIVING";
            case DECIDING  : return "DECIDING";
            case VISITINGM : return "VISITINGM";
            case WAITING   : return "WAITING";
            case BOARDING  : return "BOARDING";
            case VISITINGI : return "VISITINGI";
            case LEAVING   : return "LEAVING";
        }

        return null;
    }

    public String toString() {

        String str = (getGuide() != null)?" >> Guide " + getGuide().getId():"";

        str += (ready.hasQueuedThreads())?" >> is ready.":"";

        return super.toString() + str;

    }
}
