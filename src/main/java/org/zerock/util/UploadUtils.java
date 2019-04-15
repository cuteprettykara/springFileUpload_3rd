package org.zerock.util;

import java.io.File;
import java.io.IOException;
import java.text.DecimalFormat;
import java.util.Calendar;
import java.util.UUID;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.util.FileCopyUtils;

public class UploadUtils {
	private static final Logger log = LoggerFactory.getLogger(UploadUtils.class);
	
	public static String uploadFile(
			String uploadPath, 
			String originalFilename, 
			byte[] fileData) throws IOException {
		
		UUID uid = UUID.randomUUID();
		
		String savedName = uid.toString() + "_" + originalFilename;
		
		File target = new File(uploadPath + calcPath(uploadPath), savedName);
		log.info("target: {}", target);
		
		FileCopyUtils.copy(fileData, target);
		
		return savedName;
	}
	
	private static String calcPath(String uploadPath) {
		
		Calendar cal = Calendar.getInstance();
		
		String yearPath = File.separator + cal.get(Calendar.YEAR);
		
		String monthPath = yearPath
				+ File.separator
				+ new DecimalFormat("00").format(cal.get(Calendar.MONTH) + 1);
		
		String datePath = monthPath
				+ File.separator
				+ new DecimalFormat("00").format(cal.get(Calendar.DATE));
		
		makeDir(uploadPath, datePath);
		
		return datePath;
	}

	private static void makeDir(String uploadPath, String datePath) {
		File dirPath = new File(uploadPath + datePath);

 		if (!dirPath.exists()) 	dirPath.mkdirs();
	}
}
