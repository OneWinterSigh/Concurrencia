package entrega4;

// Exclusión mutua para _cualquier número de hilos_ usando
// semáforos.
//
// Objetivo: garantizar la exclusión mutua en sc_inc y sc_dec sin
//           utilizar más mecanismo de concurrencia que los semáforos.
//           En esta ocasión queda _prohibido_ el uso de espera activa.
//
// Las propiedades que deberán cumplirse siguen siendo:
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
// Aunque los semáforos son un mecanismo nativo en el lenguaje Java,
// para este ejercicio te pedimos que uses los que definimos en nuestra
// librería de concurrencia CCLib, que podéis descargar desde el
// aula virtual.
//
// ¿Qué características debemos exigir a nuestra implementación 

// Importa la librería de semáforos
import es.upm.babel.cclib.Semaphore;

class CC_04_MutexSem {
    // Número de operaciones aritméticas
    static final int NUM_OPS = 10000;

    // Número de hilos a lanzar
    static final int NUM_THREADS = 100;

    // Esta vez vamos a encapsular el contador en una clase
    // *** INTRODUCE TUS MODIFICACIONES AQUÍ **************
    static class Contador {

        // el valor del contador, en sí
        private volatile int val;
        private volatile Semaphore semaforo = new Semaphore(1);

        // constructores
        public Contador() {
            this.val = 0;

        }

        public Contador(int ini) {
            this.val = ini;
        }

        // modificadores
        public void inc() {
            semaforo.await();
            this.val++;
            semaforo.signal();
        }

        public void dec() {
            semaforo.await();
            this.val--;
            semaforo.signal();
        }

        // observador
        public int valContador() {
            return this.val;
        }
    }
    // *** FIN CÓDIGO MODIFICABLE *************************

    // *** NO TOCAR DE AQUÍ EN ADELANTE *******************

    // La labor del hilo incrementador es incrementar el valor del
    // contador compartido NUM_OPS veces.
    static class Incrementador extends Thread {
        // guardamos una referencia al contador compartido
        Contador cont;

        public Incrementador(Contador c) {
            this.cont = c;
        }

        public void run() {
            for (int i = 0; i < NUM_OPS; i++) {
                this.cont.inc();
            }
        }
    }

    // La labor del hilo decrementador es incrementar el valor del
    // contador compartido NUM_OPS veces.
    static class Decrementador extends Thread {
        // guardamos una referencia al contador compartido
        Contador cont;

        public Decrementador(Contador c) {
            this.cont = c;
        }

        public void run() {
            for (int i = 0; i < NUM_OPS; i++) {
                this.cont.dec();
            }
        }
    }

    public static final void main(final String[] args)
            throws InterruptedException {

        // creamos el contador
        Contador cont = new Contador(); // a 0 por defecto

        // Creamos las tareas
        Thread incrementador[] = new Thread[NUM_THREADS];
        Thread decrementador[] = new Thread[NUM_THREADS];

        for (int i = 0; i < NUM_THREADS; i++) {
            incrementador[i] = new Incrementador(cont);
            decrementador[i] = new Decrementador(cont);
        }

        // las lanzamos
        for (int i = 0; i < NUM_THREADS; i++) {
            incrementador[i].start();
            decrementador[i].start();
        }

        // Esperamos a que terminen
        for (int i = 0; i < NUM_THREADS; i++) {
            incrementador[i].join();
            decrementador[i].join();
        }

        // Simplemente se muestra el valor final de la variable:
        System.out.println(cont.valContador());
    }
}