package my;

import mpi.MPI;
import mpi.MPIException;

import java.io.IOException;
import java.math.BigInteger;
import java.util.Arrays;

public class Utils {
    static MatrixMPI gaussianEliminationForRows(int rank, MatrixMPI mat, int N, int[] rowsToSolve) throws MPIException, IOException, ClassNotFoundException {
        mat = forwardElim(mat, N, rowsToSolve);

        MatrixMPI solutions = backSubstitution(mat, N, rowsToSolve);

        MPI.Finalize();

        if (rank == 0) {
            return solutions;
        }
        return null;
    }

    static MatrixMPI forwardElim(MatrixMPI mat, int N, int[] rowsToSolve) throws MPIException, IOException, ClassNotFoundException {
        for (int k = 0; k < N; k++) {
            for (int i : rowsToSolve) {
                if (i <= k) {
                    continue;
                }
                Fraction factor = mat.get(i, k).divide(mat.get(k, k));

                for (int j = k + 1; j <= N; j++) {
                    mat.set(i, j,
                            mat.get(i, j).subtract(mat.get(k, j).multiply(factor)));
                    //mat[i][j] = (mat[i][j].multiply(mat[k][k])).subtract(mat[k][j].multiply(mat[i][k]));
                }
                mat.set(i, k, new Fraction(0));
            }

            //Sync changed rows with other processors
            MatrixMPI procRows = mat.getSubMatrix(rowsToSolve);
            mat.gatherMatrixTo(procRows, 0);
            mat.bcast(0);
        }

        return mat;
    }


    static MatrixMPI backSubstitution(MatrixMPI mat, int N, int[] rowsToSolve) throws MPIException, IOException, ClassNotFoundException {
        int[] reversedRowsToSolve = MyMath.reverse(rowsToSolve);
        for (int col = N-1; col >= 0; col--) {
            mat.set(col, N,
                    mat.get(col, N).divide(mat.get(col, col)));
            mat.set(col, col, new Fraction(1));
            for (int row: reversedRowsToSolve) {
                if (row >= col || row < 0) {
                    continue;
                }
                mat.set(row, N,
                        mat.get(row, N).subtract(mat.get(row, col).multiply(mat.get(col,N))));
                mat.set(row, col, new Fraction(0));
            }

            MatrixMPI procRows = mat.getSubMatrix(rowsToSolve);
            mat.gatherMatrixTo(procRows, 0);
            mat.bcast(0);
        }
        return mat;
    }

    public static int[][] getSendCountAndDispls(int processorSize, int matrixRows, int matrixCols) {
        int[] sendCount = new int[processorSize];
        int[] displs = new int[processorSize];

        for (int i = 0; i < processorSize; i++) {
            sendCount[i] = matrixRows/processorSize * matrixCols;
            displs[i] = matrixRows/processorSize * matrixCols * i;
        }
        int amountOfLeftRows = matrixRows % processorSize;
        for (int i = 0; i < amountOfLeftRows; i++) {
            sendCount[i]+=matrixCols;
            for (int j = i+1; j < processorSize; j++) {
                displs[j]+=matrixCols;
            }
        }
        return new int[][]{sendCount, displs};
    }

    private static void initVector(Fraction[] vector, int N) {
        for (int i = 0; i < N; i++) {
            vector[i] = new Fraction(BigInteger.valueOf((int) (Math.random() * 10 + 1)), BigInteger.ONE);
        }
    }

    static void initMatrix(Fraction[][] matrix, int rows, int cols) {
        for (int i = 0; i < rows; i++) {
            initVector(matrix[i], cols);
        }
    }

    public static boolean checkSolution(MatrixMPI matrix, Fraction[] solutions) {
        Fraction[] check = new Fraction[matrix.getRows()];
        for (int i = 0; i < matrix.getRows(); i++) {
            Fraction sum = new Fraction(0);
            for (int j = 0; j < matrix.getColumns(); j++) {
                sum = sum.add(matrix.get(i, j).multiply(solutions[j]));
            }
            check[i] = sum;
        }

        System.out.println("Check: " + Arrays.toString(check));
        for (int i = 0; i < matrix.getRows(); i++) {
            if (!check[i].equals(matrix.get(i, matrix.getLastColumnI()))) {
                return false;
            }
        }
        return true;
    }
}
