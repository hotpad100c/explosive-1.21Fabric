package mypals.ml.config;

import com.google.gson.GsonBuilder;
import dev.isxander.yacl3.config.v2.api.ConfigClassHandler;
import dev.isxander.yacl3.config.v2.api.SerialEntry;
import dev.isxander.yacl3.config.v2.api.serializer.GsonConfigSerializerBuilder;
import mypals.ml.ExplosionVisualizer;


import mypals.ml.explotionManage.FakeExplosion;
import net.fabricmc.loader.api.FabricLoader;

import java.awt.*;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.Set;

public class VisualizerConfig {
    public static ConfigClassHandler<VisualizerConfig> HANDLER = ConfigClassHandler.createBuilder(VisualizerConfig.class)
            .id(ExplosionVisualizer.id("config"))
            .serializer(config -> GsonConfigSerializerBuilder.create(config)
                    .setPath(FabricLoader.getInstance().getConfigDir().resolve("ExplosionVisualizer.json5"))
                    .appendGsonBuilder(GsonBuilder::setPrettyPrinting) // not needed, pretty print by default
                    .setJson5(true)
                    .build())
            .build();


    @SerialEntry
    public static boolean showInfo = false;
    @SerialEntry
    public static boolean showRayCastInfo = false;
    @SerialEntry
    public static boolean showBlockDestroyInfo = false;
    @SerialEntry
    public static boolean showDamageInfo = false;
    @SerialEntry
    public static boolean showExplosionBlockDamageRayInfo = false;

    @SerialEntry
    public static String BlockDetectionRayIcon = "⧈";
    @SerialEntry
    public static float BlockDetectionRayIconSize = 0.005F;
    @SerialEntry
    public static boolean EnableAlpha = true;
    @SerialEntry
    public static int Xmin = 0, Ymin = 0, Zmin = 0;
    @SerialEntry
    public static int Xmax = 16, Ymax = 16, Zmax = 16;
    @SerialEntry
    public static int LayerMin = 0, LayerMax = 100;
    @SerialEntry
    public static boolean Invert = false;


    @SerialEntry
    public static Color BlockDestroyIconColor = Color.yellow;
    @SerialEntry
    public static String BlockDestroyIcon = "!";
    @SerialEntry
    public static float BlockDestroyIconSize = 0.045F;

    @SerialEntry
    public static Color EntitySamplePoion_Safe_IconColor = Color.green;

    @SerialEntry
    public static Color EntitySamplePoion_Danger_IconColor = Color.red;

    @SerialEntry
    public static Color EntitySamplePoion_Blocked_IconColor = Color.MAGENTA;

    @SerialEntry
    public static String EntitySamplePoion_Safe_Icon = "√";
    @SerialEntry
    public static String EntitySamplePoion_Danger_Icon = "X";
    @SerialEntry
    public static String EntitySamplePoion_Blocked_Icon = "❖";
    @SerialEntry
    public static float EntitySamplePoionIconSize = 0.01F;

    @SerialEntry
    public static ArrayList<FakeExplosion> fakeExplosions = new ArrayList<FakeExplosion>();


}
