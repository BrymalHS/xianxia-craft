package xianxiacraft.xianxiacraft.handlers.Manuals;
import org.bukkit.ChatColor;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import xianxiacraft.xianxiacraft.QiManagers.PointManager;
import xianxiacraft.xianxiacraft.QiManagers.ScoreboardManager1;
import xianxiacraft.xianxiacraft.util.CountNearbyBlocks;

import static xianxiacraft.xianxiacraft.QiManagers.PointManager.getPoints;
import static xianxiacraft.xianxiacraft.QiManagers.PointManager.getStage;

public class IceManual extends Manual {


    public IceManual(){
        super("Ice Manual",0.01,6,4);

    }

    public static void iceManualPointIncrement(ItemStack itemInHand, Player player){
        int stage = getStage(player);
        int points = getPoints(player);

        if (itemInHand.getType() == Material.ICE){
            if(points + 1 ==  (int) (20 * Math.pow(2,(stage+1)) - 20)){
                if(!(CountNearbyBlocks.countNearbyBlocks(player,Material.BLUE_ICE) >= (Math.pow(2,stage-1)))){
                    //send message "Breakthrough requirement not met. Consult your manual."
                    player.sendMessage(ChatColor.GOLD + "Breakthrough requirement not met. Consult your manual.");
                    return;
                }
                itemInHand.setAmount(itemInHand.getAmount()-1);
                PointManager.addPoints(player,1);
                ScoreboardManager1.updateScoreboard(player);
                return;
            }

            itemInHand.setAmount(itemInHand.getAmount()-1);
            PointManager.addPoints(player,1);
            ScoreboardManager1.updateScoreboard(player);
        }
    }



}


