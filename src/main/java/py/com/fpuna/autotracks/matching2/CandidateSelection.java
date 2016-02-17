package py.com.fpuna.autotracks.matching2;

import java.util.ArrayList;
import java.util.List;
import java.util.Locale;
import javax.persistence.EntityManager;
import javax.persistence.PersistenceContext;
import py.com.fpuna.autotracks.matching2.model.Candidate;
import py.com.fpuna.autotracks.matching2.model.CandidateSet;
import py.com.fpuna.autotracks.matching2.model.Point;
import py.com.fpuna.autotracks.matching2.model.Edge;
import py.com.fpuna.autotracks.matching2.model.Vertex;

public class CandidateSelection {
    private static final double MIN_DIST = 10;
    private static final double MAX_DIST = 150;
    private static final double RELATION_DEGREES_METER = 111159.0;
    private static final double ACCURACY_MULTIPLIER = 1.2;

    private static final String SQL = " SELECT "
            + " id, "
            + " source, "
            + " target, "
            + " km, "
            + " kmh, "
            + " x1, "
            + " y1, "
            + " x2, "
            + " y2, "
            + " ST_X(point) as lon, "
            + " ST_Y(point) as lat "
            + " FROM (\n"
            + "   SELECT r.*, \n"
            + "     ST_LineInterpolatePoint(\n"
            + "       geom_way, ST_LineLocatePoint(geom_way, :geom)\n"
            + "     ) AS point\n"
            + "   FROM asu_2po_4pgr r \n"
            + "   WHERE ST_DWithin(:geom, geom_way, :dist)\n"
            + " ) a";

    @PersistenceContext
    EntityManager em;

    public List<CandidateSet> getCandidateSet(List<Point> points) {
        List<CandidateSet> candidateSets = new ArrayList<>();
        for (Point point : points) {
            List<Candidate> candidates = new ArrayList<>();
            List<?> resultList = getResultList(point);
            for (int i = 0; i < resultList.size(); i++) {
                Object[] columns = (Object[]) resultList.get(i);
                Candidate candidate = createCandidate(columns);
                candidate.setEdge(createEdge(columns));
                candidates.add(candidate);
            }
            CandidateSet candidateSet = createCandidateSet(candidates, point);
            candidateSets.add(candidateSet);
        }
        return candidateSets;
    }

    private List<?> getResultList(Point point) {
        double lon = point.getLongitude();
        double lat = point.getLatitude();
        String geom = "ST_GeomFromText('SRID=4326;POINT(" + lon + " " + lat + ")')";
        String radius;
        if (point.getAccuracy() != null && point.getAccuracy() > MIN_DIST
                && point.getAccuracy() < MAX_DIST) {
            radius = getAcuracyInDegrees(point.getAccuracy() * ACCURACY_MULTIPLIER);
        } else if (point.getAccuracy() < MAX_DIST){
            radius = getAcuracyInDegrees(MIN_DIST);
        } else {
            radius = getAcuracyInDegrees(MAX_DIST);
        }
        String sql = SQL.replace(":geom", geom).replace(":dist", radius);
        List<?> result = em.createNativeQuery(sql).getResultList();
        return result;
    }

    private Candidate createCandidate(Object[] columns) {
        Candidate candidate = new Candidate();
        candidate.setLongitude((Double) columns[9]);
        candidate.setLatitude((Double) columns[10]);
        return candidate;
    }

    private Edge createEdge(Object[] columns) {
        Vertex source = new Vertex();
        source.setId((Integer) columns[1]);
        source.setLongitude((Double) columns[5]);
        source.setLatitude((Double) columns[6]);
        Vertex target = new Vertex();
        target.setId((Integer) columns[2]);
        target.setLongitude((Double) columns[7]);
        target.setLatitude((Double) columns[8]);
        Edge edge = new Edge();
        edge.setId((Integer) columns[0]);
        edge.setSpeed(((Integer) columns[4]) * 1000.0 / 3600.0); // convertir a m/s
        edge.setLength(((Double) columns[3]) * 1000.0); // convertir a m
        edge.setSource(source);
        edge.setTarget(target);
        return edge;
    }

    private CandidateSet createCandidateSet(List<Candidate> candidates, Point point) {
        CandidateSet candidateSet = new CandidateSet();
        candidateSet.setCandidates(candidates);
        candidateSet.setPoint(point);
        return candidateSet;
    }
    
    private String getAcuracyInDegrees(Double accuracy) {
        double degrees = accuracy / RELATION_DEGREES_METER;
        String retorno = String.format(Locale.US,"%.8f", degrees);
        return retorno;
    }

}
