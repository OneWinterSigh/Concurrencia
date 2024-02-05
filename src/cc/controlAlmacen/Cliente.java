package cc.controlAlmacen;

import es.upm.babel.cclib.ConcIO;
import java.util.Random;

public class Cliente extends Thread {
  private final String indice;
  private final String itemIndice;
  private final ControlAlmacen cr;
  private Random random = new Random(0);

  public Cliente (String indice,
                  String itemIndice,
                  ControlAlmacen cr) {
    this.indice = indice;
    this.itemIndice = itemIndice;
    this.cr = cr;
  }

  public void run () {
    while (true) {
      
      int n = random.nextInt(2)+1;
      ConcIO.printfnl("Cliente %s intenta comprar %d productos %s", indice, n, itemIndice);
      boolean result = cr.comprar(indice,itemIndice,n);
      if (result)
        ConcIO.printfnl("Cliente %s compro %d productos %s", indice, n, itemIndice);
      else
        ConcIO.printfnl("Cliente %s NO logro comprar %d productos %s", indice, n, itemIndice);

      if (result) {
        try { Thread.sleep(random.nextInt(100)); } catch (InterruptedException x) { };
        ConcIO.printfnl("Cliente %s pide entrega de %d productos %s", indice, n, itemIndice);
        cr.entregar(indice,itemIndice,n);
        ConcIO.printfnl("Cliente %s recibio entrega de %d productos %s", indice, n, itemIndice);
        
        try { Thread.sleep(random.nextInt(100)); } catch (InterruptedException x) { };
        if (random.nextInt(2) == 0) {
          ConcIO.printfnl("Cliente %s devuelve %d productos %s", indice, n, itemIndice);
          cr.devolver(indice,itemIndice,n);
          try { Thread.sleep(random.nextInt(100)); } catch (InterruptedException x) { };
        }
      } else try { Thread.sleep(random.nextInt(100)); } catch (InterruptedException x) { };

    }
  }
}
