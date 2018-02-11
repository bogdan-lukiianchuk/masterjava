package ru.javaops.masterjava.matrix;

import java.util.Arrays;
import java.util.Random;
import java.util.concurrent.ForkJoinPool;
import java.util.concurrent.RecursiveAction;

/**
 * gkislin
 * 03.07.2016
 */
public class MatrixUtil {

    public static int[][] concurrentMultiply(int[][] matrixA, int[][] matrixB) {
        final int matrixSize = matrixA.length;
        final int[][] matrixC = new int[matrixSize][matrixSize];
        final int THRESHOLD = 15000;
        MultiplyMatrixUsingForkJoinPool task = new MultiplyMatrixUsingForkJoinPool(
                new MultiplyMatrixProblem(
                        matrixA, matrixB, 0, matrixSize, matrixC
                ),
                THRESHOLD);
        ForkJoinPool.commonPool().execute(task);
        task.join();
        return matrixC;
    }

    private static class MultiplyMatrixProblem {
        private final int[][] matrixA;
        private final int[][] matrixB;
        private final int bStartColumn;
        private final int bEndColumn;
        private final int[][] result;

        MultiplyMatrixProblem(int[][] matrixA, int[][] matrixB, int bStartColumn, int bEndColumn, int[][] result) {
            this.matrixA = matrixA;
            this.matrixB = matrixB;
            this.bStartColumn = bStartColumn;
            this.bEndColumn = bEndColumn;
            this.result = result;
        }

        void solve() {
            final int matrixSize = matrixA.length;

            for (int j = bStartColumn; j < bEndColumn; j++) {
                final int jCopy = j;
                final int[] columnJ = Arrays.stream(matrixB).mapToInt(b -> b[jCopy]).toArray();
                for (int i = 0; i < matrixSize; i++) {
                    int sum = 0;
                    for (int k = 0; k < matrixSize; k++) {
                        sum += matrixA[i][k] * columnJ[k];
                    }
                    result[i][j] = sum;
                }
            }
        }

        public int getSize() {
            return bEndColumn - bStartColumn > 1 ? (bEndColumn - bStartColumn) * matrixA.length : 1;
        }
    }

    private static class MultiplyMatrixUsingForkJoinPool extends RecursiveAction {
        private final MultiplyMatrixProblem problem;
        private final int threshold;

        MultiplyMatrixUsingForkJoinPool(MultiplyMatrixProblem problem, int threshold) {
            this.problem = problem;
            this.threshold = threshold;
        }

        @Override
        protected void compute() {
            if (problem.getSize() < threshold) {
                problem.solve();
                return;
            }
            int mid = (problem.bEndColumn - problem.bStartColumn) / 2;
            MultiplyMatrixProblem left = new MultiplyMatrixProblem(
                    problem.matrixA, problem.matrixB, problem.bStartColumn, problem.bStartColumn + mid, problem.result);
            MultiplyMatrixProblem right = new MultiplyMatrixProblem(
                    problem.matrixA, problem.matrixB, problem.bStartColumn + mid, problem.bEndColumn, problem.result);
            invokeAll(new MultiplyMatrixUsingForkJoinPool(left, threshold), new MultiplyMatrixUsingForkJoinPool(right, threshold));
        }
    }

    public static int[][] singleThreadMultiply(int[][] matrixA, int[][] matrixB) {
        final int matrixSize = matrixA.length;
        final int[][] matrixC = new int[matrixSize][matrixSize];

        for (int j = 0; j < matrixSize; j++) {
            final int jCopy = j;
            final int[] columnJ = Arrays.stream(matrixB).mapToInt(b -> b[jCopy]).toArray();
            for (int i = 0; i < matrixSize; i++) {
                int sum = 0;
                for (int k = 0; k < matrixSize; k++) {
                    sum += matrixA[i][k] * columnJ[k];
                }
                matrixC[i][j] = sum;
            }
        }
        return matrixC;
    }

    public static int[][] create(int size) {
        int[][] matrix = new int[size][size];
        Random rn = new Random();

        for (int i = 0; i < size; i++) {
            for (int j = 0; j < size; j++) {
                matrix[i][j] = rn.nextInt(10);
            }
        }
        return matrix;
    }

    public static boolean compare(int[][] matrixA, int[][] matrixB) {
        final int matrixSize = matrixA.length;
        for (int i = 0; i < matrixSize; i++) {
            for (int j = 0; j < matrixSize; j++) {
                if (matrixA[i][j] != matrixB[i][j]) {
                    return false;
                }
            }
        }
        return true;
    }
}
