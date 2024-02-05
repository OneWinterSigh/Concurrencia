package entrega6;

// TODO: importar la clase de los semáforos.
import es.upm.babel.cclib.Semaphore;

// Almacén FIFO concurrente para N datos

class AlmacenN {
    private int capacidad = 0;
    private int[] almacenado = null;
    private int nDatos = 0;
    private int aExtraer = 0;
    private int aInsertar = 0;

    // TODO: declaración de los semáforos necesarios
    //
    //
    //

    public AlmacenN(int n) {
        capacidad = n;
        almacenado = new int[capacidad];
        nDatos = 0;
        aExtraer = 0;
        aInsertar = 0;

        // TODO: inicialización de los semáforos
        // si no se ha hecho más arriba
        //
        //
    }

    public void almacenar(int producto) {
        // TODO: protocolo de acceso a la sección crítica y código de
        // sincronización para poder almacenar.
        //
        //

        // Sección crítica //////////////
        almacenado[aInsertar] = producto;
        nDatos++;
        aInsertar++;
        aInsertar %= capacidad;
        // //////////////////////////////

        // TODO: protocolo de salida de la sección crítica y código de
        // sincronización para poder extraer.
        //
        //
    }

    public int extraer() {
        int result;

        // TODO: protocolo de acceso a la sección crítica y código de
        // sincronización para poder extraer.
        //
        //

        // Sección crítica ///////////
        result = almacenado[aExtraer];
        nDatos--;
        aExtraer++;
        aExtraer %= capacidad;
        // ///////////////////////////

        // TODO: protocolo de salida de la sección crítica y código de
        // sincronización para poder almacenar.
        //
        //

        return result;
    }
}