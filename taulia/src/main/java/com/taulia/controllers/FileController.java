package com.taulia.controllers;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;

import com.taulia.enums.OutputType;
import com.taulia.services.FileService;

@RestController
@RequestMapping("/file")
public class FileController {

	@Autowired
	FileService fileService;
	
	@PostMapping("/{type}")
	public ResponseEntity<?> extractBuyers(@PathVariable("type") OutputType type, @RequestParam("file") MultipartFile file) {
		ResponseEntity<String> response = new ResponseEntity<String>("Successful extraction.", HttpStatus.OK);
		try {
			switch(type) {
			case CSV:
				fileService.extractBuyersToCSV(file);
				break;
			case XML:
				fileService.extractBuyersToXML(file);
				break;
			default:
				response = new ResponseEntity<String>("Unsupported type in url.", HttpStatus.BAD_REQUEST);
				break;
			}
		} catch (Exception e) {
			e.printStackTrace();
			response = new ResponseEntity<String>("Server side error.", HttpStatus.INTERNAL_SERVER_ERROR);
		}
		
		return response;
	}
}
