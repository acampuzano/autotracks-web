package py.com.fpuna.autotracks.model;

import java.io.Serializable;
import java.sql.Timestamp;
import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.JoinColumn;
import javax.persistence.ManyToOne;

/**
 *
 * @author Alfredo Campuzano
 */
@Entity
public class Localizacion implements Serializable {

    private static final long serialVersionUID = 1L;

    @Id
    @GeneratedValue(strategy = GenerationType.AUTO)
    private Long id;

    @Column(name = "LATITUD", nullable = false)
    private Double latitud;

    @Column(name = "LONGITUD", nullable = false)
    private Double longitud;

    @Column(name = "DIRECCION", nullable = true)
    private Float direccion;

    @Column(name = "ALTITUD", nullable = true)
    private Double altitud;

    @Column(name = "EXACTITUD", nullable = true)
    private Float exactitud;

    @Column(name = "VELOCIDAD", nullable = true)
    private Float velocidad;

    @Column(name = "FECHA", nullable = false)
    private Timestamp fecha;

    @Column(name = "IMEI", nullable = true)
    private String imei;

    @Column(name = "LATITUD_MATCH", nullable = true)
    private Double latitudMatch;

    @Column(name = "LONGITUD_MATCH", nullable = true)
    private Double longitudMatch;

    @Column(name = "MATCHED", nullable = true)
    private Boolean matched;

    @JoinColumn(name = "RUTA")
    @ManyToOne(optional = true)
    private Ruta ruta;
    
    @Column(name = "WAY_ID", nullable = true)
    private Integer wayId;
    
    @Column(name = "ORIGEN", nullable = true, length = 3)
    private String origen;

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public Double getLatitud() {
        return latitud;
    }

    public void setLatitud(Double latitud) {
        this.latitud = latitud;
    }

    public Double getLongitud() {
        return longitud;
    }

    public void setLongitud(Double longitud) {
        this.longitud = longitud;
    }

    public Float getDireccion() {
        return direccion;
    }

    public void setDireccion(Float direccion) {
        this.direccion = direccion;
    }

    public Double getAltitud() {
        return altitud;
    }

    public void setAltitud(Double altitud) {
        this.altitud = altitud;
    }

    public Float getExactitud() {
        return exactitud;
    }

    public void setExactitud(Float exactitud) {
        this.exactitud = exactitud;
    }

    public Float getVelocidad() {
        return velocidad;
    }

    public void setVelocidad(Float velocidad) {
        this.velocidad = velocidad;
    }

    public Timestamp getFecha() {
        return fecha;
    }

    public void setFecha(Timestamp fecha) {
        this.fecha = fecha;
    }

    public String getImei() {
        return imei;
    }

    public void setImei(String imei) {
        this.imei = imei;
    }

    public Double getLatitudMatch() {
        return latitudMatch;
    }

    public void setLatitudMatch(Double latitudMatch) {
        this.latitudMatch = latitudMatch;
    }

    public Double getLongitudMatch() {
        return longitudMatch;
    }

    public void setLongitudMatch(Double longitudMatch) {
        this.longitudMatch = longitudMatch;
    }

    public Boolean getMatched() {
        return matched;
    }

    public void setMatched(Boolean matched) {
        this.matched = matched;
    }

    public Ruta getRuta() {
        return ruta;
    }

    public void setRuta(Ruta ruta) {
        this.ruta = ruta;
    }

    public Integer getWayId() {
        return wayId;
    }

    public void setWayId(Integer wayId) {
        this.wayId = wayId;
    }

    public String getOrigen() {
        return origen;
    }

    public void setOrigen(String origen) {
        this.origen = origen;
    }

    @Override
    public int hashCode() {
        int hash = 0;
        hash += (id != null ? id.hashCode() : 0);
        return hash;
    }

    @Override
    public boolean equals(Object object) {
		// TODO: Warning - this method won't work in the case the id fields are
        // not set
        if (!(object instanceof Localizacion)) {
            return false;
        }
        Localizacion other = (Localizacion) object;
        if ((this.id == null && other.id != null)
                || (this.id != null && !this.id.equals(other.id))) {
            return false;
        }
        return true;
    }

    @Override
    public String toString() {
        return "py.com.asunciontracks.ejb.entity.Localizacion[ id=" + id + " ]";
    }

}
