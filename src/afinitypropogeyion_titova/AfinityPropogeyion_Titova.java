
package afinitypropogeyion_titova;

import java.awt.EventQueue;
import java.io.BufferedReader;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import static java.lang.Double.max;
import static java.lang.Double.min;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;
import java.util.Map;
import java.util.Random;
import java.util.SortedSet;
import java.util.TreeMap;
import java.util.TreeSet;
import java.util.logging.Level;
import java.util.logging.Logger;
import javafx.util.Pair;

/**
 *
 * @author titova_ekaterina
 */
public class AfinityPropogeyion_Titova {

    /**
     * @param args the command line arguments
     */
    public static void main(String[] args) throws IOException {

        Graph graph = new Graph(); // граф пользователей
        int maxit = 50;
        double damping = 0.001;
        double[] Results = new double[5];
        double[] ResultsAll = new double[5];

        // Считываем граф пользователей
        read_training_text_edges("/home/titova_ekaterina/course_Ml/Titova/AP/Gowalla_edges.txt", graph);

        //Вектор локаций
        ArrayList<Integer>[] vecOfLocation = new ArrayList[graph.n];

        // Считываем Локализации по пользователям
        readLocation("/home/titova_ekaterina/course_Ml/Titova/AP/Gowalla_totalCheckins.txt", vecOfLocation);

        int[] examplar = new int[graph.n]; // К какому классу принадлежит i-ый экземпляр

        // Кластеризация
        clusteringGraph(graph, examplar, maxit, damping);
        
        FileWriter csvWriter = new FileWriter("/home/titova_ekaterina/course_Ml/Titova/AfinityPropogeyion_Titova/Results_Hist.csv");
        csvWriter.append(",");
        for (int i = 0; i < examplar.length; ++i) {
            csvWriter.append(examplar[i] + ",");
        }
        csvWriter.append("\n");
        csvWriter.flush();
        csvWriter.close();

        // Shuffle
        List<Integer> indexes = new ArrayList<>(0);
        for (int i = 0; i < examplar.length; ++i) {
            indexes.add(i);
        }
        Collections.shuffle(indexes, new Random());

        int crossValCount = examplar.length / 5;

        for (int cr = 0; cr < 5; cr++) {

            ArrayList<Integer> examplarTrain = new ArrayList();
            ArrayList<Integer> examplarTest = new ArrayList();
            ArrayList<Integer>[] vecTrain = new ArrayList[examplar.length - crossValCount];
            ArrayList<Integer>[] vecTest = new ArrayList[crossValCount];

            int indexTrain = 0;
            int indexTest = 0;
            for (int j = 0; j < indexes.size(); j++) {
                if (j < crossValCount * cr || j >= crossValCount * (cr + 1)) {
                    examplarTrain.add(examplar[indexes.get(j)]);
                    vecTrain[indexTrain] = vecOfLocation[indexes.get(j)];
                    indexTrain++;
                } else {
                    examplarTest.add(examplar[indexes.get(j)]);
                    vecTest[indexTest] = vecOfLocation[indexes.get(j)];
                    indexTest++;
                }
            }

            TreeMap<Integer, Integer> topByTrainAll = new TreeMap();
            for (int i = 0; i < vecTrain.length; ++i) {
                if (vecTrain[i] != null) {
                    for (int j = 0; j < vecTrain[i].size(); ++j) {
                        if (topByTrainAll.get(vecTrain[i].get(j)) != null) {
                            topByTrainAll.put(vecTrain[i].get(j), topByTrainAll.get(vecTrain[i].get(j)) + 1);
                        } else {
                            topByTrainAll.put(vecTrain[i].get(j), 1);
                        }
                    }
                }
            }

            TreeMap<Integer, Integer> TOPLocationclusterAllTrain;
            TOPLocationclusterAllTrain = putFirstEntries(10, sortByValues(topByTrainAll));

            // Считаем количества вхождений мест для каждого кластера
            TreeMap<Integer, TreeMap<Integer, Integer>> topByTrainInEachCluster = new TreeMap();
            for (int i = 0; i < vecTrain.length; ++i) {
                if (vecTrain[i] != null) {
                    for (int j = 0; j < vecTrain[i].size(); ++j) {
                        if (topByTrainInEachCluster.get(examplarTrain.get(i)) != null) {
                            if (topByTrainInEachCluster.get(examplarTrain.get(i)).get(vecTrain[i].get(j)) != null) {
                                topByTrainInEachCluster.get(examplarTrain.get(i)).put(vecTrain[i].get(j), topByTrainInEachCluster.get(examplarTrain.get(i)).get(vecTrain[i].get(j)) + 1);
                            } else {
                                topByTrainInEachCluster.get(examplarTrain.get(i)).put(vecTrain[i].get(j), 1);
                            }
                        } else {
                            topByTrainInEachCluster.put(examplarTrain.get(i), new TreeMap());
                            if (topByTrainInEachCluster.get(examplarTrain.get(i)).get(vecTrain[i].get(j)) != null) {
                                topByTrainInEachCluster.get(examplarTrain.get(i)).put(vecTrain[i].get(j), topByTrainInEachCluster.get(examplarTrain.get(i)).get(vecTrain[i].get(j)) + 1);
                            } else {
                                topByTrainInEachCluster.get(examplarTrain.get(i)).put(vecTrain[i].get(j), 1);
                            }
                        }
                    }
                }
            }

            // Одбираем Top-10
            TreeMap<Integer, TreeMap<Integer, Integer>> TOPLocationcluster = new TreeMap();

            for (Map.Entry<Integer, TreeMap<Integer, Integer>> entry : topByTrainInEachCluster.entrySet()) {
                TOPLocationcluster.put(entry.getKey(), putFirstEntries(10, sortByValues(entry.getValue())));
            }

            // Считаем качество разбиения
            double AllSumm = 0;
            double AllSummTopGlobal = 0;
            int sizeLocation = 0;
            for (int i = 0; i < vecTest.length; ++i) {
                if (vecTest[i] != null) {
                    sizeLocation += vecTest[i].size();

                    for (Map.Entry<Integer, Integer> entry : TOPLocationclusterAllTrain.entrySet()) {
                        if (vecTest[i].contains(entry.getKey())) {
                            AllSummTopGlobal++;
                        }
                    }

                    if (TOPLocationcluster.get(examplarTest.get(i)) != null) {

                        for (Map.Entry<Integer, Integer> entry : TOPLocationcluster.get(examplarTest.get(i)).entrySet()) {
                            if (vecTest[i].contains(entry.getKey())) {
                                AllSumm++;
                            }
                        }
                    } else {
                        for (Map.Entry<Integer, Integer> entry : TOPLocationclusterAllTrain.entrySet()) {
                            if (vecTest[i].contains(entry.getKey())) {
                                AllSumm++;
                            }
                        }
                    }
                }
            }
            System.out.println("AllSumm/sizeLocation = " + AllSumm / (double) sizeLocation);
            Results[cr] = (AllSumm / (double) sizeLocation);
            System.out.println("AllSummTopGlobal/sizeLocation = " + AllSummTopGlobal / (double) sizeLocation);
            ResultsAll[cr] = (AllSummTopGlobal / (double) sizeLocation);
            System.out.println("--------------------------");
        }

        Statistic stResults = Statistics.calcMeanAndSig(Results);
        Statistic stResultsAll = Statistics.calcMeanAndSig(ResultsAll);

        FileWriter csvWriter2 = new FileWriter("/home/titova_ekaterina/course_Ml/Titova/AfinityPropogeyion_Titova/Results_Table.csv");

        csvWriter2.append(
                ",1,2,3,4,5,E,SD,\n");
        csvWriter2.append(
                "Results on top all," + ResultsAll[0] + "," + ResultsAll[1] + "," + ResultsAll[2] + "," + ResultsAll[3] + "," + ResultsAll[4] + "," + stResults.getMean() + "," + stResults.getSigma() + ",\n");
        csvWriter2.append(
                "Results on top on cluster," + Results[0] + "," + Results[1] + "," + Results[2] + "," + Results[3] + "," + Results[4] + "," + stResultsAll.getMean() + "," + stResultsAll.getSigma() + ",\n");
        csvWriter2.flush();

        csvWriter2.close();
    }

    public static <K, V extends Comparable<V>> TreeMap<K, V>
            sortByValues(final Map<K, V> map) {
        Comparator<K> valueComparator
                = new Comparator<K>() {
                    public int compare(K k1, K k2) {
                        int compare
                        = map.get(k2).compareTo(map.get(k1));
                        return compare;
                    }
                };

        TreeMap<K, V> sortedByValues
                = new TreeMap<K, V>(valueComparator);
        sortedByValues.putAll(map);
        return sortedByValues;
    }

    public static TreeMap<Integer, Integer> putFirstEntries(int max, TreeMap<Integer, Integer> source) {
        int count = 0;
        TreeMap<Integer, Integer> target = new TreeMap<Integer, Integer>();
        for (Map.Entry<Integer, Integer> entry : source.entrySet()) {
            if (count >= max) {
                break;
            }
            target.put(entry.getKey(), entry.getValue());
            count++;
        }
        return target;
    }

    public static boolean pred(Pair<Integer, Integer> a, Pair<Integer, Integer> b) {
        return a.getValue() > b.getValue();
    }

    public static void readLocation(String data, ArrayList<Integer>[] vec) {

        try (BufferedReader br = new BufferedReader(new FileReader(data))) {
            String line;
            while ((line = br.readLine()) != null) {
                String[] tokens = line.split("\t");
                if (tokens.length == 5) {
                    int userId = Integer.parseInt(tokens[0]);
                    int locationId = Integer.parseInt(tokens[4]);
                    if (vec[userId] == null) {
                        vec[userId] = new ArrayList();
                    }
                    vec[userId].add(locationId);

                }
            }
        } catch (Exception ex) {
            Logger.getLogger(AfinityPropogeyion_Titova.class
                    .getName()).log(Level.SEVERE, null, ex);
        }
    }

    public static void read_training_text_edges(String data, Graph graph) {

        graph.n = 196591;
        graph.outEdges = new ArrayList[graph.n];
        graph.inEdges = new ArrayList[graph.n];
        ArrayList<Edge> edges = graph.edges;

        int index = 0;
        Random r = new Random();

        try (BufferedReader br = new BufferedReader(new FileReader(data))) {
            String line;
            while ((line = br.readLine()) != null) {
                String[] tokens = line.split("\t");
                if (tokens.length == 2) {
                    int user1 = Integer.parseInt(tokens[0]);
                    int user2 = Integer.parseInt(tokens[1]);
                    index++;
                    edges.add(new Edge(user1, user2, -1.0));

                }
            }
        } catch (Exception ex) {
            Logger.getLogger(AfinityPropogeyion_Titova.class
                    .getName()).log(Level.SEVERE, null, ex);
        }

        for (int i = 0; i < graph.n; ++i) {
            edges.add(new Edge(i, i, -1e100));
        }

        for (int i = 0; i < edges.size(); ++i) {
            Edge p = edges.get(i);
            p.similarity += (1e-16 * p.similarity + 1e-300) * (r.nextDouble() / 2);
            if (graph.outEdges[p.source] == null) {
                graph.outEdges[p.source] = new ArrayList();
            }
            if (graph.inEdges[p.destination] == null) {
                graph.inEdges[p.destination] = new ArrayList();
            }

            graph.outEdges[p.source].add(p);
            graph.inEdges[p.destination].add(p);
        }
    }

    public static void clusteringGraph(Graph graph, int[] examplar, int maxit, double damping) {
        // Кластеризуем пользоватлей
        for (int i = 0; i < maxit; ++i) {
            updateResponsibilities(graph, damping);
            updateAvailabilities(graph, damping);
            if (updateExamplars(graph, examplar)) {
                System.out.println(" is update! i = " + i);
            } else {
                System.out.println(" is not update! i = " + i);
            }
        }
    }

    public static double update(double variable, double newValue, double damping) {
        return damping * variable + (1.0 - damping) * newValue;
    }

    public static void updateResponsibilities(Graph graph, double damping) {
        for (int i = 0; i < graph.n; ++i) {
            ArrayList<Edge> edges = graph.outEdges[i];
            int m = edges.size();
            double max1 = -1e200, max2 = -1e200;
            double argmax1 = -1;
            for (int k = 0; k < m; ++k) {
                double value = edges.get(k).similarity + edges.get(k).availability;
                if (value > max1) {
                    double temp = max1;
                    max1 = value;
                    value = temp;
                    argmax1 = k;
                }
                if (value > max2) {
                    max2 = value;
                }
            }
            // update responsibilities
            for (int k = 0; k < m; ++k) {
                if (k != argmax1) {
                    edges.get(k).responsibility = update(edges.get(k).responsibility, edges.get(k).similarity - max1, damping);
                } else {
                    edges.get(k).responsibility = update(edges.get(k).responsibility, edges.get(k).similarity - max2, damping);
                }
            }
        }
    }

    public static void updateAvailabilities(Graph graph, double damping) {
        for (int k = 0; k < graph.n / 2; ++k) {
            ArrayList<Edge> edges = graph.inEdges[k];
            int m = edges.size();
            // calculate sum of positive responsibilities
            double sum = 0.0;
            double rkk = 0.0;
            for (int i = 0; i < m; ++i) {
                if (i < m - 1) {
                    sum += max(0.0, edges.get(i).responsibility);
                } else {
                    rkk = edges.get(i).responsibility;
                }
            }
            for (int i = 0; i < m; ++i) {
                if (i < m - 1) {
                    double t = min(0.0, rkk + sum - max(0.0, edges.get(i).responsibility));
                    edges.get(i).availability = update(edges.get(i).availability, t, damping);
                } else {
                    edges.get(i).availability = update(edges.get(i).availability, sum, damping);
                }
            }
        }
    }

    public static boolean updateExamplars(Graph graph, int[] examplar) {
        boolean changed = false;
        for (int i = 0; i < graph.n; ++i) {
            ArrayList<Edge> edges = graph.outEdges[i];
            int m = edges.size();
            double maxValue = -1e200;
            int argmax = i;
            for (int k = 0; k < m; ++k) {
                double value = edges.get(k).availability + edges.get(k).responsibility;
                if (value > maxValue) {
                    maxValue = value;
                    argmax = edges.get(k).destination;
                }
            }
            if (examplar[i] != argmax) {
                examplar[i] = argmax;
                changed = true;
            }
        }
        return changed;
    }

}
