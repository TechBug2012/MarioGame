package net.extrillius.blocktest;

import org.bukkit.*;
import org.bukkit.block.Block;
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Entity;
import org.bukkit.entity.Fireball;
import org.bukkit.entity.Item;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.block.Action;
import org.bukkit.event.entity.EntityDamageByEntityEvent;
import org.bukkit.event.entity.PlayerDeathEvent;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.event.player.PlayerMoveEvent;
import org.bukkit.inventory.ItemStack;
import org.bukkit.plugin.java.JavaPlugin;

import java.util.HashMap;
import java.util.Map;

/**
 * Created by mattbstanciu on 1/26/17.
 */
public class Mario extends JavaPlugin implements Listener {
    public Boolean extraLife = false;
    public Boolean fire = false;

    public void onEnable() {
        getServer().getPluginManager().registerEvents(this, this);
    }

    @EventHandler
    public void move(PlayerMoveEvent e) {
        Player p = e.getPlayer();
        Location l = p.getLocation();
        Block b = l.add(0,2,0).getBlock();

        if (p.getLocation().subtract(0, 0.001, 0).getBlock().getType() == Material.AIR) {
            if (b.getY() - p.getEyeLocation().getY() <= 1 && b.getType() == Material.GOLD_BLOCK) {
                b.setType(Material.BRICK);

                ThingGenerator generator = new ThingGenerator();
                String thing = generator.generateThing();
                Material item;
                Item droppedItem;
                switch (thing) {
                    case "bigmushroom":
                        item = Material.RED_MUSHROOM;
                        p.getWorld().dropItem(l.add(0,3,0), new ItemStack(item));
                        break;
                    case "1upmushroom":
                        item = Material.BROWN_MUSHROOM;
                        p.getWorld().dropItem(l.add(0,3,0), new ItemStack(item));
                        break;
                    case "fireflower":
                        item = Material.YELLOW_FLOWER;
                        p.getWorld().dropItem(l.add(0,3,0), new ItemStack(item));
                        break;
                }
            }
        }

        if (p.getInventory().contains(Material.RED_MUSHROOM)) {
            p.getInventory().remove(Material.RED_MUSHROOM);
            p.setMaxHealth(40);
            p.setHealth(40);
            p.getWorld().playSound(p.getLocation(), Sound.LEVEL_UP, 10, 1);
        }
        else if (p.getInventory().contains(Material.BROWN_MUSHROOM)) {
            p.getWorld().playSound(p.getLocation(), Sound.LEVEL_UP, 10, 1);
            final Sound oneUp = Sound.LEVEL_UP;
            final World world = p.getWorld();
            final Location loc = p.getLocation();
            getServer().getScheduler().scheduleSyncDelayedTask(this, new Runnable() {
                @Override
                public void run() {
                    world.playSound(loc, oneUp, 10, 1);
                }
            }, 10L);
            extraLife = true;
            p.getInventory().remove(Material.BROWN_MUSHROOM);
        }
        else if (p.getInventory().contains(Material.YELLOW_FLOWER)) {
            p.getWorld().playSound(p.getLocation(), Sound.GHAST_FIREBALL, 10, 1);
            fire = true;
            p.getInventory().remove(Material.YELLOW_FLOWER);
        }

        if (extraLife) {
            p.setMaxHealth(40);
            p.setHealth(40);
        }
    }

    @EventHandler
    public void damage(EntityDamageByEntityEvent e) {
        if (e.getDamager() instanceof Player && e.getEntity() instanceof Player) {
            Player damager = (Player) e.getDamager();
            Entity damaged = e.getEntity();
            if (fire) {
                damaged.setFireTicks(100);
            }
        }
    }

    @EventHandler
    public void shootFire(PlayerInteractEvent e) {
        Player p = e.getPlayer();
        if (e.getAction() == Action.RIGHT_CLICK_AIR) {
            if (fire) {
                p.launchProjectile(Fireball.class).setVelocity(p.getLocation().getDirection().multiply(2));
            }
        }
    }

    @EventHandler
    public void onDeath(PlayerDeathEvent e) { //might not work
        Map<Player, Location> loc = new HashMap<>();
        Player p = e.getEntity();
        if (extraLife) {
            loc.put(p, p.getLocation());
            p.teleport(loc.get(p));
            p.setMaxHealth(40);
            p.setHealth(40);
        }
        else {
            extraLife = false;
            fire = false;
            p.resetMaxHealth();
        }
    }

    public boolean onCommand(CommandSender sender, Command cmd, String label, String[] args) {
        Player p = (Player) sender;
        if (cmd.getName().equalsIgnoreCase("disablepowerups")) { //temporary until I add other ways to deactivate powerups
            fire = false;
            extraLife = false;
            p.setMaxHealth(20);
            p.setHealth(20);
            p.sendMessage(ChatColor.GREEN + "Powerups disabled");
        }
        if (cmd.getName().equalsIgnoreCase("powerupstatus")) {
            StringBuilder builder = new StringBuilder();

            if (fire) {
                builder.append("fire ");
            }
            else if (extraLife) {
                builder.append("1up ");
            }
            else if (p.getHealth() > 20) {
                builder.append("redmushroom ");
            }
            else {
                p.sendMessage(ChatColor.GREEN + "nothing");
            }
            p.sendMessage(builder.toString());
        }
        return true;
    }

    //TODO flag jump
}
