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

/** Энд: главный остров (для дракона) + пустота + крупные внешние острова с 3D-шумом. */
public class EndGen extends ChunkGenerator {
    public static final double OUTER_START = 420; // с какого радиуса начинаются внешние острова
    private static final List<Biome> BIOMES = List.of(Biome.THE_END, Biome.SMALL_END_ISLANDS,
            Biome.END_HIGHLANDS, Biome.END_MIDLANDS, Biome.END_BARRENS);

    private final CustomWorldsPlugin plugin;
    private Noise noise;

    public EndGen(CustomWorldsPlugin plugin) {
        this.plugin = plugin;
    }

    private static final class Noise {
        final long seed;
        final SimplexOctaveGenerator edge, islands, biome;

        Noise(long seed) {
            this.seed = seed;
            Random r = new Random(seed ^ 0x454E44L);
            edge = new SimplexOctaveGenerator(r, 3);
            edge.setScale(1 / 40.0);
            islands = new SimplexOctaveGenerator(r, 4);
            islands.setScale(1 / 150.0);
            biome = new SimplexOctaveGenerator(r, 2);
            biome.setScale(1 / 220.0);
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
                double wx = cx * 16 + x, wz = cz * 16 + z;
                double d = Math.hypot(wx, wz);
                double bump = n.edge.noise(wx, wz, 2.0, 0.5, true);

                double radius = 88 + bump * 14;
                if (d < radius) {
                    double t = d / radius;
                    int top = (int) (64 + (1 - t) * 4 + bump * 2);
                    int bottom = 64 - (int) (32 * Math.sqrt(1 - t * t));
                    for (int y = bottom; y <= top; y++) data.setBlock(x, y, z, Material.END_STONE);
                } else if (d >= OUTER_START) {
                    for (int y = 20; y <= 110; y++) {
                        double v = n.islands.noise(wx, y * 3.0, wz, 2.0, 0.5, true);
                        double f = Math.abs(y - 64) / 36.0;
                        if (y < 64) f *= 1.4;
                        if (v - f * 0.45 > 0.22) data.setBlock(x, y, z, Material.END_STONE);
                    }
                }
            }
        }
    }

    @Override
    public BiomeProvider getDefaultBiomeProvider(WorldInfo info) {
        return new BiomeProvider() {
            @Override
            public Biome getBiome(WorldInfo wi, int x, int y, int z) {
                double d = Math.hypot(x, z);
                if (d < 100) return Biome.THE_END;
                if (d < OUTER_START) return Biome.SMALL_END_ISLANDS;
                double b = noise(wi.getSeed()).biome.noise(x, z, 2.0, 0.5, true);
                if (b > 0.2) return Biome.END_HIGHLANDS;
                if (b > -0.2) return Biome.END_MIDLANDS;
                return Biome.END_BARRENS;
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
    @Override public boolean shouldGenerateCaves() { return false; }
    @Override public boolean shouldGenerateDecorations() { return true; }
    @Override public boolean shouldGenerateMobs() { return true; }
    @Override public boolean shouldGenerateStructures() { return true; }
}
