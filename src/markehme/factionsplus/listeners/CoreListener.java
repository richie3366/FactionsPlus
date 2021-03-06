package markehme.factionsplus.listeners;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;

import markehme.factionsplus.FactionsPlus;
import markehme.factionsplus.FactionsPlusRules;
import markehme.factionsplus.FactionsPlusScoreboard;
import markehme.factionsplus.Utilities;
import markehme.factionsplus.config.Config;
import markehme.factionsplus.extras.FType;
import markehme.factionsplus.references.FP;

import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.Material;
import org.bukkit.Server;
import org.bukkit.entity.Cow;
import org.bukkit.entity.Entity;
import org.bukkit.entity.EntityType;
import org.bukkit.entity.Horse;
import org.bukkit.entity.MushroomCow;
import org.bukkit.entity.Pig;
import org.bukkit.entity.Player;
import org.bukkit.entity.Sheep;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;

import org.bukkit.event.entity.EntityDamageByEntityEvent;
import org.bukkit.event.entity.EntityDamageEvent;
import org.bukkit.event.entity.EntityShootBowEvent;
import org.bukkit.event.entity.PlayerDeathEvent;
import org.bukkit.event.entity.ProjectileLaunchEvent;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.event.inventory.InventoryType;
import org.bukkit.event.player.PlayerCommandPreprocessEvent;
import org.bukkit.event.player.PlayerFishEvent;
import org.bukkit.event.player.PlayerInteractEntityEvent;
import org.bukkit.event.player.PlayerJoinEvent;
import org.bukkit.event.player.PlayerMoveEvent;
import org.bukkit.event.player.PlayerShearEntityEvent;
import org.bukkit.inventory.ItemStack;

import com.massivecraft.factions.entity.BoardColls;
import com.massivecraft.factions.entity.Faction;
import com.massivecraft.factions.entity.UPlayer;
import com.massivecraft.factions.event.FactionsEventChunkChange;
import com.massivecraft.factions.event.FactionsEventCreate;
import com.massivecraft.factions.event.FactionsEventMembershipChange;
import com.massivecraft.factions.event.FactionsEventMembershipChange.MembershipChangeReason;
import com.massivecraft.factions.event.FactionsEventRelationChange;
import com.massivecraft.mcore.ps.PS;


public class CoreListener implements Listener{
	public static Server fp;
	
	/**
	 * Fixes issues with changing relationship between wilderness/safezone/warzone
	 * @param event
	 */
	@EventHandler
	public void onFactionsEventRelationChange(FactionsEventRelationChange event) {
		if(FType.valueOf(event.getFaction()) == FType.WILDERNESS || FType.valueOf(event.getOtherFaction()) == FType.WILDERNESS) {
			if(Config._extras._Fixes.disallowChangingRelationshipToWilderness._) {
				event.getUSender().msg(ChatColor.RED + "You can't change the relationship towards the wilderness!");
				event.setCancelled(true);
				return;
			}
		}
		
		if(FType.valueOf(event.getFaction()) == FType.SAFEZONE || FType.valueOf(event.getOtherFaction()) == FType.SAFEZONE) {
			if(Config._extras._Fixes.disallowChangingRelationshipToSafezone._) {
				event.getUSender().msg(ChatColor.RED + "You can't change the relationship towards a safezone!");
				event.setCancelled(true);
				return;
			}
		}
		
		if(FType.valueOf(event.getFaction()) == FType.WARZONE || FType.valueOf(event.getOtherFaction()) == FType.WARZONE) {
			if(Config._extras._Fixes.disallowChangingRelationshipToWarzone._) {
				event.getUSender().msg(ChatColor.RED + "You can't change the relationship towards a warzone!");
				event.setCancelled(true);
				return;
			}
		}
	}
	
	@EventHandler(priority = EventPriority.NORMAL )
	public void onPlayerThrowPotion(ProjectileLaunchEvent event) {
		
		if(!(event.getEntity() instanceof Player)) {
			return;
		}
		
		if ((event.getEntityType() == EntityType.SPLASH_POTION) &&
				(((Player)event.getEntity().getShooter()).isFlying()) &&
					!Config._extras._Flight.allowSplashPotionsWhileFlying._) {
			
			// So it is a splash potion, the player is flying, and the config disallows it
			((Player)event.getEntity().getShooter()).sendMessage(ChatColor.RED + "You can't throw potions while flying!");
			event.setCancelled(true);
		}
	}
	
	@EventHandler(priority = EventPriority.NORMAL )
	public void onPlayerBowShoot(EntityShootBowEvent event) {
		// Flying Player can't damage others
		if(!Config._extras._Flight.allowAttackingWhileFlying._ ) {
			
			// Confirm it is a player
			if( ( event.getEntity() instanceof Player ) ) {
				Player player = (Player) event.getEntity();
				
				// Is the player flying?
				if( player.isFlying() && !player.isOp()) {
					
					player.sendMessage(ChatColor.RED + "You can't attack while flying!");
					event.setCancelled(true);
					
				}
					
			}
		}
	}
	
	@EventHandler(priority = EventPriority.NORMAL)
	public void onPlayerAttack(final EntityDamageByEntityEvent event) {
		Entity e = event.getEntity();
		
		// Flying Player can't damage others 
		if(!Config._extras._Flight.allowAttackingWhileFlying._) {
			// Confirm it is PvP
			if(e instanceof Player && event.getDamager() instanceof Player) {
				Player damager = (Player) event.getDamager();
				
				// Confirm the flying player is attacking
				if(damager.isFlying() && !damager.isOp()) {
					
					damager.sendMessage(ChatColor.RED + "You can't attack while flying!");
					event.setCancelled(true);
					
				}
				
			}
		}
		
		// .. 
	}
	
	@EventHandler(priority = EventPriority.MONITOR)
	public void onPlayerMove(PlayerMoveEvent event) {
				
		// Check for permission factionsplus.flightinterritory
		if(FP.permission.has(event.getPlayer(), "factionsplus.flightinownterritory") && !event.getPlayer().isOp() && !UPlayer.get(event.getPlayer()).isUsingAdminMode()) {
			if(BoardColls.get().getFactionAt(PS.valueOf(event.getPlayer().getLocation())).getId() == UPlayer.get(event.getPlayer()).getFactionId()) {
				event.getPlayer().setAllowFlight(true);
			} else {
				event.getPlayer().setAllowFlight(false);
			}
		}
		
	}
	
	@EventHandler(priority = EventPriority.MONITOR)
	public void onLandClaim(FactionsEventChunkChange event) {
		
		if(Config._extras._protection.worldguardCanBuildCheck._) {
			
			
			if(event.getUSender() == null) return;
			
			if( !event.getUSender().getFaction().isNone() && !event.getUSender().isUsingAdminMode() && !event.getUSender().getPlayer().isOp() && !FactionsPlus.permission.has(event.getUSender().getPlayer(), "factionsplus.bypassregioncheck")) {
				
				// Ensure WorldGuard exists! 
				if( Bukkit.getServer().getPluginManager().isPluginEnabled( "WorldGuard" ) ) {
					
					if( FP.worldGuardPlugin != null ) {
						
						// Check for chunks
						if( Utilities.checkForRegionsInChunk(event.getUSender().getPlayer().getLocation(), event.getUSender().getPlayer()) ) {
							
							// TODO: 	Check region flags - this can be done by instead returning an array
							//			of the regions in the chunk, checking the size of the array and then
							//			loop through if we're supporting checkForWorldGuardRegionFlags
							
							// The flag will be "faction" /region flag SomeRegion faction MyFactionName
							// and thus, that Faction can build. 
							
							event.setCancelled(true);
							
							event.getUSender().msg(ChatColor.RED + "There is a WorldGuard region in the way here!");
							
							
						}
					
					}
				
				}
			
			}
			
		}
			
	}
	
	@EventHandler(priority = EventPriority.NORMAL)
	public void onEntityDamage(EntityDamageEvent event) {
		
		if (event.getEntity() instanceof Player) {
			
			Player p = (Player) event.getEntity();
			
			Faction factionAt = BoardColls.get().getFactionAt( PS.valueOf( p.getLocation() ) );
			
			if( FType.valueOf( factionAt ) == FType.SAFEZONE && Config._extras._protection.safeZonesExtraSafe._ ) {
				
				event.setCancelled( true );
				
			}
			
		}
	}
	
	@EventHandler(priority = EventPriority.NORMAL)
	public void onVillagerTrade( InventoryClickEvent event ) {
		
		if( event.getWhoClicked() == null ){
			return; // NPE issue possibly? 
		}
		
		// is a player, ya?
		if( event.getWhoClicked() instanceof Player ) {
			
			UPlayer uPlayer = UPlayer.get( event.getWhoClicked() );
			
			if( uPlayer == null ) {
				return; // NPE issue possibly? 
			}
			
			// detects villager trade with player
			if( event.getInventory().getType() == InventoryType.MERCHANT && !uPlayer.isUsingAdminMode() ) {
				
				Faction factionAt = BoardColls.get().getFactionAt( PS.valueOf( uPlayer.getPlayer().getLocation() ) );
				
				if( factionAt != uPlayer.getFaction() && !factionAt.isNone() && FType.valueOf( factionAt) != FType.SAFEZONE ) {
					
					event.setCancelled( true );
					uPlayer.msg( ChatColor.RED + "You can not do that in another Factions territory." );
					
					return; // No further checks required 
					
				}
			}
		}
		
	}
	
	@EventHandler(priority = EventPriority.NORMAL)
	public void onPlayerCreateFaction( FactionsEventCreate event ) {
		
		if(Config._factions.factionNameFirstLetterForceUpperCase._ ) {
			
			String upperFactionName = Character.toUpperCase(event.getFactionName().charAt(0)) + event.getFactionName().substring(1);
			
			Faction wFaction = Faction.get( event.getFactionId() );
			
			wFaction.setName( upperFactionName );
			
		}
	}
	
	@EventHandler(priority = EventPriority.NORMAL)
	public void onPlayerFish( PlayerFishEvent event ) {
		
		if( Config._extras._protection.stricterFarmingProtection._ ) {
			
			Player player = event.getPlayer();
			UPlayer uPlayer = UPlayer.get( player );
			
			Faction factionAt = BoardColls.get().getFactionAt( PS.valueOf( player.getLocation() ) );
			
			// Are they in their own Faction land, or in wilderness
			if( factionAt != uPlayer.getFaction() && !factionAt.isNone() && FType.valueOf( factionAt) != FType.SAFEZONE  ) {
				
				event.setCancelled( true );
				player.sendMessage( ChatColor.RED + "You can not do that in another Factions territory." );
				
				return; // No further checks required 
				
			}
	        
			// Check location of hook (hook could be in, player could be out) 
			
			Faction factionAtHook = BoardColls.get().getFactionAt( PS.valueOf( event.getHook().getLocation() ) ); 
			
			if( factionAtHook != uPlayer.getFaction() && !factionAtHook.isNone() &&  FType.valueOf( factionAt) != FType.SAFEZONE  ) {
				
				event.setCancelled( true );
				player.sendMessage( ChatColor.RED + "You can not do that in another Factions territory." );
				
			}
		}
	}
	
	@EventHandler(priority = EventPriority.NORMAL)
	public void onPlayerInteractEntity( PlayerInteractEntityEvent event ) {
		
	    Player player = event.getPlayer();
	    
	    Entity currentEntity = event.getRightClicked();
	    // disable animal stealing of horses (with a leash) / pigs (also stops inventory stealing - I think)
	    if ( 
	    		( event.getRightClicked() instanceof Horse && player.getItemInHand().getType().equals( Material.LEASH ) ) ||
	    		event.getRightClicked() instanceof Pig || 
	    		event.getRightClicked() instanceof Horse 
	    ) {
	    	
	    	
	    	
	    	Faction entityAt = BoardColls.get().getFactionAt( PS.valueOf( currentEntity.getLocation() ));
	    	
	    	if( entityAt.getId() != UPlayer.get( player).getFaction().getId() && FType.valueOf( entityAt ) == FType.FACTION ) {
	    		
	    		player.sendMessage( "You can not interact with that animal in this Factions land." );
	    		player.updateInventory();
	    		
	    	}
	    	
	    }

	}
	
	@EventHandler(priority = EventPriority.NORMAL)
	public void onPlayerMilkEvent( PlayerInteractEntityEvent event ) {
		Player player = event.getPlayer();
		
		if( ( 
			(event.getRightClicked() instanceof Cow && player.getItemInHand().getType().equals( Material.BUCKET ) || ( event.getRightClicked() instanceof MushroomCow && player.getItemInHand().getType().equals( Material.BOWL ) ) ) )
			&& Config._extras._protection.stricterFarmingProtection._
		) {
			
			UPlayer uPlayer = UPlayer.get( player );
			
			Faction factionAt = BoardColls.get().getFactionAt( PS.valueOf( player.getLocation() ) );
			
			if( factionAt != uPlayer.getFaction() && !factionAt.isNone() && FType.valueOf( factionAt) != FType.SAFEZONE  ) {
				
				event.setCancelled( true );
				player.sendMessage( ChatColor.RED + "You can not do that in another Factions territory." );
				
				return; // No further checks required 
			}
			
			// Also check location of cow (player could be outside area, but cow inside / using hacks)
			
			Faction factionAtCow = BoardColls.get().getFactionAt( PS.valueOf( event.getRightClicked().getLocation() ) );
			
			if( factionAtCow != uPlayer.getFaction() && !factionAtCow.isNone() &&  FType.valueOf( factionAtCow) != FType.SAFEZONE  ) {
				
				event.setCancelled( true );
				player.sendMessage( ChatColor.RED + "You can not do that in another Factions territory." );
				
				return;
				
			}
		}
	}
    
	@EventHandler(priority = EventPriority.NORMAL)
	public void onPlayerShearEntityEvent(PlayerShearEntityEvent event) {
		
	    if ( event.getEntity() instanceof Sheep && Config._extras._protection.stricterFarmingProtection._) {
	    	
	        Player player = event.getPlayer();
	        UPlayer uPlayer = UPlayer.get( player );
	        
	        Faction factionAt = BoardColls.get().getFactionAt( PS.valueOf( player.getLocation() ) );
	        
	        // Are they in their own Faction land, or in wilderness
	        if( factionAt != uPlayer.getFaction() && !factionAt.isNone() ) {
	        	
	        	event.setCancelled( true );
	        	player.sendMessage( ChatColor.RED + "You can not do that in another Factions territory." );
	        	
	        	return; // No further checks required
	        }
	        
	        // Check location of sheep (sheep could be inside, player could be outside / using hacks)
			
			Faction factionAtSheep = BoardColls.get().getFactionAt( PS.valueOf( event.getEntity().getLocation() ) );
			
			if( factionAtSheep != uPlayer.getFaction() && !factionAtSheep.isNone() &&  FType.valueOf( factionAtSheep) != FType.SAFEZONE  ) {
		        
				event.setCancelled( true );
				player.sendMessage( ChatColor.RED + "You can not do that in another Factions territory." );
				
			}
	    }
		
	}
		
	@EventHandler
	public void onPlayerJoinEvent(PlayerJoinEvent event) {
		if( event.getPlayer().isOp() ) {
			
			if( FP.update_avab ) {
				
				event.getPlayer().sendMessage( ChatColor.GOLD + "[FactionsPlus] " + ChatColor.WHITE + "Attention op: There is an update available for FactionsPlus! Please upgrade ASAP." );
			
			}
			
		}
		
		if( Config._extras._scoreboards.sendScoreboardOnJoin._ && Config._extras._scoreboards.showScoreboardOfFactions._ ) {
			
			
			if( FactionsPlusScoreboard.scoreBoard != null ) {
				event.getPlayer().setScoreboard( FactionsPlusScoreboard.scoreBoard );
			}
				
		}
		
	}
		
	@EventHandler
	public void onPlayerCommandPreprocess(PlayerCommandPreprocessEvent event) {
		if( event.isCancelled() ) {
			return;
		}

		Player player = event.getPlayer();
		
		Faction factionHere = BoardColls.get().getFactionAt( PS.valueOf( player.getLocation() ) );
		
		if( FType.valueOf( factionHere) == FType.WARZONE ) {
			
			if (!player.isOp() && !UPlayer.get(player).isUsingAdminMode()) {
				if(FactionsPlus.commandsDisabledInWarzone.containsKey(event.getMessage().toLowerCase().split(" ")[0].replace("/" , ""))) {
					event.setCancelled(true);
					player.sendMessage("You can't use that command in a WarZone!");
				}
			}
		}
	}
		
	@EventHandler(priority=EventPriority.MONITOR)
	public void onFactionsMembershipChange(FactionsEventMembershipChange event) {
		
		// Show rules on join 
		if( event.getReason() == MembershipChangeReason.JOIN ) {
			if(Config._rules.onFirstJoinFactionShowRules._) {
				if ( new File(Config.folderFRules+File.separator+event.getUPlayer().getFactionId()+".rules").exists() ) {
					FactionsPlusRules.sendRulesToPlayer( event.getUPlayer() );
				}
			}
			
			return;
		}
		
		// Remove data on leave
		if( event.getReason() == MembershipChangeReason.LEAVE ) { 
			Faction faction = event.getUPlayer().getFaction();
			if (faction.getUPlayers().size() == 1) {
				// Last player, so remove all data
				removeFPData(faction);
			}
			
			return;
		}
		
		// Remove data on disband 
		if( event.getReason() == MembershipChangeReason.DISBAND ) {
			
			removeFPData( event.getUPlayer().getFaction() );
			
			return;
		}
		
	}
	
	@EventHandler(priority = EventPriority.HIGHEST)
	public void onPlayerDeath(PlayerDeathEvent event) {
		
		final Player currentPlayer = event.getEntity();
				
		if( FType.valueOf( BoardColls.get().getFactionAt( PS.valueOf(currentPlayer.getLocation() ) ) )  == FType.WARZONE ) {
			
			if(!FactionsPlus.permission.has(currentPlayer, "factionsplus.keepItemsOnDeathInWarZone")) {
				return;
			} else {
				currentPlayer.sendMessage(ChatColor.RED + "You died in the WarZone, so you get to keep your items.");
			}
			
			final ItemStack[] playersArmor = currentPlayer.getInventory().getArmorContents();
			final ItemStack[] playersInventory = currentPlayer.getInventory().getContents();
			
			// EntityDamageEvent damangeEvent = currentPlayer.getLastDamageCause();
			
			// In the future - maybe only specific death events? e.g. maybe only by mobs/players
			// not from fall damage or sucide. -- configurable of course 
			
			// Players current armor 
			
			
			Bukkit.getScheduler().scheduleSyncDelayedTask(fp.getPluginManager().getPlugin("FactionsPlus"), new Runnable() {
				@Override
				public void run() {
					currentPlayer.getInventory().setArmorContents(playersArmor);
				}
	
			});
	
			for (ItemStack is : playersArmor) {
				event.getDrops().remove(is);
			}
			
			// Players Experience
			event.setDroppedExp(0);
	
			for (int i = 0; i < playersInventory.length; i++) {
				// drop nothing!
				event.getDrops().remove(playersInventory[i]);
	
			}
			
			
			Bukkit.getScheduler().scheduleSyncDelayedTask(fp.getPluginManager().getPlugin("FactionsPlus"), new Runnable() {
	
				@Override
				public void run() {
					currentPlayer.getInventory().setContents(playersInventory);
				}
	
			});
		}

	}
	
	
	private final void removeFPData( Faction forFaction ) {
		// Annoucements
		File tempFile = new File( Config.folderAnnouncements, forFaction.getId() );
		if ( tempFile.exists() ) {
			tempFile.delete();
		}
		tempFile = null;
		
		// Bans
		File tempDir = Config.folderFBans;
		if ( tempDir.isDirectory() ) {
			for ( File file : tempDir.listFiles() ) {
				if ( file.getName().startsWith( forFaction.getId() + "." ) ) {
					file.delete();
				}
			}
		}
		tempDir = null;
		
		// Rules
		tempFile = new File( Config.folderFRules, forFaction.getId() );
		if ( tempFile.exists() ) {
			tempFile.delete();
		}
		tempFile = null;
		
		// Jailed Players and Jail locations
		tempDir = Config.folderJails;
		if ( tempDir.isDirectory() ) {
			for ( File file : tempDir.listFiles() ) {
				if ( file.getName().startsWith( "jaildata." + forFaction.getId() + "." ) ) {
					file.delete();
				} else
					if ( file.getName().equals( "loc." + forFaction.getId() ) ) {
						file.delete();
					}
			}
		}
		
		// Warps
		tempFile = new File( Config.folderWarps, forFaction.getId() );
		if ( tempFile.exists() ) {
			tempFile.delete();
		}
		tempFile = null;
	}
	

}
