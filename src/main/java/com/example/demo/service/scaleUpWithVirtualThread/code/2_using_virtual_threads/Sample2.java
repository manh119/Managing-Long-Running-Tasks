import java.util.*;

public class Sample2 {
  private static Scanner scanner = new Scanner(System.in);

  public static void doWork() {
    try { Thread.sleep(5000); } catch(Exception ex) {}
  }

  public static void pause() {
    System.out.println("Press return to continue");

    scanner.nextLine();
  }

  public static void main(String[] args) {
    var MAX = 1000;

    pause();

    for(var i = 0; i < MAX; i++) {
      //new Thread(() -> doWork()).start();
      Thread.startVirtualThread(() -> doWork());
    }

    pause();
  }
}

