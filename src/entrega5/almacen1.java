package entrega5;

// TODO: importar la clase de los semáforos.
import es.upm.babel.cclib.Semaphore;

// Almacen concurrente para un dato
class Almacen1 {
	// Producto a almacenar
	private int almacenado;

	// TODO: declaración e inicialización de los semáforos
	// necesarios
	Semaphore semaforo1 = new Semaphore(1);
	Semaphore semaforo2 = new Semaphore(0);

	public Almacen1() {
	}

	public void almacenar(int producto) {
		// TODO: protocolo de acceso a la sección crítica y código de
		// sincronización para poder almacenar.
		//
		// Codigo de acceso a la seccion crítica
		semaforo1.await();

		// Sección crítica
		almacenado = producto;

		// TODO: protocolo de salida de la sección crítica y código de
		// sincronización para poder extraer.
		semaforo2.signal();
	}

	public int extraer() {
		int result;

		// TODO: protocolo de acceso a la sección crítica y código de
		// sincronización para poder extraer.
		semaforo2.await();

		// Sección crítica
		result = almacenado;

		// TODO: protocolo de salida de la sección crítica y código de
		// sincronización para poder almacenar.
		semaforo1.signal();
		return result;
	}
}
