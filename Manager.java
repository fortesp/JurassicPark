import java.util.concurrent.Semaphore;
import java.util.concurrent.TimeUnit;

public class Manager extends Person {

    public final static int SLEEPING = 0,
                            GROUPING = 1,
                            WAITING  = 2,
                            ORGANIZE = 3;

    // Semaphore to determine when the Manager should be sleeping. Released by Visitors. Initialy closed.
    Semaphore active = new Semaphore(0);
    // Semaphore to make the Manager wait for an available Guide. Released by Guides. Initialy closed.
    Semaphore guide  = new Semaphore(0);

    public Manager(Island island) {

        super(island);
    }

    public void run() {

        try {

            while (true) {

                sleeping();

                grouping();

                waitforguide();

                organizing();
            }

        } catch (InterruptedException e) {  }
    }

    private void sleeping() throws InterruptedException {

        setCurrentState(SLEEPING);

        // Closes the guide Sempahore
        guide.drainPermits();

        // Releases 3 permits for the Queue Semaphore
        island.queue.release(3);

        // Closes the active semaphore and tries to acquire a permit immediately after.
        active.drainPermits();
        active.acquire();
    }

    private void grouping() throws InterruptedException {

        setCurrentState(GROUPING);

        JurassicPark.debug("Waiting for " + island.queue.availablePermits() + " visitors to fill the car.");

        // 100ms to wait for more visitors to come to fill the gap
        active.tryAcquire(100, TimeUnit.MILLISECONDS);

        // Closes the queue Semaphore. Nobody else can come until it finihes the rest.
        island.queue.drainPermits();
     }

    private void waitforguide() throws InterruptedException {

        setCurrentState(WAITING);
        if (!Guide.active.hasQueuedThreads()) guide.acquire();

    }

    private void organizing() throws InterruptedException {

        setCurrentState(ORGANIZE);

        // Clears the group array
        island.clearGroup();

        // Adds the visitors that are ready for boarding into the temporary array group.
        int i = 0;
        for (Visitor visitor : island.getVisitors()) {
            if (visitor.ready.hasQueuedThreads()) {
                 island.getGroup()[i++] = visitor;
                 if(i == island.MAX_GROUP) break;
            }
        }

        // Calls the guide to get back to work!
        JurassicPark.debug("Manager calls a guide.");
        Guide.active.release();
    }

    public String getStateDescription(int state) {

        switch (state) {

            case SLEEPING: return "SLEEPING";
            case GROUPING: return "GROUPING";
            case WAITING:  return "WAITING";
            case ORGANIZE: return "ORGANIZE";
        }

        return null;
    }
}
