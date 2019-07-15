package com.sekwah.explosion;

import org.bukkit.FluidCollisionMode;
import org.bukkit.Location;
import org.bukkit.Particle;
import org.bukkit.block.Block;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.metadata.FixedMetadataValue;
import org.bukkit.plugin.java.JavaPlugin;
import org.bukkit.util.Vector;

public final class Explosion extends JavaPlugin implements Listener {

    final static int TICKS_PER_SECOND = 20;
    final static String EXPLODING_META = "explosion";
    final static String EXPLODING_STATE = "exploding_state";
    final static String STARTING_TICK = "starting_tick";

    @Override
    public void onEnable() {
        this.getServer().getOnlinePlayers().forEach(player -> player.removeMetadata(EXPLODING_META, this));
        //this.getServer().getOnlinePlayers().forEach(player -> player.setMetadata(EXPLODING_STATE, new FixedMetadataValue(this, "swirling")));
        //this.getServer().getOnlinePlayers().forEach(player -> player.setMetadata(EXPLODING_META, new FixedMetadataValue(this, "exploding")));
        this.getServer().getPluginManager().registerEvents(this, this);
        this.getServer().getScheduler().scheduleSyncRepeatingTask(this, () -> this.getServer().getOnlinePlayers().forEach(Explosion::updateSpell),0,0);

    }

    @Override
    public void onDisable() {
        // Plugin shutdown logic
    }

    @EventHandler
    public void onInteract(PlayerInteractEvent event) {
        Player player = event.getPlayer();

        System.out.println("INTERACT");

        if(!player.hasMetadata(EXPLODING_META)) {
            player.playSound(player.getLocation(), "magic.explosion", 1, 1);
            player.setMetadata(EXPLODING_META, new FixedMetadataValue(this, "exploding"));
            player.setMetadata(EXPLODING_STATE, new FixedMetadataValue(this, "gathering"));
            player.setMetadata(STARTING_TICK, new FixedMetadataValue(this, player.getWorld().getFullTime()));
            this.getServer().getScheduler().scheduleSyncDelayedTask(this, () -> {
                int particleCount = 40;
                double angleApart = Math.PI * 2 / particleCount;
                Vector vector = new Vector(2, 0, 0);
                Location playerLoc = player.getLocation();
                for (int i = 0; i < particleCount; i++) {
                    vector.rotateAroundY(angleApart);
                    Location spawnLoc = playerLoc.clone().subtract(vector);
                    player.spawnParticle(Particle.CLOUD, spawnLoc.add(0, 0.4, 0), 0, vector.getX(), 0, vector.getZ(), 0.04, null);
                }
            }, 7 * TICKS_PER_SECOND);
            this.getServer().getScheduler().scheduleSyncDelayedTask(this, () ->
                            player.setMetadata(EXPLODING_STATE, new FixedMetadataValue(this, "more_gathering"))
                    , 7 * TICKS_PER_SECOND);
            this.getServer().getScheduler().scheduleSyncDelayedTask(this, () -> {
                Location playerLoc = player.getEyeLocation();
                double distance = 5;
                for (int i = 0; i < 50; i++) {
                    player.spawnParticle(Particle.CLOUD, playerLoc, 0, distance * (Math.random() - 0.5), distance * (Math.random() - 0.5), distance * (Math.random() - 0.5), 0.04, null);
                }
                player.setMetadata(EXPLODING_STATE, new FixedMetadataValue(this, "even_more_gathering"));
            }, (long) (9.2 * TICKS_PER_SECOND));
            this.getServer().getScheduler().scheduleSyncDelayedTask(this, () ->
                            player.setMetadata(EXPLODING_STATE, new FixedMetadataValue(this, "swirling"))
                    , 10 * TICKS_PER_SECOND);
            this.getServer().getScheduler().scheduleSyncDelayedTask(this, () ->
                            player.setMetadata(EXPLODING_STATE, new FixedMetadataValue(this, "even_more_gathering"))
                    , 15 * TICKS_PER_SECOND);
            this.getServer().getScheduler().scheduleSyncDelayedTask(this, () -> player.removeMetadata(EXPLODING_META, this), 19 * TICKS_PER_SECOND);
            this.getServer().getScheduler().scheduleSyncDelayedTask(this, () -> {
                Block target = player.getTargetBlockExact(64, FluidCollisionMode.ALWAYS);
                if(target != null) {
                    player.getWorld().createExplosion(target.getLocation(), 20);
                }
            }, 21 * TICKS_PER_SECOND);
        }
    }

    private static void updateSpell(Player player) {
        if(player.hasMetadata(EXPLODING_META) && player.hasMetadata(EXPLODING_STATE)) {
            int amount = 0;
            Location playerLoc = player.getLocation();
            switch (player.getMetadata(EXPLODING_STATE).get(0).asString()) {
                case "swirling":
                    amount -= 4;
                    Vector vector = new Vector(Math.sin(System.currentTimeMillis() / 200d), 0, -0.5);
                    vector.rotateAroundY(Math.toRadians(-playerLoc.getYaw()));
                    player.spawnParticle(Particle.CRIT_MAGIC, player.getEyeLocation().add(vector), 0, 0,0.1,0, 0.06, null);
                case "even_more_gathering":
                    amount += 14;
                    for(int i = 0; i<amount;i++) {
                        Location fireLoc = getRandomLocation(player.getEyeLocation(), 10);
                        Location playerDir = player.getEyeLocation().clone().subtract(fireLoc);
                        player.spawnParticle(Particle.FLAME, fireLoc, 0, playerDir.getX(), playerDir.getY(), playerDir.getZ(), 0.06, null);
                    }
                    break;
                case "more_gathering":
                    amount += 5;
                case "gathering":
                    amount += 1;
                    for(int i = 0; i<amount;i++) {
                        Location fireLoc = getRandomLocation(player.getEyeLocation(), 10);
                        Location playerDir = player.getEyeLocation().clone().subtract(fireLoc);
                        player.spawnParticle(Particle.FLAME, fireLoc, 0, playerDir.getX(), playerDir.getY(), playerDir.getZ(), 0.02, null);
                    }
                    break;
            }
        }
    }

    private static Location getRandomLocation(Location loc, double distance) {
        return loc.clone().add(distance * (Math.random() - 0.5), distance * (Math.random() - 0.5), distance * (Math.random() - 0.5));
    }
}
