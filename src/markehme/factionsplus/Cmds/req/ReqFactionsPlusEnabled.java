package markehme.factionsplus.Cmds.req;

import org.bukkit.command.CommandSender;

import markehme.factionsplus.MCore.UConf;

import com.massivecraft.mcore.cmd.MCommand;
import com.massivecraft.mcore.cmd.req.ReqAbstract;

public class ReqFactionsPlusEnabled extends ReqAbstract {
	private static final long serialVersionUID = 1L;

	// -------------------------------------------- //
	// INSTANCE & CONSTRUCT
	// -------------------------------------------- //

	private static ReqFactionsPlusEnabled i = new ReqFactionsPlusEnabled();
	public static ReqFactionsPlusEnabled get() { return i; }

	// -------------------------------------------- //
	// OVERRIDE
	// -------------------------------------------- //

	@Override
	public boolean apply(CommandSender sender, MCommand command) {
		return !UConf.isDisabled(sender);
	}

	@Override
	public String createErrorMessage(CommandSender sender, MCommand command) {
		return UConf.getDisabledMessage(sender);
	}

}
