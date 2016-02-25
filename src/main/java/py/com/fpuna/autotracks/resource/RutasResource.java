package py.com.fpuna.autotracks.resource;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import com.google.gson.JsonPrimitive;
import java.net.HttpURLConnection;
import java.sql.Timestamp;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Map;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.inject.Inject;
import javax.ws.rs.Consumes;
import javax.ws.rs.GET;
import javax.ws.rs.POST;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import javax.ws.rs.Produces;
import javax.ws.rs.QueryParam;
import javax.ws.rs.WebApplicationException;
import javax.ws.rs.core.Response;
import py.com.fpuna.autotracks.model.Localizacion;
import py.com.fpuna.autotracks.model.Resultado;
import py.com.fpuna.autotracks.model.Ruta;
import py.com.fpuna.autotracks.model.Trafico;
import py.com.fpuna.autotracks.model.TraficoComplejo;
import py.com.fpuna.autotracks.service.RutasService;
import py.com.fpuna.autotracks.util.TimestampDeserializer;

@Path("rutas")
@Produces("application/json")
@Consumes("application/json")
public class RutasResource {
    
    private static Logger logger = Logger.getLogger("Autotracks");

    @Inject
    RutasService rutasService;

    @GET
    public List<Ruta> obtenerRutas() {
        List<Ruta> rutas = rutasService.obtenerRutas();
        for (Ruta ruta : rutas) {
            ruta.setLocalizaciones(null);
        }
        return rutas;
    }
    
    @GET
    @Path("/fecha")
    public List<Ruta> obtenerRutasFecha(@QueryParam("inicio") String inicio, 
            @QueryParam("fin") String fin) {
        SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
        Date fechaIni = new Date();
        Date fechaFin = new Date();
        
        try {
            fechaIni = sdf.parse(inicio);
            fechaFin = sdf.parse(fin);
        } catch (ParseException ex) {
            Logger.getLogger(RutasResource.class.getName()).log(Level.SEVERE, "Error al transformar la fecha", ex);
        }
        
        Logger.getLogger(RutasResource.class.getName()).log(Level.INFO, "inicio: {0} fin: {1}", 
                new Object[]{sdf.format(fechaIni), sdf.format(fechaFin)});
        
        List<Ruta> rutas = rutasService.obtenerRutas(new Timestamp(fechaIni.getTime()),
                new Timestamp(fechaFin.getTime()));
        
        for (Ruta ruta : rutas) {
            ruta.setLocalizaciones(null);
        }
        return rutas;
    }

    @GET
    @Path("/{id}/localizaciones")
    public List<Localizacion> obtenerLocalizaciones(@PathParam("id") long id) {
        List<Localizacion> localizaciones = rutasService.obtenerLocalizaciones(id);
        for (Localizacion localizacion : localizaciones) {
            localizacion.setRuta(null);
        }
        return localizaciones;
    }

    @POST
    public Resultado guardarRuta(Ruta ruta) {
        // Si la app Android envio un serverId, seteamos este id como el id de
        // la ruta para agregar a la ruta existentes las localizaciones enviadas
        if (ruta.getServerId() != null) {
            ruta.setId(ruta.getServerId());
        }
        // Seteamos la ruta en cada localizacion para que se guarde la relacion
        // en la base de datos
        for (Localizacion l : ruta.getLocalizaciones()) {
            l.setRuta(ruta);
            l.setMatched(false);
        }
        // Guardamos la ruta y retornamos en el resultado el id de la ruta
        // guardada para que la app Android pueda reenviarnos el serverId.
        ruta = rutasService.guardarRuta(ruta);
        return new Resultado(true, null, ruta.getId());
    }

    /**
     * Servicio para obtener el estado del tráfico en un momento dado
     * @param fecha
     * @return 
     */
    @GET
    @Path("/traficoFecha")
    public List<Trafico> obtenerTrafico(@QueryParam("fecha") String fecha) {
        SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
        Date fec = new Date();
        try {
            fec = sdf.parse(fecha);
        } catch (ParseException ex) {
            Logger.getLogger(RutasResource.class.getName()).log(Level.SEVERE, "Error al transformar la fecha", ex);
        }
        return rutasService.obtenerTrafico(new Timestamp(fec.getTime()));
    }
    
    /**
     * Servicio para obtener el tráfico en una fecha dada durante un periodo de tiempo dado
     * @param fecha fecha
     * @param minutos tiempo en minutos
     * @return 
     */
    @GET
    @Path("/traficoFechaTiempo")
    public List<Trafico> obtenerTraficoTiempo(@QueryParam("fecha") String fecha,
            @QueryParam("minutos") String minutos) {
        SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
        Date fec = new Date();
        Integer milis = 0;
        try {
            //se transforma el tiempo a milisegundos
            milis = Integer.parseInt(minutos) * 60 * 1000;
            fec = sdf.parse(fecha);
        } catch (ParseException ex) {
            Logger.getLogger(RutasResource.class.getName()).log(Level.SEVERE, "Error al transformar la fecha", ex);
        }
        return rutasService.obtenerTrafico(new Timestamp(fec.getTime()), milis);
    }
    
    @GET
    @Path("/trafico")
    public List<Trafico> obtenerTrafico() {
        return rutasService.obtenerTraficoActual();        
    }
    
    @GET
    @Path("/traficoZona")
    public List<Map<String,Object>> obtenerTrafico(@QueryParam("lat") String lat,
            @QueryParam("lon") String lon,
            @QueryParam("radio") String radio) {
        if (lat != null && !lat.isEmpty() && lon != null
                && !lon.isEmpty() && radio != null && !radio.isEmpty()) {
            return rutasService.obtenerTraficoActual(lat,lon,Double.valueOf(radio));
        } else {
            throw new WebApplicationException(
                Response.status(HttpURLConnection.HTTP_BAD_REQUEST)
                  .entity("Incluir todos los parámetros requeridos lat, lon, radio")
                  .build()
              );
        }
    }
    
    @GET
    @Path("/traficoGlobal")
    public List<Trafico> obtenerTraficoGlobal() {
        return rutasService.obtenerTrafico();
    }
    
    /**
     * Método para guardar las localizaciones recibidas desde los buses
     * @param ruta
     * @return 
     */
    @POST
    @Path("/bus")
    public Resultado guardarRutaBus(String localizaciones) {
        JsonParser parser = new JsonParser();
        JsonElement jElement = parser.parse(localizaciones);
        JsonArray jArray = jElement.getAsJsonArray();
        
        List<Ruta> rutas = new ArrayList<Ruta>();
        
        GsonBuilder gsonBuilder = new GsonBuilder();
        gsonBuilder.registerTypeAdapter(Timestamp.class, new TimestampDeserializer());
        Gson gson = gsonBuilder.create();
        
        JsonObject jObject;
        JsonPrimitive jPrimitive;
        Localizacion loc;
        Ruta rutaTmp;
        long busId;
        
        logger.info("total de lozalizaciones: " + jArray.size());
        
        try {
            for (JsonElement json: jArray) {
                jObject = json.getAsJsonObject();
                jPrimitive = jObject.getAsJsonPrimitive("busId");
                busId = jPrimitive.getAsLong();
                jObject.remove("busId");
                rutaTmp = new Ruta();
                rutaTmp.setServerId(busId);

                //se obtiene la localización
                loc = gson.fromJson(jObject.toString(), Localizacion.class);
                
                if (rutas.contains(rutaTmp)) {
                    rutaTmp = rutas.get(rutas.indexOf(rutaTmp));
                    loc.setRuta(rutaTmp);
                    loc.setMatched(false);
                    rutaTmp.getLocalizaciones().add(loc);
                } else {
                    rutaTmp.setLocalizaciones(new ArrayList<Localizacion>());
                    rutaTmp.setFecha(loc.getFecha());
                    loc.setRuta(rutaTmp);
                    loc.setMatched(false);
                    rutaTmp.getLocalizaciones().add(loc);
                    rutas.add(rutaTmp);
                }
            }
            
            logger.info("rutas: " + rutas.size());

            //Se guardan las rutas
            for (Ruta ruta: rutas) {
                rutasService.guardarRuta(ruta);
            }
            
        } catch (Exception e) {
            logger.log(Level.SEVERE, "error a guardar localizaciones de buses", e);
            return new Resultado(false, "error al guardar las localizaciones: " + e.getMessage(), new Long(0));
        }
        
        return new Resultado(true, "las lozalizaciones se guardaron correctamente", new Long(0));
    }

}
