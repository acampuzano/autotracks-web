package py.com.fpuna.autotracks.service;

import java.math.BigInteger;
import java.sql.Timestamp;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.logging.Logger;
import javax.ejb.Stateless;
import javax.inject.Inject;
import javax.persistence.EntityManager;
import javax.persistence.PersistenceContext;
import py.com.fpuna.autotracks.matching.LocationUtils;
import py.com.fpuna.autotracks.matching2.Matcher;
import py.com.fpuna.autotracks.model.Localizacion;
import py.com.fpuna.autotracks.model.Ruta;
import py.com.fpuna.autotracks.model.Trafico;
import py.com.fpuna.autotracks.model.TraficoComplejo;

@Stateless
public class RutasService {
    
    private static Logger logger = Logger.getLogger(RutasService.class.getSimpleName());
    
    private static final Integer DOS_HORAS = 2 * 60 * 60 * 1000;
    private static final Integer UNA_HORA = 1 * 60 * 60 * 1000;
    private static final Integer MEDIA_HORA = 30 * 60 * 1000;
    private static final Integer QUINCE_MINUTOS = 15 * 60 * 1000;

    @Inject
    Matcher matcher;

    @PersistenceContext
    EntityManager em;

    public List<Ruta> obtenerRutas() {
        return em.createQuery("SELECT r FROM Ruta r order by r.fecha").getResultList();
    }
    
    public List<Ruta> obtenerRutas(Timestamp inicio, Timestamp fin) {
        SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
        return em.createQuery("SELECT r FROM Ruta r where r.fecha between '" + sdf.format(inicio) + "' AND '"
                + sdf.format(fin) + "' order by r.fecha").getResultList();
    }

    public List<Localizacion> obtenerLocalizaciones(long id) {
        return em.createQuery("SELECT l FROM Localizacion l WHERE l.ruta.id = :id ORDER BY l.fecha")
                .setParameter("id", id).getResultList();
    }

    public Ruta guardarRuta(Ruta ruta) {
        ruta = em.merge(ruta);
        if (ruta.getLocalizaciones() != null && !ruta.getLocalizaciones().isEmpty()) {
            matcher.match(ruta.getLocalizaciones());
        }
        return ruta;
    }

    public List<Trafico> obtenerTrafico() {
        return em.createQuery("SELECT new py.com.fpuna.autotracks.model.Trafico(r.name, r.x1, r.y1, r.x2, r.y2, COUNT(l.id), SUM(l.velocidad))"
                + "FROM Localizacion l, Asu2po4pgr r where l.wayId = r.id group by r.id", Trafico.class).getResultList();
    }
    
    /**
     * Permite obtener el estado del tráfico en un momento dado
     * @param fecha
     * @return 
     */
    public List<Trafico> obtenerTrafico(Timestamp fecha) {
        Calendar inicio = Calendar.getInstance();
        //se setea el inicio 30 min antes
        inicio.setTimeInMillis(fecha.getTime() - MEDIA_HORA);
        SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
        String query = "SELECT new py.com.fpuna.autotracks.model.Trafico(r.name, r.x1, r.y1, r.x2, r.y2, COUNT(l.id), SUM(l.velocidad))"
                + "FROM Localizacion l, Asu2po4pgr r where l.wayId = r.id and l.fecha between '" + sdf.format(inicio.getTime()) + "' and '" 
                + sdf.format(fecha) + "' group by r.id";
        return em.createQuery(query, Trafico.class).getResultList();
    }
    /**
     * Permite obtener el estado del tráfico en un momento dado, durante un periodo dado
     * @param fecha fecha del tráfico
     * @param tiempo periodo de tiempo en milis
     * @return 
     */
    public List<Trafico> obtenerTrafico(Timestamp fecha, Integer tiempo) {
        Calendar inicio = Calendar.getInstance();
        //se setea el inicio 30 min antes
        inicio.setTimeInMillis(fecha.getTime() - tiempo);
        SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
        String query = "SELECT new py.com.fpuna.autotracks.model.Trafico(r.name, r.x1, r.y1, r.x2, r.y2, COUNT(l.id), SUM(l.velocidad))"
                + "FROM Localizacion l, Asu2po4pgr r where l.wayId = r.id and l.fecha between '" + sdf.format(inicio.getTime()) + "' and '" 
                + sdf.format(fecha) + "' group by r.id";
        return em.createQuery(query, Trafico.class).getResultList();
    }
    
    /**
     * Permite obtener el estado actual del tráfico en un momento dado
     * @return 
     */
    public List<Trafico> obtenerTraficoActual() {
        Calendar fin = Calendar.getInstance();
        Calendar inicio = Calendar.getInstance();
        //se setea el inicio 30 min antes
        inicio.setTimeInMillis(fin.getTimeInMillis() - MEDIA_HORA);
        SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
        String query = "SELECT new py.com.fpuna.autotracks.model.Trafico(r.name, r.x1, r.y1, r.x2, r.y2, COUNT(l.id), SUM(l.velocidad))"
                + "FROM Localizacion l, Asu2po4pgr r where l.wayId = r.id and l.fecha between '" + sdf.format(inicio.getTime()) + "' and '" 
                + sdf.format(fin.getTime()) + "' group by r.id";
        return em.createQuery(query, Trafico.class).getResultList();
    }
    
    /**
     * Permite obtener el estado actual del tráfico en un momento dado dentro de un radio determinado
     * @param lat
     * @param lon
     * @param radio
     * @return 
     */
    public List<Map<String, Object>> obtenerTraficoActual(String lat, String lon, Double radio) {
        Calendar fin = Calendar.getInstance();
        Calendar inicio = Calendar.getInstance();
        //se setea el inicio 30 min antes
        inicio.setTimeInMillis(fin.getTimeInMillis() - MEDIA_HORA);
        SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
        String query = "SELECT r.x1, r.y1, r.x2, r.y2, COUNT(l.id), SUM(l.velocidad), r.source, r.target "
                + "FROM localizacion l, asu_2po_4pgr r where l.way_id = r.id and l.fecha between '" + sdf.format(inicio.getTime()) + "' and '" 
                + sdf.format(fin.getTime()) + "' and ST_DWithin(ST_GeomFromText('SRID=4326;POINT(" + lon + " " + lat + ")'), geom_way, "
                + LocationUtils.metersToDegrees(radio) + ") group by r.id";
        List<?> resultList = em.createNativeQuery(query).getResultList();
        List<Map<String, Object>> retorno = new ArrayList<>();
        for (int i = 0; i < resultList.size(); i++) {
                Object[] columns = (Object[]) resultList.get(i);
                retorno.add(getTraficoComplejoMap(columns));
        }
        return retorno;
    }
    
    private TraficoComplejo getTraficoComplejo(Object [] columns) {
        return new TraficoComplejo((String)columns[0], (Double)columns[1], (Double)columns[2],
                (Double)columns[3], (Double)columns[4], (Long)columns[5], (Double)columns[6],
                (Long)columns[7], (Long)columns[8]);
    }
    
    private Map<String, Object> getTraficoComplejoMap(Object [] columns) {
        Map<String, Object> mapa = new HashMap<>();
        
        mapa.put("x1", (Double)columns[0]);
        mapa.put("y1", (Double)columns[1]);
        mapa.put("x2", (Double)columns[2]);
        mapa.put("y2", (Double)columns[3]);
        mapa.put("cantidad", (BigInteger)columns[4]);
        mapa.put("kmh", (Float)columns[5]);
        mapa.put("source", (Integer)columns[6]);
        mapa.put("target", (Integer)columns[7]);
        
        return mapa;
    }
}
