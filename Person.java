
abstract public class Person extends Thread {

    protected Island island = null;

    private int custom_state = 0;

    public Person(Island island) {

        this.island = island;
    }

    public void start() {

        super.start();
    }

    void sleep() throws InterruptedException {

        sleep(JurassicPark.getRandomTime());
    }

    final int getCurrentState() {

        return custom_state;
    }

    void setCurrentState(int state) {

        custom_state = state;

        island.reportStatus();
    }

    public String getStateDescription() {

        return getStateDescription(getCurrentState());
    }

    public String getType() {

        return this.getClass().toString().replace("class", "").trim();
    }

    protected abstract String getStateDescription(int state);

    public String toString() {

        return String.format("%1$10s", getType())+ " " + getId() + " | State: " + this.getStateDescription();
    }
}
