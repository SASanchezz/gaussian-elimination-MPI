package my;

import java.io.IOException;

import mpi.MPI;
import mpi.MPIException;

public class Main {
    public static void main(String[] args) throws MPIException, IOException, ClassNotFoundException {
        // Matrix size
        int N = 160;

        MPI.Init(args);

        int rank = MPI.COMM_WORLD.getRank();
        int size = MPI.COMM_WORLD.getSize();

        int[][] sendCountAndDispls = Utils.getSendCountAndDispls(size, N, N+1);
        int[] sendCount = sendCountAndDispls[0];
        int[] displs = sendCountAndDispls[1];

        Fraction[][] rawMatrix = new Fraction[N][N+1];
        MatrixMPI matrix = new MatrixMPI();
        MatrixMPI matrixCopy = null;

        if (rank == 0) {
            Utils.initMatrix(rawMatrix, N, N+1);
// Test matrix for N = 3
//            rawMatrix = new Fraction[][] {
//                    {new Fraction(3), new Fraction(2),new Fraction(-4), new Fraction(3)},
//                    {new Fraction(2), new Fraction(3), new Fraction(3), new Fraction(15)},
//                    {new Fraction(5), new Fraction(-3), new Fraction(1), new Fraction(14)}
//            };
            matrix.setMatrix(rawMatrix);
            matrixCopy = new MatrixMPI(rawMatrix);

            System.out.println("Initial matrix:");
            System.out.println(matrix);
            System.out.println("\n");
        }
        //START TIMER
        long timeStart = System.currentTimeMillis();

        matrix.bcast(0);

        int count = sendCount[rank];
        int displ = displs[rank];

        int rows = count / (N+1);
        int rowsOffset = displ / (N+1);
        int[] rowsToSolve = new int[rows];
        for (int i = 0; i < rows; i++) {
            rowsToSolve[i] = rowsOffset + i;
        }

        MatrixMPI finalMatrix = Utils.gaussianEliminationForRows(rank, matrix, N, rowsToSolve);

        //FINISH TIMER
        long timeFinish = System.currentTimeMillis();

        assert finalMatrix != null;

        if (rank == 0) {
            Fraction [] solutions = finalMatrix.getLastColumn();

            if (Utils.checkSolution(matrixCopy, solutions)) {
                System.out.println("Solution is correct");
            } else {
                System.out.println("Solution is incorrect");
            }

            System.out.println("Time: " + (timeFinish - timeStart) + " ms");
        }
    }
}
