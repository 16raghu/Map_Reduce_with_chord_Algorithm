package edu.csulb.cecs574.chord.jaxb.model;

import java.util.ArrayList;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlElementWrapper;
import javax.xml.bind.annotation.XmlRootElement;

@XmlRootElement(namespace = "edu.csulb.cecs327.chord.jaxb.model")
public class FileDiscriptor {

	
	 @XmlElement(name = "FileName")
	String FileName;
	 @XmlElement(name = "size")
	Long size;
	 @XmlElement(name = "No_of_chunks")
	Integer No_of_chunks;
	 @XmlElementWrapper(name = "Chunks")
	ArrayList<String>  Chunks;
	FileAtributes f_atributes;
	
}
