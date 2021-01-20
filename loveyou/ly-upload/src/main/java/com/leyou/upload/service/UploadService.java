package com.leyou.upload.service;

import com.aliyun.oss.OSS;
import com.aliyun.oss.common.utils.BinaryUtil;
import com.aliyun.oss.model.MatchMode;
import com.aliyun.oss.model.PolicyConditions;
import com.leyou.common.constants.LyConstants;
import com.leyou.common.exception.pojo.ExceptionEnum;
import com.leyou.common.exception.pojo.LyException;
import com.leyou.upload.config.OSSProperties;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import javax.imageio.ImageIO;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.util.Date;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.UUID;

@Service
public class UploadService {

    @Autowired
    private OSSProperties ossProps;

    @Autowired
    private OSS ossClient;

//    public String uploadImage(MultipartFile file) {
//
//        try {
//            //判断文件流是否为空
//            InputStream inputStream = file.getInputStream();
//            //判断是否为图片流
//            BufferedImage image =  ImageIO.read(inputStream);
//            if(image==null){
//                throw new LyException(ExceptionEnum.INVALID_FILE_TYPE);
//            }
//
//
//            //1.生成uuid的随机文件名称
//            //1.1 生成uuid
//            String uuid = UUID.randomUUID().toString();
//            //1.2 获取文件原名
//            String oldName = file.getOriginalFilename();
//            //1.3 获取文件后缀
//            String extName = oldName.substring(oldName.lastIndexOf("."));
//            //1.4 最终的文件名称
//            String fileName = uuid + extName;
//
//            //2.把文件保存到nginx图片服务器
//            file.transferTo(new File(LyConstants.IMAGE_PATH,fileName));
//
//            //3.返回图片的访问路径
//            return LyConstants.IMAGE_URL+fileName;
//        } catch (IOException e) {
//            e.printStackTrace();
//            throw new LyException(ExceptionEnum.FILE_UPLOAD_ERROR);
//        }
//
//    }

    public Map<String, Object> ossSignature() {
        try {
            long expireTime = ossProps.getExpireTime();
            long expireEndTime = System.currentTimeMillis() + expireTime * 1000;
            Date expiration = new Date(expireEndTime);
            // PostObject请求最大可支持的文件大小为5 GB，即CONTENT_LENGTH_RANGE为5*1024*1024*1024。
            PolicyConditions policyConds = new PolicyConditions();
            policyConds.addConditionItem(PolicyConditions.COND_CONTENT_LENGTH_RANGE, 0, ossProps.getMaxFileSize());
            policyConds.addConditionItem(MatchMode.StartWith, PolicyConditions.COND_KEY, ossProps.getDir());

            String postPolicy = ossClient.generatePostPolicy(expiration, policyConds);
            byte[] binaryData = postPolicy.getBytes("utf-8");
            String encodedPolicy = BinaryUtil.toBase64String(binaryData);
            String postSignature = ossClient.calculatePostSignature(postPolicy);

            Map<String, Object> respMap = new LinkedHashMap<String, Object>();
            respMap.put("accessId", ossProps.getAccessKeyId());
            respMap.put("policy", encodedPolicy);
            respMap.put("signature", postSignature);
            respMap.put("dir", ossProps.getDir());
            respMap.put("host", ossProps.getHost());
            respMap.put("expire", String.valueOf(expireEndTime));
            // respMap.put("expire", formatISO8601Date(expiration));
            return respMap;
        } catch (Exception e) {
            // Assert.fail(e.getMessage());
            System.out.println(e.getMessage());
            throw new LyException(ExceptionEnum.FILE_UPLOAD_ERROR);
        } finally {
            ossClient.shutdown();
        }
    }
}
