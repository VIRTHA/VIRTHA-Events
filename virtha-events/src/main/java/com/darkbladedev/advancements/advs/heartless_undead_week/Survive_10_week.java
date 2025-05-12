package com.darkbladedev.advancements.advs.heartless_undead_week;

import com.fren_gor.ultimateAdvancementAPI.util.AdvancementKey;
import com.darkbladedev.advancements.advs.AdvancementTabNamespaces;
import com.fren_gor.ultimateAdvancementAPI.advancement.display.AdvancementDisplay;
import com.fren_gor.ultimateAdvancementAPI.advancement.BaseAdvancement;
import org.bukkit.Material;
import com.fren_gor.ultimateAdvancementAPI.advancement.display.AdvancementFrameType;
import com.fren_gor.ultimateAdvancementAPI.advancement.Advancement;

public class Survive_10_week extends BaseAdvancement  {

  public static AdvancementKey KEY = new AdvancementKey(AdvancementTabNamespaces.heartless_undead_week_NAMESPACE, "survive_10_week");


  public Survive_10_week(Advancement parent) {
    super(KEY.getKey(), new AdvancementDisplay(Material.CLOCK, "§d§lSuperviviente veterano", AdvancementFrameType.TASK, true, true, 1f, 2f , "§fSobrevive 10 veces a la", "§a§lSemana de los no-muertos"), parent, 1);
  }
}