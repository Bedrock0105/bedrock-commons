package org.bedrock.common.resource.model.email;

import jakarta.activation.DataSource;
import jakarta.activation.FileTypeMap;
import lombok.Getter;
import org.bedrock.common.code.util.IoUtil;
import org.bedrock.common.code.util.ObjectUtil;
import org.bedrock.common.code.util.StringUtil;
import org.springframework.mail.javamail.ConfigurableMimeFileTypeMap;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;


public class AttachmentDataSource implements DataSource {
    private static final ConfigurableMimeFileTypeMap defaultTypeMap = new ConfigurableMimeFileTypeMap();

    private FileTypeMap fileTypeMap;

    private final byte[] bytes;

    private String fileName;

    private String contentType;

    /**
     * 内嵌元素id
     */
    @Getter
	private String contentId;


    /**
     * @param fileName    附件名称
     * @param inputStream 附件的io流
     * @Discription 创建一个附件类
     */
    public AttachmentDataSource(String fileName, InputStream inputStream) {
        this.fileName = fileName;
        this.bytes = IoUtil.copyToByteArray(inputStream);
    }


    /**
     * @param fileName 附件名称
     * @param bytes    附件数据
     * @Discription 创建一个附件类
     */
    public AttachmentDataSource(String fileName, byte[] bytes) {
        this.fileName = fileName;
        this.bytes = bytes;
    }


    /**
     * @param fileName    附件名称
     * @param inputStream 附件的io流
     * @param contentType 附件类型 Mime
     * @Discription 创建一个附件类
     */
    public AttachmentDataSource(String fileName, InputStream inputStream, String contentType) {
        this.fileName = fileName;
        this.contentType = contentType;
        this.bytes = IoUtil.copyToByteArray(inputStream);
    }


    /**
     * @param fileName    附件名称
     * @param bytes       附件数据
     * @param contentType 附件类型 Mime
     * @Discription 创建一个附件类
     */
    public AttachmentDataSource(String fileName, byte[] bytes, String contentType) {
        this.fileName = fileName;
        this.contentType = contentType;
        this.bytes = bytes;
    }

	public AttachmentDataSource setContentId(String contentId) {
        this.contentId = contentId;
        return this;
    }


    /**
     * 设置 fileTypeMap
     */
    public AttachmentDataSource setFileTypeMap(FileTypeMap fileTypeMap) {
        this.fileTypeMap = fileTypeMap;
        return this;
    }

    /**
     * 重新设置附件名称
     */
    public AttachmentDataSource setFileName(String fileName) {
        this.fileName = fileName;
        return this;
    }

    /**
     * 重新设置附件类型 Mime
     */
    public AttachmentDataSource setContentType(String contentType) {
        this.contentType = contentType;
        return this;
    }


    @Override
    public InputStream getInputStream() throws IOException {
        return new ByteArrayInputStream(bytes);
    }

    @Override
    public OutputStream getOutputStream() throws IOException {
        throw new UnsupportedOperationException("Read-only javax.activation.DataSource");
    }

    @Override
    public String getContentType() {
        return StringUtil.isBlank(this.contentType) ? getFileTypeMap().getContentType(getName()) : this.contentType;
    }

    @Override
    public String getName() {
        return this.fileName;
    }

    public FileTypeMap getFileTypeMap() {
        return ObjectUtil.isNotEmpty(this.fileTypeMap) ? this.fileTypeMap : defaultTypeMap;
    }


}
