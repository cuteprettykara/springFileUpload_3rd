package org.zerock.controller;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.util.UUID;

import org.apache.commons.io.IOUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.util.FileCopyUtils;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.multipart.MultipartFile;
import org.zerock.util.MediaUtils;
import org.zerock.util.UploadUtils;

@Controller
public class UploadController {
	private static final Logger log = LoggerFactory.getLogger(UploadController.class);
	
	private static final String UPLOAD_DIRECTORY = "upload";
	private static final String UPLOAD_PATH = System.getProperty("user.home") + File.separator + UPLOAD_DIRECTORY;
	
	@RequestMapping(value="/uploadForm", method=RequestMethod.GET)
	public void uploadForm() {
		log.info("uploadPath : {}", UPLOAD_PATH);
	}
	
	@RequestMapping(value="/uploadForm", method=RequestMethod.POST)
	public String uploadForm(MultipartFile file, Model model) throws IOException {
		log.info("originalFileName: {}", file.getOriginalFilename());
		log.info("size: {}", file.getSize());
		log.info("contentType: {}", file.getContentType());
		
		String savedName = uploadFile(file.getOriginalFilename(), file.getBytes());
		
		model.addAttribute("savedName", savedName);

		return "uploadResult";
	}

	private String uploadFile(String originalFilename, byte[] fildData) throws IOException {
		UUID uid = UUID.randomUUID();
		
		String savedName = uid.toString() + "_" + originalFilename;
		
		File target = new File(UPLOAD_PATH, savedName);
		
		FileCopyUtils.copy(fildData, target);
		
		return savedName;
	}
	
	@RequestMapping(value="/uploadAjax", method=RequestMethod.GET)
	public void uploadAjax() {
	}
	
	@ResponseBody
	@RequestMapping(value="/uploadAjax", method=RequestMethod.POST,
					produces = "text/plain;charset=UTF-8")
	public ResponseEntity<String> uploadAjax(MultipartFile file) throws IOException {
		log.info("originalFileName: {}", file.getOriginalFilename());
		log.info("size: {}", file.getSize());
		log.info("contentType: {}", file.getContentType());
		
		String savedName = UploadUtils.uploadFile(UPLOAD_PATH, file.getOriginalFilename(), file.getBytes());
		
		return new ResponseEntity<String>(savedName, HttpStatus.CREATED);
	}
	
	@ResponseBody
	@RequestMapping("/displayFile")
	public ResponseEntity<byte[]> displayFile(String fileName) throws IOException {
		
		InputStream in = null;
		ResponseEntity<byte[]> entity = null;
		
		log.info("FILE NAME: {}", fileName);
		
		String formatName = fileName.substring(fileName.lastIndexOf(".") + 1);
		
		MediaType mType = MediaUtils.getMediaType(formatName);
		
		HttpHeaders headers = new HttpHeaders();
		
		try {
			in = new FileInputStream(UPLOAD_PATH + fileName);
			
			if (mType != null) {
				// MIME 타입을 이미지 타입으로 설정
				headers.setContentType(mType);
			} else {
				// MIME 타입을 다운로드용으로 설정
				headers.setContentType(MediaType.APPLICATION_OCTET_STREAM);

				fileName = fileName.substring(fileName.indexOf("_") + 1);
				
				// 파일의 이름이 한글일 경우 깨지지 않도록 한글 처리 인코딩을 한다.
				String encodedFileName = new String(fileName.getBytes("UTF-8"), "ISO-8859-1");
				
				headers.add("Content-Disposition", "attachment; filename=\"" + encodedFileName + "\"");
			}
			
			entity = new ResponseEntity<byte[]>(IOUtils.toByteArray(in), headers, HttpStatus.CREATED);
			
		} catch (Exception e) {
			e.printStackTrace();
			entity = new ResponseEntity<byte[]>(HttpStatus.BAD_REQUEST);
		} finally {
			in.close();
		}
		
		return entity;
	}
	
	@ResponseBody
	@RequestMapping("/deleteFile")
	public ResponseEntity<String> deleteFile(String fileName) throws IOException {
		log.info("deleteFile : {}", fileName);
		
		String formatName = fileName.substring(fileName.lastIndexOf(".") + 1);
		
		MediaType mType = MediaUtils.getMediaType(formatName);
		
		// 이미지 타입일 경우 : 원본 파일 삭제
		if (mType != null) {
			String front = fileName.substring(0,12);
			String end = fileName.substring(14);
			new File(UPLOAD_PATH + (front + end).replace('/', File.separatorChar)).delete();
		}
		
		// 이미지 타입일 경우 : 썸네일 이미지 삭제
		// 이미지 타입이 아닐 경우 : 파일 삭제
		new File(UPLOAD_PATH + fileName.replace('/', File.separatorChar)).delete();
		
		return new ResponseEntity<String>("deleted", HttpStatus.OK);
	}
}
