package entrega1;

public class CC_01_Threads {

    public static class Hilo extends Thread {
        private int n;
        private int t;

        public Hilo(int n, int tiempo) {
            this.n = n;
            t = tiempo;
        }

        public void run() {
            System.out.println("Este es el hilo " + n + " y acabo de empezar");
            try {
                Thread.sleep(this.n * this.t); //
            } catch (Exception e) {
            }

            System.out.println("Este es el hilo " + n + " y acabo de terminar");
        }
    }

    public static void main(String args[]) {
        int tiempo = 1000;
        int n = 100;
        Hilo[] arrHilos = new Hilo[n];

        for (int i = 0; i < arrHilos.length; i++) {
            arrHilos[i] = new Hilo(i, tiempo);
            arrHilos[i].start();
        }

        // NO se si esto es completamente correcto
        for (int i = 0; i < arrHilos.length; i++) {
            try {
                arrHilos[i].join();
            } catch (Exception e) {
            }
        }

        System.out.println("Ya han terminado todos los hilos");
    }
}