 package edu.csulb.cecs574.chord;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.math.BigInteger;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.rmi.RemoteException;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.Scanner;
import java.util.Timer;
import java.util.TimerTask;

import org.apache.commons.lang3.SerializationUtils;

/**
 * Class represents a user who uses a Chord to communicate
 * @author Jared Coleman and Raghunandan Kayyottu
 * @version 1.01 2017-16-03
 */
public class ChordUser {
   private int port; //local port for user's Chord node

   /**
    * Provides hash code for a String to be used as GUID
    * @param objectName String to hash
    * @return hash code
    */
   private long md5(String objectName) {
      try {
         MessageDigest m = MessageDigest.getInstance("MD5");
         m.reset();
         m.update(objectName.getBytes());
         BigInteger bigInt = new BigInteger(1,m.digest());
         return Math.abs(bigInt.longValue())% 1917711205;
      } catch(NoSuchAlgorithmException e) {
         e.printStackTrace();
      }
      return 0;
   }

   /**
    * Only provided constructor for ChordUser\n
    * Creates a simple user interface for a user to write, read, and delete files from p2p ring
    * @param p 
    */
   public ChordUser(int p) {
      this.port = p;

      Timer timer1 = new Timer();
      timer1.scheduleAtFixedRate(new TimerTask() {
         @Override
         public void run() {
            try {
               long guid = md5("" + port);
               Chord chord = new Chord(port, guid);
               
               try {
                  Files.createDirectories(Paths.get(guid+"/repository"));
               } catch(IOException e) {
                  e.printStackTrace();
               }
               System.out.println("Usage: \n\tjoin <ip> <port>\n\twrite <file> "
                       + "(the file must be an integer stored in the working directory, i.e, ./"+guid+"/file");
               System.out.println("\tread <file>\n\tdelete <file>\n\tprint\n\tappend <file1> <file2>\n\tprintsorted <file>\n\tprintfd <file>");

               Scanner scan= new Scanner(System.in);
               String delims = "[ ]+";
               while (true) {
                  String text= scan.nextLine();
                  String[] tokens = text.split(delims);
                  if (tokens[0].equals("join") && tokens.length == 3) {
                     try {
                        int portToConnect = Integer.parseInt(tokens[2]);
                        if(portToConnect != port) {
                           chord.joinRing(tokens[1], portToConnect);
                        }
                     } catch (IOException e) {
                        e.printStackTrace();
                     }
                  } else if (tokens[0].equals("print")) {
                     chord.print();
                  } else if (tokens[0].equals("write") && tokens.length == 2) {
                     try {
                        String path;
                        String fileName = tokens[1];
                        long guidObject = md5(fileName);
                        path = ".\\" +  guid + "\\" + fileName; // path to file
                        //creating instance
                        FileDiscriptor fd = new FileDiscriptor();
                        fd.setFileName(fileName);
                        fd.setSize(0l);
                        SerializationUtils serilizutils = new SerializationUtils();
                        serilizutils.serialize(fd);
                        
                       // FileStream file = new FileStream(path);
                        ChordMessageInterface peer = chord.locateSuccessor(guidObject);
                        //peer.put(guidObject, file); // put file into ring
                        peer.put(guidObject, serilizutils.serialize(fd)); 
                        
                     } catch (IOException e) {
                        System.out.println("Invalid File!");
                     }
                  } else if (tokens[0].equals("read") && tokens.length == 2) {
                     InputStream in = null;
                     FileOutputStream out;
                     String fileName = tokens[1];
                     long guidObject = md5(fileName);
                     try {
                        ChordMessageInterface peer = chord.locateSuccessor(guidObject);
                        in = peer.get(guidObject);
                        if(in != null) {
                           String path = ".\\" +  guid + "\\" + fileName;
                           out = new FileOutputStream(path);
                           while (in.available() > 0)
                              out.write(in.read());
                           out.close();
                        }
                     } catch (IOException ex) {
                        System.out.println(fileName + " does not exist");
                     } finally {
                        try {
                           if(in != null) in.close();
                        } catch (IOException ex) {
                           ex.printStackTrace();
                        }
                     }
                  } else if  (tokens[0].equals("delete") && tokens.length == 2) {
                     InputStream in = null;
                     String fileName = tokens[1];
                     long guidObject = md5(fileName);
                     try {
                        ChordMessageInterface peer = chord.locateSuccessor(guidObject);
                        peer.delete(guidObject);
                     } catch (IOException ex) {
                        System.out.println(fileName + " does not exist");
                     } finally {
                        try {
                           if(in != null) in.close();
                        } catch (IOException ex) {
                           ex.printStackTrace();
                        }
                     }
                  } else if (tokens[0].equals("leave")) {
                     chord.leave();
                  }
                  
                  else if (tokens[0].equals("ls") && tokens.length == 1) {
                      try {
                          int portToConnect = Integer.parseInt(tokens[2]);
                          if(portToConnect != port) {
                             chord.joinRing(tokens[1], portToConnect);
                          }
                       } catch (IOException e) {
                          e.printStackTrace();
                       }
                    }
                  else if (tokens[0].equals("append") && tokens.length == 3) {
                	  
                	  //read  
                      InputStream in = null;
                      FileOutputStream out;
                      String fileName = tokens[1];
                      long guidObject = md5(fileName);
                      try {
                         ChordMessageInterface peer = chord.locateSuccessor(guidObject);
                         SerializationUtils sdUtils = new SerializationUtils();
                         FileDiscriptor fd = (FileDiscriptor)sdUtils.deserialize(peer.getBytes(guidObject));
                         
                         //asdsa
                         String fileName1 = tokens[2];
                         long guidObject1 = md5(fileName1);
                      
                         
                         //write 
                         try {
                             String path;
                             
                             path = ".\\" +  guid + "\\" + fileName1; // path to file
                             //creating instance
                             String current = new java.io.File( "." ).getCanonicalPath();
                             System.out.println("Current dir:"+current);
                             System.out.println("path "+path );
                             FileStream file = new FileStream(path);
                             ChordMessageInterface peer1 = chord.locateSuccessor(guidObject1);
                             //peer.put(guidObject, file); // put file into ring
                             peer1.put(guidObject1, file); 
                             fd.addChunk(guidObject1);
                             //peer.put(guidObject, );
                             fd.setSize( fd.getSize()+ (new File(path).length()));
                             //fd.setSize(0l);
                             SerializationUtils serilizutils1 = new SerializationUtils();
                             serilizutils1.serialize(fd);
                             
                            // FileStream file = new FileStream(path);
                            // ChordMessageInterface peer = chord.locateSuccessor(guidObject);
                             //peer.put(guidObject, file); // put file into ring
                             peer.put(guidObject, serilizutils1.serialize(fd)); 
                                  
                             System.out.println(fd.getFileName()  +" fd sizs = "+ fd.getSize() +" chunks = "+ fd.Chunks.size()   );
                             
                             
                             
                          } catch (IOException e) {
                             System.out.println("Invalid File!");
                             e.printStackTrace();
                          }
                         //write
                         
                         if(in != null) {
                            String path = ".\\" +  guid + "\\" + fileName;
                            
                            
                            out = new FileOutputStream(path);
                            while (in.available() > 0)
                               out.write(in.read());
                            out.close();
                         }
                      } catch (IOException ex) {
                         System.out.println(fileName + " does not exist");
                      } finally {
                         try {
                            if(in != null) in.close();
                         } catch (IOException ex) {
                            ex.printStackTrace();
                         }
                      }
                   }
                  else if (tokens[0].equals("printfd") && tokens.length == 2) {
                      InputStream in = null;
                      FileOutputStream out;
                      String fileName = tokens[1];
                      long guidObject = md5(fileName);
                     
                      try {
                         
                    	 ChordMessageInterface peer = chord.locateSuccessor(guidObject);
                         in = peer.get(guidObject);
                         if(in != null) {
                            String path = ".\\" +  guid + "\\" + fileName;
                            SerializationUtils sdUtils = new SerializationUtils();
                            FileDiscriptor fd = (FileDiscriptor)sdUtils.deserialize(peer.getBytes(guidObject));
                            
                            System.out.println(" Requsted File name  = "+ fd.getFileName()  +"\n Requsted File Size  = "+ fd.getSize() +"\n Requsted File chunks = "+ fd.Chunks.size()   );
                          
                         }
                      } catch (IOException ex) {
                         System.out.println(fileName + " does not exist");
                      } finally {
                         try {
                            if(in != null) in.close();
                         } catch (IOException ex) {
                            ex.printStackTrace();
                         }
                      }
                   }
                  else if (tokens[0].equals("sort") && tokens.length == 2) {
                      InputStream in = null;
                      FileOutputStream out;
                      String fileName = tokens[1];
                      long guidObject = md5(fileName);
                     
                      try {
                         
                    	 ChordMessageInterface peer = chord.locateSuccessor(guidObject);
                         in = peer.get(guidObject);
                         if(in != null) {
                            String path = ".\\" +  guid + "\\" + fileName;
                            SerializationUtils sdUtils = new SerializationUtils();
                            FileDiscriptor fd = (FileDiscriptor)sdUtils.deserialize(peer.getBytes(guidObject));
                            
                                                       // assign work to each cord
                            chord.chunksorting.clear();
                            for (int i = 0; i < fd.Chunks.size(); i++) {
                            	System.out.println("Starting sort = "+ i);
                            	 ChordMessageInterface peer1 = chord.locateSuccessor(fd.Chunks.get(i));
                            	 peer1.sort(fd.Chunks.get(i),chord);
                            	 chord.chunksorting.put(fd.Chunks.get(i),Boolean.FALSE);
                            	
							}
                            System.out.println(" Requsted File name  = "+ fd.getFileName()  +"\n Requsted File Size  = "+ fd.getSize() +"\n Requsted File chunks = "+ fd.Chunks.size()   );
                            

                            
                         }
                      } catch (IOException ex) {
                         System.out.println(fileName + " does not exist");
                      } finally {
                         try {
                            if(in != null) in.close();
                         } catch (IOException ex) {
                            ex.printStackTrace();
                         }
                      }
                   }
                  else if (tokens[0].equals("printsorted") && tokens.length == 1) {
                     
                     
                      try {
                         
                    	 ChordMessageInterface peer = chord.locateSuccessor(0);
                         peer.printSorted();
                            
                            
                         
                      } catch (Exception ex) {
                         System.out.println(ex.getMessage() + " does not exist");
                      } finally {}
                   }
                  
                  
                  
                  
                  
               }
            } catch(RemoteException e) {
               e.printStackTrace();
            } catch (IOException e) {
               e.printStackTrace();
            }
         }
      }, 1000, 1000);
   }

   static public void main(String args[]) {
      if (args.length < 1 ) {
         throw new IllegalArgumentException("Parameter: <port>");
      }
      try {
         ChordUser chordUser = new ChordUser(Integer.parseInt(args[0]));
      } catch (Exception e) {
         e.printStackTrace();
         System.exit(1);
      }
   } 
}