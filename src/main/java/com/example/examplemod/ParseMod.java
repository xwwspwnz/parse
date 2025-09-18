package com.example.examplemod;

import net.minecraft.command.CommandSource;
import net.minecraft.command.Commands;
import net.minecraft.entity.player.ServerPlayerEntity;
import net.minecraft.server.MinecraftServer;
import net.minecraft.util.text.StringTextComponent;
import net.minecraftforge.event.RegisterCommandsEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.io.BufferedWriter;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.List;

@Mod(ParseMod.MOD_ID)
public class ParseMod {
    public static final String MOD_ID = "parsemod";
    private static final Logger LOGGER = LogManager.getLogger();

    public ParseMod() {
        LOGGER.info("ParseMod initialized");
    }

    @Mod.EventBusSubscriber(modid = MOD_ID, bus = Mod.EventBusSubscriber.Bus.FORGE)
    public static class CommandRegistration {
        @SubscribeEvent
        public static void onRegisterCommands(RegisterCommandsEvent event) {
            event.getDispatcher().register(
                    Commands.literal("parse")
                            .requires(source -> source.hasPermission(2))
                            .executes(context -> runParse(context.getSource()))
            );
        }

        private static int runParse(CommandSource source) {
            MinecraftServer server = source.getServer();
            List<ServerPlayerEntity> players = server.getPlayerList().getPlayers();
            Path outputPath = server.getServerDirectory().toPath().resolve("player_list.txt");

            try (BufferedWriter writer = Files.newBufferedWriter(outputPath, StandardCharsets.UTF_8)) {
                for (ServerPlayerEntity player : players) {
                    writer.write(player.getGameProfile().getName());
                    writer.newLine();
                }
            } catch (IOException e) {
                LOGGER.error("Failed to write player list", e);
                source.sendFailure(new StringTextComponent("Error writing player_list.txt: " + e.getMessage()));
                return 0;
            }

            source.sendSuccess(new StringTextComponent(String.format(
                    "File player_list.txt updated successfully. Players online: %d",
                    players.size())), false);
            return players.size();
        }
    }
}
