package com.example.service.impl;

import com.baomidou.mybatisplus.core.toolkit.Wrappers;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.example.entity.dto.Account;
import com.example.entity.dto.StoreImage;
import com.example.mapper.AccountMapper;
import com.example.mapper.ImageStoreMapper;
import com.example.service.ImageService;
import com.example.utils.Const;
import com.example.utils.FlowUtils;
import io.minio.*;
import jakarta.annotation.Resource;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.compress.utils.IOUtils;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.io.OutputStream;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.UUID;

@Slf4j
@Service
public class ImageServiceImpl extends ServiceImpl<ImageStoreMapper, StoreImage> implements ImageService {

    @Resource
    MinioClient client;

    @Resource
    AccountMapper accountMapper;

    @Resource
    FlowUtils flowUtils;

    private final SimpleDateFormat format = new SimpleDateFormat("yyyyMMdd");

    public void fetchImageFromMinio(OutputStream stream, String image) throws Exception {
        GetObjectArgs args = GetObjectArgs.builder()
                .bucket("study")
                .object(image)
                .build();
        GetObjectResponse response = client.getObject(args);
        IOUtils.copy(response, stream);
    }

    @Override
    public String uploadAvatar(MultipartFile file, int id) throws IOException {
        log.info("开始上传头像，用户ID: {}, 文件名: {}, 文件大小: {}", id, file.getOriginalFilename(), file.getSize());
        
        String imageName = UUID.randomUUID().toString().replace("-", "");
        imageName = "/avatar/" + imageName;
        log.info("生成的图片路径: {}", imageName);
        
        PutObjectArgs args = PutObjectArgs.builder()
                .bucket("study")
                .stream(file.getInputStream(), file.getSize(), -1)
                .object(imageName)
                .build();
        try {
            log.info("开始上传到MinIO...");
            client.putObject(args);
            log.info("MinIO上传成功");
            
            String avatar = accountMapper.selectById(id).getAvatar();
            log.info("用户当前头像: {}", avatar);
            
            this.deleteOldAvatar(avatar);
            log.info("删除旧头像完成");
            
            if(accountMapper.update(null, Wrappers.<Account>update()
                    .eq("id", id).set("avatar", imageName)) > 0) {
                log.info("数据库更新成功");
                return imageName;
            } else {
                log.error("数据库更新失败");
                return null;
            }
        } catch (Exception e) {
            log.error("图片上传出现问题，详细错误信息: ", e);
            log.error("错误类型: {}", e.getClass().getName());
            log.error("错误消息: {}", e.getMessage());
            if (e.getCause() != null) {
                log.error("根本原因: {}", e.getCause().getMessage());
            }
            return null;
        }
    }

    @Override
    public String uploadImage(MultipartFile file, int id) throws IOException {
        String key = Const.FORUM_IMAGE_COUNTER + id;
        if(!flowUtils.limitPeriodCounterCheck(key, 20, 3600))
            return null;
        String imageName = UUID.randomUUID().toString().replace("-", "");
        Date date = new Date();
        imageName = "/cache/" + format.format(date) + "/" + imageName;
        PutObjectArgs args = PutObjectArgs.builder()
                .bucket("study")
                .stream(file.getInputStream(), file.getSize(), -1)
                .object(imageName)
                .build();
        try {
            client.putObject(args);
            if(this.save(new StoreImage(id, imageName, date))) {
                return imageName;
            } else {
                return null;
            }
        } catch (Exception e) {
            log.error("图片上传出现问题: "+ e.getMessage(), e);
            return null;
        }
    }

    private void deleteOldAvatar(String avatar) throws Exception {
        if(avatar == null || avatar.isEmpty()) return;
        RemoveObjectArgs remove = RemoveObjectArgs.builder()
                .bucket("study")
                .object(avatar)
                .build();
        client.removeObject(remove);
    }
}
