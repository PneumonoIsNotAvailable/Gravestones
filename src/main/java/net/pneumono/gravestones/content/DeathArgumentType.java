package net.pneumono.gravestones.content;

import com.mojang.brigadier.StringReader;
import com.mojang.brigadier.arguments.ArgumentType;
import com.mojang.brigadier.context.CommandContext;
import com.mojang.brigadier.exceptions.CommandSyntaxException;
import com.mojang.brigadier.exceptions.DynamicCommandExceptionType;
import com.mojang.brigadier.suggestion.Suggestions;
import com.mojang.brigadier.suggestion.SuggestionsBuilder;
import net.minecraft.commands.CommandSourceStack;
import net.minecraft.commands.SharedSuggestionProvider;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.NbtAccounter;
import net.minecraft.nbt.NbtIo;
import net.minecraft.network.chat.Component;
import net.pneumono.gravestones.Gravestones;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.concurrent.CompletableFuture;

public class DeathArgumentType implements ArgumentType<String> {
    protected static final DynamicCommandExceptionType COULD_NOT_READ = new DynamicCommandExceptionType(
            name -> Component.literal("Could not read death file " + name)
    );

    @Override
    public String parse(StringReader reader) {
        int i = reader.getCursor();

        while (reader.canRead() && isCharValid(reader.peek())) {
            reader.skip();
        }

        return reader.getString().substring(i, reader.getCursor());
    }

    public static boolean isCharValid(char c) {
        return c >= '0' && c <= '9' || c >= 'a' && c <= 'z' || c >= 'A' && c <= 'Z' || c == '_' || c == '/' || c == '.' || c == '-';
    }

    @Override
    public <S> CompletableFuture<Suggestions> listSuggestions(CommandContext<S> context, SuggestionsBuilder builder) {
        if (!(context.getSource() instanceof CommandSourceStack serverCommandSource)) {
            if (context.getSource() instanceof SharedSuggestionProvider commandSource) {
                return commandSource.customSuggestion(context);
            }
            return Suggestions.empty();
        }

        File rootFile = Gravestones.GRAVESTONES_ROOT.apply(serverCommandSource.getServer());

        File[] playerFiles = rootFile.listFiles();
        String[] playerFileStrings = rootFile.list();
        if (playerFiles == null || playerFileStrings == null) return Suggestions.empty();

        List<String> deathFiles = new ArrayList<>();
        for (int i = 0; i < playerFiles.length; ++i) {
            File playerFile = playerFiles[i];
            String[] playerDeaths = playerFile.list();
            if (playerDeaths != null) {
                int finalI = i;
                deathFiles.addAll(Arrays.stream(playerDeaths).map(file -> playerFileStrings[finalI] + "/" + file).toList());
            }
        }

        return SharedSuggestionProvider.suggest(deathFiles, builder);
    }

    public static DeathArgumentType death() {
        return new DeathArgumentType();
    }

    public static CompoundTag getDeath(CommandContext<CommandSourceStack> context, String name) throws CommandSyntaxException {
        File deathsFile = Gravestones.GRAVESTONES_ROOT.apply(context.getSource().getServer());
        String file = context.getArgument(name, String.class);
        File deathFile = new File(deathsFile, file);

        CompoundTag nbt;
        try {
            nbt = NbtIo.readCompressed(
                    deathFile/*? if >=1.20.3 {*/.toPath(),/*?}*/
                    //? if =1.20.3 {
                    /*NbtAccounter.unlimitedHeap()
                    *///?} else if >=1.20.5 {
                    NbtAccounter.unlimitedHeap()
                    //?}
            );
        } catch (IOException e) {
            throw COULD_NOT_READ.create(file);
        }
        return nbt;
    }
}
