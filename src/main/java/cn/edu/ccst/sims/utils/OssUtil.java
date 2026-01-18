package cn.edu.ccst.sims.utils;

import com.aliyun.oss.OSS;
import com.aliyun.oss.OSSClientBuilder;
import com.aliyun.oss.common.auth.DefaultCredentialProvider;
import com.aliyun.oss.common.auth.CredentialsProvider;
import com.aliyun.oss.model.PolicyConditions;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import java.nio.charset.StandardCharsets;
import java.text.SimpleDateFormat;
import java.util.*;

/**
 * 阿里云OSS工具类
 */
@Component
public class OssUtil {

    @Value("${aliyun.oss.endpoint}")
    private String endpoint;

    @Value("${aliyun.oss.accessKeyId}")
    private String accessKeyId;

    @Value("${aliyun.oss.accessKeySecret}")
    private String accessKeySecret;

    @Value("${aliyun.oss.bucketName}")
    private String bucketName;

    @Value("${aliyun.oss.domain}")
    private String domain;

    /**
     * 生成OSS上传签名
     */
    public Map<String, Object> generateSignature() {
        try {
            // 不再生成目录，直接使用根目录
            String dir = "";

            // 生成Policy
            long expireTime = 30; // 30分钟过期
            long expireEndTime = System.currentTimeMillis() + expireTime * 60 * 1000;

            // 创建PolicyConditions
            PolicyConditions policyConds = new PolicyConditions();
            policyConds.addConditionItem(PolicyConditions.COND_CONTENT_LENGTH_RANGE, 0, 1048576000); // 最大1GB

            // 生成Policy JSON
            String postPolicy = generatePostPolicy(expireEndTime, policyConds);
            byte[] binaryData = postPolicy.getBytes(StandardCharsets.UTF_8);
            String policy = Base64.getEncoder().encodeToString(binaryData);

            // 生成Signature
            javax.crypto.Mac mac = javax.crypto.Mac.getInstance("HmacSHA1");
            mac.init(new javax.crypto.spec.SecretKeySpec(accessKeySecret.getBytes(StandardCharsets.UTF_8), "HmacSHA1"));
            byte[] signatureBytes = mac.doFinal(policy.getBytes(StandardCharsets.UTF_8));
            String signature = Base64.getEncoder().encodeToString(signatureBytes);

            Map<String, Object> result = new HashMap<>();
            result.put("accessId", accessKeyId);
            result.put("policy", policy);
            result.put("signature", signature);
            result.put("dir", dir);
            result.put("host", domain); // 使用domain作为上传地址
            result.put("expire", String.valueOf(expireEndTime));
            result.put("bucket", bucketName);

            // 调试输出
            System.out.println("OSS签名生成成功:");
            System.out.println("accessId: " + accessKeyId);
            System.out.println("domain: " + domain);
            System.out.println("host: " + domain);

            return result;
        } catch (Exception e) {
            System.err.println("生成OSS签名失败: " + e.getMessage());
            e.printStackTrace();
            throw new RuntimeException("生成OSS签名失败: " + e.getMessage());
        }
    }

    private String generatePostPolicy(long expireEndTime, PolicyConditions policyConds) {
        try {
            SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss'Z'");
            dateFormat.setTimeZone(TimeZone.getTimeZone("UTC"));

            String expiration = dateFormat.format(new Date(expireEndTime));

            // 构建最简化的Policy JSON
            StringBuilder policyJson = new StringBuilder();
            policyJson.append("{\"expiration\":\"").append(expiration).append("\",\"conditions\":[");

            // 只添加最基本的条件：bucket和文件大小
            policyJson.append("[\"eq\",\"$bucket\",\"").append(bucketName).append("\"],");
            policyJson.append("[\"content-length-range\",0,1048576000]");

            policyJson.append("]}");

            System.out.println("生成的Policy: " + policyJson.toString());

            return policyJson.toString();
        } catch (Exception e) {
            throw new RuntimeException("生成Policy失败: " + e.getMessage());
        }
    }

    /**
     * 创建OSS客户端
     */
    private OSS createOSSClient() {
        CredentialsProvider credentialsProvider = new DefaultCredentialProvider(accessKeyId, accessKeySecret);

        return new OSSClientBuilder()
                .build(endpoint, credentialsProvider);
    }

    /**
     * 上传文件到OSS
     */
    public String uploadFile(byte[] fileBytes, String fileName, String fileType) {
        try {
            OSS ossClient = createOSSClient();

            // 生成文件名
            String objectName = "uploads/" + new SimpleDateFormat("yyyy-MM-dd").format(new Date()) + "/" + fileName;

            // 将byte[]转换为InputStream
            java.io.ByteArrayInputStream inputStream = new java.io.ByteArrayInputStream(fileBytes);

            // 上传文件
            ossClient.putObject(bucketName, objectName, inputStream);

            // 返回文件URL
            return domain + "/" + objectName;
        } catch (Exception e) {
            throw new RuntimeException("文件上传失败: " + e.getMessage());
        }
    }

    /**
     * 删除OSS文件
     */
    public void deleteFile(String fileUrl) {
        try {
            OSS ossClient = createOSSClient();

            // 从URL中提取objectName
            String objectName = fileUrl.substring(fileUrl.indexOf("uploads/"));

            ossClient.deleteObject(bucketName, objectName);
        } catch (Exception e) {
            throw new RuntimeException("文件删除失败: " + e.getMessage());
        }
    }
}
