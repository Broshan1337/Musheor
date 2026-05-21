// Decompiled and deobfuscated from musheor-1.5 1.21.11.jar
package musheor.commands;

import com.mojang.brigadier.arguments.ArgumentType;
import com.mojang.brigadier.arguments.StringArgumentType;
import com.mojang.brigadier.builder.LiteralArgumentBuilder;
import com.mojang.brigadier.context.CommandContext;
import com.mojang.brigadier.suggestion.Suggestions;
import com.mojang.brigadier.suggestion.SuggestionsBuilder;
import java.lang.reflect.Method;
import java.lang.reflect.Modifier;
import java.nio.file.FileVisitOption;
import java.nio.file.Files;
import java.nio.file.LinkOption;
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
import net.minecraft.class_1657;   // PlayerEntity
import net.minecraft.ItemStack;   // Item
import net.minecraft.ItemStack;   // ItemStack
import net.minecraft.class_1935;   // ItemConvertible
import net.minecraft.GuiGraphics;   // CommandSource
import net.minecraft.class_2960;   // Identifier
import net.minecraft.MinecraftClient;    // MinecraftClient
import net.minecraft.class_7923;   // Registries

/**
 * .execute ClassName.methodName [arg1 arg2 ...]
 *
 * Developer/debug command that dynamically invokes any public or declared method
 * on a class within the musheor mod by reflection.
 *
 * At startup, scans all .class files under the "musheor" mod container and
 * builds an autocomplete index of class names → method names.
 *
 * Supported argument types (auto-converted from strings):
 *   String, int/Integer, double/Double, float/Float, boolean/Boolean,
 *   long/Long, Identifier, Item, ItemStack, PlayerEntity (@p or "self")
 */
public class Execute extends Command {
    /** class simple name → Class object */
    private static final Map<String, Class<?>> classRegistry = new HashMap(); // was: jOdDDFXSeWl4

    /** class simple name → list of method names (for autocomplete) */
    private static final Map<String, List<String>> methodIndex = new HashMap<String, List<String>>(); // was: mp3zoXQFKUKYj5

    public Execute() {
        super("execute", "Run test methods dynamically and auto-suggest available ones.", new String[0]);
        scanModClasses("musheor");
    }

    @Override
    public void build(LiteralArgumentBuilder<GuiGraphics> builder) {
        builder.then(Execute.argument("call", (ArgumentType) StringArgumentType.greedyString())
            .suggests(this::buildSuggestions)
            .executes(this::executeCall));
    }

    private int executeCall(CommandContext<GuiGraphics> ctx) { // was: mp3zoXQFKUKYj5(CommandContext)
        String input = StringArgumentType.getString(ctx, "call");
        try {
            String[] parts = input.split(" ");
            if (parts.length == 0) {
                this.error("Usage: .execute ClassName.methodName [args...]", new Object[0]);
                return 1;
            }
            String[] nameParts = parts[0].split("\\.");
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
            String[] args   = Arrays.copyOfRange(parts, 1, parts.length);
            Method method   = findMethod(clazz, methodName, args.length);
            if (method == null) {
                this.error("No suitable method found for " + methodName + " with " + args.length + " args.", new Object[0]);
                return 1;
            }
            Object[] converted = convertArgs(method.getParameterTypes(), args);
            Object instance    = Modifier.isStatic(method.getModifiers())
                ? null : clazz.getDeclaredConstructor(new Class[0]).newInstance(new Object[0]);
            method.setAccessible(true);
            Object result = method.invoke(instance, converted);
            this.info("Executed " + parts[0] + (result != null ? " -> " + String.valueOf(result) : ""), new Object[0]);
        } catch (Exception e) {
            this.error("Error: " + e.getClass().getSimpleName() + " - " + e.getMessage(), new Object[0]);
            e.printStackTrace();
        }
        return 1;
    }

    /** Finds a declared method on {@code clazz} by name and exact parameter count. */
    private Method findMethod(Class<?> clazz, String name, int paramCount) { // was: jOdDDFXSeWl4(Class,String,int)
        for (Method m : clazz.getDeclaredMethods()) {
            if (!m.getName().equals(name) || m.getParameterCount() != paramCount) continue;
            return m;
        }
        return null;
    }

    /** Converts string argument array to typed objects matching {@code paramTypes}. */
    private Object[] convertArgs(Class<?>[] paramTypes, String[] args) { // was: jOdDDFXSeWl4(Class[],String[])
        Object[] result = new Object[args.length];
        for (int i = 0; i < args.length; ++i) {
            result[i] = convertArg(args[i], paramTypes[i]);
        }
        return result;
    }

    /** Converts a single string argument to the target type. */
    private Object convertArg(String value, Class<?> targetType) { // was: jOdDDFXSeWl4(String,Class)
        try {
            if (targetType == String.class)                             return value;
            if (targetType == Integer.TYPE || targetType == Integer.class) return Integer.parseInt(value);
            if (targetType == Double.TYPE  || targetType == Double.class)  return Double.parseDouble(value);
            if (targetType == Float.TYPE   || targetType == Float.class)   return Float.valueOf(Float.parseFloat(value));
            if (targetType == Boolean.TYPE || targetType == Boolean.class) return Boolean.parseBoolean(value);
            if (targetType == Long.TYPE    || targetType == Long.class)    return Long.parseLong(value);
            if (targetType == class_2960.class) return class_2960.method_60654(value); // Identifier.of
            if (targetType == ItemStack.class) {
                return class_7923.field_41178.method_63535(class_2960.method_60654(value)); // Registries.ITEM.get
            }
            if (targetType == ItemStack.class) { // ItemStack
                String[] parts = value.split(":");
                int count = 1;
                ItemStack item;
                if (parts.length >= 2 && parts[0].equalsIgnoreCase("minecraft")) {
                    item = (ItemStack) class_7923.field_41178.method_63535(class_2960.method_60655(parts[0], parts[1]));
                    if (parts.length == 3) {
                        try { count = Integer.parseInt(parts[2]); } catch (Exception ignored) {}
                    }
                } else {
                    item = parts.length == 1
                        ? (ItemStack) class_7923.field_41178.method_63535(class_2960.method_60655("minecraft", parts[0]))
                        : (ItemStack) class_7923.field_41178.method_63535(class_2960.method_60654(value));
                }
                if (item == null) return null;
                return new ItemStack((class_1935) item, count);
            }
            if (targetType == class_1657.class) { // PlayerEntity
                MinecraftClient mc = MeteorClient.mc;
                if (value.equalsIgnoreCase("@p") || value.equalsIgnoreCase("self")) return mc.field_1724;
            }
            return value; // fallback: pass raw string
        } catch (Exception e) {
            System.err.println("[Execute] Failed to convert '" + value + "' to " + targetType.getSimpleName() + ": " + e);
            return null;
        }
    }

    /** Provides autocomplete: class names before ".", then "ClassName.methodName" after ".". */
    private CompletableFuture<Suggestions> buildSuggestions( // was: jOdDDFXSeWl4(CommandContext,SuggestionsBuilder)
            CommandContext<GuiGraphics> ctx, SuggestionsBuilder builder) {
        String remaining = builder.getRemaining();
        if (!remaining.contains(".")) {
            classRegistry.keySet().stream()
                .filter(name -> name.startsWith(remaining))
                .forEach(builder::suggest);
        } else {
            String className = remaining.substring(0, remaining.indexOf('.'));
            List<String> methods = methodIndex.get(className);
            if (methods != null) {
                String prefix = className + ".";
                methods.stream()
                    .map(m -> prefix + m)
                    .filter(s -> s.startsWith(remaining))
                    .forEach(builder::suggest);
            }
        }
        return builder.buildFuture();
    }

    /** Walks the mod's classpath under the given package prefix and registers all classes. */
    private void scanModClasses(String packagePrefix) { // was: jOdDDFXSeWl4(String)
        ModContainer mod = FabricLoader.getInstance().getModContainer("musheor").orElse(null);
        if (mod == null) {
            System.err.println("[Execute] Could not find musheor mod container.");
            return;
        }
        String pathPrefix = packagePrefix.replace('.', '/');
        for (Path root : mod.getRootPaths()) {
            Path dir = root.resolve(pathPrefix);
            if (!Files.exists(dir, new LinkOption[0])) continue;
            try {
                Stream<Path> stream = Files.walk(dir, new FileVisitOption[0]);
                try {
                    stream.filter(p -> p.getFileName().toString().endsWith(".class"))
                        .forEach(p -> {
                            String fqn = root.relativize(p).toString()
                                .replace('/', '.').replace('\\', '.').replace(".class", "");
                            registerClass(fqn);
                        });
                } finally {
                    if (stream != null) stream.close();
                }
            } catch (Exception e) {
                System.err.println("[Execute] Scan failed on " + root + ": " + e);
            }
        }
    }

    /** Loads a class by fully-qualified name and adds it to the registry. */
    private void registerClass(String fqn) { // was: mp3zoXQFKUKYj5(String)
        try {
            Class<?> clazz = Class.forName(fqn);
            String simpleName = clazz.getSimpleName();
            classRegistry.put(simpleName, clazz);
            ArrayList<String> methods = new ArrayList<String>();
            for (Method m : clazz.getDeclaredMethods()) {
                if (m.isSynthetic()) continue;
                methods.add(m.getName());
            }
            methodIndex.put(simpleName, methods);
        } catch (Throwable ignored) {}
    }
}
