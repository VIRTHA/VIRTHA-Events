package com.darkbladedev.advancements.advs.heartless_init;

import com.fren_gor.ultimateAdvancementAPI.util.AdvancementKey;
import com.darkbladedev.advancements.advs.AdvancementTabNamespaces;
import com.fren_gor.ultimateAdvancementAPI.advancement.display.AdvancementDisplay;
import com.fren_gor.ultimateAdvancementAPI.advancement.BaseAdvancement;
import org.bukkit.Material;
import com.fren_gor.ultimateAdvancementAPI.advancement.display.AdvancementFrameType;
import com.fren_gor.ultimateAdvancementAPI.advancement.Advancement;

public class Week_survive extends BaseAdvancement  {

  public static AdvancementKey KEY = new AdvancementKey(AdvancementTabNamespaces.heartless_init_NAMESPACE, "week_survive");


  public Week_survive(Advancement parent) {
    super(KEY.getKey(), new AdvancementDisplay(Material.RED_BED, "Superviviente por temporada", AdvancementFrameType.TASK, true, true, 1f, 0f , "Sobrevive una semana", "entera en §cHeartless§f."), parent, 1);
  }
}