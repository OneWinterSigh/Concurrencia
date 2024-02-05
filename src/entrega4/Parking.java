package entrega4;

import es.upm.babel.cclib.Semaphore;

// Parking.java
//
// Simulación "tontorrona" de un aparcamiento
// para familiarizarnos con el uso de semáforos
// como contadores seguros de permisos.
//
// El código solo muestra por consola los eventos de
// las barreras.
// Modifícalo para que muestre el número de coches
// en cada momento y para que tenga retardos aleatorios.
//
// Julio Mariño -- MMXV - MMXXIII

public class Parking {

    // La capacidad del parking
    private static final int CAPAC = 5;

    // Hay múltiples barreras de acceso
    private static final int NUM_ENTRADAS = 3;
    // ...y de salida:
    private static final int NUM_SALIDAS = 3;
    // OJO: ¿QUÉ PASA SI NUM_ENTRADAS != NUM_SALIDAS?

    // Para que la simulación acabe en algún momento
    private static int NUM_ITERS = 3;

    // semáforo para gestionar acceso al aparcamiento
    // cuenta el número de plazas libres
    // inicialmente el parking está vacío
    static Semaphore sPlazas = new Semaphore(CAPAC);

    // otro semáforo para contar el número de coches
    // dentro del aparcamiento
    // incialmente el parking está vacío
    static Semaphore sCoches = new Semaphore(0);

    // Barreras de acceso al párking
    static class BarreraIn extends Thread {

        private int id;

        public BarreraIn(int i) {
            this.id = i;
        }

        public void run() {
            for (int i = 0; i < NUM_ITERS; i++) {
                // esperamos a que llegue un coche
                // OPCIONAL: esperamos tiempo aleatorio

                // anunciamos llegada de un vehículo
                System.out.printf("Llega un coche a la barrera E%d.\n",
                        this.id);
                // nos aseguramos de que hay sitio en el párking
                sPlazas.await();

                // anunciamos entrada de vehículo
                System.out.printf("Entrando coche por barrera E%d.\n",
                        this.id);

                // aparcar lleva un cierto tiempo
                // OPCIONAL: esperar un tiempo aleatorio

                // actualizamos la cuenta de coches dentro del párking
                sCoches.signal();
            }
        }
    }

    // Barreras de salida del párking
    static class BarreraOut extends Thread {

        private int id;

        public BarreraOut(int i) {
            this.id = i;
        }

        public void run() {
            for (int i = 0; i < NUM_ITERS; i++) {
                // esperamos a que un coche quiera salir del párking
                // OPCIONAL: meter espera aleatoria
                try {
                    Thread.sleep(1000);
                } catch (Exception e) {
                }

                // nos aseguramos de que hay coches aparcados en el párking
                sCoches.await();

                // anunciamos salida del vehículo
                System.out.printf("Saliendo coche por la barrera S%d.\n",
                        this.id);

                // actualizamos la cuenta de plazas libres del párking
                sPlazas.signal();
            }
        }
    }

    public static void main(String args[]) {

        // Creación de los arrays que contendrán los threads
        BarreraIn[] entradas = new BarreraIn[NUM_ENTRADAS];
        BarreraOut[] salidas = new BarreraOut[NUM_SALIDAS];

        // Creación de los hilos
        for (int i = 0; i < NUM_ENTRADAS; i++) {
            entradas[i] = new BarreraIn(i);
        }
        for (int i = 0; i < NUM_SALIDAS; i++) {
            salidas[i] = new BarreraOut(i);
        }

        // Lanzamiento de los threads
        for (int i = 0; i < NUM_ENTRADAS; i++) {
            entradas[i].start();
        }
        for (int i = 0; i < NUM_SALIDAS; i++) {
            salidas[i].start();
        }

        // Espera hasta la terminacion de los threads
        try {
            for (int i = 0; i < NUM_ENTRADAS; i++) {
                entradas[i].join();
            }
            for (int i = 0; i < NUM_SALIDAS; i++) {
                salidas[i].join();
            }
        } catch (Exception ex) {
            ex.printStackTrace();
            System.exit(-1);
        }

        System.exit(0);
    }
}