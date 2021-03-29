public class Task {
   private int humans;
   private int floors;
   private int capacity;
   private int delay;
   
   public Task(int humans, int floors, int capacity, int delay) {
       this.humans = humans;
       this.floors = floors;
       this.capacity = capacity;
       this.delay = delay;
   }
   
   public int getHumans() {
       return humans;
   }
   
   public int getFloors() {
       return floors;
   }
   
   public int getCapacity() {
       return capacity;
   }
   
   public int getDelay() {
       return delay;
   }

   public void setDelay(int delay) {
       this.delay = delay;
   }
   
}
