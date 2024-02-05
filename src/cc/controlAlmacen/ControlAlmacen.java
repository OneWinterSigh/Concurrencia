package cc.controlAlmacen;

public interface ControlAlmacen {
  public boolean comprar(String clientId, String productoId, int cantidad);
  public void entregar(String clientId, String productoId, int cantidad);
  public void devolver(String clientId, String productoId, int cantidad);

  public void ofrecerReabastecer(String productoId, int cantidad);
  public void reabastecer(String productoId, int cantidad);
}
