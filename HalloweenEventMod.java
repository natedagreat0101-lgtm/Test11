package com.halloween;

import net.fabricmc.api.ClientModInitializer;
import net.fabricmc.fabric.api.client.event.lifecycle.v1.ClientTickEvents;
import net.fabricmc.fabric.api.client.rendering.v1.HudRenderCallback;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.font.TextRenderer;
import net.minecraft.client.sound.PositionedSoundInstance;
import net.minecraft.client.sound.SoundInstance;
import net.minecraft.sound.SoundEvent;
import net.minecraft.util.Identifier;

import java.time.DayOfWeek;
import java.time.Duration;
import java.time.LocalDateTime;
import java.time.temporal.TemporalAdjusters;

public class HalloweenEventMod implements ClientModInitializer {
    public static final String MOD_ID = "halloween";
    public static final Identifier SPOOKY_SOUND_ID = new Identifier(MOD_ID, "spooky_music");
    public static final SoundEvent SPOOKY_MUSIC = SoundEvent.of(SPOOKY_SOUND_ID);

    private static SoundInstance currentTrack = null;

    @Override
    public void onInitializeClient() {
        HudRenderCallback.EVENT.register((drawContext, tickDelta) -> {
            MinecraftClient client = MinecraftClient.getInstance();
            if (client == null || client.world == null) return;

            LocalDateTime now = LocalDateTime.now();
            LocalDateTime nextSundayNoon = now.with(TemporalAdjusters.nextOrSame(DayOfWeek.SUNDAY))
                                               .withHour(12).withMinute(0).withSecond(0).withNano(0);

            if (now.isAfter(nextSundayNoon)) {
                nextSundayNoon = nextSundayNoon.plusWeeks(1);
            }

            Duration remaining = Duration.between(now, nextSundayNoon);
            long days = remaining.toDays();
            long hours = remaining.toHoursPart();
            long minutes = remaining.toMinutesPart();
            long seconds = remaining.toSecondsPart();

            String text = String.format("§6§lHalloween Event in: §e%dd %02dh %02dm %02ds", days, hours, minutes, seconds);

            TextRenderer textRenderer = client.textRenderer;
            int screenWidth = client.getWindow().getScaledWidth();
            int textWidth = textRenderer.getWidth(text);

            int x = (screenWidth - textWidth) / 2;
            int y = 8;

            drawContext.drawTextWithShadow(textRenderer, text, x, y, 0xFFFFFF);
        });

        ClientTickEvents.END_CLIENT_TICK.register(client -> {
            if (client.world == null) return;

            client.getMusicTracker().stop();

            if (currentTrack == null || !client.getSoundManager().isPlaying(currentTrack)) {
                currentTrack = PositionedSoundInstance.master(SPOOKY_MUSIC, 1.0f, 1.0f);
                client.getSoundManager().play(currentTrack);
            }
        });
    }
}
