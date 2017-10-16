 package edu.csulb.cecs574.chord;

import java.rmi.Remote;
import java.rmi.RemoteException;
import java.io.InputStream;
import java.io.IOException;

/**
 * Provides an interface for Chord RMI communication
 * @author Jared Coleman and Raghunandn Kayyottu
 * @version 1.01 2017-16-03
 */
public interface ChordMessageInterface extends Remote {
   public ChordMessageInterface getPredecessor()  throws RemoteException;
   ChordMessageInterface locateSuccessor(long key) throws RemoteException;
   ChordMessageInterface closestPrecedingNode(long key) throws RemoteException;
   public void joinRing(String Ip, int port)  throws RemoteException;
   public void transferKeys() throws IOException;
   public void leave() throws RemoteException;
   public void notify(ChordMessageInterface j) throws RemoteException;
   public boolean isAlive() throws RemoteException;
   public long getId() throws RemoteException;

   public void put(long guidObject, InputStream file) throws IOException, RemoteException;
   public void put(long guidObject, byte[] file) throws IOException, RemoteException;
   
   public InputStream get(long guidObject) throws IOException, RemoteException;
   public byte[] getBytes(long guidObject) throws IOException, RemoteException;
   public void delete(long guidObject) throws IOException, RemoteException;
   public void sort(long guidObject, ChordMessageInterface cordinator) throws  RemoteException;
   public void sortKey(long key, String value) throws  RemoteException;
   public boolean endSorting(long key) throws  RemoteException;
   public void printSorted() throws  RemoteException;
}
