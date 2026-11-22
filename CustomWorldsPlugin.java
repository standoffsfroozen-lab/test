package dev.cw;

import dev.cw.dungeon.DungeonPopulator;
import dev.cw.gen.EndGen;
import dev.cw.gen.NetherGen;
import dev.cw.gen.OverworldGen;
import org.bukkit.Bukkit;
import org.bukkit.World;
import org.bukkit.WorldCreator;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.command.TabCompleter;
import org.bukkit.entity.Player;
import org.bukkit.generator.ChunkGenerator;
import org.bukkit.plugin.java.JavaPlugin;

import java.util.ArrayList;
import java.util.List;

public final class CustomWorldsPlugin extends JavaPlugin implements CommandExecutor, TabCompleter {

    @Override
    public void onEnable() {
        saveDefaultConfig();
        getCommand("cw").setExecutor(this);
        getCommand("cw").setTabCompleter(this);

        // автозагрузка созданных ранее миров
        Bukkit.getScheduler().runTask(this, () -> {
            for (String entry : getConfig().getStringList("worlds")) {
                String[] p = entry.split(":");
                if (p.length == 2 && Bukkit.getWorld(p[0]) == null) createWorld(p[0], p[1]);
            }
        });
    }

    /** Вызывается Bukkit для bukkit.yml -> worlds -> <имя> -> generator: CustomWorlds:normal|nether|end */
    @Override
    public ChunkGenerator getDefaultWorldGenerator(String worldName, String id) {
        String type = id == null ? "" : id.toLowerCase();
        if (type.isEmpty()) {
            String n = worldName.toLowerCase();
            type = n.contains("nether") ? "nether" : n.contains("end") ? "end" : "normal";
        }
        return generatorFor(type);
    }

    private ChunkGenerator generatorFor(String type) {
        return switch (type) {
            case "nether" -> new NetherGen(this);
            case "end" -> new EndGen(this);
            default -> new OverworldGen(this);
        };
    }

    private World createWorld(String name, String type) {
        World.Environment env = switch (type) {
            case "nether" -> World.Environment.NETHER;
            case "end" -> World.Environment.THE_END;
            default -> World.Environment.NORMAL;
        };
        return new WorldCreator(name).environment(env).generator(generatorFor(type)).createWorld();
    }

    @Override
    public boolean onCommand(CommandSender s, Command c, String l, String[] a) {
        if (a.length == 0) {
            s.sendMessage("/cw create <имя> <normal|nether|end> | /cw tp <имя> | /cw list | /cw reload");
            return true;
        }
        switch (a[0].toLowerCase()) {
            case "create" -> {
                if (a.length < 3) { s.sendMessage("Использование: /cw create <имя> <normal|nether|end>"); return true; }
                String type = a[2].toLowerCase();
                if (!List.of("normal", "nether", "end").contains(type)) { s.sendMessage("Тип: normal, nether или end"); return true; }
                if (Bukkit.getWorld(a[1]) != null) { s.sendMessage("Мир с таким именем уже существует."); return true; }
                s.sendMessage("Создаю мир " + a[1] + " (" + type + ")...");
                World w = createWorld(a[1], type);
                if (w == null) { s.sendMessage("Не удалось создать мир."); return true; }
                List<String> list = new ArrayList<>(getConfig().getStringList("worlds"));
                list.add(a[1] + ":" + type);
                getConfig().set("worlds", list);
                saveConfig();
                s.sendMessage("Готово! /cw tp " + a[1]);
            }
            case "tp" -> {
                if (!(s instanceof Player p)) { s.sendMessage("Только для игроков."); return true; }
                if (a.length < 2) { s.sendMessage("Использование: /cw tp <имя>"); return true; }
                World w = Bukkit.getWorld(a[1]);
                if (w == null) { s.sendMessage("Мир не найден."); return true; }
                p.teleport(w.getSpawnLocation());
            }
            case "list" -> Bukkit.getWorlds().forEach(w -> s.sendMessage("- " + w.getName() + " [" + w.getEnvironment() + "]"));
            case "reload" -> { reloadConfig(); s.sendMessage("Конфиг перезагружен (новые чанки используют новые шансы)."); }
            default -> s.sendMessage("Неизвестная подкоманда.");
        }
        return true;
    }

    @Override
    public List<String> onTabComplete(CommandSender s, Command c, String l, String[] a) {
        if (a.length == 1) return List.of("create", "tp", "list", "reload");
        if (a.length == 3 && a[0].equalsIgnoreCase("create")) return List.of("normal", "nether", "end");
        if (a.length == 2 && a[0].equalsIgnoreCase("tp")) return Bukkit.getWorlds().stream().map(World::getName).toList();
        return List.of();
    }

    public DungeonPopulator newPopulator() {
        return new DungeonPopulator(this);
    }
}
