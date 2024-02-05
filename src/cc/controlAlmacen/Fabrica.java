package cc.controlAlmacen;

import es.upm.babel.cclib.ConcIO;
import java.util.Random;

public class Fabrica extends Thread {
  private final String itemIndice;
  private final ControlAlmacen cr;
  private Random random = new Random(0);

  public Fabrica (String itemIndice,
                  ControlAlmacen cr) {
    this.itemIndice = itemIndice;
    this.cr = cr;
  }

  public void run () {
    while (true) {
      int n = random.nextInt(5)+1;
      ConcIO.printfnl("Fabrica ofrece %d productos %s", n, itemIndice);
      cr.ofrecerReabastecer(itemIndice,n);

      try { Thread.sleep(random.nextInt(100)); } catch (InterruptedException x) { };
      ConcIO.printfnl("Fabrica manda %d productos %s", n, itemIndice);
      cr.reabastecer(itemIndice,n);
      ConcIO.printfnl("%d productos %s han llegado al almacen", n, itemIndice);

      try { Thread.sleep(random.nextInt(100)); } catch (InterruptedException x) { };
    }
  }
}
