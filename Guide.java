import java.util.concurrent.Semaphore;

public class Guide extends Person {

    public final static int SLEEPING = 0,
                            WORKING  = 1;

    // This semaphore determines when a Guide should be sleeping. It is awaken by the Manager.
    static Semaphore active = new Semaphore(0);

    public Guide(Island island) {
        super(island);
    }

    public void run() {

        try {

            while(true) {

                sleeping();

                loading();

                visiting();

                unloading();
            }

        } catch(InterruptedException e) { }

    }

    private void sleeping() throws InterruptedException {

        setCurrentState(Guide.SLEEPING);
        Guide.active.acquire();
    }

    private void visiting() throws InterruptedException {

        // Visiting park...
        setCurrentState(Guide.WORKING);
        sleep(JurassicPark.getRandomInt(1000, 3000));
    }

    private void loading() throws InterruptedException {

        island.loadVisitors(this);
    }

    private void unloading() throws InterruptedException {

        island.unloadVisitors(this);
    }

    public String getStateDescription(int state) {

        switch(state) {
            case SLEEPING  : return "SLEEPING";
            case WORKING   : return "WORKING";
        }

        return null;
    }

}
