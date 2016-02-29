package py.com.fpuna.autotracks.matching2;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.ejb.Stateless;
import javax.inject.Inject;
import py.com.fpuna.autotracks.matching2.model.Candidate;
import py.com.fpuna.autotracks.matching2.model.CandidateSet;
import py.com.fpuna.autotracks.matching2.model.Point;
import py.com.fpuna.autotracks.matching2.model.Result;

@Stateless
public class SpatialTemporalMatching {

    private static final Logger LOG = Logger.getLogger(SpatialTemporalMatching.class.getName());

    private static final double MU = 50;
    private static final double SIGMA = 100;
    private static final double PI = 3.14;

    @Inject
    CandidateSelection candidateSelection;

    @Inject
    ShortestPathCalculation shortestPathCalculation;

    /**
     *
     * @param trayectory
     * @return
     */
    public List<Candidate> match(List<Point> trayectory) {
        List<Candidate> path = new ArrayList<>();
        boolean error = false;
        long inicio = System.currentTimeMillis();
        try {
            List<CandidateSet> graph = getCandidateSet(trayectory);
            path = findMatchedSequence(graph);       
        } catch (Exception e) {
//            LOG.log(Level.SEVERE, "Error al realizar el map matching", e);
            LOG.log(Level.SEVERE, "Error al realizar el map matching: " + e.getMessage());
            error = true;
        }
        if (!error) {
            LOG.log(Level.INFO, "Map Matching listo para ruta de: " + trayectory.size()
                    + " puntos en " + (System.currentTimeMillis() - inicio)/1000 + "s");
        }
        return path;
    }

    private List<Candidate> findMatchedSequence2(List<CandidateSet> graph) {

        for (int i = 0; i < graph.size(); i++) {
            if (i == 0) {
                CandidateSet currentSet = graph.get(i);
                Point p = currentSet.getPoint();
                for (Candidate c : currentSet.getCandidates()) {
                    c.setF(N(c, p));
                }
            } else {
                CandidateSet currentSet = graph.get(i);
                CandidateSet previousSet = graph.get(i - 1);
                Point p1 = previousSet.getPoint();
                Point p2 = currentSet.getPoint();
                for (Candidate c2 : currentSet.getCandidates()) {
                    c2.setF(Double.MIN_VALUE);
                    for (Candidate c1 : previousSet.getCandidates()) {
                        if (!getShortestPath(c1, c2).isEmpty()) {                        
//                        if (!getSimpleShortestPath(c1, c2).equals(Double.valueOf("0"))) {
                            double alt = c1.getF() + F(c1, p1, c2, p2);
                            if (alt > c2.getF()) {
                                c2.setF(alt);
                            }
                        }
                    }
                }
            }
        }

        Candidate previous = null;
        List<Candidate> finalCandidates = new ArrayList<>();
        for (CandidateSet set : graph) {
            List<Candidate> candidates = set.getCandidates();
            Candidate candidate = candidates.get(0);
            for (Candidate c : candidates) {
                if (c.getF() > candidate.getF()) {
                    if (previous == null || !getShortestPath(previous, c).isEmpty()) {
                        candidate = c;
                    }
                }
            }
            previous = candidate;
            finalCandidates.add(previous);
        }

        return finalCandidates;
    }

    /**
     *
     * @param graph
     * @return
     */
    private List<Candidate> findMatchedSequence(List<CandidateSet> graph) {

        for (int i = 0; i < graph.size(); i++) {
            if (i == 0) {
                CandidateSet currentSet = graph.get(i);
                Point p = currentSet.getPoint();
                for (Candidate c : currentSet.getCandidates()) {
                    c.setF(N(c, p));
                }
            } else {
                CandidateSet currentSet = graph.get(i);
                CandidateSet previousSet = graph.get(i - 1);
                Point p1 = previousSet.getPoint();
                Point p2 = currentSet.getPoint();
                for (Candidate c2 : currentSet.getCandidates()) {
                    double max = Double.MIN_VALUE;
                    for (Candidate c1 : previousSet.getCandidates()) {
//                        if (!getShortestPath(c1, c2).isEmpty()) {
//                        if (!getSimpleShortestPath(c1, c2).equals(Double.valueOf("0"))) {
                            double alt = c1.getF() + F(c1, p1, c2, p2);
                            if (alt > max) {
                                max = alt;
                                c2.setPre(c1);
                            }
                            c2.setF(max);
//                        }
                    }
                    if (c2.getPre() == null) {
                        int j = currentSet.getCandidates().indexOf(c2);
//                        LOG.log(Level.SEVERE, "El candidato {0} del punto {1}no se puede alcanzar", new Object[]{j, i});
                    }
                }
            }
        }

        CandidateSet lastSet = graph.get(graph.size() - 1);
        List<Candidate> candidates = lastSet.getCandidates();
        Candidate candidate = candidates.get(0);
        for (int i = 1; i < candidates.size(); i++) {
            if (candidates.get(i).getF() > candidate.getF()) {
                candidate = candidates.get(i);
            }
        }

        List<Candidate> finalCandidates = new ArrayList<>();
        while (candidate != null) {
            finalCandidates.add(candidate);
            candidate = candidate.getPre();
        }

        Collections.reverse(finalCandidates);
        return finalCandidates;
    }

    /**
     *
     * @param points
     * @return
     */
    public List<CandidateSet> getCandidateSet(List<Point> points) throws Exception {
        return candidateSelection.getCandidateSet(points);
    }

    /**
     *
     * @param c1
     * @param p1
     * @param c2
     * @param p2
     * @return
     */
    private double F(Candidate c1, Point p1, Candidate c2, Point p2) {
        return FS(c1, p1, c2, p2); // * FT(c1, p1, c2, p2);
    }

    /*
     * SPATIAL ANALISYS
     */
    /**
     *
     * @param c1
     * @param p1
     * @param c2
     * @param p2
     * @return
     */
    private double FS(Candidate c1, Point p1, Candidate c2, Point p2) {
        return N(c2, p2) * V(c1, p1, c2, p2);
    }

    /**
     *
     * @param c
     * @param p
     * @return
     */
    private double N(Candidate c, Point p) {
        return (1 / (Math.sqrt(2 * PI) * SIGMA)) * Math.exp(-((Math.pow((c.distanceTo(p) - MU), 2)) / (2 * Math.pow(SIGMA, 2))));
    }

    /**
     *
     * @param c1
     * @param p1
     * @param c2
     * @param p2
     * @return
     */
    public double V(Candidate c1, Point p1, Candidate c2, Point p2) {
        double distanceTo = p1.distanceTo(p2);
        double shortestPathLength = simpleShortestPathLength(c1, c2);
        return distanceTo < shortestPathLength ? 
               distanceTo / shortestPathLength : 
               shortestPathLength / distanceTo;
    }

    /**
     *
     * @param c1
     * @param c2
     * @return
     */
    private double shortestPathLength(Candidate c1, Candidate c2) {
        double length = 0;
        List<Result> edges = getShortestPath(c1, c2);
        if (edges.isEmpty()) {
            length = c1.distanceTo(c2);
        } else {
            for (Result edge : edges) {
                length += edge.getLength();
            }
        }
        return length;
    }
    
    /**
     * 
     * @param c1
     * @param c2
     * @return 
     */
    private double simpleShortestPathLength(Candidate c1, Candidate c2) {
        return getSimpleShortestPath(c1, c2);
    }

    /*
     * TEMPORAL ANALISYS
     */
    /**
     *
     * @param c1
     * @param p1
     * @param c2
     * @param p2
     * @return
     */
    private double FT(Candidate c1, Point p1, Candidate c2, Point p2) {
        double speed = v(c1, p1, c2, p2);
        double a = 0, b = 0, c = 0;
        for (Result e : getShortestPath(c1, c2)) {
            a += e.getSpeed() * speed;
            b += Math.pow(e.getSpeed(), 2);
            c += Math.pow(speed, 2);
        }
        return a / (Math.sqrt(b) * Math.sqrt(c));
    }

    /**
     *
     * @param c1
     * @param p1
     * @param c2
     * @param p2
     * @return
     */
    private double v(Candidate c1, Point p1, Candidate c2, Point p2) {
        // TODO esto puede producir Infinite si time -> 0!!!
        double distance = shortestPathLength(c1, c2); // esta en m
        double time = (p2.getTime() - p1.getTime()) / 1000.0; // convertir a s
        return distance / time; // m/s
    }

    /*
     * SHORTESTS PATH
     */
    Map<Candidate, Map<Candidate, List<Result>>> shortestsPathsCache = new HashMap<>();
//    Map<Candidate, Map<Candidate, Double>> simpleShortestsPathsCache = new HashMap<>(); //createLRUMap(MAX_CACHE);

    /**
     *
     * @param c1
     * @param c2
     * @return
     */
    private List<Result> getShortestPath(Candidate c1, Candidate c2) {
        Map<Candidate, List<Result>> paths = shortestsPathsCache.get(c1);
        if (paths != null) {
            List<Result> path = paths.get(c2);
            if (path != null) {
                return path;
            }
        }
        List<Result> path = shortestPathCalculation.getResults(c1, c2);
        if (paths == null) {
            paths = new HashMap<>();
        }
        paths.put(c2, path);
        shortestsPathsCache.put(c1, paths);
        return path;
    }
    
    private Double getSimpleShortestPath(Candidate c1, Candidate c2) {
//        Map<Candidate, Double> paths = simpleShortestsPathsCache.get(c1);
//        if (paths != null) {
//            Double path = paths.get(c2);
//            if (path != null) {
//                return path;
//            }
//        }
        Double path = shortestPathCalculation.getShortestPathDistance(c1, c2);
//        if (paths == null) {
//            paths = new HashMap<>();
//        }
//        paths.put(c2, path);
//        simpleShortestsPathsCache.put(c1, paths);
        return path;
    }
    
    public static <K, V> Map<K, V> createLRUMap(final int maxEntries) {
        return new LinkedHashMap<K, V>(maxEntries*10/7, 0.7f, true) {
            @Override
            protected boolean removeEldestEntry(Map.Entry<K, V> eldest) {
                return size() > maxEntries;
            }
        };
    }

}
