package xianxiacraft.xianxiacraft.handlers;

import org.bukkit.*;
import org.bukkit.block.Block;
import org.bukkit.block.Container;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;

import org.bukkit.event.block.BlockBreakEvent;
import org.bukkit.event.player.PlayerInteractEvent;

import org.bukkit.event.player.PlayerMoveEvent;
import org.bukkit.util.Vector;
import xianxiacraft.xianxiacraft.XianxiaCraft;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;


import static xianxiacraft.xianxiacraft.QiManagers.ManualManager.getManual;
import static xianxiacraft.xianxiacraft.QiManagers.PointManager.getStage;
import static xianxiacraft.xianxiacraft.QiManagers.QiManager.getQi;
import static xianxiacraft.xianxiacraft.QiManagers.QiManager.subtractQi;
import static xianxiacraft.xianxiacraft.QiManagers.ScoreboardManager1.updateScoreboard;
import static xianxiacraft.xianxiacraft.QiManagers.TechniqueManager.getAuraBool;
import static xianxiacraft.xianxiacraft.QiManagers.TechniqueManager.getMoveBool;
import static xianxiacraft.xianxiacraft.util.FreezeEffect.createIce;
import static xianxiacraft.xianxiacraft.util.FreezeEffect.removeIce;


public class MoveEvents implements Listener {

    XianxiaCraft plugin;

    public MoveEvents(XianxiaCraft plugin){
        Bukkit.getPluginManager().registerEvents(this,plugin);
        this.plugin = plugin;
    }

    @EventHandler
    public void onPlayerMine(BlockBreakEvent event){
        Player player = event.getPlayer();
        //so that players cant infinitely farm their aura blocks.
        if(getAuraBool(player)){
            event.setCancelled(true);
        }

        Block block = event.getBlock();
        //if block is in any of the originalBlocks player maps that is not the player making this change, dont change it
        for (UUID playerId : originalBlocks.keySet()) {
            if (originalBlocks.get(playerId).containsKey(block)) {
                event.setCancelled(true);
            }
        }
    }

    private Map<UUID,Map<Block,Material>> originalBlocks = new HashMap<>();
    // Map<Block,Material> originalBlocks = new HashMap<>();

    private Map<UUID,Boolean> isCleanedMap = new HashMap<>();
    //boolean isCleaned = true;

    @EventHandler
    public void onPlayerMove(PlayerMoveEvent event){
        Player player = event.getPlayer();

        if(getMoveBool(player)){
            String playerManual = getManual(player);

            if(playerManual.equals("Ice Manual")){
                if(!player.isSneaking()) {

                    Location[] locations = new Location[9];

                    Location playerLocation = player.getLocation();
                    Location baseLocation = playerLocation.subtract(1, 1, 1);

                    int index = 0;
                    for (int x = 0; x < 3; x++) {
                        for (int z = 0; z < 3; z++) {
                            locations[index] = baseLocation.clone().add(x, 0, z);
                            index++;
                        }
                    }

                    for (Location location : locations) {
                        createIce(location);
                        Bukkit.getScheduler().runTaskLater(plugin, () -> removeIce(location), 100);
                    }
                }
            }
        }

        //qiaura stuff

        if(getAuraBool(player)){
            String playerManual = getManual(player);
            isCleanedMap.put(player.getUniqueId(),false);
            //isCleaned = false;

            Material blockType1;
            Material blockType2;

            switch(playerManual){
                case "Ice Manual":
                    blockType1 = Material.PACKED_ICE;
                    blockType2 = Material.BLUE_ICE;
                    break;
                case "Sugar Fiend":
                    blockType1 = Material.NETHERRACK;
                    blockType2 = Material.NETHER_QUARTZ_ORE;
                    break;
                case "Fatty Manual":
                    blockType1 = Material.DIRT;
                    blockType2 = Material.COARSE_DIRT;
                    break;
                case "Fungal Manual":
                    blockType1 = Material.MYCELIUM;
                    blockType2 = Material.MYCELIUM;
                    break;
                case "Ironskin Manual":
                    blockType1 = Material.RAW_IRON_BLOCK;
                    blockType2 = Material.IRON_BLOCK;
                    break;
                case "LightningManual":
                    blockType1 = Material.RAW_COPPER_BLOCK;
                    blockType2 = Material.COPPER_BLOCK;
                    break;
                case "Phoenix Manual":
                    blockType1 = Material.HONEYCOMB_BLOCK;
                    blockType2 = Material.MAGMA_BLOCK;
                    break;
                case "Poison Manual":
                    blockType1 = Material.WARPED_HYPHAE;
                    blockType2 = Material.MANGROVE_ROOTS;
                    break;
                case "Space Manual":
                    blockType1 = Material.BLACK_CONCRETE;
                    blockType2 = Material.END_STONE;
                    break;
                case "Demonic Manual":
                    blockType1 = Material.SCULK;
                    blockType2 = Material.SCULK;
                    break;
                default:
                    blockType1 = Material.AIR;
                    blockType2 = Material.AIR;
            }


            Location center = player.getLocation().clone().subtract(0,1,0);

            int radius = getStage(player)/2;

            World world = center.getWorld();
            int centerX = center.getBlockX();
            int centerY = center.getBlockY();
            int centerZ = center.getBlockZ();


            //revert changes
            Map<Block, Material> playerOriginalBlocks = originalBlocks.computeIfAbsent(player.getUniqueId(), k -> new HashMap<>());

            for (Map.Entry<Block, Material> entry : playerOriginalBlocks.entrySet()) {
                Block block = entry.getKey();
                Material originalType = entry.getValue();
                block.setType(originalType);
            }

            playerOriginalBlocks.clear();

            for (int x = centerX - radius; x <= centerX + radius; x++) {
                for (int y = centerY - radius; y <= centerY + radius; y++) {
                    for (int z = centerZ - radius; z <= centerZ + radius; z++) {
                        int i = (centerX - x) * (centerX - x) + (centerY - y) * (centerY - y) + (centerZ - z) * (centerZ - z);
                        if (i <= radius*radius) {
                            assert world != null;
                            Block block = world.getBlockAt(x, y, z);

                            //if block is container then skip it
                            if(block.getState() instanceof Container){
                                continue;
                            }

                            //if block is in any of the originalBlocks player maps that is not the player making this change, dont change it
                            boolean foundInOtherPlayerMap = false;
                            for (UUID playerId : originalBlocks.keySet()) {
                                if (!playerId.equals(player.getUniqueId()) && originalBlocks.get(playerId).containsKey(block)) {
                                    foundInOtherPlayerMap = true;
                                    break;
                                }
                            }
                            if(!foundInOtherPlayerMap){
                                originalBlocks.get(player.getUniqueId()).put(block,block.getType());
                                // make sure it isn't air or bedrock
                                if(block.getType() != Material.AIR && block.getType() != Material.CAVE_AIR && block.getType() != Material.BEDROCK && block.getType() != blockType1 && block.getType() != blockType2){

                                    int blockOrNoBlock = (int) (Math.random() * 5);

                                    if(blockOrNoBlock == 1){
                                        block.setType(blockType2);
                                    } else {
                                        block.setType(blockType1);
                                    }
                                }
                            }
                        }

                    }
                }
            }



        }
        if((!getAuraBool(player)) && (!isCleanedMap.getOrDefault(player.getUniqueId(),true))){
            //revert changes
            Map<Block, Material> playerOriginalBlocks = originalBlocks.computeIfAbsent(player.getUniqueId(), k -> new HashMap<>());

            //Map<Block, Material> playerOriginalBlocks = originalBlocks.get(playerId);

            for (Map.Entry<Block, Material> entry : playerOriginalBlocks.entrySet()) {
                Block block = entry.getKey();
                Material originalType = entry.getValue();
                block.setType(originalType);
            }

            playerOriginalBlocks.clear();
            /*
            for (Block block1 : originalBlocks.keySet()) {
                block1.setType(originalBlocks.get(block1));
            }
            originalBlocks.clear();
             */
            isCleanedMap.put(player.getUniqueId(),true);
            //isCleaned = true;
        }

    }

    @EventHandler
    public void onRightClick(PlayerInteractEvent event){

        Player player = event.getPlayer();

        if(getMoveBool(player)) {
            String playerManual = getManual(player);
            switch (playerManual) {
                case "Space Manual":
                    if (event.getAction().toString().contains("RIGHT_CLICK")) { //SPACE MANUAL TP DASH
                        if (!(getQi(player) >= 50)) {
                            player.sendMessage(ChatColor.GOLD + "You do not have enough qi to teleport.");
                            return;
                        }
                        subtractQi(player, 50);
                        updateScoreboard(player);

                        Vector direction = player.getLocation().getDirection();

                        double teleportDistance = 3 * getStage(player) + 5;

                        Vector teleportVector = direction.multiply(teleportDistance);
                        player.teleport(player.getLocation().add(teleportVector));
                    }
                    break;
                case "LightningManual":  // LIGHTNING MANUAL TP DASH
                    if (event.getAction().toString().contains("RIGHT_CLICK")) {
                        if (!(getQi(player) >= 20)) {
                            player.sendMessage(ChatColor.GOLD + "You do not have enough qi to dash.");
                            return;
                        }
                        subtractQi(player, 20);
                        updateScoreboard(player);

                        Vector direction = player.getLocation().getDirection();
                        direction.setY(0).normalize(); //so they cant fly

                        double teleportDistance = 3 * getStage(player) + 5;

                        Block blockingBlock = null;

                        // Check for any blocks in the teleport path
                        for (double distance = 0; distance <= teleportDistance; distance += 0.5) {
                            Vector checkPosition = player.getEyeLocation().toVector().add(direction.clone().multiply(distance));
                            Location checkLocation = checkPosition.toLocation(player.getWorld());
                            Block block = checkLocation.getBlock();
                            if (!block.isPassable()) {
                                blockingBlock = block;
                                break; //break loop if tp is blocked
                            }
                        }

                        if (blockingBlock != null) {
                            Block highestSolidBlock = null;
                            for (int y = blockingBlock.getY() + 1; y <= player.getWorld().getMaxHeight(); y++) {
                                Block aboveBlock = blockingBlock.getWorld().getBlockAt(blockingBlock.getX(), y, blockingBlock.getZ());
                                if (aboveBlock.getType().isSolid()) {
                                    highestSolidBlock = aboveBlock;
                                } else {
                                    break;
                                }
                            }

                            if (highestSolidBlock != null) {
                                player.teleport(highestSolidBlock.getLocation());
                                player.getWorld().playSound(player.getLocation(), Sound.ENTITY_LIGHTNING_BOLT_THUNDER, 1.0f, 1.0f);
                            }
                        } else {
                            Vector teleportVector = direction.multiply(teleportDistance);
                            player.teleport(player.getLocation().add(teleportVector));
                            player.getWorld().playSound(player.getLocation(), Sound.ENTITY_LIGHTNING_BOLT_THUNDER, 1.0f, 1.0f);
                        }
                    }
                    break;

                case "Phoenix Manual":
                    if (event.getAction().toString().contains("RIGHT_CLICK")) { //SPACE MANUAL TP DASH
                        if (!(getQi(player) >= 30)) {
                            player.sendMessage(ChatColor.GOLD + "You do not have enough qi to dash.");
                            return;
                        }
                        subtractQi(player, 30);
                        updateScoreboard(player);

                        Vector direction = player.getLocation().getDirection();
                        direction.setY(1).normalize(); //to launch into air

                        double strength = 1 + getStage(player)/3.0; //pushing strength scales with stage
                        Vector velocity = direction.multiply(strength);
                        //velocity.setY(1); //pushing height

                        player.setVelocity(velocity);
                        break;
                    }
                case "Poison Manual":
                case "Demonic Manual":
                case "Ironskin Manual":
                case "Sugar Fiend":
                    if (event.getAction().toString().contains("RIGHT_CLICK")) {
                        if (!(getQi(player) >= 20)) {
                            player.sendMessage(ChatColor.GOLD + "You do not have enough qi to dash.");
                            return;
                        }
                        subtractQi(player, 20);
                        updateScoreboard(player);

                        Vector direction = player.getLocation().getDirection();
                        direction.setY(0).normalize(); //so they don't fly

                        double strength = 1 + getStage(player)/3.0; //pushing strength scales with stage
                        Vector velocity = direction.multiply(strength);
                        //velocity.setY(1); //pushing height

                        player.setVelocity(velocity);
                        break;
                    }
            } //end of switch statement

        }
    }




}
