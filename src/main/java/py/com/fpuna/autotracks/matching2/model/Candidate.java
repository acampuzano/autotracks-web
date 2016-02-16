package py.com.fpuna.autotracks.matching2.model;

import java.util.Objects;
import py.com.fpuna.autotracks.model.Ruta;

public class Candidate extends Coordinate {

    private Edge edge;
    private Vertex nearest;
    private Candidate pre;
    private double f;

    public Edge getEdge() {
        return edge;
    }

    public void setEdge(Edge edge) {
        this.edge = edge;
    }

    public Candidate getPre() {
        return pre;
    }

    public void setPre(Candidate pre) {
        this.pre = pre;
    }

    public double getF() {
        return f;
    }

    public void setF(double f) {
        this.f = f;
    }

    public Vertex getNearestVertex() {
        if (nearest == null) {
            if (distanceTo(edge.getSource()) < distanceTo(edge.getTarget())) {
                nearest = edge.getSource();
            } else {
                nearest = edge.getTarget();
            }
        }
        return nearest;
    }
    
    @Override
    public boolean equals(Object obj) {
        if (obj == null) {
            return false;
        } else if (obj instanceof Candidate) {
            Candidate objCandidate = (Candidate) obj;
            if (objCandidate.getEdge() != null
                    && objCandidate.getNearestVertex() != null
                    && objCandidate.getEdge().equals(this.edge)
                    && objCandidate.getNearestVertex().equals(this.getNearestVertex())) {
                return true;
            }
        }
        return false; //To change body of generated methods, choose Tools | Templates.
    }

    @Override
    public int hashCode() {
        int hash = 8;
        hash = 57 * hash + Objects.hashCode(this.getEdge()) + Objects.hashCode(this.getNearestVertex());
        return hash;
    }

}
