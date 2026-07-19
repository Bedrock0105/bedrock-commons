package org.bedrock.common.resource.model.email;

import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.experimental.Accessors;

import javax.annotation.Nullable;
import java.util.List;

@Data
@Accessors(chain = true)
@EqualsAndHashCode(callSuper = true)
public class EmailSendCCAndBCC extends EmailSend {

	/**
	 * 抄送人
	 * The "Cc" (carbon copy) recipients.
	 */
	@Nullable
	private List<String> cc;

	/**
	 * 密送人
	 * The "Bcc" (blind carbon copy) recipients.
	 */
	@Nullable
	private List<String> bcc;
}
