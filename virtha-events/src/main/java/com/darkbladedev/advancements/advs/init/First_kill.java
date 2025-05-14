package com.darkbladedev.advancements.advs.init;

import com.fren_gor.ultimateAdvancementAPI.util.AdvancementKey;
import com.darkbladedev.advancements.advs.AdvancementTabNamespaces;
import com.fren_gor.ultimateAdvancementAPI.advancement.display.AdvancementDisplay;
import com.fren_gor.ultimateAdvancementAPI.advancement.BaseAdvancement;
import org.bukkit.Material;
import com.fren_gor.ultimateAdvancementAPI.advancement.display.AdvancementFrameType;
import com.fren_gor.ultimateAdvancementAPI.advancement.Advancement;

public class First_kill extends BaseAdvancement  {

  public static AdvancementKey KEY = new AdvancementKey(AdvancementTabNamespaces.init_NAMESPACE, "first_kill");


  public First_kill(Advancement parent) {
    super(KEY.getKey(), new AdvancementDisplay(Material.IRON_SWORD, "§dPrimera kill", AdvancementFrameType.GOAL, true, true, 1f, 1f , "§7Mata a un jugador por primera", "§7vez en §cHeartless"), parent, 1);
  }
}