package markehme.factionsplus.Cmds;


import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.io.PrintWriter;
import java.util.Scanner;

import markehme.factionsplus.FactionsPlusTemplates;
import markehme.factionsplus.Utilities;
import markehme.factionsplus.config.Config;
import markehme.factionsplus.config.sections.Section_Warps;
import markehme.factionsplus.references.FP;
import markehme.factionsplus.references.FPP;

import org.bukkit.ChatColor;

import com.massivecraft.factions.cmd.req.ReqFactionsEnabled;
import com.massivecraft.factions.cmd.req.ReqHasFaction;
import com.massivecraft.mcore.cmd.req.ReqIsPlayer;

public class CmdRemoveWarp extends FPCommand {
	public CmdRemoveWarp() {
		
		this.aliases.add( "removewarp" );
		this.aliases.add( "deletewarp" );
		this.aliases.add( "unsetwarp" );
		
		this.fpidentifier = "removewarp";
		
		this.requiredArgs.add( "name" );
		this.errorOnToManyArgs = true;
		
		this.addRequirements( ReqFactionsEnabled.get() );
		this.addRequirements( ReqIsPlayer.get() );
		this.addRequirements( ReqHasFaction.get() );
		
		this.setHelp( "remove a warp" );
		this.setDesc( "remove a warp" );
		
	}
	
	@Override
	public void performfp() {
		String warpname = this.arg( 0 );
		
		if( ! FP.permission.has( sender, "factionsplus.deletewarp" ) ) {
			sender.sendMessage(ChatColor.RED + "No permission!");
			return;
		}
		
		
		if( ! Section_Warps.canSetOrRemoveWarps( usender ) ) {
			/*if( FP.permission.has( sender, "factionsplus.debug" ) ) {
				msg("Config Says: leadersCanSetWarps:          " + Config._warps.leadersCanSetWarps._);
				msg("Factions Says: (you) isLeader:            " + Utilities.isLeader(usender));
				msg("-- ");
				msg("Config Says: officersCanSetWarps:         " + Config._warps.officersCanSetWarps._);
				msg("Factions Says: (you) isOfficer:           " + Utilities.isOfficer(usender));
				msg("-- ");
				msg("Config Says: membersCanSetWarps:          " + Config._warps.membersCanSetWarps._);
				msg("Factions Says: (you) FactionID != null:   " + (usender.getFactionId() != null));
				msg("-- ");
				
				return;
			}*/
			msg( ChatColor.RED + "Sorry, your ranking is not high enough to remove warps!" );
			return;
		}
		
		try {
			boolean found = false;
			
			// Get out working files
			File currentWarpFile 		= 		new File( Config.folderWarps, usender.getFactionId() );
			File currentWarpFileTMP 	= 		new File( Config.folderWarps,  usender.getFactionId() + ".tmp" );
			
			// Scan through the warp file for the correct
			
			FileReader fr				=		null;
			Scanner scanner				=		null;
			
			try {
				fr = new FileReader( currentWarpFile );
				scanner = new Scanner( fr );
				
				while ( scanner.hasNextLine() ) {
					
					String[] warp = scanner.nextLine().split( ":" );
					
					if ( ( warp.length < 1 ) || ( !warp[0].equalsIgnoreCase( warpname ) ) ) {
						continue;
					}
					
					found = true;
					
					break;
				}
				
			} finally {
				if ( null != scanner ) {
					scanner.close();
				}
				if ( null != fr ) {
					try {
						fr.close();
					} catch ( IOException e ) {
						e.printStackTrace();
					}
				}
			}
			
		    if (!found) {
		    	return;
		    }
		    
		    if(Config._economy.costToDeleteWarp._ > 0.0d && Config._economy.isHooked()) {
				if ( ! Utilities.doFinanceCrap(Config._economy.costToDeleteWarp._, "a removal of a warp", usender ) ) {
					return;
				}
			}

			PrintWriter wrt			=	null;
			BufferedReader rdr		=	null;
			
			try {
				wrt = new PrintWriter( new FileWriter( currentWarpFileTMP ) );
				rdr = new BufferedReader( new FileReader( currentWarpFile ) );
				
				String line;
				
				while ( ( line = rdr.readLine() ) != null ) {
					String[] warp = line.split( ":" );
					
					if ( ( warp.length >= 1 ) && ( warp[0].equalsIgnoreCase( warpname ) ) ) {
						continue;
					}
					
					wrt.println( line );
				}
				
			} finally {
				if ( null != rdr ) {
					try {
						rdr.close();
					} catch ( IOException e ) {
						e.printStackTrace();
					}
				}
				if ( null != wrt ) {
					wrt.close();
				}
			}
		
		    if ( !currentWarpFile.delete() ) {
		    	FPP.severe( "[FactionsPlus] Cannot delete " + currentWarpFile.getName() );
		        return;
		    }
		    
		    if ( ! currentWarpFileTMP.renameTo( currentWarpFile ) ) {
		    	FPP.severe( "[FactionsPlus] Cannot rename " + currentWarpFileTMP.getName() + " to " + currentWarpFile.getName() );
		        return;
		    }
		    
		} catch ( Exception e ) {
			FPP.info( "Unexpected error " + e.getMessage() );
			e.printStackTrace();
		    return;
		}
		
		String[] warped_removed_args = { warpname };
		
				
		msg( FactionsPlusTemplates.Go("warped_removed", warped_removed_args) );
		

	}
}
