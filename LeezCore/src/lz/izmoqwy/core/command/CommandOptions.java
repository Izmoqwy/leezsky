package lz.izmoqwy.core.command;

import lombok.Builder;
import lombok.Getter;
import lombok.Setter;
import lombok.experimental.Accessors;

@Getter
@Builder
public class CommandOptions {

	private String permission;
	private boolean playerOnly;
	private boolean needsArg ;

	private int cooldown;

}
