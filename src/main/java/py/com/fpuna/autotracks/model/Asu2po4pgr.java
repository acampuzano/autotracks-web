package py.com.fpuna.autotracks.model;

import java.io.Serializable;
import javax.persistence.*;

@Entity
@Table(name = "asu_2po_4pgr")
public class Asu2po4pgr implements Serializable {

    private static final long serialVersionUID = 1L;

    @Id
    private Integer id;

    @Column(name = "osm_name")
    private String name;

    private double x1;

    private double x2;

    private double y1;

    private double y2;
    
    private long source;
    
    private long target;

    public Asu2po4pgr() {
    }

    public Integer getId() {
        return this.id;
    }

    public void setId(Integer id) {
        this.id = id;
    }

    public String getName() {
        return this.name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public double getX1() {
        return this.x1;
    }

    public void setX1(double x1) {
        this.x1 = x1;
    }

    public double getX2() {
        return this.x2;
    }

    public void setX2(double x2) {
        this.x2 = x2;
    }

    public double getY1() {
        return this.y1;
    }

    public void setY1(double y1) {
        this.y1 = y1;
    }

    public double getY2() {
        return this.y2;
    }

    public void setY2(double y2) {
        this.y2 = y2;
    }

    public long getSource() {
        return source;
    }

    public void setSource(long source) {
        this.source = source;
    }

    public long getTarget() {
        return target;
    }

    public void setTarget(long target) {
        this.target = target;
    }

}
