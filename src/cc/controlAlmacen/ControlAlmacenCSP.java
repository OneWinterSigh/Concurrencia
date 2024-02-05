package cc.controlAlmacen;

// paso de mensajes con JCSP
import org.jcsp.lang.*;

// TODO: otros imports
import es.upm.aedlib.map.*;
import es.upm.aedlib.indexedlist.*;
import es.upm.aedlib.Entry;

import java.util.Map;
import java.util.Hashtable;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.Set;

public class ControlAlmacenCSP implements ControlAlmacen, CSProcess {

	// algunas constantes de utilidad
	static final int PRE_KO = -1;
	static final int NOSTOCK = 0;
	static final int STOCKOK = 1;
	static final int SUCCESS = 0;
	// TODO: añadid las que creáis convenientes

	// canales para comunicación con el servidor
	private final Any2OneChannel chComprar = Channel.any2one();
	private final Any2OneChannel chEntregar = Channel.any2one();
	private final Any2OneChannel chDevolver = Channel.any2one();
	private final Any2OneChannel chOfrecerReabastecer = Channel.any2one();
	private final Any2OneChannel chReabastecer = Channel.any2one();

	// Resource state --> server side !!

	// peticiones de comprar
	private static class PetComprar {
		public String productId;
		public int q;
		public One2OneChannel chresp;

		PetComprar(String productId, int q) {
			this.productId = productId;
			this.q = q;
			this.chresp = Channel.one2one();
		}
	}

	// peticiones de entregar
	private static class PetEntregar {
		public String productId;
		public int q;
		public One2OneChannel chresp;

		PetEntregar(String productId, int q) {
			this.productId = productId;
			this.q = q;
			this.chresp = Channel.one2one();
		}
	}

	// peticiones de devolver
	private static class PetDevolver {
		public String productId;
		public int q;
		public One2OneChannel chresp;

		PetDevolver(String productId, int q) {
			this.productId = productId;
			this.q = q;
			this.chresp = Channel.one2one();
		}
	}

	// para aplazar peticiones de ofrecerReabastecer
	private static class PetOfrecerReabastecer {
		public String productId;
		public int q;
		public One2OneChannel chresp;

		PetOfrecerReabastecer(String productId, int q) {
			this.productId = productId;
			this.q = q;
			this.chresp = Channel.one2one();
		}
	}

	// peticiones de reabastecer
	private static class PetReabastecer {
		public String productId;
		public int q;
		public One2OneChannel chresp;

		PetReabastecer(String productId, int q) {
			this.productId = productId;
			this.q = q;
			this.chresp = Channel.one2one();
		}
	}

	// INTERFAZ ALMACEN
	public boolean comprar(String clientId, String itemId, int cantidad) {

		// petición al servidor
		PetComprar pet = new PetComprar(itemId, cantidad);
		chComprar.out().write(pet);

		// recibimos contestación del servidor
		// puede ser una de {PRE_KO, NOSTOCK, STOCKOK}
		int respuesta = (Integer) pet.chresp.in().read();

		// no se cumple PRE:
		if (respuesta == PRE_KO)
			throw new IllegalArgumentException();
		// se cumple PRE:
		return (respuesta == STOCKOK);
	}

	public void entregar(String clientId, String itemId, int cantidad) {
		// petición al servidor
		PetEntregar pet = new PetEntregar(itemId, cantidad);
		chEntregar.out().write(pet);

		// recibimos contestación del servidor
		// puede ser una de {PRE_KO, NOSTOCK, STOCKOK}
		int respuesta = (Integer) pet.chresp.in().read();

		// no se cumple PRE:
		if (respuesta == PRE_KO)
			throw new IllegalArgumentException();
	}

	public void devolver(String clientId, String itemId, int cantidad) {
		// petición al servidor
		PetDevolver pet = new PetDevolver(itemId, cantidad);
		chDevolver.out().write(pet);

		// recibimos contestación del servidor
		// puede ser una de {PRE_KO, NOSTOCK, STOCKOK}
		int respuesta = (Integer) pet.chresp.in().read();

		// no se cumple PRE:
		if (respuesta == PRE_KO)
			throw new IllegalArgumentException();
	}

	public void ofrecerReabastecer(String itemId, int cantidad) {
		// petición al servidor
		PetOfrecerReabastecer pet = new PetOfrecerReabastecer(itemId, cantidad);
		chOfrecerReabastecer.out().write(pet);

		// recibimos contestación del servidor
		// puede ser una de {PRE_KO, NOSTOCK, STOCKOK}
		int respuesta = (Integer) pet.chresp.in().read();

		// no se cumple PRE:
		if (respuesta == PRE_KO)
			throw new IllegalArgumentException();
	}

	public void reabastecer(String itemId, int cantidad) {
		// petición al servidor
		PetReabastecer pet = new PetReabastecer(itemId, cantidad);
		chReabastecer.out().write(pet);

		// recibimos contestación del servidor
		// puede ser una de {PRE_KO, NOSTOCK, STOCKOK}
		int respuesta = (Integer) pet.chresp.in().read();

		// no se cumple PRE:
		if (respuesta == PRE_KO)
			throw new IllegalArgumentException();
	}

	// atributos de la clase
	Map<String, Integer> tipoProductos; // stock mínimo para cada producto

	public ControlAlmacenCSP(Map<String, Integer> tipoProductos) {
		this.tipoProductos = tipoProductos;
		new ProcessManager(this).start(); // al crearse el servidor también se arranca...
	}

	// SERVIDOR
	public void run() {

		// para recepción alternativa condicional
		Guard[] entradas = {
				chComprar.in(),
				chEntregar.in(),
				chDevolver.in(),
				chOfrecerReabastecer.in(),
				chReabastecer.in()
		};
		Alternative servicios = new Alternative(entradas);
		// OJO ORDEN!!
		final int COMPRAR = 0;
		final int ENTREGAR = 1;
		final int DEVOLVER = 2;
		final int OFRECER_REABASTECER = 3;
		final int REABASTECER = 4;
		// condiciones de recepción: todas a CIERTO

		// estado del recurso
		// TODO: vuestra estructura de datos traída de monitores
		HashTableMap<String, Producto> almacenMonitor = new HashTableMap<>();

		Iterable<String> keys = tipoProductos.keySet();
		// Relleno el almacen con todos los productos del catalogo, inicialidados a 0
		Iterator iter = keys.iterator();
		Object prov;
		while (iter.hasNext()) {
			prov = (String) iter.next();
			almacenMonitor.put((String) prov,
					new Producto(tipoProductos.get(((String) prov))));
		}
		// TODO: estructuras aux. para peticiones aplazadas
		IndexedList<Object> listaCondiciones = new ArrayIndexedList<>();

		// bucle de servicio
		while (true) {
			// vars. auxiliares
			// tipo de la última petición atendida
			int choice = -1; // una de {COMPRAR, ENTREGAR, DEVOLVER, OFRECER_REABASTECER, REABASTECER}

			// todas las peticiones incluyen un producto y una cantidad
			String itemId = "";
			int cantidad = -1;

			choice = servicios.fairSelect();
			switch (choice) {
				case COMPRAR: // CPRE = Cierto
					PetComprar petC = (PetComprar) chComprar.in().read();
					// comprobar PRE:
					// ** CÓDIGO ORIENTATIVO!! ADAPTAD A VUESTRA ESTRUCTURA DE DATOS!! **
					itemId = petC.productId;
					cantidad = petC.q;
					Producto prodC = almacenMonitor.get(itemId);
					if (cantidad <= 0 || prodC == null) { // PRE_KO
						petC.chresp.out().write(PRE_KO);
					} else { // PRE_OK
						boolean result = prodC.getDisponibles() + prodC.getEnCamino() >= cantidad
								+ prodC.getComprados();
						if (result) { // hay stock suficiente
							prodC.setComprados(prodC.getComprados() + cantidad);
							petC.chresp.out().write(STOCKOK);
						} else { // no hay stock suficiente
							petC.chresp.out().write(NOSTOCK);
						}
					}
					break;
				case ENTREGAR: // CPRE en diferido
					PetEntregar petE = (PetEntregar) chEntregar.in().read();
					itemId = petE.productId;
					cantidad = petE.q;
					Producto prodE = almacenMonitor.get(itemId);
					if (cantidad <= 0 || prodE == null) {
						petE.chresp.out().write(PRE_KO);
					} else {
						if (prodE.getDisponibles() < cantidad) {
							listaCondiciones.add(listaCondiciones.size(), petE);
						} else {
							// hay stock suficiente
							prodE.setDisponibles(prodE.getDisponibles() - cantidad);
							prodE.setComprados(prodE.getComprados() - cantidad);
							petE.chresp.out().write(STOCKOK);
						}
					}
					break;
				case DEVOLVER: // CPRE = Cierto
					PetDevolver petD = (PetDevolver) chDevolver.in().read();
					// comprobar PRE:
					itemId = petD.productId;
					cantidad = petD.q;
					Producto prodD = almacenMonitor.get(itemId);
					if (cantidad <= 0 || prodD == null) { // PRE_KO
						petD.chresp.out().write(PRE_KO);
					} else { // PRE_OK
						prodD.setDisponibles(prodD.getDisponibles() + cantidad);
						petD.chresp.out().write(SUCCESS);
					}
					break;
				case OFRECER_REABASTECER: // CPRE en diferido
					PetOfrecerReabastecer petOR = (PetOfrecerReabastecer) chOfrecerReabastecer.in().read();
					itemId = petOR.productId;
					cantidad = petOR.q;
					Producto prodOR = almacenMonitor.get(itemId);

					if (cantidad <= 0 || prodOR == null) {
						petOR.chresp.out().write(PRE_KO);
					} else {
						if ((prodOR.getDisponibles() + prodOR.getEnCamino() - prodOR.getComprados()) < prodOR
								.getMinDisponibles()) {
							prodOR.setEnCamino(prodOR.getEnCamino() + cantidad);
							petOR.chresp.out().write(STOCKOK);
						} else {
							listaCondiciones.add(listaCondiciones.size(), petOR);
						}
					}
					break;
				case REABASTECER: // CPRE = Cierto
					PetReabastecer petR = (PetReabastecer) chReabastecer.in().read();
					itemId = petR.productId;
					cantidad = petR.q;
					Producto prodR = almacenMonitor.get(itemId);

					if (cantidad <= 0 || prodR == null) {
						petR.chresp.out().write(PRE_KO);
					} else {
						prodR.setDisponibles(prodR.getDisponibles() + cantidad);
						prodR.setEnCamino(prodR.getEnCamino() - cantidad);
						petR.chresp.out().write(STOCKOK);
					}
					break;
			} // switch
			LinkedList<String> listaEntregas = new LinkedList<String>();
			LinkedList<String> listaOfrecerReabastecer = new LinkedList<String>();

			Iterator iterDes = listaCondiciones.iterator();
			Object provDes;

			for (int i = 0; i < listaCondiciones.size(); i++) {
				provDes = listaCondiciones.get(i);
				if (provDes instanceof PetEntregar) {
					itemId = ((PetEntregar) provDes).productId;
					cantidad = ((PetEntregar) provDes).q;
					Producto prodDes = almacenMonitor.get(itemId);
					if (prodDes.getDisponibles() >= cantidad && !listaEntregas.contains(itemId)) {

						prodDes.setDisponibles(prodDes.getDisponibles() - cantidad);
						prodDes.setComprados(prodDes.getComprados() - cantidad);
						((PetEntregar) provDes).chresp.out().write(STOCKOK);
						listaCondiciones.remove(provDes);
						i--;
					} else if (!listaEntregas.contains(itemId)) {
						listaEntregas.add(itemId);
					}
				}
				if (provDes instanceof PetOfrecerReabastecer) {
					itemId = ((PetOfrecerReabastecer) provDes).productId;
					cantidad = ((PetOfrecerReabastecer) provDes).q;
					Producto prodOR = almacenMonitor.get(itemId);
					if ((prodOR.getDisponibles() + prodOR.getEnCamino() - prodOR.getComprados()) < prodOR
							.getMinDisponibles() && !listaOfrecerReabastecer.contains(itemId)) {
						prodOR.setEnCamino(prodOR.getEnCamino() + cantidad);
						((PetOfrecerReabastecer) provDes).chresp.out().write(STOCKOK);
						listaCondiciones.remove(provDes);
						i--;
					} else if (!listaOfrecerReabastecer.contains(itemId)) {
						listaOfrecerReabastecer.add(itemId);
					}
				}
			}

			/*
			 * while (iterDes.hasNext()) {
			 * provDes = iterDes.next();
			 * if (provDes instanceof PetEntregar) {
			 * itemId = ((PetEntregar) provDes).productId;
			 * cantidad = ((PetEntregar) provDes).q;
			 * Producto prodDes = almacenMonitor.get(itemId);
			 * if (prodDes.getDisponibles() >= cantidad && !listaEntregas.contains(itemId))
			 * {
			 * 
			 * prodDes.setDisponibles(prodDes.getDisponibles() - cantidad);
			 * prodDes.setComprados(prodDes.getComprados() - cantidad);
			 * ((PetEntregar) provDes).chresp.out().write(STOCKOK);
			 * listaCondiciones.remove(provDes);
			 * } else if (!listaEntregas.contains(itemId)) {
			 * listaEntregas.add(itemId);
			 * }
			 * }
			 * if (provDes instanceof PetOfrecerReabastecer) {
			 * itemId = ((PetOfrecerReabastecer) provDes).productId;
			 * cantidad = ((PetOfrecerReabastecer) provDes).q;
			 * Producto prodOR = almacenMonitor.get(itemId);
			 * if ((prodOR.getDisponibles() + prodOR.getEnCamino() - prodOR.getComprados())
			 * < prodOR
			 * .getMinDisponibles() && !listaOfrecerReabastecer.contains(itemId)) {
			 * prodOR.setEnCamino(prodOR.getEnCamino() + cantidad);
			 * ((PetOfrecerReabastecer) provDes).chresp.out().write(STOCKOK);
			 * listaCondiciones.remove(provDes);
			 * } else if (!listaOfrecerReabastecer.contains(itemId)) {
			 * listaOfrecerReabastecer.add(itemId);
			 * }
			 * }
			 * }
			 */

		} // bucle servicio
	} // run SERVER

	// Clases auxiliares
	//
	// TODO: traed de vuestra sol. por monitores
	//
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
