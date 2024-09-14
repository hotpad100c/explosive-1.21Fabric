package mypals.ml.config;
import com.terraformersmc.modmenu.api.ConfigScreenFactory;
import com.terraformersmc.modmenu.api.ModMenuApi;
import dev.isxander.yacl3.api.*;
import dev.isxander.yacl3.api.controller.*;

import java.awt.*;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.Optional;
import java.util.concurrent.CompletableFuture;
import dev.isxander.yacl3.gui.image.ImageRenderer;
import mypals.ml.ExplosionVisualizer;
import mypals.ml.explotionManage.FakeExplosion;
import net.minecraft.client.MinecraftClient;
import net.minecraft.text.Text;
import net.minecraft.util.Colors;
import net.minecraft.util.Formatting;
import net.minecraft.util.Identifier;

import static mypals.ml.ExplosionVisualizer.UpadteSettings;


public class ModMenuIntegration implements ModMenuApi {
    public static boolean test = false;
    public static int intSlider = 5;
    public static int Xmin = 0, Ymin = 0, Zmin = 0;
    public static int Xmax = 16, Ymax = 16, Zmax = 16;
    public static int LayerMin = 0, LayerMax = 100;


    @Override
    public ConfigScreenFactory<?> getModConfigScreenFactory() {
        MinecraftClient client = MinecraftClient.getInstance();
        var instance = VisualizerConfig.HANDLER;
        return parentScreen -> YetAnotherConfigLib.createBuilder()
                .title(Text.literal("ExplosionVisualizer"))
                .category(ConfigCategory.createBuilder()
                        .name(Text.literal("ExplosionVisualizer"))

                        .group(OptionGroup.createBuilder()
                                .name(Text.literal("Renders"))
                                //.description(OptionDescription.of(Text.literal("Main mod features")))
                                .description(OptionDescription.createBuilder()
                                        .text(Text.literal("Main features"))
                                        .image(ExplosionVisualizer.id("textures/all-explosives.png"), 192, 108)
                                        .build())

                                .option(Option.<Boolean>createBuilder()
                                        .name(Text.literal("Main render"))
                                        .description(OptionDescription.createBuilder()
                                                .text(Text.literal("This is the main render.Make sure to turn this on first!").formatted(Formatting.YELLOW))
                                                .image(ExplosionVisualizer.id("textures/main-render.png"), 192, 108)
                                                .build()
                                        )
                                        .binding(true, () -> instance.instance().showInfo, bool -> instance.instance().showInfo = bool)
                                        .controller(BooleanControllerBuilder::create)
                                        .build())

                                .option(Option.<Boolean>createBuilder()
                                        .name(Text.literal("Block destruction"))
                                        .description(OptionDescription.createBuilder()
                                                .text(Text.literal("Render the blocks to be destroyed by explosion."))
                                                .image(ExplosionVisualizer.id("textures/block-destruction.png"), 192, 108)
                                                .build()
                                        )
                                        .binding(false, () -> instance.instance().showBlockDestroyInfo, bool -> instance.instance().showBlockDestroyInfo = bool)
                                        .controller(BooleanControllerBuilder::create)
                                        .build())

                                .option(Option.<Boolean>createBuilder()
                                        .name(Text.literal("Block detection ray"))
                                        .description(OptionDescription.createBuilder()
                                                .text(Text.literal("Render the detection rays for detecting the blocks to be destroyed by explosion."))
                                                .image(ExplosionVisualizer.id("textures/ray.png"), 192, 108)
                                                .build()
                                        )
                                        .binding(false, () -> instance.instance().showExplosionBlockDamageRayInfo, bool -> instance.instance().showExplosionBlockDamageRayInfo = bool)
                                        .controller(BooleanControllerBuilder::create)
                                        .build())

                                .option(Option.<Boolean>createBuilder()
                                        .name(Text.literal("Sample-points"))
                                        .description(OptionDescription.createBuilder()
                                                .text(Text.literal("Render in range entity's sample-points for ray-cast in explosion."))
                                                .image(ExplosionVisualizer.id("textures/sample-points.png"), 192, 108)
                                                .build()
                                        )
                                        .binding(false, () -> instance.instance().showRayCastInfo, bool -> instance.instance().showRayCastInfo = bool)
                                        .controller(BooleanControllerBuilder::create)
                                        .build())

                                .option(Option.<Boolean>createBuilder()
                                        .name(Text.literal("Explosion damage"))
                                        .description(OptionDescription.createBuilder()
                                                .text(Text.literal("Render explosion damage for in-range entities."))
                                                .image(ExplosionVisualizer.id("textures/damage.png"), 192, 108)
                                                .build()
                                        )
                                        .binding(false, () -> instance.instance().showDamageInfo, bool -> instance.instance().showDamageInfo = bool)
                                        .controller(BooleanControllerBuilder::create)
                                        .build())

                                .build())

                        .build()
                )

                .title(Text.literal("Settings"))
                .category(ConfigCategory.createBuilder()
                        .name(Text.literal("Settings"))
                        .group(OptionGroup.createBuilder()
                                .name(Text.literal("Block detection ray settings"))
                                .description(OptionDescription.createBuilder()
                                        .text(Text.literal("In Minecraft, 1532 rays are emitted from the center of the explosion towards the outermost layer of a 16x16x16 voxel grid surrounding the explosion center. \n" +
                                        "(See\" https://minecraft.fandom.com/wiki/Explosion\" for details.)\n" +
                                        "\n" +
                                        "Displaying them all together can look very cluttered. This command allows you to slice these rays for easier viewing."))
                                        .image(ExplosionVisualizer.id("textures/example.png"), 500, 500)
                                        .build())
                                .option(Option.<Integer>createBuilder()
                                        .name(Text.literal("X-min"))
                                        .description(OptionDescription.of(Text.literal("(0~16)")))
                                        .binding(0, () -> instance.instance().Xmin, v -> instance.instance().Xmin = v)
                                        .controller(opt -> IntegerSliderControllerBuilder.create(opt)
                                                .range(0, 16)
                                                .step(1)
                                                .formatValue(val -> Text.literal(val + "")))
                                        .build())

                                .option(Option.<Integer>createBuilder()
                                        .name(Text.literal("X-max"))
                                        .description(OptionDescription.of(Text.literal("(0~16)")))
                                        .binding(16, () -> instance.instance().Xmax, v -> instance.instance().Xmax = v)
                                        .controller(opt -> IntegerSliderControllerBuilder.create(opt)
                                                .range(0, 16)
                                                .step(1)
                                                .formatValue(val -> Text.literal(val + "")))
                                        .build())
                                .option(Option.<Integer>createBuilder()
                                        .name(Text.literal("Y-min"))
                                        .description(OptionDescription.of(Text.literal("(0~16)")))
                                        .binding(0, () -> instance.instance().Ymin, v -> instance.instance().Ymin = v)
                                        .controller(opt -> IntegerSliderControllerBuilder.create(opt)
                                                .range(0, 16)
                                                .step(1)
                                                .formatValue(val -> Text.literal(val + "")))
                                        .build())

                                .option(Option.<Integer>createBuilder()
                                        .name(Text.literal("Y-max"))
                                        .description(OptionDescription.of(Text.literal("(0~16)")))
                                        .binding(16, () -> instance.instance().Ymax, v -> instance.instance().Ymax = v)
                                        .controller(opt -> IntegerSliderControllerBuilder.create(opt)
                                                .range(0, 16)
                                                .step(1)
                                                .formatValue(val -> Text.literal(val + "")))
                                        .build())
                                .option(Option.<Integer>createBuilder()
                                        .name(Text.literal("Z-min"))
                                        .description(OptionDescription.of(Text.literal("(0~16)")))
                                        .binding(0, () -> instance.instance().Zmin, v -> instance.instance().Zmin = v)
                                        .controller(opt -> IntegerSliderControllerBuilder.create(opt)
                                                .range(0, 16)
                                                .step(1)
                                                .formatValue(val -> Text.literal(val + "")))
                                        .build())
                                .option(Option.<Integer>createBuilder()
                                        .name(Text.literal("Z-max"))
                                        .description(OptionDescription.of(Text.literal("(0~16)")))
                                        .binding(16, () -> instance.instance().Zmax, v -> instance.instance().Zmax = v)
                                        .controller(opt -> IntegerSliderControllerBuilder.create(opt)
                                                .range(0, 16)
                                                .step(1)
                                                .formatValue(val -> Text.literal(val + "")))
                                        .build())

                                .option(Option.<Boolean>createBuilder()
                                        .name(Text.literal("Invert"))
                                        .binding(false, () -> instance.instance().Invert, bool -> instance.instance().Invert = bool)
                                        .controller(BooleanControllerBuilder::create)
                                        .build())

                                .option(Option.<Integer>createBuilder()
                                        .name(Text.literal("Layer-min"))
                                        .description(OptionDescription.of(Text.literal("Minimal rendering layer")))
                                        .binding(0, () -> instance.instance().LayerMin, v -> instance.instance().LayerMin = v)
                                        .controller(opt -> IntegerSliderControllerBuilder.create(opt)
                                                .range(0, 100)
                                                .step(1)
                                                .formatValue(val -> Text.literal(val + "")))
                                        .build())

                                .option(Option.<Integer>createBuilder()
                                        .name(Text.literal("Layer-max"))
                                        .description(OptionDescription.of(Text.literal("Maximum rendering layer")))
                                        .binding(100, () -> instance.instance().LayerMax, v -> instance.instance().LayerMax = v)
                                        .controller(opt -> IntegerSliderControllerBuilder.create(opt)
                                                .range(0, 100)
                                                .step(1)
                                                .formatValue(val -> Text.literal(val + "")))
                                        .build())
                                .option(Option.<String>createBuilder()
                                        .name(Text.literal("Block detection ray render icon"))
                                        .description(OptionDescription.of(Text.literal("Text of the icon for the detection rays")))
                                        .binding("⧈", () -> instance.instance().BlockDetectionRayIcon, v -> instance.instance().BlockDetectionRayIcon = v)
                                        .controller(StringControllerBuilder::create)
                                        .build())
                                .option(Option.<Float>createBuilder()
                                        .name(Text.literal("Block detection ray render icon_size"))
                                        .description(OptionDescription.of(Text.literal("Size of the icon for the detection rays")))
                                        .binding(0.05F, () -> instance.instance().BlockDetectionRayIconSize, v -> instance.instance().BlockDetectionRayIconSize = v)
                                        .controller(opt -> FloatSliderControllerBuilder.create(opt)
                                                .range(0F, 0.1F)
                                                .step(0.005F)
                                                .formatValue(val -> Text.literal(val + "")))
                                        .build())
                                .option(Option.<Boolean>createBuilder()
                                        .name(Text.literal("Render with alpha"))
                                        .description(OptionDescription.of(Text.literal("Render explosion power by ray's alpha.")))
                                        .binding(false, () -> instance.instance().EnableAlpha, bool -> instance.instance().EnableAlpha = bool)
                                        .controller(BooleanControllerBuilder::create)
                                        .build())


                                .build()

                        )
                        .group(OptionGroup.createBuilder()
                                .name(Text.literal("Block-destruction render settings"))
                                .option(Option.<Color>createBuilder()
                                        .name(Text.literal("Block destruction render color"))
                                        .description(OptionDescription.of(Text.literal("Color of the icon for the blocks will be destroyed by the explosion")))
                                        .binding(Color.YELLOW, () -> instance.instance().BlockDestroyIconColor, v -> instance.instance().BlockDestroyIconColor = v)
                                        .controller(opt -> ColorControllerBuilder.create(opt)
                                                .allowAlpha(true))
                                        .build())
                                .option(Option.<String>createBuilder()
                                        .name(Text.literal("Block destruction render icon"))
                                        .description(OptionDescription.of(Text.literal("Text of the icon for the blocks will be destroyed by the explosion")))
                                        .binding("!", () -> instance.instance().BlockDestroyIcon, v -> instance.instance().BlockDestroyIcon = v)
                                        .controller(StringControllerBuilder::create)
                                        .build())
                                .option(Option.<Float>createBuilder()
                                        .name(Text.literal("Block destruction render icon_size"))
                                        .description(OptionDescription.of(Text.literal("Size of the icon for the blocks will be destroyed by the explosion")))
                                        .binding(0.05F, () -> instance.instance().BlockDestroyIconSize, v -> instance.instance().BlockDestroyIconSize = v)
                                        .controller(opt -> FloatSliderControllerBuilder.create(opt)
                                                .range(0F, 0.2F)
                                                .step(0.05F)
                                                .formatValue(val -> Text.literal(val + "")))
                                        .build())

                                .build()
                        )

                        .group(OptionGroup.createBuilder()
                                .name(Text.literal("Entity sample-point render settings"))
                                .option(Option.<Color>createBuilder()
                                        .name(Text.literal("Entity sample-point(safe) render color"))
                                        .binding(Color.GREEN, () -> instance.instance().EntitySamplePoion_Safe_IconColor, v -> instance.instance().EntitySamplePoion_Safe_IconColor = v)
                                        .controller(opt -> ColorControllerBuilder.create(opt)
                                                .allowAlpha(true))
                                        .build())
                                .option(Option.<String>createBuilder()
                                        .name(Text.literal("Entity sample-point(safe) render icon"))
                                        .binding("√", () -> instance.instance().EntitySamplePoion_Safe_Icon, v -> instance.instance().EntitySamplePoion_Safe_Icon = v)
                                        .controller(StringControllerBuilder::create)
                                        .build())

                                .option(Option.<Color>createBuilder()
                                        .name(Text.literal("Entity sample-point(danger) render color"))
                                        .binding(Color.RED, () -> instance.instance().EntitySamplePoion_Danger_IconColor, v -> instance.instance().EntitySamplePoion_Danger_IconColor = v)
                                        .controller(opt -> ColorControllerBuilder.create(opt)
                                                .allowAlpha(true))
                                        .build())
                                .option(Option.<String>createBuilder()
                                        .name(Text.literal("Entity sample-point(danger) render icon"))
                                        .binding("X", () -> instance.instance().EntitySamplePoion_Danger_Icon, v -> instance.instance().EntitySamplePoion_Danger_Icon = v)
                                        .controller(StringControllerBuilder::create)
                                        .build())

                                .option(Option.<Color>createBuilder()
                                        .name(Text.literal("Entity sample-point(blocked position) render color"))
                                        .binding(Color.MAGENTA, () -> instance.instance().EntitySamplePoion_Blocked_IconColor, v -> instance.instance().EntitySamplePoion_Blocked_IconColor = v)
                                        .controller(opt -> ColorControllerBuilder.create(opt)
                                                .allowAlpha(true))
                                        .build())
                                .option(Option.<String>createBuilder()
                                        .name(Text.literal("Entity sample-point(blocked position) render icon"))
                                        .binding("❖", () -> instance.instance().EntitySamplePoion_Blocked_Icon, v -> instance.instance().EntitySamplePoion_Blocked_Icon = v)
                                        .controller(StringControllerBuilder::create)
                                        .build())


                                .option(Option.<Float>createBuilder()
                                        .name(Text.literal("Entity sample-point(safe) icon_size"))
                                        .description(OptionDescription.of(Text.literal("Size of the icon for the blocks will be destroyed by the explosion")))
                                        .binding(0.01F, () -> instance.instance().EntitySamplePoionIconSize, v -> instance.instance().EntitySamplePoionIconSize = v)
                                        .controller(opt -> FloatSliderControllerBuilder.create(opt)
                                                .range(0F, 0.03F)
                                                .step(0.01F)
                                                .formatValue(val -> Text.literal(val + "")))
                                        .build())

                                .build()
                        )

                        .build()
                )
                .save(() -> {
                    instance.save();
                    UpadteSettings();
                })
                .build()
        .generateScreen(parentScreen);
    }
}
