package com.taulia.services.impl;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.FileWriter;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.util.Arrays;
import java.util.Base64;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.xml.stream.XMLOutputFactory;
import javax.xml.stream.XMLStreamException;
import javax.xml.stream.XMLStreamWriter;

import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import com.taulia.utils.Constants;

@Service
public class FileService implements com.taulia.services.FileService {
	
	private final String DIR;
	
	public FileService() {
		this(false); // is not used by a unit test
	}
	
	public FileService(Boolean isTest) { // create DIR if does not exists.
		this.DIR = isTest ? Constants.TEST_DIR : Constants.DIR;
		File directory = new File(DIR);
	    if (!directory.exists()) {
	        directory.mkdirs();
	    }
	}

	@Override
	public void extractBuyersToCSV(MultipartFile file) {
		
		//map of buyers with writers for output file
		Map<String, BufferedWriter> buyerMap = new HashMap<>();
		//should close reader automatically(implements closable)
		try(BufferedReader reader = new BufferedReader(new InputStreamReader(file.getInputStream()));) {
			//first line should be header
			String headers = reader.readLine();
			List<String> values = null;
			String line = null;
			String buyerName = null;
			//read and write when buffer is full
			while((line = reader.readLine()) != null) {
			  values = Arrays.asList(line.split(","));
			  buyerName = values.get(Constants.BUYER_NAME_POSITION);
			  write(buyerMap, values, headers, line, buyerName);
			}
		} catch (IOException e) {
			e.printStackTrace();
		} finally {
			//close the buyer's writer
			buyerMap.forEach((k,v) -> {
				try {
					v.flush();
					v.close();
				} catch (IOException e) {
					e.printStackTrace();
				}
			});
		}
	}
	
	@Override
	public void extractBuyersToXML(MultipartFile file) {
		//map of buyers with writers for output file
		Map<String, XMLStreamWriter> buyerMap = new HashMap<>();
		//should close reader automatically(implements closable)
		try(BufferedReader reader = new BufferedReader(new InputStreamReader(file.getInputStream()));) {
			//first line should be header
			List<String> headers = Arrays.asList(reader.readLine().split(","));
			List<String> values = null;
			String line = null;
			String buyerName = null;
			//read and write when buffer is full
			while((line = reader.readLine()) != null) {
			  values = Arrays.asList(line.split(","));
			  buyerName = values.get(Constants.BUYER_NAME_POSITION);
			  write(buyerMap, values, headers, buyerName);
			}
		} catch (IOException e) {
			e.printStackTrace();
		} finally {
			//close the buyer's writer
			buyerMap.forEach((k,v) -> {
				try {
					v.flush();
					v.close();
				} catch (XMLStreamException e) {
					e.printStackTrace();
				}
			});
		}
	}

	private void write(Map<String, BufferedWriter> buyerMap, List<String> values, String headers, String line, String buyerName) {
		try {
			if(buyerMap.containsKey(buyerName)) {
				  buyerMap.get(buyerName).append(line);
				  buyerMap.get(buyerName).newLine();
			  } else {
				  BufferedWriter bw = new BufferedWriter(new FileWriter(DIR + buyerName + Constants.CSV));
				  bw.append(headers);
				  bw.newLine();
				  bw.append(line);
				  bw.newLine();
				  buyerMap.put(buyerName, bw);
			  }
			buyerMap.get(buyerName).flush();
		} catch (IOException e) {
			e.printStackTrace();
		}
	}
	
	private void write(Map<String, XMLStreamWriter> buyerMap, List<String> values, List<String> headers, String buyerName) {
		try {
			if(buyerMap.containsKey(buyerName)) {
				  buyerMap.get(buyerName).writeCharacters(System.getProperty("line.separator"));
				  for(int i = 0; i < headers.size(); i++) {
					  if(i != Constants.BASE64_POSITION) {
						  buyerMap.get(buyerName).writeStartElement(headers.get(i));
						  buyerMap.get(buyerName).writeCharacters(values.get(i));
						  buyerMap.get(buyerName).writeEndElement();
					  } else {
						  base64ToFile(values.get(Constants.FILE_NAME_POSITION), values.get(i));
					  }
				  }
				  buyerMap.get(buyerName).writeCharacters(System.getProperty("line.separator"));
			  } else {
				  XMLOutputFactory output = XMLOutputFactory.newInstance();
				  XMLStreamWriter writer = output.createXMLStreamWriter(
				                              new FileOutputStream(DIR + buyerName + Constants.XML));
				  writer.writeStartDocument();
				  writer.writeCharacters(System.getProperty("line.separator"));
				  for(int i = 0; i < headers.size(); i++) {
					  if(i != Constants.BASE64_POSITION) {
						  writer.writeStartElement(headers.get(i));
						  writer.writeCharacters(values.get(i));
						  writer.writeEndElement();
					  } else {
						  base64ToFile(values.get(Constants.FILE_NAME_POSITION), values.get(i));
					  }
				  }
				  writer.writeCharacters(System.getProperty("line.separator"));
				  buyerMap.put(buyerName, writer);
			  }
			buyerMap.get(buyerName).flush();
		} catch(XMLStreamException | FileNotFoundException e) {
			e.printStackTrace();
		}
	}
	
	private void base64ToFile(String fileName, String base64) {
		if(base64 != null && !base64.equals(Constants.EMPTY_STRING) &&
		   fileName != null && !fileName.equals(Constants.EMPTY_STRING)) {
			  byte[] data = Base64.getDecoder().decode(base64);
			  //should close file stream automatically(implements closable)
			  try (OutputStream stream = new FileOutputStream(DIR + fileName)) {
			      stream.write(data);
			  } catch (IOException e) {
				e.printStackTrace();
			}
		}
	}
}
