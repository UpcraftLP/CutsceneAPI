package com.raphydaphy.cutsceneapi.fakeworld;

import com.raphydaphy.cutsceneapi.cutscene.CutsceneManager;
import com.raphydaphy.cutsceneapi.mixin.client.ClientWorldHooks;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.network.ClientPlayerEntity;
import net.minecraft.client.world.ClientChunkManager;
import net.minecraft.client.world.ClientWorld;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.biome.Biome;
import net.minecraft.world.biome.Biomes;
import net.minecraft.world.chunk.Chunk;
import net.minecraft.world.chunk.ChunkManager;
import net.minecraft.world.chunk.ChunkPos;
import net.minecraft.world.chunk.ChunkStatus;
import net.minecraft.world.dimension.DimensionType;
import net.minecraft.world.level.LevelInfo;

import java.util.Arrays;
import java.util.HashMap;
import java.util.Map;
import java.util.function.BooleanSupplier;

public class CutsceneWorld extends ClientWorld
{
	private Map<ChunkPos, CutsceneChunk> chunkMap = new HashMap<>();
	public boolean cloneExisting;
	private CutsceneChunkManager cutsceneChunkManager;

	public CutsceneWorld(MinecraftClient client, ClientWorld realWorld, boolean cloneExisting)
	{
		super(((ClientWorldHooks) realWorld).getCutsceneNetHandler(), new LevelInfo(realWorld.getLevelProperties()), DimensionType.OVERWORLD, client.getProfiler(), client.worldRenderer);
		this.cloneExisting = cloneExisting;
		cutsceneChunkManager = new CutsceneChunkManager(this);
	}

	@Override
	public Chunk getChunk(int chunkX, int chunkZ, ChunkStatus status, boolean boolean_1)
	{
		ChunkPos pos = new ChunkPos(chunkX, chunkZ);
		if (chunkMap.containsKey(pos)) return chunkMap.get(pos);
		Biome[] biomes = new Biome[16 * 16];
		ClientWorld realWorld = CutsceneManager.getRealWorld();
		if (cloneExisting && realWorld != null)
		{
			for (int x = 0; x < 16; x++)
			{
				for (int z = 0; z < 16; z++)
				{
					biomes[x * 16 + z] = realWorld.getBiome(new BlockPos(chunkX * 16 + x, 0, chunkZ * 16 + z));
				}
			}
		} else
		{
			Arrays.fill(biomes, Biomes.PLAINS);
		}
		CutsceneChunk chunk = new CutsceneChunk(this, new ChunkPos(chunkX, chunkZ), biomes);
		chunkMap.put(pos, chunk);
		return chunk;
	}

	public void addPlayer(ClientPlayerEntity player)
	{
		getPlayers().add(player);
		((ClientWorldHooks) this).getCutsceneEntities().add(player);
	}

	@Override
	public void method_18116()
	{
		super.method_18116();
	}

	@Override
	public ClientChunkManager method_2935()
	{
		return cutsceneChunkManager;
	}

	@Override
	public ChunkManager getChunkManager()
	{
		return cutsceneChunkManager;
	}

	@Override
	public void method_8441(BooleanSupplier booleanSupplier_1)
	{
		this.getWorldBorder().update();
		this.tickTime();
		this.getProfiler().push("blocks");
		this.cutsceneChunkManager.tick(booleanSupplier_1);
		((ClientWorldHooks) this).updateCutsceneLighting();
		this.getProfiler().pop();
	}
}
