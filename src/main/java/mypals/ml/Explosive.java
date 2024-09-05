package mypals.ml;

import com.mojang.blaze3d.systems.RenderSystem;
import mypals.ml.explotionManage.ExplotionAffectdDataManage.ExplosionCastLines.ExplosionCastLine;
import net.fabricmc.fabric.api.client.rendering.v1.WorldRenderContext;
import net.fabricmc.fabric.api.client.rendering.v1.WorldRenderEvents;
import net.minecraft.client.option.KeyBinding;
import com.mojang.brigadier.CommandDispatcher;
import com.mojang.brigadier.arguments.BoolArgumentType;
import mypals.ml.KeyBindingManage.KeyBindings;
import mypals.ml.explotionManage.*;
import mypals.ml.explotionManage.ExplotionAffectdDataManage.DamagedEntityData.EntityToDamage;
import mypals.ml.explotionManage.ExplotionAffectdDataManage.DamagedEntityData.SamplePointsData.SamplePointData;
import mypals.ml.explotionManage.ExplotionAffectdDataManage.ExplosionAffectedObjects;
import mypals.ml.renderer.InfoRenderer;
import net.fabricmc.api.ModInitializer;
import net.fabricmc.fabric.api.client.command.v2.ClientCommandManager;
import net.fabricmc.fabric.api.client.command.v2.ClientCommandRegistrationCallback;
import net.fabricmc.fabric.api.client.command.v2.FabricClientCommandSource;
import net.fabricmc.fabric.api.client.event.lifecycle.v1.ClientTickEvents;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.render.*;
import net.minecraft.client.util.InputUtil;
import net.minecraft.client.util.math.MatrixStack;
import net.minecraft.text.Text;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Vec3d;
import net.minecraft.util.math.Vec3i;
import net.minecraft.world.World;
import org.lwjgl.glfw.GLFW;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.HashSet;
import java.util.List;
import java.util.Set;

import static mypals.ml.KeyBindingManage.KeyBindings.TOGGLE_RENDERER_E;
import static mypals.ml.KeyBindingManage.KeyBindings.TOGGLE_RENDERER_F3;
import static mypals.ml.explotionManage.ExplosionSimulateManager.simulateExplosiveBlocks;
import static mypals.ml.explotionManage.ExplosionSimulateManager.simulateExplosiveEntitys;
import static mypals.ml.renderer.InfoRenderer.render;

public class Explosive implements ModInitializer {
	public static final String MOD_ID = "explosive";

	// This logger is used to write text to the console and the log file.
	// It is considered best practice to use your mod id as the logger's name.
	// That way, it's clear which mod wrote info, warnings, and errors.
	public static final Logger LOGGER = LoggerFactory.getLogger(MOD_ID);




		public static Set<BlockPos> blocksToDestroy = new HashSet<>();
		public static Set<Vec3d> explotionCenters = new HashSet<>();
	    public static Set<EntityToDamage> entitysToDamage = new HashSet<>();

		public static  Set<SamplePointData> samplePointDatas = new HashSet<>();

		public static Set<ExplosionCastLine> explosionCastedLines = new HashSet<>();

		public static boolean showInfo = false;
		public static boolean showRayCastInfo = false;
		public static boolean showBlockDestroyInfo = false;
		public static boolean showDamageInfo = false;
		public static boolean showExplotionBlockDamageRayInfo = false;
		public static void register(CommandDispatcher<FabricClientCommandSource> dispatcher) {
			dispatcher.register(
					ClientCommandManager.literal("explosive")
							.then(ClientCommandManager.literal("showExplosionInfo")
								.then(ClientCommandManager.argument("toggle", BoolArgumentType.bool())
										.executes(context -> {
											boolean toggle = BoolArgumentType.getBool(context, "toggle");
											setOnOff(toggle);
											return 1;
										})
								)
							)
							.then(ClientCommandManager.literal("showExplosionEntityDamageInfo")
									.then(ClientCommandManager.argument("toggle", BoolArgumentType.bool())
											.executes(context -> {
												boolean toggle = BoolArgumentType.getBool(context, "toggle");

												setDamageOnOff(toggle);
												return 1;
											})
									))
							.then(ClientCommandManager.literal("showExplosionRayCastInfo")
									.then(ClientCommandManager.argument("toggle", BoolArgumentType.bool())
											.executes(context -> {
												boolean toggle = BoolArgumentType.getBool(context, "toggle");
												setRayCastInfoOnOff(toggle);
												return 1;
											})
									)
							)
							.then(ClientCommandManager.literal("showExplosionBlockDamageInfo")
									.then(ClientCommandManager.argument("toggle", BoolArgumentType.bool())
											.executes(context -> {
												boolean toggle = BoolArgumentType.getBool(context, "toggle");

												setBlockDestroyInfoOnOff(toggle);
												return 1;
											})
									))
							.then(ClientCommandManager.literal("showExplotionBlockDamageRayInfo")
									.then(ClientCommandManager.argument("toggle", BoolArgumentType.bool())
											.executes(context -> {
												boolean toggle = BoolArgumentType.getBool(context, "toggle");

												setExplotionBlockDamageRayInfoOnOff(toggle);
												return 1;
											})
									))
			);
		}
		public static void setOnOff(boolean toggle)
		{

			showInfo = toggle;
			if(toggle)
				showBlockDestroyInfo = true;
		}
		public static void setRayCastInfoOnOff(boolean toggle)
	{
		showRayCastInfo = toggle;
	}
		public static void setBlockDestroyInfoOnOff(boolean toggle)
	{
		showBlockDestroyInfo = toggle;
	}
		public static void setDamageOnOff(boolean toggle)
	{
		showDamageInfo = toggle;
	}
		public static void setExplotionBlockDamageRayInfoOnOff(boolean toggle)
	{
		showExplotionBlockDamageRayInfo = toggle;
	}
	@Override
	public void onInitialize() {
		WorldRenderEvents.BEFORE_DEBUG_RENDER.register((WorldRenderContext context) -> {
			if(showInfo) {
				RenderSystem.setShader(GameRenderer::getPositionColorProgram);
				RenderSystem.setShaderColor(1f, 1f, 1f, 1f);
				RenderSystem.depthMask(false);
				RenderSystem.enableBlend();
				RenderSystem.defaultBlendFunc();
				RenderSystem.setShaderColor(1f, 1f, 1f, 1f);
				BufferBuilder buffer = Tessellator.getInstance().begin(VertexFormat.DrawMode.DEBUG_LINES, VertexFormats.POSITION_COLOR);
				render(context.matrixStack(), context.tickCounter(), buffer);
				RenderSystem.applyModelViewMatrix();
				RenderSystem.setShaderColor(1, 1, 1, 1);

				RenderSystem.disableBlend();
			}
		});
		ClientCommandRegistrationCallback.EVENT.register((dispatcher, registryAccess) -> {
				register(dispatcher);
			});
			ClientTickEvents.END_CLIENT_TICK.register(this::onClientTick);
			MinecraftClient client = MinecraftClient.getInstance();
			if(client != null && client.getWindow() != null) {
				if (InputUtil.isKeyPressed(MinecraftClient.getInstance().getWindow().getHandle(), GLFW.GLFW_KEY_F3)) {
					if (InputUtil.isKeyPressed(MinecraftClient.getInstance().getWindow().getHandle(), GLFW.GLFW_KEY_E)) {
						setOnOff(!showInfo);
						assert MinecraftClient.getInstance().player != null;
						MinecraftClient.getInstance().player.sendMessage(Text.of("Explosive object render now set to " + showInfo), false);
						KeyBinding.unpressAll();
					}
				}
			}
		}
		private void onClientTick(MinecraftClient client) {
			if (showInfo) {
				try {
					explosionCastedLines.clear();
					blocksToDestroy.clear();
					entitysToDamage.clear();
					explotionCenters.clear();
					samplePointDatas.clear();
					if (client.world != null && client.player != null) {
						World world = client.world;
						BlockPos playerPos = client.player.getBlockPos();


						List<ExplosionData> exBlockPos = ExplosiveObjectFinder.findExplosiveBlocksInRange(world, playerPos);
						List<ExplosionData> exEntityPos = ExplosiveObjectFinder.findCrystlesInRange(world, playerPos);
						for (ExplosionData explotion : exBlockPos) {
							Vec3d p_d = new Vec3d(explotion.getPosition().toVector3f());
							Vec3i p_i = new Vec3i((int) p_d.x, (int) p_d.y, (int) p_d.z);
							ExplosionAffectedObjects EAO = simulateExplosiveBlocks(world, new BlockPos(p_i), explotion.getStrength());
							explosionCastedLines.addAll(EAO.getExplotionCastedLines());
							blocksToDestroy.addAll(EAO.getBlocksToDestriy());
							entitysToDamage.addAll(EAO.getEntitysToDamage());
							explotionCenters.addAll(EAO.getExplotionCenters());
							samplePointDatas.addAll(EAO.getSamplePointData());

						}
						for (ExplosionData explotion : exEntityPos) {
							ExplosionAffectedObjects EAO = simulateExplosiveEntitys(world, explotion.getPosition(), explotion.getStrength());
							explosionCastedLines.addAll(EAO.getExplotionCastedLines());
							blocksToDestroy.addAll(EAO.getBlocksToDestriy());
							entitysToDamage.addAll(EAO.getEntitysToDamage());
							explotionCenters.addAll(EAO.getExplotionCenters());
							samplePointDatas.addAll(EAO.getSamplePointData());
						}
						InfoRenderer.setCastedLines(explosionCastedLines);
						InfoRenderer.setBlocksToDamage(blocksToDestroy);
						InfoRenderer.setEntitysToDamage(entitysToDamage);
						InfoRenderer.setExplotionCenters(explotionCenters);
						InfoRenderer.setSamplePointData(samplePointDatas);
					}
				} catch (Exception e) {
				e.printStackTrace();
			}
		}
	}
}