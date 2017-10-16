package edu.csulb.cecs574.chord;

import java.io.*;

/**
 * Class facilitates reading, writing, and deleting files
 * @author Jared Coleman
 * @version 1.01 2017-16-03
 */
public class FileStream extends InputStream implements Serializable {
   private int currentPosition;
   private byte[] byteBuffer;
   private int size;
   
   /**
    * Constructs a FileStream object using a specific file
    * @param pathName path of file
    * @throws FileNotFoundException
    * @throws IOException 
    */
   public FileStream(String pathName) throws FileNotFoundException, IOException {
      File file = new File(pathName);
      size = (int)file.length();
      byteBuffer = new byte[size];

      FileInputStream fileInputStream = new FileInputStream(pathName);
      int i = 0;
      while (fileInputStream.available()> 0) {
         byteBuffer[i++] = (byte)fileInputStream.read();
      }
      fileInputStream.close();	
      currentPosition = 0;	  
   }

   /**
    * Constructs an empty FileStream object
    * @throws FileNotFoundException 
    */
   public FileStream() throws FileNotFoundException {
      currentPosition = 0;	  
   }

   /**
    * Reads a single byte from the file
    * @return byte of data from file, 0 if complete
    * @throws IOException 
    */
   @Override
   public int read() throws IOException {
      if (currentPosition < size) {
         return (int)byteBuffer[currentPosition++];
      } else return 0;
   }

   /**
    * Returns the size of unread data in the file
    * @return size of unread data in file, 0 if complete
    * @throws IOException 
    */
   @Override
   public int available() throws IOException
   {
      return size - currentPosition;
   }
}