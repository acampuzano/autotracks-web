package py.com.fpuna.autotracks.matching2.model;

import java.util.Objects;

public class Edge {
    
    private int id;
    private double speed; // in km/h
    private double length; // in km
    private Vertex source;
    private Vertex target;
    
    public Edge() {
    }

    public int getId() {
        return id;
    }
    
    public void setId(int id) {
        this.id = id;
    }
    
    public double getSpeed() {
        return speed;
    }
    
    public void setSpeed(double speed) {
        this.speed = speed;
    }
    
    public double getLength() {
        return length;
    }
    
    public void setLength(double length) {
        this.length = length;
    }
    
    public Vertex getSource() {
        return source;
    }
    
    public void setSource(Vertex source) {
        this.source = source;
    }
    
    public Vertex getTarget() {
        return target;
    }
    
    public void setTarget(Vertex target) {
        this.target = target;
    }

    public void calculateDistance() {
        length = source.distanceTo(target);
    }
    
    @Override
    public boolean equals(Object obj) {
        if (obj == null) {
            return false;
        } else if (obj instanceof Edge) {
            Edge objEdge = (Edge) obj;
            if (objEdge.getId() == this.getId()) {
                return true;
            }
        }
        return false;
    }

    @Override
    public int hashCode() {
        int hash = 5;
        hash = 57 * hash + Objects.hashCode(this.id);
        return hash;
    }

}
