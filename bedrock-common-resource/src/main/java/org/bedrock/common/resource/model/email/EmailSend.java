package org.bedrock.common.resource.model.email;

import lombok.Data;
import lombok.experimental.Accessors;

import javax.annotation.Nullable;
import java.util.List;


@Data
@Accessors(chain = true)
public class EmailSend {
    /**
     * 发送人
     */
    @Nullable
    private String form;

    /**
     * 收件人
     */
    private List<String> to;

    /**
     * 主题
     */
    private String subject;

    /**
     * 内容
     */
    private String content;

    /**
     * 内容是否html
     */
    private Boolean isHtml = false;


}
