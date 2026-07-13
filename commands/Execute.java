// Decompiled and deobfuscated from musheor-1.6.1 1.21.11.jar
// (source class was obfuscated as obf.aY0a71o)
package musheor.commands;

import com.mojang.brigadier.arguments.ArgumentType;
import com.mojang.brigadier.arguments.StringArgumentType;
import com.mojang.brigadier.builder.LiteralArgumentBuilder;
import com.mojang.brigadier.context.CommandContext;
import com.mojang.brigadier.suggestion.Suggestions;
import com.mojang.brigadier.suggestion.SuggestionsBuilder;
import java.lang.reflect.Method;
import java.lang.reflect.Modifier;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.CompletableFuture;
import java.util.stream.Stream;
import meteordevelopment.meteorclient.MeteorClient;
import meteordevelopment.meteorclient.commands.Command;
import net.fabricmc.loader.api.FabricLoader;
import net.fabricmc.loader.api.ModContainer;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.server.command.ServerCommandSource;
import net.minecraft.util.Identifier;
import net.minecraft.client.MinecraftClient;
import net.minecraft.registry.Registries;

/**
 * .execute ClassName.methodName [arg1 arg2 ...]
 *
 * Developer/debug command that dynamically invokes a declared method on a class
 * WITHIN THE musheor MOD by reflection.
 *
 * SECURITY NOTE: at startup it walks only the "musheor" Fabric mod container's
 * own .class files (see scanModClasses) and indexes their class/method names.
 * It cannot load or invoke code outside the mod's own jar, and does not read any
 * class name from the network. This is a self-test console, not a code loader.
 *
 * Supported argument types (auto-converted from strings):
 *   String, int/Integer, double/Double, float/Float, boolean/Boolean,
 *   long/Long, Identifier, Item, ItemStack, PlayerEntity (@p or "self")
 */
public class Execute extends Command {
    /** class simple name → Class object */
    private static final Map<String, Class<?>> classRegistry = new HashMap<>(); // was: FvaNWO (Map)

    /** class simple name → list of method names (for autocomplete) */
    private static final Map<String, List<String>> methodIndex = new HashMap<>(); // was: Q90GLXQ0Pef (Map)

    public Execute() {
        super("execute", "Run test methods dynamically and auto-suggest available ones.", new String[0]);
        scanModClasses("musheor");
    }

    @Override
    public void build(LiteralArgumentBuilder<ServerCommandSource> builder) {
        builder.then(Execute.argument("call", (ArgumentType) StringArgumentType.greedyString())
            .suggests(this::buildSuggestions)
            .executes(this::executeCall));
    }

    private int executeCall(CommandContext<ServerCommandSource> ctx) { // was: FvaNWO(CommandContext)
        String input = StringArgumentType.getString(ctx, "call");
        try {
            String[] tokens = input.split(" ");
            if (tokens.length == 0) {
                this.error("Usage: .execute ClassName.methodName [args...]", new Object[0]);
                return 1;
            }
            String[] nameParts = tokens[0].split("\\.");
            if (nameParts.length != 2) {
                this.error("Use format: ClassName.methodName", new Object[0]);
                return 1;
            }
            String className  = nameParts[0];
            String methodName = nameParts[1];
            Class<?> clazz = classRegistry.get(className);
            if (clazz == null) {
                this.error("Unknown class: " + className, new Object[0]);
                return 1;
            }
            String[] args   = Arrays.copyOfRange(tokens, 1, tokens.length);
            Method method   = findMethod(clazz, methodName, args.length);
            if (method == null) {
                this.error("No suitable method found for " + methodName + " with " + args.length + " args.", new Object[0]);
                return 1;
            }
            Object[] converted = convertArgs(method.getParameterTypes(), args);
            Object instance = Modifier.isStatic(method.getModifiers()) ? null : clazz.getDeclaredConstructor().newInstance();
            method.setAccessible(true);
            Object result = method.invoke(instance, converted);
            this.info("Executed " + tokens[0] + (result != null ? " -> " + result : ""), new Object[0]);
        } catch (Exception e) {
            this.error("Error: " + e.getClass().getSimpleName() + " - " + e.getMessage(), new Object[0]);
            e.printStackTrace();
        }
        return 1;
    }

    /** Finds a declared method by name and parameter count. */
    private Method findMethod(Class<?> clazz, String name, int argCount) { // was: FvaNWO(Class,String,int)
        for (Method m : clazz.getDeclaredMethods()) {
            if (m.getName().equals(name) && m.getParameterCount() == argCount) {
                return m;
            }
        }
        return null;
    }

    /** Converts a string[] of arguments to the target parameter types. */
    private Object[] convertArgs(Class<?>[] types, String[] args) { // was: FvaNWO(Class[],String[])
        Object[] converted = new Object[args.length];
        for (int i = 0; i < args.length; i++) {
            converted[i] = convertArg(args[i], types[i]);
        }
        return converted;
    }

    /** Converts a single string argument to the given type. */
    private Object convertArg(String s, Class<?> type) { // was: FvaNWO(String,Class)
        try {
            if (type == String.class)   return s;
            if (type == int.class     || type == Integer.class) return Integer.parseInt(s);
            if (type == double.class  || type == Double.class)  return Double.parseDouble(s);
            if (type == float.class   || type == Float.class)   return Float.parseFloat(s);
            if (type == boolean.class || type == Boolean.class) return Boolean.parseBoolean(s);
            if (type == long.class    || type == Long.class)    return Long.parseLong(s);
            if (type == Identifier.class) {
                return Identifier.of(s);
            }
            if (type == Item.class) {
                return Registries.ITEM.get(Identifier.of(s));
            }
            if (type == ItemStack.class) {
                String[] parts = s.split(":");
                int count = 1;
                Item item;
                if (parts.length >= 2 && parts[0].equalsIgnoreCase("minecraft")) {
                    item = (Item) Registries.ITEM.get(Identifier.of(parts[0], parts[1]));
                    if (parts.length == 3) {
                        try { count = Integer.parseInt(parts[2]); } catch (Exception ignored) {}
                    }
                } else if (parts.length == 1) {
                    item = (Item) Registries.ITEM.get(Identifier.of("minecraft", parts[0]));
                } else {
                    item = (Item) Registries.ITEM.get(Identifier.of(s));
                }
                return item == null ? null : new ItemStack(item, count);
            }
            if (type == PlayerEntity.class) {
                MinecraftClient mc = MeteorClient.mc;
                if (s.equalsIgnoreCase("@p") || s.equalsIgnoreCase("self")) {
                    return mc.player;
                }
            }
            return s;
        } catch (Exception e) {
            System.err.println("[Execute] Failed to convert '" + s + "' to " + type.getSimpleName() + ": " + e);
            return null;
        }
    }

    /** Autocomplete: suggests "ClassName" then "ClassName.methodName". */
    private CompletableFuture<Suggestions> buildSuggestions(CommandContext<ServerCommandSource> ctx, SuggestionsBuilder builder) { // was: FvaNWO(CommandContext,SuggestionsBuilder)
        String input = builder.getRemaining();
        if (!input.contains(".")) {
            classRegistry.keySet().stream().filter(name -> name.startsWith(input)).forEach(builder::suggest);
        } else {
            String className = input.substring(0, input.indexOf('.'));
            List<String> methods = methodIndex.get(className);
            if (methods != null) {
                String prefix = className + ".";
                methods.stream().map(m -> prefix + m).filter(name -> name.startsWith(input)).forEach(builder::suggest);
            }
        }
        return builder.buildFuture();
    }

    /** Walks the musheor mod container's own .class files and indexes them. */
    private void scanModClasses(String basePackage) { // was: FvaNWO(String)
        ModContainer container = (ModContainer) FabricLoader.getInstance().getModContainer("musheor").orElse(null);
        if (container == null) {
            System.err.println("[Execute] Could not find musheor mod container.");
            return;
        }
        String pkgPath = basePackage.replace('.', '/');
        for (Path root : container.getRootPaths()) {
            Path pkgDir = root.resolve(pkgPath);
            if (!Files.exists(pkgDir)) continue;
            try (Stream<Path> stream = Files.walk(pkgDir)) {
                stream.filter(p -> p.getFileName().toString().endsWith(".class")).forEach(p -> {
                    String rel = root.relativize(p).toString()
                        .replace('/', '.').replace('\\', '.').replace(".class", "");
                    indexClass(rel);
                });
            } catch (Exception e) {
                System.err.println("[Execute] Scan failed on " + root + ": " + e);
            }
        }
    }

    /** Loads a class by name and records its simple name + declared method names. */
    private void indexClass(String fullName) { // was: Q90GLXQ0Pef(String)
        try {
            Class<?> clazz = Class.forName(fullName);
            String simple = clazz.getSimpleName();
            classRegistry.put(simple, clazz);
            List<String> names = new ArrayList<>();
            for (Method m : clazz.getDeclaredMethods()) {
                if (!m.isSynthetic()) {
                    names.add(m.getName());
                }
            }
            methodIndex.put(simple, names);
        } catch (Throwable ignored) {
        }
    }
}
