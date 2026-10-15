package com.frikifest.frikijobs.listener;

import com.frikifest.frikijobs.FrikiJobs;
import com.frikifest.frikijobs.gui.JobsGUI;
import com.frikifest.frikijobs.model.Job;
import org.bukkit.ChatColor;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.inventory.ItemStack;

public class GUIListener implements Listener {

    @EventHandler
    public void onInventoryClick(InventoryClickEvent event) {
        if (!(event.getWhoClicked() instanceof Player player)) return;
        String title = event.getView().getTitle();

        if (title.equals(ChatColor.DARK_GREEN + "💼 Selección de Trabajos")) {
            event.setCancelled(true);
            ItemStack clickedItem = event.getCurrentItem();
            if (clickedItem == null || clickedItem.getType() == Material.AIR) return;

            int slot = event.getSlot();
            String jobId = null;
            if (slot == 10) jobId = "minero";
            else if (slot == 11) jobId = "granjero";
            else if (slot == 12) jobId = "hechicero";
            else if (slot == 13) jobId = "pescador";
            else if (slot == 14) jobId = "guerrero";
            else if (slot == 15) jobId = "lenador";
            else if (slot == 16) jobId = "alquimista";
            else if (slot == 26) {
                String currentJob = FrikiJobs.getInstance().getPlayerJobManager().getPlayerJob(player.getUniqueId());
                if (currentJob != null) {
                    double penalty = FrikiJobs.getInstance().getConfig().getDouble("quit_penalty", 50.0);
                    if (FrikiJobs.getInstance().getEconomyManager().getBalance(player) >= penalty) {
                        FrikiJobs.getInstance().getEconomyManager().withdrawPlayer(player, penalty);
                        FrikiJobs.getInstance().getPlayerJobManager().setPlayerJob(player.getUniqueId(), null);
                        player.sendMessage(ChatColor.RED + "[FrikiJobs] Has abandonado tu trabajo. Se te han descontado $" + penalty + ".");
                        player.closeInventory();
                    } else {
                        player.sendMessage(ChatColor.RED + "[FrikiJobs] No tienes suficiente dinero para pagar la multa de $" + penalty + ".");
                    }
                }
                return;
            }

            if (jobId != null) {
                JobsGUI.openJobDetailMenu(player, jobId);
            }
        } else if (title.startsWith(ChatColor.DARK_BLUE + "📋 ")) {
            event.setCancelled(true);
            ItemStack clickedItem = event.getCurrentItem();
            if (clickedItem == null || clickedItem.getType() == Material.AIR) return;

            int slot = event.getSlot();
            if (slot == 18) {
                JobsGUI.openJobsMenu(player);
                return;
            }

            if (slot == 22 && clickedItem.getType() == Material.EMERALD) {
                String jobNameClean = ChatColor.stripColor(event.getView().getTitle()).replace("📋 ", "").toLowerCase();
                // Buscar el id real
                String targetJobId = null;
                for (Job j : FrikiJobs.getInstance().getJobManager().getJobs().values()) {
                    if (j.getName().equalsIgnoreCase(jobNameClean)) {
                        targetJobId = j.getId();
                        break;
                    }
                }

                if (targetJobId != null) {
                    String currentJob = FrikiJobs.getInstance().getPlayerJobManager().getPlayerJob(player.getUniqueId());
                    if (currentJob != null) {
                        double penalty = FrikiJobs.getInstance().getConfig().getDouble("quit_penalty", 50.0);
                        if (FrikiJobs.getInstance().getEconomyManager().getBalance(player) < penalty) {
                            player.sendMessage(ChatColor.RED + "[FrikiJobs] No tienes suficiente dinero para pagar la multa de cambio ($" + penalty + ").");
                            return;
                        }
                        FrikiJobs.getInstance().getEconomyManager().withdrawPlayer(player, penalty);
                    }

                    FrikiJobs.getInstance().getPlayerJobManager().setPlayerJob(player.getUniqueId(), targetJobId);
                    Job newJobObj = FrikiJobs.getInstance().getJobManager().getJob(targetJobId);
                    player.sendMessage(ChatColor.GREEN + "[FrikiJobs] ¡Te has unido exitosamente al trabajo de " + (newJobObj != null ? newJobObj.getName() : targetJobId) + "!");
                    player.closeInventory();
                }
            }
        }
    }
}
