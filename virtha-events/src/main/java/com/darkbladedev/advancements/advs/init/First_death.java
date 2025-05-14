package com.darkbladedev.advancements.advs.init;

import com.fren_gor.ultimateAdvancementAPI.util.AdvancementKey;
import com.darkbladedev.advancements.advs.AdvancementTabNamespaces;
import com.fren_gor.ultimateAdvancementAPI.advancement.display.AdvancementDisplay;
import com.fren_gor.ultimateAdvancementAPI.advancement.BaseAdvancement;
import org.bukkit.Material;
import com.fren_gor.ultimateAdvancementAPI.advancement.display.AdvancementFrameType;
import com.fren_gor.ultimateAdvancementAPI.advancement.Advancement;

public class First_death extends BaseAdvancement  {

  public static AdvancementKey KEY = new AdvancementKey(AdvancementTabNamespaces.init_NAMESPACE, "first_death");


  public First_death(Advancement parent) {
    super(KEY.getKey(), new AdvancementDisplay(Material.SKELETON_SKULL, "§dPrimera muerte", AdvancementFrameType.GOAL, true, true, 1f, 0f , "§7Muere por primera vez en §cHeartless"), parent, 1);
  }
}