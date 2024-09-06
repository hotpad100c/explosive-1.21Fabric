package mypals.ml;

import com.mojang.blaze3d.systems.RenderSystem;
import com.mojang.brigadier.arguments.FloatArgumentType;
import com.mojang.brigadier.arguments.IntegerArgumentType;
import com.mojang.brigadier.arguments.StringArgumentType;
import com.mojang.brigadier.suggestion.SuggestionProvider;
import mypals.ml.explotionManage.ExplotionAffectdDataManage.ExplosionCastLines.ExplosionCastLine;
import net.fabricmc.fabric.api.client.rendering.v1.WorldRenderContext;
import net.fabricmc.fabric.api.client.rendering.v1.WorldRenderEvents;
import net.minecraft.client.network.ClientPlayerEntity;
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
import net.minecraft.command.CommandSource;
import net.minecraft.text.Text;
import net.minecraft.util.Formatting;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Vec3d;
import net.minecraft.util.math.Vec3i;
import net.minecraft.world.World;
import org.lwjgl.glfw.GLFW;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.HashSet;
import java.util.List;
import java.util.Objects;
import java.util.Set;
import java.util.stream.Collectors;

import static mypals.ml.KeyBindingManage.KeyBindings.TOGGLE_RENDERER_E;
import static mypals.ml.KeyBindingManage.KeyBindings.TOGGLE_RENDERER_F3;
import static mypals.ml.explotionManage.ExplosionSimulateManager.*;
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

		public static  Set<FakeExplosion> fakeExplosions = new HashSet<>();

		public static Set<ExplosionCastLine> explosionCastedLines = new HashSet<>();

		public static boolean showInfo = false;
		public static boolean showRayCastInfo = false;
		public static boolean showBlockDestroyInfo = false;
		public static boolean showDamageInfo = false;
		public static boolean showExplosionBlockDamageRayInfo = false;


		public static int Xmin = 0, Ymin = 0, Zmin = 0;
		public static int Xmax = 16, Ymax = 16, Zmax = 16;


		public static void register(CommandDispatcher<FabricClientCommandSource> dispatcher) {
			ClientPlayerEntity player = MinecraftClient.getInstance().player;
			dispatcher.register(
					ClientCommandManager.literal("explosive")
							.then(ClientCommandManager.literal("mainRender")
								.then(ClientCommandManager.argument("toggle", BoolArgumentType.bool())
										.executes(context -> {
											boolean toggle = BoolArgumentType.getBool(context, "toggle");

											Text coloredMessage = Text.literal("Explosive: Main render -> " + toggle).formatted(Formatting.GOLD);

                                            assert player != null;
                                            player.sendMessage(coloredMessage, false);

											setOnOff(toggle);
											return 1;
										})
								)
							)
							.then(ClientCommandManager.literal("renderEntityDamageInfo")
									.then(ClientCommandManager.argument("toggle", BoolArgumentType.bool())
											.executes(context -> {
												boolean toggle = BoolArgumentType.getBool(context, "toggle");

												Text coloredMessage = Text.literal("Explosive: Entity damage render -> " + toggle).formatted(Formatting.GREEN);

                                                assert player != null;
                                                player.sendMessage(coloredMessage, false);

												setDamageOnOff(toggle);
												return 1;
											})
									))
							.then(ClientCommandManager.literal("renderEntityRayCastInfo")
									.then(ClientCommandManager.argument("toggle", BoolArgumentType.bool())
											.executes(context -> {
												boolean toggle = BoolArgumentType.getBool(context, "toggle");

												Text coloredMessage = Text.literal("Explosive: Entity damage ray/sample-point render -> " + toggle).formatted(Formatting.GREEN);

                                                assert player != null;
                                                player.sendMessage(coloredMessage, false);

												setRayCastInfoOnOff(toggle);
												return 1;
											})
									)
							)
							.then(ClientCommandManager.literal("renderBlockDestructionInfo")
									.then(ClientCommandManager.argument("toggle", BoolArgumentType.bool())
											.executes(context -> {
												boolean toggle = BoolArgumentType.getBool(context, "toggle");

												Text coloredMessage = Text.literal("Explosive: Block destruction render -> " + toggle).formatted(Formatting.GREEN);

                                                assert player != null;
                                                player.sendMessage(coloredMessage, false);

												setBlockDestroyInfoOnOff(toggle);
												return 1;
											})
									))
							.then(ClientCommandManager.literal("renderExplosionBlockDetectionRayInfo")
									.then(ClientCommandManager.argument("toggle", BoolArgumentType.bool())
											.executes(context -> {
												boolean toggle = BoolArgumentType.getBool(context, "toggle");

												Text coloredMessage = Text.literal("Explosive: Block detection ray render -> " + toggle).formatted(Formatting.GREEN);

												assert player != null;
												player.sendMessage(coloredMessage, false);

												setExplosionBlockDamageRayInfoOnOff(toggle);
												return 1;
											})
									))
							.then(ClientCommandManager.literal("blockDamageRayRendererRangeSettings")
									.then(ClientCommandManager.argument("Xmin", IntegerArgumentType.integer(-1, 16))
											.then(ClientCommandManager.argument("Xmax", IntegerArgumentType.integer(-1, 16))
													.then(ClientCommandManager.argument("Ymin", IntegerArgumentType.integer(-1, 16))
															.then(ClientCommandManager.argument("Ymax", IntegerArgumentType.integer(-1, 16))
																	.then(ClientCommandManager.argument("Zmin", IntegerArgumentType.integer(-1, 16))
																			.then(ClientCommandManager.argument("Zmax", IntegerArgumentType.integer(-1, 16))
																					.executes(context -> {
																						int Xmin = IntegerArgumentType.getInteger(context, "Xmin");
																						int Xmax = IntegerArgumentType.getInteger(context, "Xmax");
																						int Ymin = IntegerArgumentType.getInteger(context, "Ymin");
																						int Ymax = IntegerArgumentType.getInteger(context, "Ymax");
																						int Zmin = IntegerArgumentType.getInteger(context, "Zmin");
																						int Zmax = IntegerArgumentType.getInteger(context, "Zmax");
																						setDestructionRayRendererRange(Xmin, Xmax, Ymin,Ymax,Zmin,Zmax);

																						Text coloredMessage = Text.literal("Explosive: Block detection ray render range updated.").formatted(Formatting.GREEN);

																						assert player != null;
																						player.sendMessage(coloredMessage, false);

																						return 1;
																					})
																			)
																	)
															)
													)
											)
									)
							)
							.then(ClientCommandManager.literal("fakeExplosion")
									.then(ClientCommandManager.literal("add")
											.then(ClientCommandManager.argument("name", StringArgumentType.string())
												.then(ClientCommandManager.argument("x", FloatArgumentType.floatArg())
														.then(ClientCommandManager.argument("y", FloatArgumentType.floatArg())
																.then(ClientCommandManager.argument("z", FloatArgumentType.floatArg())
																		.then(ClientCommandManager.argument("power", FloatArgumentType.floatArg())
																				.then(ClientCommandManager.argument("ignoreBlockInside", BoolArgumentType.bool())
																					.executes(context -> {

																						float x = FloatArgumentType.getFloat(context, "x");
																						float y = FloatArgumentType.getFloat(context, "y");
																						float z = FloatArgumentType.getFloat(context, "z");
																						float p = FloatArgumentType.getFloat(context, "power");
																						boolean ignoreBlockInside = BoolArgumentType.getBool(context,"ignoreBlockInside");
																						String name = StringArgumentType.getString(context, "name");

																						for(FakeExplosion FE : fakeExplosions)
																						{
																							if(Objects.equals(FE.name, name)) {

																								Text coloredMessage = Text.literal("Explosive: Failed to add fake explosion " + name + "at " + new Vec3d(x, y, z) + " : duplicate naming!").formatted(Formatting.RED);

																								assert player != null;
																								player.sendMessage(coloredMessage, false);
																								return 1;
																							}
																						}
																						fakeExplosions.add(new FakeExplosion(x, y, z, p, ignoreBlockInside, name));
																						Text coloredMessage = Text.literal("Explosive: Fake explosion :" + name + " was added at " + new Vec3d(x, y, z) + " with power of" + p).formatted(Formatting.GREEN);
																						assert player != null;
																						player.sendMessage(coloredMessage, false);
																						return 1;
																					})
																				)
																		)
																)
														)
												)
											)
									)
									.then(ClientCommandManager.literal("remove")
											.then(ClientCommandManager.argument("name", StringArgumentType.string())
													.suggests(suggestFromSet(fakeExplosions))
													.executes(context -> {
														String n = StringArgumentType.getString(context, "name");
                                                        fakeExplosions.removeIf(fe -> Objects.equals(fe.name, n));
														Text coloredMessage = Text.literal("Explosive: Removed fake explosion : " + n).formatted(Formatting.DARK_GREEN);

														assert player != null;
														player.sendMessage(coloredMessage, false);
														return 1;
													})
											)
											.then(ClientCommandManager.literal("all")
													.executes(context -> {
														fakeExplosions.clear();
														Text coloredMessage = Text.literal("Explosive: Cleared all fake explosions").formatted(Formatting.DARK_GREEN);

														assert player != null;
														player.sendMessage(coloredMessage, false);
														return 1;
													})
											)
									)
							)
			);
		}
	private static SuggestionProvider<FabricClientCommandSource> suggestFromSet(Set<FakeExplosion> explosions) {
		return (context, builder) -> {
			// 提取每个FakeExplosion的name变量，并提供为建议
			Set<String> names = explosions.stream()
					.map(fakeExplosion -> fakeExplosion.name)
					.collect(Collectors.toSet());
			return CommandSource.suggestMatching(names, builder);
		};
	}
		public static void setDestructionRayRendererRange(int XMin, int XMax,int YMin, int YMax,int ZMin, int ZMax)
		{
			Xmin = XMin;
			Xmax = XMax;
			Ymin = YMin;
			Ymax = YMax;
			Zmin = ZMin;
			Zmax = ZMax;
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
		if(toggle)
			showInfo = true;
	}
		public static void setBlockDestroyInfoOnOff(boolean toggle)
	{
		showBlockDestroyInfo = toggle;
		if(toggle)
			showInfo = true;
	}
		public static void setDamageOnOff(boolean toggle)
	{
		showDamageInfo = toggle;
		if(toggle)
			showInfo = true;
	}
		public static void setExplosionBlockDamageRayInfoOnOff(boolean toggle)
	{
		showExplosionBlockDamageRayInfo = toggle;
		if(toggle)
			showInfo = true;
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
						for (ExplosionData explosion : exEntityPos) {
							ExplosionAffectedObjects EAO = simulateExplosiveEntitys(world, explosion.getPosition(), explosion.getStrength());
							explosionCastedLines.addAll(EAO.getExplotionCastedLines());
							blocksToDestroy.addAll(EAO.getBlocksToDestriy());
							entitysToDamage.addAll(EAO.getEntitysToDamage());
							explotionCenters.addAll(EAO.getExplotionCenters());
							samplePointDatas.addAll(EAO.getSamplePointData());
						}
						for(FakeExplosion fe: fakeExplosions)
						{
							ExplosionAffectedObjects EAO = simulateFakeExplosions(world, new Vec3d(fe.x, fe.y, fe.z), fe.power, fe.ignorBlockInside);
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