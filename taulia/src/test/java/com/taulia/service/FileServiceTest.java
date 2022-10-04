package com.taulia.service;

import static org.junit.Assert.assertTrue;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.nio.file.Files;

import org.junit.Test;
import org.springframework.mock.web.MockMultipartFile;
import org.springframework.web.multipart.MultipartFile;

import com.taulia.services.impl.FileService;
import com.taulia.utils.Constants;

public class FileServiceTest {

	@Test
	public void testExtractBuyersToCSV() throws IOException {
		FileService fs = new FileService(true);
		String path = "src/test/resources/testCase1.csv";
		File file = new File(path);
		MultipartFile result = new MockMultipartFile(file.getName(),
				file.getName(), "text/csv", Files.readAllBytes(file.toPath()));
		fs.extractBuyersToCSV(result);
		
		try(BufferedReader inputFileReader = new BufferedReader(new InputStreamReader(result.getInputStream()));) {
			String inputFileLine = null;
			String resultFileLine = null;
			String[] values = null;
			inputFileReader.readLine(); //skip header
			while((inputFileLine = inputFileReader.readLine()) != null) {
				values = inputFileLine.split(",");
				File resultFile = new File(Constants.TEST_DIR + values[Constants.BUYER_NAME_POSITION] + Constants.CSV);
				boolean lineExists = false;
				try(BufferedReader resultFileReader = new BufferedReader(new InputStreamReader(new FileInputStream(resultFile)));) {
					while((resultFileLine = resultFileReader.readLine()) != null) {
						if(inputFileLine.equals(resultFileLine)) {
							lineExists=true;
						}
					}
					// if the the line from the input file does not have a at least one match 
					// in the output file we fail the test.
					if(!lineExists) {  
						assertTrue(false);
					}
				} catch (IOException e) {
					e.printStackTrace();
				}
			}
			//all lines in the input file have at least one match in the output files, we pass the test
			assertTrue(true);
		} catch (IOException e) {
			e.printStackTrace();
		}
	}
	
//	@Test
//	public void testExtractBuyersToXML() {
//		
//	}
}
