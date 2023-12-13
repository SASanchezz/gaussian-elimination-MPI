package my;

import mpi.MPI;
import mpi.MPIException;
import mpi.Status;

import java.io.*;

public class MpiUtils {
    public static void sendObject(Object o, int dest, int tag) throws IOException,MPIException{
        ByteArrayOutputStream bos=new ByteArrayOutputStream();
        ObjectOutputStream oos=new ObjectOutputStream(bos);
        oos.writeObject(o);
        byte []tmp=bos.toByteArray();
        MPI.COMM_WORLD.send(tmp,tmp.length, MPI.BYTE, dest, tag);
    }

    public static Object recvObject(int source, int tag) throws IOException,MPIException,ClassNotFoundException{
        Status st = MPI.COMM_WORLD.probe(source, tag);
        int size=st.getCount(MPI.BYTE);
        byte[]tmp=new byte[size];
        MPI.COMM_WORLD.recv(tmp, size, MPI.BYTE, source, tag);
        Object res;
        ByteArrayInputStream bis=new ByteArrayInputStream(tmp);
        ObjectInputStream ois=new ObjectInputStream(bis);
        res=ois.readObject();
        return res;
    }
}
