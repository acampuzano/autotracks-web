package py.com.fpuna.autotracks.matching2.model;

import java.util.Objects;

public class Vertex extends Coordinate {

    private int id;

    public Vertex() {
    }

    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }
    
    @Override
    public boolean equals(Object obj) {
        if (obj == null) {
            return false;
        } else if (obj instanceof Vertex) {
            Vertex objVertex = (Vertex) obj;
            if (objVertex.getId() == this.getId()) {
                return true;
            }
        }
        return false;
    }

    @Override
    public int hashCode() {
        int hash = 5;
        hash = 75 * hash + Objects.hashCode(this.id);
        return hash;
    }
}
