package com.taulia.services;

import org.springframework.web.multipart.MultipartFile;

public interface FileService {
	public void extractBuyersToCSV(MultipartFile file);
	public void extractBuyersToXML(MultipartFile file);
}
