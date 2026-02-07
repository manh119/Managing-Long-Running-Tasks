import java.util.*;
import java.nio.file.*;
import java.util.concurrent.*;

public class Sample6 {
  private static Scanner scanner = new Scanner(System.in);

  public static void pause() {
    System.out.println("Press return to continue");

    scanner.nextLine();
  }

  public static void fetch() {
    try {
      var size = Files.lines(Paths.get("Sample.java")).count();
    } catch(Exception ex) {
    }
  }
   
  public static void main(String[] args) throws Exception {
    var MAX = 1000;
    //var MAX = 10000;
    
    pause();

    //var executorService = Executors.newFixedThreadPool(MAX);
    var executorService = Executors.newVirtualThreadPerTaskExecutor();

    for(var i = 0; i < MAX; i++) {
      executorService.submit(() -> fetch());
    }

    executorService.shutdown();
    executorService.awaitTermination(10, TimeUnit.SECONDS);

    pause();
  }
}

