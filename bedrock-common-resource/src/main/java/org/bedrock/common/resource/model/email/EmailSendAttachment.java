package org.bedrock.common.resource.model.email;

import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.experimental.Accessors;

import javax.annotation.Nullable;
import java.util.List;



@Data
@Accessors(chain = true)
@EqualsAndHashCode(callSuper = true)
public class EmailSendAttachment extends EmailSendCCAndBCC{
    /**
     * 附件
     */
    @Nullable
    private List<AttachmentDataSource> attachments;


    /**
     * 内联元素
     */
    @Nullable
    private List<AttachmentDataSource> inlineElement;
}
