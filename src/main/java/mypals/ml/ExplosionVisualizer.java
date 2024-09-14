package mypals.ml;

import com.mojang.blaze3d.systems.RenderSystem;
import com.mojang.brigadier.arguments.FloatArgumentType;
import com.mojang.brigadier.arguments.IntegerArgumentType;
import com.mojang.brigadier.arguments.StringArgumentType;
import com.mojang.brigadier.suggestion.SuggestionProvider;
import mypals.ml.config.VisualizerConfig;
import mypals.ml.explotionManage.ExplotionAffectdDataManage.ExplosionCastLines.ExplosionCastLine;
import net.fabricmc.fabric.api.client.rendering.v1.WorldRenderContext;
import net.fabricmc.fabric.api.client.rendering.v1.WorldRenderEvents;
import net.minecraft.client.network.ClientPlayerEntity;
import net.minecraft.client.option.KeyBinding;
import com.mojang.brigadier.CommandDispatcher;
import com.mojang.brigadier.arguments.BoolArgumentType;
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
import net.minecraft.command.CommandSource;
import net.minecraft.entity.EntityType;
import net.minecraft.entity.decoration.DisplayEntity;
import net.minecraft.nbt.NbtCompound;
import net.minecraft.text.Text;
import net.minecraft.util.Formatting;
import net.minecraft.util.Identifier;
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

import static mypals.ml.explotionManage.ExplosionSimulateManager.*;
import static mypals.ml.renderer.IRenderer.renderSelectionBox;
import static mypals.ml.renderer.InfoRenderer.render;

public class ExplosionVisualizer implements ModInitializer {
	//public static final String MOD_ID = "explosive";
	public static final String MOD_ID = "explosion-visualizer";

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
		public static int LayerMin = 0, LayerMax = 100;


	public static Identifier id(String path) {
		return Identifier.of(MOD_ID, path);
	}
		public static void register(CommandDispatcher<FabricClientCommandSource> dispatcher) {
			ClientPlayerEntity player = MinecraftClient.getInstance().player;
			dispatcher.register(
					ClientCommandManager.literal("explosionVisualizer")
							.then(ClientCommandManager.literal("mainRender")
								.then(ClientCommandManager.argument("toggle", BoolArgumentType.bool())
										.executes(context -> {
											boolean toggle = BoolArgumentType.getBool(context, "toggle");

											Text coloredMessage = Text.literal("ExplosionVisualizer: Main render -> " + toggle).formatted(Formatting.GOLD);

                                            assert player != null;
                                            player.sendMessage(coloredMessage, false);

											setOnOff(toggle);
											return 1;
										})
								)
							)
							.then(ClientCommandManager.literal("renderEntityDamage")
									.then(ClientCommandManager.argument("toggle", BoolArgumentType.bool())
											.executes(context -> {
												boolean toggle = BoolArgumentType.getBool(context, "toggle");

												Text coloredMessage = Text.literal("ExplosionVisualizer: Entity damage render -> " + toggle).formatted(Formatting.GREEN);

                                                assert player != null;
                                                player.sendMessage(coloredMessage, false);

												setDamageOnOff(toggle);
												return 1;
											})
									))
							.then(ClientCommandManager.literal("renderEntityRayCast")
									.then(ClientCommandManager.argument("toggle", BoolArgumentType.bool())
											.executes(context -> {
												boolean toggle = BoolArgumentType.getBool(context, "toggle");

												Text coloredMessage = Text.literal("ExplosionVisualizer: Entity damage ray/sample-point render -> " + toggle).formatted(Formatting.GREEN);

                                                assert player != null;
                                                player.sendMessage(coloredMessage, false);

												setRayCastInfoOnOff(toggle);
												return 1;
											})
									)
							)
							.then(ClientCommandManager.literal("renderBlockDestruction")
									.then(ClientCommandManager.argument("toggle", BoolArgumentType.bool())
											.executes(context -> {
												boolean toggle = BoolArgumentType.getBool(context, "toggle");

												Text coloredMessage = Text.literal("ExplosionVisualizer: Block destruction render -> " + toggle).formatted(Formatting.GREEN);

                                                assert player != null;
                                                player.sendMessage(coloredMessage, false);

												setBlockDestroyInfoOnOff(toggle);
												return 1;
											})
									))
							.then(ClientCommandManager.literal("renderBlockDetectionRay")
									.then(ClientCommandManager.argument("toggle", BoolArgumentType.bool())
											.executes(context -> {
												boolean toggle = BoolArgumentType.getBool(context, "toggle");

												Text coloredMessage = Text.literal("ExplosionVisualizer: Block detection ray render -> " + toggle).formatted(Formatting.GREEN);

												assert player != null;
												player.sendMessage(coloredMessage, false);

												setExplosionBlockDamageRayInfoOnOff(toggle);
												return 1;
											})
									))
							.then(ClientCommandManager.literal("blockDamageRayRendererSettings")
									.then(ClientCommandManager.literal("range")
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
																							SetDestructionRayRenderRange(Xmin, Xmax, Ymin,Ymax,Zmin,Zmax);

																							Text coloredMessage = Text.literal("ExplosionVisualizer: Block detection ray render range updated.").formatted(Formatting.GREEN);

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
									.then(ClientCommandManager.literal("layer")
											.then(ClientCommandManager.argument("LayerMin", IntegerArgumentType.integer(0, 114514))
													.then(ClientCommandManager.argument("LayerMax", IntegerArgumentType.integer(0, 114514))
															.executes(context -> {
																int LayerMin = IntegerArgumentType.getInteger(context, "LayerMin");
																int LayerMax = IntegerArgumentType.getInteger(context, "LayerMax");
																SetDestructionRayRenderLayer(LayerMin,LayerMax);

																Text coloredMessage = Text.literal("ExplosionVisualizer: Block detection ray render layer updated.").formatted(Formatting.GREEN);

																assert player != null;
																player.sendMessage(coloredMessage, false);

																return 1;
															})
													)
											)
									)
									.then(ClientCommandManager.literal("resetAll")
										.executes(context -> {
											SetDestructionRayRenderLayer(0,100);
											SetDestructionRayRenderRange(0,16,0,16,0,16);

											Text coloredMessage = Text.literal("ExplosionVisualizer: Block detection ray render settings rested!").formatted(Formatting.RED);

											assert player != null;
											player.sendMessage(coloredMessage, false);

											return 1;
										})
									)
									.then(ClientCommandManager.literal("resetLayer")
											.executes(context -> {
												SetDestructionRayRenderLayer(0,100);

												Text coloredMessage = Text.literal("ExplosionVisualizer: Block detection ray render layer rested!").formatted(Formatting.YELLOW);

												assert player != null;
												player.sendMessage(coloredMessage, false);

												return 1;
											})
									)
									.then(ClientCommandManager.literal("resetRange")
											.executes(context -> {
												SetDestructionRayRenderRange(0,16,0,16,0,16);

												Text coloredMessage = Text.literal("ExplosionVisualizer: Block detection ray render range rested!").formatted(Formatting.YELLOW);

												assert player != null;
												player.sendMessage(coloredMessage, false);

												return 1;
											})
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

																								Text coloredMessage = Text.literal("ExplosionVisualizer: Failed to add fake explosion " + name + "at " + new Vec3d(x, y, z) + " : duplicate naming!").formatted(Formatting.RED);

																								assert player != null;
																								player.sendMessage(coloredMessage, false);
																								return 1;
																							}
																						}
																						fakeExplosions.add(new FakeExplosion(x, y, z, p, ignoreBlockInside, name));
																						Text coloredMessage = Text.literal("ExplosionVisualizer: Fake explosion :" + name + " was added at " + new Vec3d(x, y, z) + " with power of" + p).formatted(Formatting.GREEN);
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
														Text coloredMessage = Text.literal("ExplosionVisualizer: Removed fake explosion : " + n).formatted(Formatting.YELLOW);

														assert player != null;
														player.sendMessage(coloredMessage, false);
														return 1;
													})
											)
											.then(ClientCommandManager.literal("all")
													.executes(context -> {
														fakeExplosions.clear();
														Text coloredMessage = Text.literal("ExplosionVisualizer: Cleared all fake explosions").formatted(Formatting.RED);

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

			Set<String> names = explosions.stream()
					.map(fakeExplosion -> fakeExplosion.name)
					.collect(Collectors.toSet());
			return CommandSource.suggestMatching(names, builder);
		};
	}

	public static void FixRangeIssue()
	{
		if(VisualizerConfig.HANDLER.instance().Xmax < VisualizerConfig.HANDLER.instance().Xmin)
		{
			VisualizerConfig.HANDLER.instance().Xmax = VisualizerConfig.HANDLER.instance().Xmin;
			VisualizerConfig.HANDLER.save();
			UpadteSettings();
		}
		if(VisualizerConfig.HANDLER.instance().Ymax < VisualizerConfig.HANDLER.instance().Ymin)
		{
			VisualizerConfig.HANDLER.instance().Ymax = VisualizerConfig.HANDLER.instance().Ymin;
			VisualizerConfig.HANDLER.save();
			UpadteSettings();
		}
		if(VisualizerConfig.HANDLER.instance().Zmax < VisualizerConfig.HANDLER.instance().Zmin)
		{
			VisualizerConfig.HANDLER.instance().Zmax = VisualizerConfig.HANDLER.instance().Zmin;
			VisualizerConfig.HANDLER.save();
			UpadteSettings();
		}
		if(VisualizerConfig.HANDLER.instance().LayerMax < VisualizerConfig.HANDLER.instance().LayerMin)
		{
			VisualizerConfig.HANDLER.instance().LayerMax = VisualizerConfig.HANDLER.instance().LayerMin + 1;
			VisualizerConfig.HANDLER.save();
			UpadteSettings();
		}


	}
	public static void SetDestructionRayRenderRange(int XMin, int XMax,int YMin, int YMax,int ZMin, int ZMax)
	{
		VisualizerConfig.HANDLER.instance().Xmin = XMin;
		VisualizerConfig.HANDLER.instance().Xmax = XMax;
		VisualizerConfig.HANDLER.instance().Ymin = YMin;
		VisualizerConfig.HANDLER.instance().Ymax = YMax;
		VisualizerConfig.HANDLER.instance().Zmin = ZMin;
		VisualizerConfig.HANDLER.instance().Zmax = ZMax;
		VisualizerConfig.HANDLER.save();
		UpadteSettings();
	}
	public static void SetDestructionRayRenderLayer(int min, int max)
	{
		VisualizerConfig.HANDLER.instance().LayerMax = max;
		VisualizerConfig.HANDLER.instance().LayerMin = min;
		VisualizerConfig.HANDLER.save();
		UpadteSettings();
	}
	public static void setOnOff(boolean toggle)
	{
		VisualizerConfig.HANDLER.instance().showInfo = toggle;
		if(toggle)
			VisualizerConfig.HANDLER.instance().showBlockDestroyInfo = true;
		VisualizerConfig.HANDLER.save();
		UpadteSettings();
	}
	public static void setRayCastInfoOnOff(boolean toggle)
	{
		VisualizerConfig.HANDLER.instance().showRayCastInfo = toggle;
		if(toggle)
			VisualizerConfig.HANDLER.instance().showInfo = true;
		VisualizerConfig.HANDLER.save();
		UpadteSettings();
	}
		public static void setBlockDestroyInfoOnOff(boolean toggle)
	{
		VisualizerConfig.HANDLER.instance().showBlockDestroyInfo = toggle;
		if(toggle)
			VisualizerConfig.HANDLER.instance().showInfo = true;
		VisualizerConfig.HANDLER.save();
		UpadteSettings();
	}
		public static void setDamageOnOff(boolean toggle)
	{
		VisualizerConfig.HANDLER.instance().showDamageInfo = toggle;
		if(toggle)
			VisualizerConfig.HANDLER.instance().showInfo = true;
		VisualizerConfig.HANDLER.save();
		UpadteSettings();
	}
		public static void setExplosionBlockDamageRayInfoOnOff(boolean toggle)
	{
		VisualizerConfig.HANDLER.instance().showExplosionBlockDamageRayInfo = toggle;
		if(toggle)
			VisualizerConfig.HANDLER.instance().showInfo = true;
		VisualizerConfig.HANDLER.save();
		UpadteSettings();
	}
	public static void UpadteSettings()
	{
		var instance = VisualizerConfig.HANDLER;
		instance.load();

		showInfo = instance.instance().showInfo;
		showDamageInfo = instance.instance().showDamageInfo;
		showBlockDestroyInfo = instance.instance().showBlockDestroyInfo;
		showRayCastInfo = instance.instance().showRayCastInfo;
		showExplosionBlockDamageRayInfo = instance.instance().showExplosionBlockDamageRayInfo;

		Xmin = instance.instance().Xmin;
		Xmax = instance.instance().Xmax;
		Ymin = instance.instance().Ymin;
		Ymax = instance.instance().Ymax;
		Zmin = instance.instance().Zmin;
		Zmax = instance.instance().Zmax;

		LayerMin = instance.instance().LayerMin;
		LayerMax = instance.instance().LayerMax;


	}
	@Override
	public void onInitialize() {
		UpadteSettings();

		WorldRenderEvents.AFTER_ENTITIES.register((WorldRenderContext context) -> {
			Camera camera = MinecraftClient.getInstance().gameRenderer.getCamera();
			renderSelectionBox(context.matrixStack(), camera, new BlockPos(0, 0, 0));


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
			FixRangeIssue();
			assert MinecraftClient.getInstance() != null;
			//createGlowingBlockDisplay(MinecraftClient.getInstance().world, new BlockPos(0, 0, 0));
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