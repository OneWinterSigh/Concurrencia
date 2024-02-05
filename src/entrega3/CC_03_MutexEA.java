package entrega3;

// Exclusión mutua para dos hilos con espera activa.
//
// Objetivo: garantizar la exclusión mutua en sc_inc y sc_dec sin
// utilizar más mecanismo de concurrencia que el de la espera activa
// (nuevas variables y bucles).
//
// Las propiedades que deberán cumplirse:
//
// - Garantía de exclusión mutua: nunca habrá dos
//   hilos ejecutando sus secciones críticas de forma simultánea.
// - Ausencia de interbloqueo (deadlock): los procesos no quedan
//   "atrapados" para siempre.
// - Ausencia de inanición (starvation): si un proceso quiere acceder
//   a su sección crítica entonces es seguro que alguna vez lo hace.
// - Ausencia de esperas innecesarias: si un proceso quiere acceder a
//   su sección crítica y ningún otro proceso está accediendo ni
//   quiere acceder entonces el primero puede acceder.
//
// Ideas:
//
// - Una variable booleana en_sc que indica que algún proceso está
//   ejecutando en la sección crítica?
// - Una variable turno?
// - Dos variables booleanas quiere_sc_inc y quiere_sc_dec que indican que un
//   determinado proceso (el incrementador o el decrementador) están
//   llegando a su sección crítica?
// - Combinaciones de lo anterior?

class CC_03_MutexEA {
    static final int NUM_OPS = 10000;

    // Generador de números aleatorios para simular tiempos de
    // ejecución
    // static final java.util.Random RNG = new java.util.Random(0);

    // Variable compartida
    volatile static int n = 0;

    // Declara aquí variables compartidas para asegurar exclusión mutua
    volatile static boolean en_sc = false; // solo es un ejemplo, no funciona
    // ****************************************************************

    // Sección no crítica
    static void no_sc() {
        // Podéis jugar con prints, retardos, etc.
        System.out.println("No SC");
        // try {
        // // No más de 2ms
        // Thread.sleep(RNG.nextInt(3));
        // }
        // catch (Exception e) {
        // e.printStackTrace();
        // }
    }

    // Secciones críticas
    static void sc_inc() {
        // System.out.println("Incrementando");
        n++;
    }

    static void sc_dec() {
        // System.out.println("Decrementando");
        n--;
    }

    // La labor del proceso incrementador es ejecutar no_sc() y luego
    // sc_inc() NUM_OPS veces.
    static class Incrementador extends Thread {
        public void run() {
            for (int i = 0; i < NUM_OPS; i++) {
                // Sección no crítica
                no_sc(); // ** NO TOCAR **

                // Protocolo de acceso a la sección crítica
                while (en_sc) {
                } // solo a modo de ejemplo
                en_sc = true; // ** NO FUNCIONA **
                // ****************************************

                // Sección crítica
                sc_inc(); // ** NO TOCAR **

                // Protocolo de salida de la sección crítica
                // Añade tu código aquí:
                en_sc = false; // solo a modo de ejemplo
                // *****************************************
            }
        }
    }

    // La labor del proceso incrementador es ejecutar no_sc() y luego
    // sc_dec() durante NUM_OPS asegurando exclusión mutua sobre
    // sc_dec().
    static class Decrementador extends Thread {
        public void run() {
            for (int i = 0; i < NUM_OPS; i++) {
                // Sección no crítica
                no_sc(); // ** NO TOCAR **

                // Protocolo de acceso a la sección crítica
                // Añade tu código aquí:
                while (en_sc) {
                    System.out.println("Estoy en un bucle");
                } // solo a modo de ejemplo
                en_sc = true; // ** NO FUNCIONA **
                // ****************************************

                // Sección crítica
                sc_dec(); // ** NO TOCAR **

                // Protocolo de salida de la sección crítica
                // Añade tu código aquí:
                en_sc = false; // solo a modo de ejemplo
                // *****************************************
            }
        }
    }

    public static final void main(final String[] args)
            throws InterruptedException {
        // Creamos las tareas
        Thread t1 = new Incrementador();
        Thread t2 = new Decrementador();

        // Las ponemos en marcha
        t1.start();
        t2.start();

        // Esperamos a que terminen
        t1.join();
        t2.join();

        // Simplemente se muestra el valor final de la variable:
        System.out.println(n);
    }
}
