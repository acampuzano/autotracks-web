package py.com.fpuna.autotracks.matching2;

import java.math.BigInteger;
import java.util.ArrayList;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.persistence.EntityManager;
import javax.persistence.PersistenceContext;
import py.com.fpuna.autotracks.matching2.model.Candidate;
import py.com.fpuna.autotracks.matching2.model.Coordinate;
import py.com.fpuna.autotracks.matching2.model.Edge;
import py.com.fpuna.autotracks.matching2.model.Result;
import py.com.fpuna.autotracks.matching2.model.Vertex;

public class ShortestPathCalculation {

    private static final Logger LOG = Logger.getLogger(ShortestPathCalculation.class.getName());

    private static final String SQL = " SELECT "
            + " p.seq, "
            + " p.node,"
            + " p.edge, "
            + " p.cost, "
            + " r.id, "
            + " r.source, "
            + " r.target, "
            + " r.kmh, "
            + " r.km, "
            + " r.x1, "
            + " r.y1, "
            + " r.x2, "
            + " r.y2 "
            + " FROM (\n"
            + "   SELECT seq, id1 AS node, id2 AS edge, cost \n"
            + "   FROM pgr_dijkstra(\n"
            + "     'SELECT id AS id, \n"
            + "       source::integer, \n"
            + "       target::integer, \n"
            + "       km::double precision AS cost \n"
            + "     FROM asu_2po_4pgr', \n"
            + "     :source, :target, true, false)\n"
            + "   ) AS p\n"
            + " LEFT JOIN asu_2po_4pgr r \n"
            + " ON p.edge = r.id\n"
            + " ORDER BY p.seq;";
    
    private static final String SQL_COSTO = "SELECT sum(cost), count(*) \n"
            + "   FROM pgr_astar(\n"
            + "     'SELECT id AS id, \n"
            + "       source::integer, \n"
            + "       target::integer, \n"
            + "       km::double precision AS cost, \n"
            + "       x1, \n"
            + "       y1, \n"
            + "       x2, \n"
            + "       y2 \n"
            + "     FROM asu_2po_4pgr', \n"
            + "     :source, :target, true, false);";

    @PersistenceContext
    EntityManager em;
    
    public Double getShortestPathDistance(Candidate c1, Candidate c2) {
        
        // Si ambos puntos candidatos estan sobre la misma linea
        if (c1.getEdge().getId() == c2.getEdge().getId()) {
            return c1.distanceTo(c2);
        }
        
        // Si ambos puntos candidatos tienen el mismo vertice mas cercano
        if (c1.getNearestVertex().getId() == c2.getNearestVertex().getId()) {
            return c1.distanceTo(c1.getNearestVertex()) + c2.distanceTo(c2.getNearestVertex());
        }
        
        return c1.distanceTo(c2.getNearestVertex()) + c2.distanceTo(c2.getNearestVertex());
        
        /*int source = c1.getNearestVertex().getId();
        int target = c2.getNearestVertex().getId();
        
        String sql = SQL_COSTO.replace(":source", String.valueOf(source))
                        .replace(":target", String.valueOf(target));
        
        List<?> resultados = em.createNativeQuery(sql).getResultList();
        
        if (resultados.isEmpty()) {
            return Double.valueOf("0");
        } else {
            Object[] columns = (Object[]) resultados.get(0);
            if (((BigInteger)columns[1]).intValue() == 0) {
                return Double.valueOf("0");
            } else {
                if (((Double)columns[0]).equals(Double.valueOf("0"))) {
                    return c1.distanceTo(c2);
                }
            }
            return (Double)columns[0] * 1000;
        }*/
    }

    public List<Result> getResults(Candidate c1, Candidate c2) {
        ArrayList<Result> results = new ArrayList<>();

        // Si ambos puntos candidatos estan sobre la misma linea
        if (c1.getEdge().getId() == c2.getEdge().getId()) {
            double speed = c1.getEdge().getSpeed();
            results.add(createResult(speed, c1, c2));
            return results;
        }

        // Si ambos puntos candidatos tienen el mismo vertice mas cercano
        if (c1.getNearestVertex().getId() == c2.getNearestVertex().getId()) {
            Coordinate cm = c1.getNearestVertex();
            double speed1 = c1.getEdge().getSpeed();
            results.add(createResult(speed1, c1, cm));
            double speed2 = c2.getEdge().getSpeed();
            results.add(createResult(speed2, cm, c2));
            return results;
        }

        int source = c1.getNearestVertex().getId();
        int target = c2.getNearestVertex().getId();
        
        List<?> resultList = getResultList(source, target);
        for (int i = 0; i < resultList.size(); i++) {
            Object[] columns = (Object[]) resultList.get(i);
            Result result = createResult(columns);
            if (result.getId() != -1) {
                Edge edge = createEdge(columns);
                result.setEdge(edge);
                results.add(result);
            }
        }

        if (results.isEmpty()) {
            LOG.log(Level.SEVERE, "No existe ningun camino entre source={0} y target={1}", new Object[]{source, target});
            return results;
        }

        Result first = results.get(0);
        if (first.getEdge().getId() == c1.getEdge().getId()) {
            first.getSource().moveTo(c1);
            first.calculateDistance();
        } else {
            double speed = c1.getEdge().getSpeed();
            results.add(createResult(speed, c1, first.getSource()));
        }

        Result last = results.get(results.size() - 1);
        if (last.getEdge().getId() == c2.getEdge().getId()) {
            last.getTarget().moveTo(c2);
            last.calculateDistance();
        } else {
            double speed = c2.getEdge().getSpeed();
            results.add(createResult(speed, first.getTarget(), c2));
        }

        return results;
    }

    private List<?> getResultList(int source, int target) {
        String sql = SQL.replace(":source", String.valueOf(source))
                        .replace(":target", String.valueOf(target));
        List<?> result = em.createNativeQuery(sql).getResultList();
        return result;
    }

    private Result createResult(Object[] columns) {
        Result result = new Result();
        result.setId((Integer) columns[2]);
        result.setNode((Integer) columns[1]);
        return result;
    }

    private Edge createEdge(Object[] columns) {
        Vertex source = new Vertex();
        source.setId((Integer) columns[5]);
        source.setLongitude((Double) columns[9]);
        source.setLatitude((Double) columns[10]);
        Vertex target = new Vertex();
        target.setId((Integer) columns[6]);
        target.setLongitude((Double) columns[11]);
        target.setLatitude((Double) columns[12]);
        Edge edge = new Edge();
        edge.setId((Integer) columns[4]);
        edge.setSpeed((Integer) columns[7]);
        edge.setLength((Double) columns[8]);
        edge.setSource(source);
        edge.setTarget(target);
        return edge;
    }

    private Result createResult(double speed, Coordinate from, Coordinate to) {
        Vertex source = new Vertex();
        source.setId(1);
        source.moveTo(from);
        Vertex target = new Vertex();
        target.setId(2);
        target.moveTo(to);
        Edge edge = new Edge();
        edge.setSpeed(speed);
        edge.setSource(source);
        edge.setTarget(target);
        edge.calculateDistance();
        Result result = new Result();
        result.setNode(source.getId());
        result.setEdge(edge);
        return result;
    }

}
