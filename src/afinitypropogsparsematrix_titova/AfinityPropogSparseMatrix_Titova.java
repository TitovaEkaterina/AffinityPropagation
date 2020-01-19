/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package afinitypropogsparsematrix_titova;

import java.io.BufferedReader;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;
import java.util.Map;
import java.util.Random;
import java.util.TreeMap;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 *
 * @author titova_ekaterina
 */
public class AfinityPropogSparseMatrix_Titova {

    /**
     * @param args the command line arguments
     */
    public static void main(String[] args) throws IOException {

        int maxit = 10;
        int countOfExemplar = 0;
        double[] Results = new double[5];
        double[] ResultsAll = new double[5];

        SparseMatrix r = new SparseMatrix();
        SparseMatrix a = new SparseMatrix();
        SparseMatrix S = new SparseMatrix();
        System.out.println("statrt");
        // Считываем граф пользователей
        countOfExemplar = read_training_text_edges("/home/titova_ekaterina/course_Ml/AP/Gowalla_edges.txt", S);

        System.out.println("end");
        // TO DO
        for (int i = 0; i < maxit; ++i) {

            System.out.println("i = " + i);
            SparseMatrix tt = a.plus(S);
            System.out.println("a.plus(S) ");
            SparseMatrix ttt = tt.maxByColWithoutK();
            System.out.println("a.plus(S).maxByColWithoutK ");
            r = S.minus(ttt);
            System.out.println("update r");
            a = r.sumInR();
            System.out.println("update a");
        }
        System.out.println("end 2");

        int[] arrayExemplar = new int[countOfExemplar];
        //Вектор локаций
        ArrayList<Integer>[] vecOfLocation = new ArrayList[countOfExemplar];
        // Считываем Локализации по пользователям
        readLocation("/home/titova_ekaterina/course_Ml/AP/Gowalla_totalCheckins.txt", vecOfLocation);

        SparseMatrix argMaxMatrix = a.plus(r);

        for (int i = 0; i < countOfExemplar; ++i) {
            arrayExemplar[i] = argMaxMatrix.argMaxByCol(argMaxMatrix, i);
        }

        FileWriter csvWriter = new FileWriter("/home/titova_ekaterina/course_Ml/AfinityPropogSparseMatrix_Titova/Results_Hist.csv");
        csvWriter.append(",");
        for (int i = 0; i < arrayExemplar.length; ++i) {
            csvWriter.append(arrayExemplar[i] + ",");
        }
        csvWriter.append("\n");
        csvWriter.flush();
        csvWriter.close();

        // Shuffle
        List<Integer> indexes = new ArrayList<>(0);
        for (int i = 0; i < arrayExemplar.length; ++i) {
            indexes.add(i);
        }
        Collections.shuffle(indexes, new Random());

        int crossValCount = arrayExemplar.length / 5;

        for (int cr = 0; cr < 5; cr++) {

            ArrayList<Integer> examplarTrain = new ArrayList();
            ArrayList<Integer> examplarTest = new ArrayList();
            ArrayList<Integer>[] vecTrain = new ArrayList[arrayExemplar.length - crossValCount];
            ArrayList<Integer>[] vecTest = new ArrayList[crossValCount];

            int indexTrain = 0;
            int indexTest = 0;
            for (int j = 0; j < indexes.size(); j++) {
                if (j < crossValCount * cr || j >= crossValCount * (cr + 1)) {
                    examplarTrain.add(arrayExemplar[indexes.get(j)]);
                    vecTrain[indexTrain] = vecOfLocation[indexes.get(j)];
                    indexTrain++;
                } else {
                    examplarTest.add(arrayExemplar[indexes.get(j)]);
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

        FileWriter csvWriter2 = new FileWriter("/home/titova_ekaterina/course_Ml/AfinityPropogSparseMatrix_Titova/Results_Table.csv");

        csvWriter2.append(",1,2,3,4,5,E,SD,\n");
        csvWriter2.append("Results on top all," + ResultsAll[0] + "," + ResultsAll[1] + "," + ResultsAll[2] + "," + ResultsAll[3] + "," + ResultsAll[4] + "," + stResults.getMean() + "," + stResults.getSigma() + ",\n");
        csvWriter2.append("Results on top on cluster," + Results[0] + "," + Results[1] + "," + Results[2] + "," + Results[3] + "," + Results[4] + "," + stResultsAll.getMean() + "," + stResultsAll.getSigma() + ",\n");
        csvWriter2.flush();

        csvWriter2.close();

    }

    public static int read_training_text_edges(String data, SparseMatrix S) {

        int countOfExemplar = 0;
        Random r = new Random();

        try (BufferedReader br = new BufferedReader(new FileReader(data))) {
            String line;
            int saveUser = -1;
            while ((line = br.readLine()) != null) {
                String[] tokens = line.split("\t");
                if (tokens.length == 2) {
                    int user1 = Integer.parseInt(tokens[0]);
                    int user2 = Integer.parseInt(tokens[1]);
                    S.addEllement(user1, user2, (float) (-1.0 + (1e-16 * -1.0 + 1e-300) * (r.nextDouble() / 2)));
                    if (saveUser != user1) {
                        countOfExemplar++;
                        S.addEllement(user1, user1, (float) -1e100);
                        saveUser = user1;
                    }

                }
            }
        } catch (Exception ex) {
            Logger.getLogger(AfinityPropogSparseMatrix_Titova.class.getName()).log(Level.SEVERE, null, ex);
        }

        return countOfExemplar;
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
            Logger.getLogger(AfinityPropogSparseMatrix_Titova.class
                    .getName()).log(Level.SEVERE, null, ex);
        }
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
}
