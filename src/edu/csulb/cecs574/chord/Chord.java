 package edu.csulb.cecs574.chord;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileOutputStream;
import java.io.FileReader;
import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.rmi.NotBoundException;
import java.rmi.RemoteException;
import java.rmi.registry.LocateRegistry;
import java.rmi.registry.Registry;
import java.util.HashMap;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.Map.Entry;
import java.util.Set;
import java.util.SortedMap;
import java.util.Timer;
import java.util.TimerTask;
import java.util.TreeMap;
import java.util.concurrent.Semaphore;

/**
 * Class represents a chord for RMI communication
 * @author Jared Coleman and Raghunandan Kayyottu
 * @version 1.01 2017-16-03
 */
public class Chord extends java.rmi.server.UnicastRemoteObject implements ChordMessageInterface {
   private static final int M = 2;

   private Registry registry;    // rmi registry for lookup the remote objects.
   private ChordMessageInterface successor;
   private ChordMessageInterface predecessor;
   private ChordMessageInterface[] finger;
   private int nextFinger;
   private long guid;   		// GUID (i)
   private SortedMap<Long,LinkedList<String>> bin = new TreeMap<Long,LinkedList<String>>();
   public HashMap <Long,Boolean> chunksorting = new HashMap<Long, Boolean>(); 
   public final Semaphore avaliable = new Semaphore(1,Boolean.TRUE);
   
   /**
    * Gets the rmi for a chord object at a given ip and port
    * @param ip ip to look for chord rmi
    * @param port port to look for chord rmi
    * @return ChordMessageInterface chord rmi
    */
   public ChordMessageInterface rmiChord(String ip, int port) {	
      ChordMessageInterface chord = null;
      try {
         Registry registry = LocateRegistry.getRegistry(ip, port);
         chord = (ChordMessageInterface)(registry.lookup("Chord"));
      } catch (RemoteException | NotBoundException e) {
         e.printStackTrace();
      }
      return chord;
   }

   /**
    * Tests if key is in semi-closed interval
    * @param key key to be tested
    * @param key1 key to be tested against (exclusive)
    * @param key2 key to be tested against (inclusive)
    * @return true if key is in interval, false if not
    */
   public Boolean isKeyInSemiCloseInterval(long key, long key1, long key2) {
      if (key1 < key2) {
         return (key > key1 && key <= key2);
      } else {
         return (key > key1 || key <= key2);
      }
   }

   /**
    * Tests if key is in open interval
    * @param key key to be tested
    * @param key1 key to be tested against (exclusive)
    * @param key2 key to be tested against (exclusive)
    * @return true if key is in interval, false if not
    */
   public Boolean isKeyInOpenInterval(long key, long key1, long key2) {
      if (key1 < key2) {
         return (key > key1 && key < key2);
      } else {
         return (key > key1 || key < key2);
      }
   }


   /**
    * Writes a file to repository
    * @param guidObject GUID of file
    * @param stream InputStream for file
    * @throws RemoteException 
    */
   @Override
   public void put(long guidObject, InputStream stream) throws RemoteException {
      try {
         String fileName = ".\\"+guid+"\\repository\\" + guidObject;
         FileOutputStream output = new FileOutputStream(fileName);
         while (stream.available() > 0)
            output.write(stream.read());
         output.close();
      } catch (IOException e) {
         System.out.println(e);
      }
   }

   /**
    * Writes a file to repository
    * @param guidObject GUID of file
    * @param stream InputStream for file
    * @throws RemoteException 
    */

@Override
public void put(long guidObject, byte[] file) throws IOException,
		RemoteException {
	try {
         String fileName = ".\\"+guid+"\\repository\\" + guidObject;
         FileOutputStream output = new FileOutputStream(fileName);
         //while (stream.available() > 0)
            output.write(file);
         output.close();
      } catch (IOException e) {
         System.out.println(e);
      }
   }

   /**
    * Gets an file from repository
    * @param guidObject
    * @return
    * @throws RemoteException
    * @throws IOException 
    */
   @Override
   public InputStream get(long guidObject) throws RemoteException, IOException {
      String fileName = ".\\"+guid+"\\repository\\" + guidObject;
      File file = new File(fileName);
      return file.isFile() && file.canRead() ? new FileStream(file.getPath()) : null;
   }
   @Override
   public byte[] getBytes(long guidObject) throws RemoteException, IOException {
      String fileName = ".\\"+guid+"\\repository\\" + guidObject;
      File file = new File(fileName);
      return file.isFile() && file.canRead() ? Files.readAllBytes(Paths.get(file.getPath()))  : null;
   }

   /**
    * Deletes file in repository
    * @param guidObject GUID of the file's name. Also used as file name in repository
    * @throws RemoteException 
    */
   @Override
   public void delete(long guidObject) throws RemoteException {
      String fileName = ".\\"+guid+"\\repository\\" + guidObject;
      File file = new File(fileName);
      if (file.isFile() && file.canWrite()) {
         file.delete();
      }
   }

   /**
    * Gets GUID of Chord object
    * @return GUID
    * @throws RemoteException 
    */
   @Override
   public long getId() throws RemoteException {
      return guid;
   }
   
   /**
    * Function to test if Chord object is alive
    * @return true, if object exists
    * @throws RemoteException if object no longer exists
    */
   @Override
   public boolean isAlive() throws RemoteException {
      return true;
   }

   /**
    * Gets Chord object's predecessor
    * @return Chord object's predecessor
    * @throws RemoteException 
    */
   @Override
   public ChordMessageInterface getPredecessor() throws RemoteException {
      return predecessor;
   }

   /**
    * Locates node that corresponds to GUID
    * @param key GUID
    * @return Smallest Chord object > GUID
    * @throws RemoteException 
    */
   @Override
   public ChordMessageInterface locateSuccessor(long key) throws RemoteException {
      if (key == guid) {
         throw new IllegalArgumentException("Key must be distinct that  " + guid);
      } else if (successor.getId() != guid) {
         if (isKeyInSemiCloseInterval(key, guid, successor.getId())) {
            return successor;
         }
         ChordMessageInterface j = closestPrecedingNode(key);

         if (j == null) {
            return null;
         } else return j.locateSuccessor(key);
      }
      return successor;
   }

   /**
    * Finds the closest preceding node corresponding to the key
    * @param key GUID
    * @return closest preceding node
    * @throws RemoteException 
    */
   @Override
   public ChordMessageInterface closestPrecedingNode(long key) throws RemoteException {
      // todo
      if(key != guid) {
         int i = M - 1;
         while (i >= 0) {
            if(isKeyInSemiCloseInterval(finger[i].getId(), guid, key)) {
               if(finger[i].getId() != key)
                  return finger[i];
               else {
                  return successor;
               }
            }
            i--;
         }
      }
      return null;
   }

   /**
    * Updates successor to join p2p ring
    * @param ip new successor's IP address
    * @param port new successor's port
    * @throws RemoteException 
    */
   @Override
   public void joinRing(String ip, int port)  throws RemoteException {
      try {
         System.out.println("Get Registry to joining ring");
         Registry registry = LocateRegistry.getRegistry(ip, port);
         ChordMessageInterface chord = (ChordMessageInterface)(registry.lookup("Chord"));
         predecessor = null;
         successor = chord.locateSuccessor(this.getId());
         System.out.println("Joining ring");
      } catch(RemoteException | NotBoundException e){
         successor = this;
      }   
   }
   
   /**
    * transfers all keys to successor node, then leaves the ring
    * @throws IOException
    * @throws RemoteException
    */
   @Override
   public void transferKeys()  throws IOException {
      File folder = new File("./"+guid+"/repository/");
      File[] files = folder.listFiles();
      System.out.println("");
      for (File file : files) {
         successor.put(Long.valueOf(file.getName()), new FileStream(file.getPath())); 
         file.delete();
         System.out.println("File: " + file.getName() + " moved to " + successor.getId());
      }
   }
   
   /**
    * Exits the program
    * @throws java.rmi.RemoteException
    */
   @Override
   public void leave() throws RemoteException {
      System.exit(0);
   }
   
   

   /**
    * Finds next successor in case of an Exception in stabilize
    * @see stabilize()
    */
   public void findingNextSuccessor() {
      int i;
      successor = this;
      for (i = 0;  i < M; i++) {
         try {
            if (finger[i].isAlive()) {
               successor = finger[i];
            }
         } catch(RemoteException | NullPointerException e) {
            finger[i] = null;
         }
      }
   }

   /**
    * Stabilizes Chord object by updating successor and successor information
    * @see notify()
    * @see findingNextSuccessor()
    */
   public void stabilize() {
      try {
         if (successor != null) {
            ChordMessageInterface x = successor.getPredecessor();

            if (x != null && x.getId() != this.getId() && isKeyInOpenInterval(x.getId(), this.getId(), successor.getId())) {
               successor = x;
            }
            if (successor.getId() != getId()) {
               successor.notify(this);
            }
         }
      } catch(RemoteException | NullPointerException e1) {
         findingNextSuccessor();
      }
   }

   /**
    * Notifies Chord object to change predecessor, if necessary and possible
    * @param j new predecessor
    * @throws RemoteException 
    */
   @Override
   public void notify(ChordMessageInterface j) throws RemoteException {
      if (predecessor == null || (predecessor != null && isKeyInOpenInterval(j.getId(), predecessor.getId(), guid))) {
         // TODO 
         //transfer keys in the range [j,i) to j;
         predecessor = j;
         try {
            File folder = new File("./"+guid+"/repository/");
            File[] files = folder.listFiles();
            for (File file : files) {
               long guidObject = Long.valueOf(file.getName());
               if(guidObject < predecessor.getId() && predecessor.getId() < guid) {
                  predecessor.put(guidObject, new FileStream(file.getPath()));
                  file.delete();
               }
            }
         } catch (ArrayIndexOutOfBoundsException e) {
            //happens sometimes when a new file is added during foreach loop
         } catch (IOException e) {
            e.printStackTrace();
         }
      }
   }

   /**
    * Updates finger table 
    */
   public void fixFingers() {
      try {
         nextFinger = (nextFinger + 1 >= M) ? 0 : nextFinger + 1;
         finger[nextFinger] = locateSuccessor(guid + (long)Math.pow(2, nextFinger));
      }
      catch(RemoteException | NullPointerException e) {
         finger[nextFinger] = null;
         //e.printStackTrace();
      }
   }

   /**
    * Sets predecessor to null if there are any problems with it
    */
   public void checkPredecessor() { 	
      try {
         if (predecessor != null && !predecessor.isAlive())
            predecessor = null;
      } catch(RemoteException e) {
         predecessor = null;
      }
   }
   
   /**
    * Only provided constructor for Chord
    * @param port port to create chord at
    * @param guid global unique identifier for Chord object
    * @throws RemoteException 
    */
   public Chord(int port, long guid) throws RemoteException {
      int j;
      finger = new ChordMessageInterface[M];
      for (j = 0; j < M; j++) {
         finger[j] = null;
      }
      this.guid = guid;

      predecessor = null;
      successor = this;
      Timer timer = new Timer();
      timer.scheduleAtFixedRate(new TimerTask() {
         @Override
         public void run() {
         stabilize();
         fixFingers();
         checkPredecessor();
         }
      }, 500, 500);
      
      Runtime.getRuntime().addShutdownHook(new Thread() {
         @Override
         public void run() { 
            try {
               transferKeys();
            } catch (IOException e) {
               e.printStackTrace();
            }
         }
      });
      
      try {
         // create the registry and bind the name and object.
         System.out.println(guid + " is starting RMI at port: "+port);
         registry = LocateRegistry.createRegistry( port );
         registry.rebind("Chord", this);
      }
      catch(RemoteException e) {
         throw e;
      } 
   }

   /**
    * Prints successor and predecessor information
    */
   void print() {   
      int i;
      try {
         System.out.println("successor "+ (successor == null ? "N/A" : successor.getId()));
         System.out.println("predecessor "+ (predecessor == null ? "N/A" : predecessor.getId()));
         for (i = 0; i < M; i++) {
            try {
               System.out.println("Finger "+ i + " " + (finger[i] == null ? "N/A" : finger[i].getId()));
            } catch(NullPointerException e) {
               finger[i] = null;
            }
         }
      }
      catch(RemoteException e) {
         System.out.println("Cannot retrive id");
      }
   }

 public void map(Long key,String value) throws RemoteException{
	 
	 //You can modify the  key and value.
	 //key = key >> 1;
	 emit(key,value);
	 
	 
 }
 
 public void sortKey(long key, String value) throws  RemoteException {
	 try {
			
		 if (isKeyInSemiCloseInterval(key, predecessor.getId(), this.getId()))
		 {
			 LinkedList<String> ll = null;
			try {
				 avaliable.acquire();	
				 if (!bin.containsKey(key)) {
					 
					 
					 ll= new LinkedList<String>();
					 bin.put(key, ll);
					 
				 }
				 
			    ll = bin.get(key);
				 ll.add(value);
				
			} catch (Exception e) {
				// TODO: handle exception
			}finally{
				 avaliable.release();
			}	 
			
		 }
		 else { 
			 
			 ChordMessageInterface succ = locateSuccessor(key);
			 succ.sortKey(key,value);
		 }
		
	} catch (RemoteException e) {
		// TODO Auto-generated catch block
		e.printStackTrace();
	}
	 
}
 private void emit(Long key , String value) throws RemoteException{
	sortKey(key, value);
	 
 } 
   public void sort(long guidObject,ChordMessageInterface cordinator) throws RemoteException{
	   
	  // System.out.println("in sort");
	   Thread t = new Thread() {
		   public void run (){
			  byte[] readBytes;
			  String thisLine = null;
			  try {
				   
				   //readBytes = getBytes(guidObject);
				   String fileName = ".\\"+guid+"\\repository\\" + guidObject;
				   
				   System.out.println("reading file "+ fileName);
				     BufferedReader br = new BufferedReader(new FileReader(fileName));
				     while ((thisLine = br.readLine()) != null) {
				           // System.out.println(thisLine);
				            String[] splitstr ;
							splitstr = thisLine.split(":");
				            Long key = Long.parseLong(splitstr[0]);
							   String value = splitstr[1]; 
							   map(key,value);
							   ///in while loop
							   
				         }  
				
					 cordinator.endSorting(guidObject);
				
				   
			} catch (IOException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
			   
		   }
	   };
	   t.start();
	   
   }
   public boolean endSorting(long key) throws  RemoteException{
	   boolean sorted = true;
	   chunksorting.put(key,true);
	   for(Long i : chunksorting.keySet()){
		   
		   if (!chunksorting.get(i)){
			   sorted= false;  
			   break;
		   }
		   
	   }
	   if (sorted = true){
		   
		   System.out.println("The File have been sorted.");
		   
	   }
	   
	   return sorted;
   }
   public void printSorted() throws  RemoteException{
	   
	   System.out.println(bin.toString() );
	   Set<Entry<Long, LinkedList<String>>> entrySet = bin.entrySet(); 
	    for (Iterator iterator = entrySet.iterator(); iterator.hasNext();) {
			Entry<Long, LinkedList<String>> entry = (Entry<Long, LinkedList<String>>) iterator
					.next();
			 System.out.println(entry.getKey()+":"+entry.getValue().toString() );
			 
			
		}
	   
	   if(successor.getId() > getId())
		   successor.printSorted();
	   
   }
}
