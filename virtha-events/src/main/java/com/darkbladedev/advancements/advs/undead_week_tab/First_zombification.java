package com.darkbladedev.advancements.advs.undead_week_tab;

import com.fren_gor.ultimateAdvancementAPI.util.AdvancementKey;
import com.darkbladedev.advancements.advs.AdvancementTabNamespaces;
import com.fren_gor.ultimateAdvancementAPI.advancement.display.AdvancementDisplay;
import com.fren_gor.ultimateAdvancementAPI.advancement.BaseAdvancement;
import org.bukkit.Material;
import com.fren_gor.ultimateAdvancementAPI.advancement.display.AdvancementFrameType;
import com.fren_gor.ultimateAdvancementAPI.advancement.Advancement;

public class First_zombification extends BaseAdvancement  {

  public static AdvancementKey KEY = new AdvancementKey(AdvancementTabNamespaces.undead_week_tab_NAMESPACE, "first_zombification");


  public First_zombification(Advancement parent) {
    super(KEY.getKey(), new AdvancementDisplay(Material.ROTTEN_FLESH, "§dSe pudrió todo!", AdvancementFrameType.GOAL, true, false, 1f, 0f , "§7Sé zombificado por primera vez."), parent, 1);
  }
}