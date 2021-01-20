package com.leyou.upload.controller;

import com.leyou.upload.service.UploadService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;

import java.util.Map;

@RestController
public class UploadController {

    @Autowired
    private UploadService uploadService;

//    @PostMapping("/image")
//    public ResponseEntity<String> uploadImage(@RequestParam("file") MultipartFile file){ // 必须是file，和前端一致的
//        String imagePath = uploadService.uploadImage(file);
//        return ResponseEntity.ok(imagePath);
//    }

    @GetMapping("/signature")
    public ResponseEntity<Map<String, Object>> ossSignature(){
        Map<String, Object> resultMap = uploadService.ossSignature();
        return ResponseEntity.ok(resultMap);
    }
}
