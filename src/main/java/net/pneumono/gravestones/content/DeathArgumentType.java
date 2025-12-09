package net.pneumono.gravestones.content;

import com.mojang.brigadier.StringReader;
import com.mojang.brigadier.arguments.ArgumentType;
import com.mojang.brigadier.context.CommandContext;
import com.mojang.brigadier.exceptions.CommandSyntaxException;
import com.mojang.brigadier.exceptions.DynamicCommandExceptionType;
import com.mojang.brigadier.suggestion.Suggestions;
import com.mojang.brigadier.suggestion.SuggestionsBuilder;
import net.minecraft.command.CommandSource;
import net.minecraft.nbt.NbtCompound;
import net.minecraft.nbt.NbtIo;
import net.minecraft.server.command.ServerCommandSource;
import net.minecraft.text.Text;
import net.pneumono.gravestones.Gravestones;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.concurrent.CompletableFuture;

//? if =1.20.3 {
/*import net.minecraft.nbt.NbtTagSizeTracker;
*///?} else if >=1.20.5 {
import net.minecraft.nbt.NbtSizeTracker;
//?}

public class DeathArgumentType implements ArgumentType<String> {
    protected static final DynamicCommandExceptionType COULD_NOT_READ = new DynamicCommandExceptionType(
            name -> Text.literal("Could not read death file " + name)
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
        if (!(context.getSource() instanceof ServerCommandSource serverCommandSource)) {
            if (context.getSource() instanceof CommandSource commandSource) {
                return commandSource.getCompletions(context);
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

        return CommandSource.suggestMatching(deathFiles, builder);
    }

    public static DeathArgumentType death() {
        return new DeathArgumentType();
    }

    public static NbtCompound getDeath(CommandContext<ServerCommandSource> context, String name) throws CommandSyntaxException {
        File deathsFile = Gravestones.GRAVESTONES_ROOT.apply(context.getSource().getServer());
        String file = context.getArgument(name, String.class);
        File deathFile = new File(deathsFile, file);

        NbtCompound nbt;
        try {
            nbt = NbtIo.readCompressed(
                    deathFile/*? if >=1.20.3 {*/.toPath(),/*?}*/
                    //? if =1.20.3 {
                    /*NbtTagSizeTracker.ofUnlimitedBytes()
                    *///?} else if >=1.20.5 {
                    NbtSizeTracker.ofUnlimitedBytes()
                    //?}
            );
        } catch (IOException e) {
            throw COULD_NOT_READ.create(file);
        }
        if (nbt == null) throw DeathArgumentType.COULD_NOT_READ.create(file);
        return nbt;
    }
}
