package dev.cw.gen;

import dev.cw.CustomWorldsPlugin;
import org.bukkit.Material;
import org.bukkit.World;
import org.bukkit.block.Biome;
import org.bukkit.generator.BiomeProvider;
import org.bukkit.generator.BlockPopulator;
import org.bukkit.generator.ChunkGenerator;
import org.bukkit.generator.WorldInfo;
import org.bukkit.util.noise.SimplexOctaveGenerator;

import java.util.List;
import java.util.Random;

/** Ад: 3D-шум, огромные пещеры, острова и лавовое море на y=31. */
public class NetherGen extends ChunkGenerator {
    private static final int LAVA = 31;
    private static final List<Biome> BIOMES = List.of(Biome.NETHER_WASTES, Biome.CRIMSON_FOREST,
            Biome.WARPED_FOREST, Biome.SOUL_SAND_VALLEY, Biome.BASALT_DELTAS);

    private final CustomWorldsPlugin plugin;
    private Noise noise;

    public NetherGen(CustomWorldsPlugin plugin) {
        this.plugin = plugin;
    }

    private static final class Noise {
        final long seed;
        final SimplexOctaveGenerator shape, biome, land;

        Noise(long seed) {
            this.seed = seed;
            Random r = new Random(seed ^ 0x4E455448L);
            shape = new SimplexOctaveGenerator(r, 4);
            shape.setScale(1 / 95.0);
            land = new SimplexOctaveGenerator(r, 3);
            land.setScale(1 / 210.0);
            biome = new SimplexOctaveGenerator(r, 2);
            biome.setScale(1 / 160.0);
        }
    }

    private synchronized Noise noise(long seed) {
        if (noise == null || noise.seed != seed) noise = new Noise(seed);
        return noise;
    }

    @Override
    public void generateNoise(WorldInfo info, Random rnd, int cx, int cz, ChunkData data) {
        Noise n = noise(info.getSeed());
        for (int x = 0; x < 16; x++) {
            for (int z = 0; z < 16; z++) {
                int wx = cx * 16 + x, wz = cz * 16 + z;
                for (int y = 0; y <= 127; y++) {
                    if (y == 0 || y == 127 || (y < 4 && rnd.nextInt(y + 1) == 0) || (y > 123 && rnd.nextInt(128 - y) == 0)) {
                        data.setBlock(x, y, z, Material.BEDROCK);
                        continue;
                    }
                    double edge = Math.abs(y - 64) / 64.0;
                    double v = n.shape.noise(wx, y * 2.2, wz, 2.0, 0.5, true) + edge * edge * 1.15;
                    // большие материки: широкая плита с холмистой поверхностью
                    double big = n.land.noise(wx, wz, 2.0, 0.5, true);
                    boolean plate = big > -0.05 && y >= 30 && y <= 44 + big * 24;
                    if (v > 0.30 || plate) data.setBlock(x, y, z, Material.NETHERRACK);
                    else if (y <= LAVA) data.setBlock(x, y, z, Material.LAVA);
                }
            }
        }
    }

    @Override
    public BiomeProvider getDefaultBiomeProvider(WorldInfo info) {
        return new BiomeProvider() {
            @Override
            public Biome getBiome(WorldInfo wi, int x, int y, int z) {
                double b = noise(wi.getSeed()).biome.noise(x, z, 2.0, 0.5, true);
                if (b > 0.55) return Biome.BASALT_DELTAS;
                if (b > 0.3) return Biome.CRIMSON_FOREST;
                if (b < -0.55) return Biome.SOUL_SAND_VALLEY;
                if (b < -0.3) return Biome.WARPED_FOREST;
                return Biome.NETHER_WASTES;
            }

            @Override
            public List<Biome> getBiomes(WorldInfo wi) {
                return BIOMES;
            }
        };
    }

    @Override public List<BlockPopulator> getDefaultPopulators(World world) { return List.of(plugin.newPopulator()); }
    @Override public boolean shouldGenerateNoise() { return false; }
    @Override public boolean shouldGenerateSurface() { return true; }
    @Override public boolean shouldGenerateBedrock() { return false; }
    @Override public boolean shouldGenerateCaves() { return true; }
    @Override public boolean shouldGenerateDecorations() { return true; }
    @Override public boolean shouldGenerateMobs() { return true; }
    @Override public boolean shouldGenerateStructures() { return true; }
}
