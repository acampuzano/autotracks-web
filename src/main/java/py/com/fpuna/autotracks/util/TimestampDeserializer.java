/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */

package py.com.fpuna.autotracks.util;

import com.google.gson.JsonDeserializationContext;
import com.google.gson.JsonDeserializer;
import com.google.gson.JsonElement;
import com.google.gson.JsonParseException;
import java.lang.reflect.Type;
import java.sql.Timestamp;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.TimeZone;
import java.util.logging.Logger;

/**
 *
 * @author Alfredo
 */
public class TimestampDeserializer implements JsonDeserializer<Timestamp>{
    
    private static Logger logger = Logger.getLogger("Autotracks");

    @Override
    public Timestamp deserialize(JsonElement json, Type typeOfT, JsonDeserializationContext context) throws JsonParseException {
        String date = json.getAsString();
        
        SimpleDateFormat formatter = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
//        formatter.setTimeZone(TimeZone.getTimeZone("America/Asuncion"));
        
        try {
            Date fecha = formatter.parse(date);
            return new Timestamp(fecha.getTime());
        } catch (Exception e) {
            throw new JsonParseException("Error al formatear fecha", e);
        }
    }
    
}
