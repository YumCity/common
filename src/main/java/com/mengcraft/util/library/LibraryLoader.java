package com.mengcraft.util.library;

import lombok.SneakyThrows;
import lombok.val;
import org.bukkit.plugin.java.JavaPlugin;

import java.util.concurrent.CompletableFuture;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;

import static com.mengcraft.util.Reflector.invoke;

/**
 * Created on 15-12-13.
 */
public class LibraryLoader {

    @SneakyThrows
    public static void load(JavaPlugin plugin, Library library) {
        if (library.present()) {
            plugin.getLogger().info("Library " + library + " present");
        } else {
            if (!library.isLoadable()) {
                init(plugin, library);
            }

            for (Library sub : library.getSublist()) {
                load(plugin, sub);
            }

            val lib = library.getFile();
            invoke(plugin.getClass().getClassLoader(), "addURL", lib.toURI().toURL());

            plugin.getLogger().info("Load library " + lib + " done");
        }
    }

    @SneakyThrows
    static void init(JavaPlugin plugin, Library library) {
        plugin.getLogger().info("Loading library " + library);

        val run = CompletableFuture.runAsync(() -> {
            while (!library.isLoadable()) {
                library.init();
            }
        });

        val lib = library.getFile();

        while (!run.isDone()) {
            try {
                run.get(1, TimeUnit.SECONDS);
            } catch (TimeoutException | InterruptedException ignore) {
            }
            plugin.getLogger().info((lib.length() / 1024) + "kb");
        }

        if (run.isCompletedExceptionally()) throw new IllegalStateException("init");
    }

}
