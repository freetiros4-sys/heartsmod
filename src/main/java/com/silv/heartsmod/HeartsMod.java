package com.silv.heartsmod;

import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.ai.attributes.AttributeInstance;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.entity.boss.enderdragon.EnderDragon;
import net.minecraft.world.entity.boss.wither.WitherBoss;
import net.minecraft.world.entity.player.Player;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.event.entity.living.LivingDeathEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.registries.ForgeRegistries;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Set;

@Mod(HeartsMod.MOD_ID)
public class HeartsMod {
    public static final String MOD_ID = "heartsmod";
    private static final Logger LOGGER = LoggerFactory.getLogger(MOD_ID);

    private static final double MEDIO_CORAZON = 1.0;
    private static final double UN_CORAZON = 2.0;

    private static final double VIDA_MINIMA = 2.0;
    private static final double VIDA_MAXIMA = 40.0;

    private static final Set<String> CATACLYSM_BOSS_IDS = Set.of(
            "netherite_monstrosity",
            "ender_guardian",
            "the_harbinger",
            "ancient_remnant",
            "the_leviathan",
            "scylla",
            "maledictus",
            "ignis"
    );

    public HeartsMod() {
        MinecraftForge.EVENT_BUS.register(this);
        LOGGER.info("HeartsMod cargado correctamente.");
    }

    @SubscribeEvent
    public void onLivingDeath(LivingDeathEvent event) {
        LivingEntity entity = event.getEntity();
        DamageSource source = event.getSource();
        Entity attacker = source.getEntity();

        if (entity instanceof Player victima) {
            if (attacker instanceof Player) {
                ajustarVidaMaxima(victima, -UN_CORAZON);
                victima.sendSystemMessage(Component.literal("§cTe mató un jugador: perdiste 1 corazón de vida máxima."));
            } else {
                ajustarVidaMaxima(victima, -MEDIO_CORAZON);
                victima.sendSystemMessage(Component.literal("§cPerdiste medio corazón de vida máxima."));
            }
        }

        if (attacker instanceof Player killer) {
            if (entity instanceof Player) {
                ajustarVidaMaxima(killer, UN_CORAZON);
                killer.sendSystemMessage(Component.literal("§a¡Mataste a un jugador! Ganaste 1 corazón de vida máxima."));
            } else if (esBoss(entity)) {
                ajustarVidaMaxima(killer, UN_CORAZON);
                killer.sendSystemMessage(Component.literal("§a¡Derrotaste a un boss! Ganaste 1 corazón de vida máxima."));
            }
        }
    }

    private boolean esBoss(LivingEntity entity) {
        if (entity instanceof WitherBoss || entity instanceof EnderDragon) {
            return true;
        }

        ResourceLocation id = ForgeRegistries.ENTITY_TYPES.getKey(entity.getType());
        if (id == null || !id.getNamespace().equals("cataclysm")) {
            return false;
        }
        return CATACLYSM_BOSS_IDS.contains(id.getPath());
    }

    private void ajustarVidaMaxima(Player player, double cambio) {
        AttributeInstance atributo = player.getAttribute(Attributes.MAX_HEALTH);
        if (atributo == null) return;

        double nuevaVida = atributo.getBaseValue() + cambio;
        nuevaVida = Math.max(VIDA_MINIMA, Math.min(VIDA_MAXIMA, nuevaVida));
        atributo.setBaseValue(nuevaVida);

        if (player.getHealth() > nuevaVida) {
            player.setHealth((float) nuevaVida);
        }
    }
}
