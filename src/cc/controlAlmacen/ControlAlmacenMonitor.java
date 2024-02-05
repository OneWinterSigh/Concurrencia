package cc.controlAlmacen;

import java.util.Set;
import java.util.Iterator;
import java.util.LinkedList;

import es.upm.aedlib.map.*;
import es.upm.babel.cclib.Monitor;
import es.upm.aedlib.indexedlist.*;

public class ControlAlmacenMonitor implements ControlAlmacen {

  // Resource state
  HashTableMap<String, Producto> almacenMonitor = new HashTableMap<>();
  // Monitors and conditions
  Monitor mutex = new Monitor();
  IndexedList<Object> listaCondiciones = new ArrayIndexedList<>();
  IndexedList<Object> listaDePendientes = new ArrayIndexedList<>();

  public ControlAlmacenMonitor(java.util.Map<String, Integer> m) {
    Set<String> keys = m.keySet();

    // Relleno el almacen con todos los productos del catalogo, inicialidados a 0
    Iterator iter = keys.iterator();
    Object prov;
    while (iter.hasNext()) {
      prov = (String) iter.next();
      almacenMonitor.put((String) prov,
          new Producto(m.get((String) prov)));
    }
  }

  // TODO -> rutina que desbloquea procesos
  private void desbloqueadorGenerico() {
    LinkedList<String> listaEntregas = new LinkedList<String>();
    LinkedList<String> listaOfrecerReabastecer = new LinkedList<String>();

    Iterator iter = this.listaCondiciones.iterator();

    Object prov;
    boolean liberado = false;

    while (iter.hasNext() && !liberado) {
      prov = iter.next();
      if (prov instanceof EspEntrega) {

        Producto producto = almacenMonitor.get(((EspEntrega) prov).prodId);
        if (((EspEntrega) prov).cantidad <= producto.getDisponibles()
            && !listaEntregas.contains(((EspEntrega) prov).prodId)) {
          ((EspEntrega) prov).c.signal();
          ((EspEntrega) prov).c.signal();
          liberado = true;
          listaCondiciones.remove(prov);
        } else {
          listaEntregas.addLast(((EspEntrega) prov).prodId);
        }
      } else if (prov instanceof EspOfrecerReabastecer) {
        if (almacenMonitor.get(((EspOfrecerReabastecer) prov).prodId).getDisponibles()
            + almacenMonitor.get(((EspOfrecerReabastecer) prov).prodId).getEnCamino()
            - almacenMonitor.get(((EspOfrecerReabastecer) prov).prodId).getComprados() < almacenMonitor
                .get(((EspOfrecerReabastecer) prov).prodId).getMinDisponibles()
            && !listaOfrecerReabastecer.contains(((EspOfrecerReabastecer) prov).prodId)) {
          ((EspOfrecerReabastecer) prov).c.signal();
          liberado = true;
          listaCondiciones.remove(prov);
        } else {
          listaOfrecerReabastecer.addLast(((EspOfrecerReabastecer) prov).prodId);
        }
      }
    }

  }

  private Object encontrarCondition(String tipoCondition, String clientId, String itemId, int cantidad) {
    Iterator iter = this.listaCondiciones.iterator();

    Object condition = null;
    boolean encontrado = false;

    while (iter.hasNext() && !encontrado) {
      condition = iter.next();
      if (condition instanceof EspEntrega && tipoCondition.equals("entrega")) {
        if (((EspEntrega) condition).clientId.equals(clientId) && ((EspEntrega) condition).prodId.equals(itemId)) {
          encontrado = true;
        }
      } else if (condition instanceof EspOfrecerReabastecer && tipoCondition.equals("ofrecerReabastecer")) {
        if (((EspOfrecerReabastecer) condition).prodId.equals(itemId)) {
          encontrado = true;
        }
      }
    }
    if (!encontrado) {
      switch (tipoCondition) {
        case "entrega":
          condition = new EspEntrega(itemId, clientId, cantidad);
          break;
        case "ofrecerReabastecer":
          condition = new EspOfrecerReabastecer(itemId, cantidad);
          break;
        default:
          break;
      }
      listaCondiciones.add(listaCondiciones.size(), condition);
    }

    return condition;
  }

  public boolean comprar(String clientId, String productoId, int cantidad) throws IllegalArgumentException {

    mutex.enter();
    Producto prod = almacenMonitor.get(productoId);
    boolean compraEfectiva = false;
    if (prod != null && cantidad > 0 && clientId != null) {
      compraEfectiva = prod.getEnCamino() + prod.getDisponibles() >= prod.getComprados() + cantidad;

      if (compraEfectiva) {
        prod.setComprados(prod.getComprados() + cantidad);
      }
    } else {
      mutex.leave();
      throw new IllegalArgumentException();
    }
    desbloqueadorGenerico();
    mutex.leave();
    return compraEfectiva;
  }

  public void entregar(String clientId, String itemId, int cantidad) throws IllegalArgumentException {
    mutex.enter();

    Producto prod = almacenMonitor.get(itemId);

    if (prod != null && cantidad > 0) {

      if (prod.getDisponibles() < cantidad) {
        EspEntrega condition = (EspEntrega) encontrarCondition("entrega", clientId, itemId, cantidad);
        condition.c.await();
      }
      prod.setDisponibles(prod.getDisponibles() - cantidad);
      prod.setComprados(prod.getComprados() - cantidad);

    } else {
      mutex.leave();
      throw new IllegalArgumentException();
    }

    desbloqueadorGenerico();

    mutex.leave();
  }

  public void devolver(String clientId, String itemId, int cantidad) throws IllegalArgumentException {

    mutex.enter();

    Producto prod = almacenMonitor.get(itemId);

    if (prod != null && cantidad > 0) {
      prod.setDisponibles(prod.getDisponibles() + cantidad);
    } else {
      mutex.leave();
      throw new IllegalArgumentException();
    }
    desbloqueadorGenerico();
    mutex.leave();

  }

  public void ofrecerReabastecer(String itemId, int cantidad) throws IllegalArgumentException {
    mutex.enter();

    Producto prod = almacenMonitor.get(itemId);

    if (prod != null && cantidad > 0) {
      if (!(prod.getDisponibles() + prod.getEnCamino() - prod.getComprados() < prod.getMinDisponibles())) {
        EspOfrecerReabastecer condition = (EspOfrecerReabastecer) encontrarCondition("ofrecerReabastecer", null, itemId,
            cantidad);
        condition.c.await();
      }

      prod.setEnCamino(prod.getEnCamino() + cantidad);
    } else {
      mutex.leave();
      throw new IllegalArgumentException();
    }

    desbloqueadorGenerico();

    mutex.leave();
  }

  public void reabastecer(String itemId, int cantidad) throws IllegalArgumentException {
    mutex.enter();

    Producto prod = almacenMonitor.get(itemId);
    if (prod != null && cantidad > 0) {
      prod.setDisponibles(prod.getDisponibles() + cantidad);
      prod.setEnCamino(prod.getEnCamino() - cantidad);
    } else {
      mutex.leave();
      throw new IllegalArgumentException();
    }

    desbloqueadorGenerico();

    mutex.leave();
  }

  public class EspEntrega {
    public String prodId;
    public String clientId;
    public int cantidad;
    public Monitor.Cond c;

    public EspEntrega(String prodId, String clientId, int cantidad) {
      this.prodId = prodId;
      this.clientId = clientId;
      this.cantidad = cantidad;
      this.c = mutex.newCond();
    }

  }

  public class EspOfrecerReabastecer {
    public String prodId;
    public int cantidad;
    public Monitor.Cond c;

    public EspOfrecerReabastecer(String prodId, int cantidad) {
      this.prodId = prodId;
      this.cantidad = cantidad;
      this.c = mutex.newCond();
    }
  }

  private class Producto {
    private int disponibles;
    private int enCamino;
    private int comprados;
    private int minDisponibles;

    public Producto(int n) {
      this.disponibles = 0;
      this.enCamino = 0;
      this.comprados = 0;
      this.minDisponibles = n;
    }

    public int getDisponibles() {
      return this.disponibles;
    }

    public int getEnCamino() {
      return this.enCamino;
    }

    public int getComprados() {
      return this.comprados;
    }

    public int getMinDisponibles() {
      return this.minDisponibles;
    }

    public void setDisponibles(int n) {
      this.disponibles = n;
    }

    public void setEnCamino(int n) {
      this.enCamino = n;
    }

    public void setComprados(int n) {
      this.comprados = n;
    }

  }
}
