package edu.csulb.cecs574.chord;

import java.io.Serializable;
import java.util.ArrayList;

public class FileDiscriptor implements Serializable{

	
	public String getFileName() {
		return FileName;
	}
	public void setFileName(String fileName) {
		FileName = fileName;
	}
	public Long getSize() {
		return size;
	}
	public void setSize(Long size) {
		this.size = size;
	}
	public Integer getNo_of_chunks() {
		return No_of_chunks;
	}
	public void setNo_of_chunks(Integer no_of_chunks) {
		No_of_chunks = no_of_chunks;
	}
	public ArrayList<Long> getChunks() {
		return Chunks;
	}
	public void addChunk(Long chunktoadd) {
		Chunks.add(chunktoadd);
	}
	public FileAtributes getF_atributes() {
		return f_atributes;
	}
	public void setF_atributes(FileAtributes f_atributes) {
		this.f_atributes = f_atributes;
	}
	String FileName;
	Long size;
	Integer No_of_chunks;
	ArrayList<Long>  Chunks = new ArrayList<Long>();//Chuncks [ID where its located and Size length]
	FileAtributes f_atributes;
	
}
