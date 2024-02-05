package cc.controlAlmacen;

import java.util.Map;
import java.util.Hashtable;

public class Plata {

  public static void main(String[] args) {
    Map<String, Integer> m = new Hashtable();
    m.put("ps5", 1);
    m.put("botellas_de_agua", 10);
    ControlAlmacen cr = new ControlAlmacenCSP(m);
    System.out.println("Almacen initializado con " + m);

    Cliente c1ps5 = new Cliente("juan", "ps5", cr);
    Cliente c2ps5 = new Cliente("paloma", "ps5", cr);
    Cliente c1ba = new Cliente("juan", "botellas_de_agua", cr);

    Fabrica fps5 = new Fabrica("ps5", cr);
    Fabrica fba = new Fabrica("botellas_de_agua", cr);

    c1ba.start();
    c2ps5.start();
    c1ps5.start();
    fps5.start();
    fba.start();
  }
}
