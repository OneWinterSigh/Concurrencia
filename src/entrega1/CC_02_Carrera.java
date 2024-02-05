package entrega1;

import es.upm.aedlib.fifo.FIFOList;

public class CC_02_Carrera {
    private static FIFOList<Integer> cola = new FIFOList<>();

    public static class par extends Thread {
        private int n;

        public par(int n) {
            this.n = n;
        }

        public void run() {
            int i = 0;

            while (i <= n) {
                if ((i % 2) == 0) {
                    cola.enqueue(i);
                }
                i++;
            }
        }
    }

    public static class impar extends Thread {
        private int n;

        public impar(int n) {
            this.n = n;
        }

        public void run() {
            int i = 0;

            while (i <= n) {
                if ((i % 2) != 0) {
                    cola.enqueue(i);
                }
                i++;
            }
        }
    }

    public static class consumidor extends Thread {
        private int comprobador;

        public consumidor() {
            comprobador = 0;
        }

        public int comprobador() {
            return comprobador;
        }

        public void run() {
            while (cola.size() != 0) {
                comprobador++;
                System.out.println("He extraido el numero: " + cola.dequeue());
                System.out.println("---------------------------");
            }
        }
    }

    public static void main(String args[]) {
        int numero = 10;

        par generador1 = new par(numero);
        impar generador2 = new impar(numero);
        consumidor cons1 = new consumidor();

        generador1.start();
        generador2.start();

        cons1.start();

        try {
            cons1.join();
        } catch (Exception e) {
        }

        if (cons1.comprobador() != numero + 1)
            System.out.println("Tenemos una condicion de carrera");
        else
            System.out.println("No tenemos una condición de carrera");
    }
}
