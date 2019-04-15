package org.zerock.controller;

import java.io.File;
import java.io.IOException;
import java.util.UUID;

import javax.annotation.Resource;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.util.FileCopyUtils;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.multipart.MultipartFile;

@Controller
public class UploadController {
	private static final Logger log = LoggerFactory.getLogger(UploadController.class);
	
	private static final String UPLOAD_DIRECTORY = "upload";
	private String uploadPath = null;
	
	@RequestMapping(value="/uploadForm", method=RequestMethod.GET)
	public void uploadForm() {
		uploadPath = System.getProperty("user.home") + File.separator + UPLOAD_DIRECTORY;
		
		log.info("uploadPath : {}", uploadPath);
	}
	
	@RequestMapping(value="/uploadForm", method=RequestMethod.POST)
	public void uploadForm(MultipartFile file, Model model) throws IOException {
		log.info("originalFileName: {}", file.getOriginalFilename());
		log.info("size: {}", file.getSize());
		log.info("contentType: {}", file.getContentType());
		
		String savedName = uploadFile(file.getOriginalFilename(), file.getBytes());
		
		model.addAttribute("savedName", savedName);
	}

	private String uploadFile(String originalFilename, byte[] fildData) throws IOException {
		UUID uid = UUID.randomUUID();
		
		String savedName = uid.toString() + "_" + originalFilename;
		
		File target = new File(uploadPath, savedName);
		
		FileCopyUtils.copy(fildData, target);
		
		return savedName;
	}
}
