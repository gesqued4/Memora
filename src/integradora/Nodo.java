
package integradora;

/**
 * @author pades
 */
public class Nodo {
    
    Memories memorie;
    Nodo anterior, siguiente;

    public Nodo(Memories memorie, Nodo anterior, Nodo siguiente) {
        this.memorie = memorie;
        this.anterior = null;
        this.siguiente = null;
    }

   

    public Nodo getAnterior() {
        return anterior;
    }

    public Nodo getSiguiente() {
        return siguiente;
    }

    public Memories getMemorie() {
        return memorie;
    }



    public void setAnterior(Nodo anterior) {
        this.anterior = anterior;
    }

    public void setSiguiente(Nodo siguiente) {
        this.siguiente = siguiente;
    }

    public void setMemorie(Memories memorie) {
        this.memorie = memorie;
    }

    
    
}
