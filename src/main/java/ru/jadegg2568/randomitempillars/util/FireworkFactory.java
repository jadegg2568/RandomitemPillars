package ru.jadegg2568.randomitempillars.util;

import org.bukkit.Color;
import org.bukkit.FireworkEffect;
import org.bukkit.Location;
import org.bukkit.entity.Firework;
import org.bukkit.inventory.meta.FireworkMeta;

import java.util.Random;

public class FireworkFactory {

    public static void spawnFirework(Location loc) {
        Color[] colors = new Color[] {Color.RED, Color.ORANGE, Color.YELLOW, Color.LIME, Color.AQUA, Color.BLUE, Color.PURPLE};
        Firework firework = loc.getWorld().spawn(loc, Firework.class);
        FireworkMeta meta = firework.getFireworkMeta();
        meta.setPower(1);
        meta.addEffect(FireworkEffect.builder()
                .with(FireworkEffect.Type.CREEPER)
                .withColor(colors[new Random().nextInt(colors.length)])
                .build());
        firework.setFireworkMeta(meta);
    }
}
