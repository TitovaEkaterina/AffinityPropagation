/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package affinity_propagation_sparse_matrix_titova;

import java.util.Map;
import java.util.TreeMap;

/**
 *
 * @author titova_ekaterina
 */
public class SparseMatrix {

    private TreeMap<Integer, TreeMap<Integer, Element>> listByRows;
    private TreeMap<Integer, TreeMap<Integer, Element>> listByCols;

    private TreeMap<Integer, Float> sumByRow;
    private TreeMap<Integer, Float> sumByCol;
    private TreeMap<Integer, Float> sumBigestThemZeroElementsByRow;
    private TreeMap<Integer, Float> sumBigestThemZeroElementsByCol;
    private TreeMap<Integer, Element> maxByRow;
    private TreeMap<Integer, Element> maxByCol;
    private TreeMap<Integer, Element> secondMaxByRow;
    private TreeMap<Integer, Element> secondMaxByCol;

    public SparseMatrix() {
        listByRows = new TreeMap();
        listByCols = new TreeMap();
        maxByRow = new TreeMap();
        maxByCol = new TreeMap();
        secondMaxByRow = new TreeMap();
        secondMaxByCol = new TreeMap();
        sumByRow = new TreeMap();
        sumByCol = new TreeMap();
    }

    public class Element {

        int row;
        int col;
        float value;

        public Element(int row, int col, float value) {
            this.col = col;
            this.row = row;
            this.value = value;
        }

        public Element minus(Element element) {
            return new Element(this.row, this.col, this.value - element.value);
        }

        public Element minus(float a) {
            return new Element(this.row, this.col, this.value - a);
        }

        public Element plus(Element element) {
            return new Element(this.row, this.col, this.value + element.value);
        }

        public Element plus(float a) {
            return new Element(this.row, this.col, this.value + a);
        }

        public Element minimumWithZero() {
            if (this.value < 0) {
                return this;
            } else {
                return new Element(this.row, this.col, 0);
            }
        }

        public Element maximimWithZero() {
            if (this.value > 0) {
                return this;
            } else {
                return new Element(this.row, this.col, 0);
            }
        }
    }

    public void addEllement(int row, int col, float value) {
        this.addEllement(new Element(row, col, value));

    }

    public void addEllement(Element element) {
        if (element.value == 0) {
            return;
        }

        if (!listByRows.containsKey(element.row)) {
            listByRows.put(element.row, new TreeMap<Integer, Element>());
            maxByRow.put(element.row, element);
            secondMaxByRow.put(element.row, new Element(element.row, element.col == 0 ? 1 : 0, 0));
            sumByRow.put(element.row, 0.0f);
            sumBigestThemZeroElementsByRow.put(element.row, 0.0f);
        }
        if (element.value > maxByRow.get(element.row).value) {
            secondMaxByRow.replace(element.row, maxByRow.get(element.row));
            maxByRow.replace(element.row, element);
        }
        sumByRow.replace(element.row, sumByRow.get(element.row)  - getElement(element.row, element.col).value + element.value);
        if (element.value > 0) {
            sumBigestThemZeroElementsByRow.replace(element.row, sumBigestThemZeroElementsByRow.get(element.row) - getElement(element.row, element.col).value + element.value);
        }
        listByRows.get(element.row).put(element.col, element);

        if (!listByCols.containsKey(element.col)) {
            listByCols.put(element.col, new TreeMap<Integer, Element>());
            maxByCol.put(element.col, element);
            secondMaxByCol.put(element.col, new Element(element.row == 0 ? 1 : 0, element.col, 0));
            sumByCol.put(element.col, 0.0f);
            sumBigestThemZeroElementsByCol.put(element.row, 0.0f);
        }
        if (element.value > maxByCol.get(element.col).value) {
            secondMaxByCol.replace(element.col, maxByCol.get(element.col));
            maxByCol.replace(element.col, element);
        }
        sumByCol.replace(element.col, sumByCol.get(element.col) + element.value);
        if (element.value > 0) {
            sumBigestThemZeroElementsByCol.replace(element.col, sumBigestThemZeroElementsByCol.get(element.col) - getElement(element.row, element.col).value + element.value);
        }
        listByCols.get(element.col).put(element.row, element);
        

    }

    public Element getElement(int row, int col) {

        if (!this.listByCols.containsKey(col)) {
            return new Element(row, col, 0);
        }

        if (!this.listByCols.get(col).containsKey(row)) {
            return new Element(row, col, 0);
        }

        return this.listByCols.get(col).get(row);

    }

    public boolean containsElement(Element element) {
        return this.getElement(element.row, element.col).value != 0;
    }

    public SparseMatrix minus(SparseMatrix sparseMatrixOther) {
        SparseMatrix sparseMatrix = new SparseMatrix();
        for (Map.Entry<Integer, TreeMap<Integer, Element>> entry : this.listByCols.entrySet()) {
            for (Map.Entry<Integer, Element> element : entry.getValue().entrySet()) {
                sparseMatrix.addEllement(element.getValue().minus(sparseMatrixOther.getElement(element.getValue().row, element.getValue().col)));
            }
        }

        for (Map.Entry<Integer, TreeMap<Integer, Element>> entry : sparseMatrixOther.listByCols.entrySet()) {
            for (Map.Entry<Integer, Element> element : entry.getValue().entrySet()) {
                Element el = this.getElement(element.getValue().row, element.getValue().col).minus(element.getValue());
                if (!sparseMatrix.containsElement(el)) {
                    sparseMatrix.addEllement(el);
                }
            }
        }

        return sparseMatrix;
    }

    public SparseMatrix plus(SparseMatrix sparseMatrixOther) {
        SparseMatrix sparseMatrix = new SparseMatrix();
        for (Map.Entry<Integer, TreeMap<Integer, Element>> entry : this.listByCols.entrySet()) {
            for (Map.Entry<Integer, Element> element : entry.getValue().entrySet()) {
                sparseMatrix.addEllement(element.getValue().plus(sparseMatrixOther.getElement(element.getValue().row, element.getValue().col)));
            }
        }

        for (Map.Entry<Integer, TreeMap<Integer, Element>> entry : sparseMatrixOther.listByCols.entrySet()) {
            for (Map.Entry<Integer, Element> element : entry.getValue().entrySet()) {
                Element el = element.getValue().plus(this.getElement(element.getValue().row, element.getValue().col));
                if (!sparseMatrix.containsElement(el)) {
                    sparseMatrix.addEllement(el);
                }
            }
        }

        return sparseMatrix;
    }

    public double maxByRow(int col) {
        if (this.maxByRow.containsKey(col)) {
            return this.maxByRow.get(col).value;
        } else {
            return 0;
        }
    }

    public double maxByRow(int col, int i) {
        if (this.maxByRow.containsKey(col)) {
            return this.maxByRow.get(col).value == this.listByRows.get(i).get(col).value ? this.secondMaxByRow.get(col).value : this.maxByRow.get(col).value;
        } else {
            return 0;
        }
    }

    public double maxByCol(int row) {
        if (this.maxByCol.containsKey(row)) {
            return this.maxByCol.get(row).value;
        } else {
            return 0;
        }
    }

    public float maxByCol(int row, int i) {
        if (this.maxByCol.containsKey(row)){
            return this.maxByCol.get(row).value == this.listByCols.get(row).get(i).value ? this.secondMaxByCol.get(row).value : this.maxByCol.get(row).value;
        } else {
            return 0;
        }
    }

    public float summByRow(int i, int k) {
        if (this.sumByRow.containsKey(k)){
            return this.sumByRow.get(k) - this.getElement(k, k).value - this.getElement(k, i).value;
        } else {
            return 0;
        }
    }

    public float summByCol(int i, int k) {
        if (this.sumByCol.containsKey(k)){
            return this.sumByCol.get(k) - - this.getElement(k, k).value - this.getElement(i, k).value;
        } else {
            return 0;
        }
    }
    
    public float summBigestThenZeroElementsByRow(int i, int k) {
        if (this.sumBigestThemZeroElementsByRow.containsKey(k)){
            return this.sumBigestThemZeroElementsByRow.get(k) 
                    - (this.getElement(k, k).value > 0 ? this.getElement(k, k).value : 0) 
                    - (this.getElement(k, i).value > 0 ? this.getElement(k, i).value : 0);
        } else {
            return 0;
        }
    }

    public float summBigestThenZeroElementsByCol(int i, int k) {
        if (this.sumBigestThemZeroElementsByCol.containsKey(k)){
            return this.sumBigestThemZeroElementsByCol.get(k) -
                    - (this.getElement(k, k).value > 0 ? this.getElement(k, k).value : 0) 
                    - (this.getElement(i, k).value > 0 ? this.getElement(i, k).value : 0);
        } else {
            return 0;
        }
    }

    public int argMaxByCol(SparseMatrix sparseMatrix, int i) {
        int argMax = 0;
        double elementMax = Double.MAX_VALUE;

        SparseMatrix summSparseMatrix = this.plus(sparseMatrix);

        for (Map.Entry<Integer, Element> element : summSparseMatrix.listByRows.get(i).entrySet()) {
            if (elementMax < element.getValue().value) {
                argMax = element.getValue().col;
            }
        }

        return argMax;
    }

    public SparseMatrix maxByColWithoutK() {
        SparseMatrix sparseMatrix = new SparseMatrix();
        for (Map.Entry<Integer, TreeMap<Integer, Element>> entry : this.listByRows.entrySet()) {
            for (Map.Entry<Integer, Element> element : entry.getValue().entrySet()) {
                sparseMatrix.addEllement(new Element(
                        element.getValue().row,
                        element.getValue().col,
                        this.maxByCol(
                                element.getValue().row,
                                element.getValue().col
                        )
                ));
            }
        }

        return sparseMatrix;
    }

    public SparseMatrix sumInR() {
        SparseMatrix sparseMatrix = new SparseMatrix();

        for (Map.Entry<Integer, TreeMap<Integer, Element>> entry : this.listByRows.entrySet()) {
            for (Map.Entry<Integer, Element> element : entry.getValue().entrySet()) {
                if (element.getValue().row != element.getValue().col) {
                    sparseMatrix.addEllement(
                            new Element(
                                    element.getValue().row,
                                    element.getValue().col,
                                    this.listByRows.get(element.getValue().col).get(element.getValue().col).value + this.summBigestThenZeroElementsByRow(
                                            element.getValue().row,
                                            element.getValue().col
                                    )
                            ).minimumWithZero()
                    );
                } else {
                    sparseMatrix.addEllement(
                            new Element(
                                    element.getValue().row,
                                    element.getValue().col,
                                    this.summBigestThenZeroElementsByRow(
                                            -1,
                                            element.getValue().col
                                    )
                            )
                    );
                }
            }
        }

        return sparseMatrix;
    }

    public void printMatrix() {
        for (Map.Entry<Integer, TreeMap<Integer, Element>> entry : this.listByCols.entrySet()) {
            for (Map.Entry<Integer, Element> element : entry.getValue().entrySet()) {
                System.out.print(element.getValue().value + " ");
            }
            System.out.println();
        }
    }

}
