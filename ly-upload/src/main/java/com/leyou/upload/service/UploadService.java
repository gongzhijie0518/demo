package com.leyou.upload.service;

import com.github.tobato.fastdfs.domain.fdfs.StorePath;
import com.github.tobato.fastdfs.service.FastFileStorageClient;
import com.leyou.common.enums.ExceptionEnum;
import com.leyou.common.exception.LyException;

import lombok.extern.slf4j.Slf4j;

import org.apache.commons.lang3.StringUtils;
import org.mockito.internal.util.StringUtil;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import javax.imageio.ImageIO;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;
import java.util.Arrays;
import java.util.List;

@Slf4j
@Service
public class UploadService {
    private static final List<String> allow_image_type = Arrays.asList("image/jpeg", "image/png", "image/bmp");
    @Autowired
    private FastFileStorageClient storageClient;


    public String uploadImage(MultipartFile file) {
        try {
            //文件校验
            if (!allow_image_type.contains(file.getContentType())) {
                throw new LyException(ExceptionEnum.INVALID_FILE_TYPE);
            }
            //校验文件类容
            BufferedImage image = ImageIO.read(file.getInputStream());
            if (image == null) {
                throw new LyException(ExceptionEnum.INVALID_FILE_TYPE);
            }
            String filename = file.getOriginalFilename();
            String extension= StringUtils.substringAfter(filename,".");
            StorePath storePath = storageClient.uploadFile(file.getInputStream(), file.getSize(), extension, null);


//            //准备保存文件地址
//            String dir = "F:\\Program Files\\nginx-1.12.2\\nginx-1.12.2\\html";
//
//        File filePath = new File(dir,  filename );
//        //保存文件
//            file.transferTo(filePath);
            //返回可访问的url路径
            String baseUrl = "http://image.leyou.com/";


            return baseUrl + storePath.getFullPath();
        } catch (IOException e) {
            log.error("【上传微服务】文件上传失败!", e);
            throw new LyException(ExceptionEnum.FILE_UPLOAD_ERROR, e);
        }

    }
}
