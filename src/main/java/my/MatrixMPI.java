package my;

import mpi.MPI;
import mpi.MPIException;

import java.io.*;

public class MatrixMPI {
    private Fraction[][] matrix;
    private int rows;
    private int columns;

    public MatrixMPI(Fraction[][] matrix) {
        this.matrix = matrix;
        this.rows = matrix.length;
        this.columns = matrix[0].length;
    }
    public MatrixMPI(int rows, int columns) {
        this.rows = rows;
        this.columns = columns;
        this.matrix = new Fraction[rows][columns];
    }
    public MatrixMPI() {
        this.rows = 0;
        this.columns = 0;
        this.matrix = new Fraction[rows][columns];
    }

    public Fraction[][] getMatrix() {
        return matrix;
    }
    public int getRows() {
        return rows;
    }
    public int getColumns() {
        return columns - 1;
    }

    public int getColumnsWithSolution() {
        return columns;
    }

    public int getLastColumnI() {
        return columns - 1;
    }

    public void setMatrix(Fraction[][] matrix) {
        this.matrix = matrix;
        this.rows = matrix.length;
        this.columns = matrix[0].length;
    }

    public Fraction get(int row, int column) {
        return matrix[row][column];
    }

    public void set(int row, int column, Fraction value) {
        matrix[row][column] = value;
    }

    public Fraction[] getLastColumn() {
        Fraction[] lastColumn = new Fraction[rows];
        for (int i = 0; i < rows; i++) {
            lastColumn[i] = matrix[i][columns - 1];
        }
        return lastColumn;
    }

    public byte[] getMatrixBytes() throws IOException {
        ByteArrayOutputStream bos=new ByteArrayOutputStream();
        ObjectOutputStream oos=new ObjectOutputStream(bos);
        oos.writeObject(this.getMatrix());
        return bos.toByteArray();
    }

    public static Fraction[][] fromBytes(byte[] bytes) throws IOException, ClassNotFoundException {
        ByteArrayInputStream bis=new ByteArrayInputStream(bytes);
        ObjectInputStream ois=new ObjectInputStream(bis);
        return (Fraction[][]) ois.readObject();
    }

    public MatrixMPI getSubMatrix(int[] rows) {
        MatrixMPI subMatrix = new MatrixMPI(rows.length, columns);
        for (int i = 0; i < rows.length; i++) {
            subMatrix.matrix[i] = matrix[rows[i]];
        }
        return subMatrix;
    }

    public void setSubMatrix(int startRow, MatrixMPI subMatrix) {
        if (subMatrix.rows >= 0) {
            System.arraycopy(subMatrix.matrix, 0, matrix, startRow + 0, subMatrix.rows);
        }
    }

    public Fraction[] getVector() {
        Object[] vector = new Object[rows * columns];
        int index = 0;
        for (Fraction[] row : matrix) {
            for (Fraction value : row) {
                vector[index++] = value;
            }
        }
        return (Fraction[]) vector;
    }

    public void bcast(int root) throws MPIException, IOException, ClassNotFoundException {
        byte[] sendBuf=null;
        int [] size=new int[1];
        int rank= MPI.COMM_WORLD.getRank();

        if (rank==root){
            sendBuf = getMatrixBytes();
            size[0]=sendBuf.length;
        }

        MPI.COMM_WORLD.bcast(size,1, MPI.INT, root);
        if (rank != root){
            sendBuf = new byte[size[0]];
        }
        MPI.COMM_WORLD.bcast(sendBuf, sendBuf.length, MPI.BYTE, root);
        if (rank != root){
            Fraction[][] rawMatrix = fromBytes(sendBuf);
            this.setMatrix(rawMatrix);
        }
    }

    public void gatherMatrixTo(MatrixMPI procRows, int root)
            throws MPIException, IOException, ClassNotFoundException {
        int myRank = MPI.COMM_WORLD.getRank();
        int procSize = MPI.COMM_WORLD.getSize();

//        System.arraycopy(sendbuf, 0, recvbuf, 0, sendbuf.length);

        if (myRank == root && MPI.COMM_WORLD.getSize() > 1) {
            int offset = procRows.getRows();
            for (int i = 1; i < procSize; i++) {
                Fraction[][] tmp = (Fraction[][]) MpiUtils.recvObject(i, i);
//                System.out.println("root receives " + Arrays.toString(tmp));
//                System.arraycopy(tmp, 0, recvbuf, offset, tmp.length);
                setSubMatrix(offset, new MatrixMPI(tmp));

                offset += tmp.length;
            }
        } else if (MPI.COMM_WORLD.getSize() > 1) {
//            System.out.println(myRank + " sends " + Arrays.toString(sendbuf));
            MpiUtils.sendObject(procRows.getMatrix(), 0, myRank);
        }
    }

    @Override
    public String toString() {
        StringBuilder sb = new StringBuilder();
        for (Fraction[] row : matrix) {
            for (Fraction value : row) {
                sb.append(value).append(" ");
            }
            sb.append("\n");
        }
        return sb.toString();
    }
}
