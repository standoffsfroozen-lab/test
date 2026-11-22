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

/**
 * Обычный мир "в стиле модов, но в меру": domain-warp (извилистые берега), широкие океаны,
 * реки, холмы, горные хребты, мезы в жарких пустынях, много биомов.
 */
public class OverworldGen extends ChunkGenerator {
    public static final int SEA = 62;

    private static final List<Biome> BIOMES = List.of(
            Biome.DEEP_OCEAN, Biome.OCEAN, Biome.COLD_OCEAN, Biome.FROZEN_OCEAN, Biome.LUKEWARM_OCEAN, Biome.WARM_OCEAN,
            Biome.RIVER, Biome.FROZEN_RIVER, Biome.BEACH, Biome.SNOWY_BEACH,
            Biome.FROZEN_PEAKS, Biome.JAGGED_PEAKS, Biome.STONY_PEAKS, Biome.SNOWY_SLOPES, Biome.GROVE, Biome.WINDSWEPT_HILLS,
            Biome.BADLANDS, Biome.DESERT, Biome.SAVANNA, Biome.JUNGLE, Biome.SPARSE_JUNGLE,
            Biome.SNOWY_TAIGA, Biome.ICE_SPIKES, Biome.SNOWY_PLAINS, Biome.TAIGA, Biome.OLD_GROWTH_PINE_TAIGA,
            Biome.MEADOW, Biome.PLAINS, Biome.SUNFLOWER_PLAINS, Biome.SWAMP, Biome.DARK_FOREST, Biome.FLOWER_FOREST,
            Biome.OLD_GROWTH_BIRCH_FOREST, Biome.FOREST, Biome.BIRCH_FOREST, Biome.CHERRY_GROVE);

    private final CustomWorldsPlugin plugin;
    private Noise noise;

    public OverworldGen(CustomWorldsPlugin plugin) {
        this.plugin = plugin;
    }

    private static final class Noise {
        final long seed;
        final SimplexOctaveGenerator warpA, warpB, cont, hill, det, mask, ridge, river, tmp, hum, var;

        Noise(long seed) {
            this.seed = seed;
            Random r = new Random(seed);
            warpA = mk(r, 2, 1 / 150.0);
            warpB = mk(r, 2, 1 / 150.0);
            cont = mk(r, 4, 1 / 650.0);
            hill = mk(r, 4, 1 / 95.0);
            det = mk(r, 3, 1 / 28.0);
            mask = mk(r, 3, 1 / 480.0);
            ridge = mk(r, 4, 1 / 280.0);
            river = mk(r, 3, 1 / 420.0);
            tmp = mk(r, 2, 1 / 750.0);
            hum = mk(r, 2, 1 / 650.0);
            var = mk(r, 2, 1 / 130.0);
        }

        private static SimplexOctaveGenerator mk(Random r, int oct, double scale) {
            SimplexOctaveGenerator g = new SimplexOctaveGenerator(r, oct);
            g.setScale(scale);
            return g;
        }
    }

    private static final class S {
        double h, t, w, v;
        boolean river;
    }

    private synchronized Noise noise(long seed) {
        if (noise == null || noise.seed != seed) noise = new Noise(seed);
        return noise;
    }

    private static double n2(SimplexOctaveGenerator g, double x, double z) {
        return g.noise(x, z, 2.0, 0.5, true);
    }

    private static double smooth(double v, double a, double b) {
        double t = Math.max(0, Math.min(1, (v - a) / (b - a)));
        return t * t * (3 - 2 * t);
    }

    private static S sample(Noise n, double x, double z) {
        S s = new S();
        double wx = x + n2(n.warpA, x, z) * 45;
        double wz = z + n2(n.warpB, x, z) * 45;

        double c = n2(n.cont, wx, wz);
        s.t = n2(n.tmp, x, z) * 1.6;
        s.w = n2(n.hum, x, z) * 1.6;
        s.v = n2(n.var, x, z) * 1.8;

        double h = SEA + 3 + c * 40 + n2(n.hill, wx, wz) * 9 + n2(n.det, wx, wz) * 2.5;

        // горные хребты: только вглубь суши
        double mask = smooth(n2(n.mask, wx, wz), 0.0, 0.45) * smooth(c, -0.1, 0.35);
        double ridge = 1 - Math.abs(n2(n.ridge, wx, wz));
        h += mask * ridge * ridge * 100;

        // мезы (терассы) в жарких сухих областях
        if (s.t > 0.45 && s.w < -0.4 && h > SEA + 2) {
            double step = 7, terr = Math.floor(h / step) * step;
            h = terr + (h - terr) * 0.15 + 5;
        }

        // реки
        double rv = Math.abs(n2(n.river, wx, wz));
        if (rv < 0.045 && c > -0.15 && h < 118) {
            double k = 1 - rv / 0.045;
            k = k * k * (3 - 2 * k);
            double bed = SEA - 4;
            if (h > bed) h += (bed - h) * k;
            s.river = k > 0.55;
        }
        s.h = h;
        return s;
    }

    private static Biome biomeAt(Noise n, int x, int z) {
        S s = sample(n, x, z);
        double h = s.h, t = s.t, w = s.w, v = s.v;
        boolean cold = t < -0.4;

        if (s.river) return cold ? Biome.FROZEN_RIVER : Biome.RIVER;
        if (h < SEA - 18) return Biome.DEEP_OCEAN;
        if (h < SEA - 2) return cold ? (t < -0.7 ? Biome.FROZEN_OCEAN : Biome.COLD_OCEAN) : (t > 0.5 ? Biome.WARM_OCEAN : t > 0.2 ? Biome.LUKEWARM_OCEAN : Biome.OCEAN);
        if (h < SEA + 1.5) return cold ? Biome.SNOWY_BEACH : Biome.BEACH;

        if (h > 150) return cold ? (v > 0 ? Biome.FROZEN_PEAKS : Biome.JAGGED_PEAKS) : Biome.STONY_PEAKS;
        if (h > 112) return cold ? Biome.SNOWY_SLOPES : Biome.WINDSWEPT_HILLS;
        if (h > 100 && cold) return Biome.GROVE;

        if (t > 0.45) {
            if (w < -0.4) return Biome.BADLANDS;
            if (w < -0.1) return Biome.DESERT;
            if (w < 0.3) return Biome.SAVANNA;
            return v > 0 ? Biome.JUNGLE : Biome.SPARSE_JUNGLE;
        }
        if (cold) {
            if (w > 0.1) return Biome.SNOWY_TAIGA;
            return v > 0.4 ? Biome.ICE_SPIKES : Biome.SNOWY_PLAINS;
        }
        if (t < -0.1) {
            if (w > 0.0) return v > 0.4 ? Biome.OLD_GROWTH_PINE_TAIGA : Biome.TAIGA;
            return w > -0.3 ? Biome.MEADOW : Biome.PLAINS;
        }
        if (w > 0.55) return Biome.SWAMP;
        if (w > 0.35) return Biome.DARK_FOREST;
        if (w > 0.15) return v > 0.35 ? Biome.FLOWER_FOREST : (v < -0.3 ? Biome.OLD_GROWTH_BIRCH_FOREST : Biome.FOREST);
        if (w > -0.05) return v > 0.3 ? Biome.CHERRY_GROVE : (v < -0.2 ? Biome.BIRCH_FOREST : Biome.FOREST);
        return v > 0.4 ? Biome.SUNFLOWER_PLAINS : Biome.PLAINS;
    }

    @Override
    public void generateNoise(WorldInfo info, Random rnd, int cx, int cz, ChunkData data) {
        Noise n = noise(info.getSeed());
        int minY = info.getMinHeight(), maxY = info.getMaxHeight();
        for (int x = 0; x < 16; x++) {
            for (int z = 0; z < 16; z++) {
                int h = (int) sample(n, cx * 16 + x, cz * 16 + z).h;
                h = Math.min(h, maxY - 2);
                for (int y = minY; y <= h; y++) {
                    Material m;
                    if (y == minY || (y < minY + 5 && rnd.nextInt(y - minY + 1) == 0)) m = Material.BEDROCK;
                    else m = y < 0 ? Material.DEEPSLATE : Material.STONE;
                    data.setBlock(x, y, z, m);
                }
                for (int y = h + 1; y <= SEA; y++) data.setBlock(x, y, z, Material.WATER);
            }
        }
    }

    @Override
    public BiomeProvider getDefaultBiomeProvider(WorldInfo info) {
        return new BiomeProvider() {
            @Override
            public Biome getBiome(WorldInfo wi, int x, int y, int z) {
                return biomeAt(noise(wi.getSeed()), x, z);
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
