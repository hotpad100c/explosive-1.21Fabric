package mypals.ml.renderer;

import mypals.ml.mathSupport.MathHelp.*;
import mypals.ml.explotionManage.ExplosionSimulator;
import mypals.ml.explotionManage.ExplotionAffectdDataManage.DamagedEntityData.EntityToDamage;
import mypals.ml.explotionManage.ExplotionAffectdDataManage.DamagedEntityData.SamplePointsData.RayCastPointInfo.RayCastData;
import mypals.ml.explotionManage.ExplotionAffectdDataManage.DamagedEntityData.SamplePointsData.SamplePointData;
import mypals.ml.explotionManage.ExplotionAffectdDataManage.ExplosionCastLines.ExplosionCastLine;
import mypals.ml.explotionManage.ExplotionAffectdDataManage.ExplosionCastLines.PointsOnLine.CastPoint;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.render.Camera;
import net.minecraft.client.render.RenderTickCounter;
import net.minecraft.client.render.VertexConsumer;
import net.minecraft.client.util.math.MatrixStack;
import net.minecraft.entity.LivingEntity;
import net.minecraft.util.Formatting;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Vec3d;

import java.util.HashSet;
import java.util.List;
import java.util.Set;

import static mypals.ml.Explosive.*;
import static mypals.ml.mathSupport.MathHelp.addAlphaWithDecay;
import static mypals.ml.renderer.LineRenderer.renderSingleLine;


public class InfoRenderer {
    public static final String SHOULD_BE_FINE = Formatting.GREEN + "√";
    private static final String WILL_DESTROY = Formatting.YELLOW + "!";

    private static final InfoRenderer INSTANCE = new InfoRenderer();

    public static InfoRenderer getInstance() {
        return INSTANCE;
    }

    public static Set<BlockPos> blocksToDamage = new HashSet<>();

    public static Set<Vec3d> explotionCenters = new HashSet<>();
    public static Set<EntityToDamage> entitysToDamage = new HashSet<>();
    public static Set<SamplePointData> samplePointData = new HashSet<>();

    public static Set<ExplosionCastLine> explosionCastedLines = new HashSet<>();

    @SuppressWarnings("ConstantConditions")
    public static void render(MatrixStack matrixStack, RenderTickCounter counter, VertexConsumer buffer) {
        BlockPos pos = new BlockPos(5, 0, 0);
        //int goldValue = Formatting.GOLD.getColorValue();
        // Example for drawing a string at (0, 0, 0) with some formatting
        //drawString(matrixStack, pos, counter, SHOULD_BE_FINE, Formatting.GREEN.getColorValue(), 0.045F);
        //drawBox(matrixStack, Vec3d.of(pos), counter, 1, Formatting.YELLOW.getColorValue());

        if (blocksToDamage != null) {
            MinecraftClient client = MinecraftClient.getInstance();
            if (client.player != null) {
                //client.player.sendMessage(Text.of("blocksToRender are" + blocksToRender), false);
            }
            if( showBlockDestroyInfo) {
                for (BlockPos p : blocksToDamage) {
                    // Render each affected block

                    drawString(matrixStack, p, counter, WILL_DESTROY, Formatting.YELLOW.getColorValue(), 0.045F);
                }
            }
            if(showDamageInfo) {
                for (EntityToDamage e : entitysToDamage) {
                    if (e.getEntity() instanceof LivingEntity livingEntity) {
                        // 获取当前健康值并将其转换为半颗心表示
                        float health = livingEntity.getHealth() / 2.0f;

                        // 获取高度
                        float height = livingEntity.getHeight();

                        // 计算伤害，并确保它不会被舍入多次
                        float damage = e.getDamage() / 2.0f;

                        // 计算伤害后的剩余健康值，确保不会低于0
                        float remainingHealth = Math.max(health - damage, 0);

                        // 准备显示的文本
                        String s = health + "♡" + " - ≈" + damage + "♡";
                        String s2 = remainingHealth > 0 ? "≈ " + remainingHealth + "♡" : "DIE?";

                        // 根据伤害占最大生命值的比例计算颜色
                        float damageFactor = Math.max(health - damage, 0);
                        int red = (int) (255 * damageFactor);
                        int green = (int) (255 * (1 - damageFactor));
                        int color = (red << 16) | (green << 8);

                        // 绘制文本
                        drawString(matrixStack, e.getEntity().getPos().add(0, height + 0.5, 0), counter, s, color, 0.025F);
                        drawString(matrixStack, e.getEntity().getPos().add(0, height, 0), counter, s2, color, 0.025F);
                    }


                }
            }
            for (ExplosionCastLine l : explosionCastedLines) {
                int color = l.getLineColor();
                for (CastPoint p : l.getPoints()) {
                    int c = addAlphaWithDecay(color, p.getStrength());
                    if(p.getStrength() > 0)
                        drawString(matrixStack, p.getPosition(), counter, "⧈", c, 0.005F);
                }
            }
            for (Vec3d v : explotionCenters) {
                int orangeColor = 16753920; // 橘色
                // Render each affected block
                String s = "\uD83D\uDCA5";
                drawString(matrixStack, v, counter, s, orangeColor, 0.045F);
            }
            if(samplePointData != null && showRayCastInfo)
            {
                for(SamplePointData d : samplePointData) {
                    if(d != null) {
                        for(RayCastData r :  d.getCastPointData()) {
                            Vec3d org = r.point;
                            Vec3d collitionPoint = r.point_hit;
                            boolean hit_target = r.hit_target;
                            if (hit_target) {
                                drawString(matrixStack, org, counter, "X", Formatting.RED.getColorValue(), 0.01F);
                            }
                            else {
                                drawString(matrixStack, org, counter, "√", Formatting.GREEN.getColorValue(), 0.01F);
                                drawString(matrixStack, collitionPoint, counter, "⬦", Formatting.LIGHT_PURPLE.getColorValue(), 0.007F);
                                //drawLine(matrixStack, buffer, org, collitionPoint, Formatting.BLUE.getColorValue(), 255);
                            }
                        }
                    }
                }

            }
        }
    }
    public static void drawLine(MatrixStack stack, VertexConsumer buffer, Vec3d p1, Vec3d p2, int color, int a)
    {
        int r = (color >> 16) & 0xFF;
        int g = (color >> 8) & 0xFF;
        int b = color & 0xFF;
        renderSingleLine(stack, buffer, (float) p1.x, (float) p1.y, (float) p1.z, (float) p2.x, (float) p2.y, (float) p2.z, r, g, b, a);
    }

    public static void setBlocksToDamage(Set<BlockPos> blocks) {blocksToDamage = blocks;}
    public static void setEntitysToDamage(Set<EntityToDamage> e) {
        entitysToDamage = e;
    }
    public static void setExplotionCenters(Set<Vec3d> v) {
        explotionCenters = v;
    }
    public static void setSamplePointData(Set<SamplePointData> d)
    {
        samplePointData = d;
    }
    public static void setCastedLines(Set<ExplosionCastLine> l)
    {
        explosionCastedLines = l;
    }

    public static void drawString(MatrixStack matrixStack, BlockPos pos, RenderTickCounter countr, String text, int color, float size) {
        // Assuming StringDrawer.drawString is a method to draw text on the screen
        MinecraftClient client = MinecraftClient.getInstance();
        StringRenderer.renderText(matrixStack, countr, pos, text, color, size);
    }
    public static void drawString(MatrixStack matrixStack, Vec3d pos, RenderTickCounter countr, String text, int color, float size) {
        // Assuming StringDrawer.drawString is a method to draw text on the screen
        MinecraftClient client = MinecraftClient.getInstance();
        StringRenderer.renderText(matrixStack, countr, pos, text, color, size);
    }
    public static void drawBox(MatrixStack matrixStack, Vec3d pos,RenderTickCounter countr, float size, int color)
    {
        float alpha = (color >> 24 & 0xFF) / 255.0f;
        float red = (color >> 16 & 0xFF) / 255.0f;
        float green = (color >> 8 & 0xFF) / 255.0f;
        float blue = (color & 0xFF) / 255.0f;
        Vec3d min = new Vec3d(pos.x-size, pos.y-size,pos.z-size);
        Vec3d max = new Vec3d(pos.x+size, pos.y+size,pos.z+size);
        MinecraftClient client = MinecraftClient.getInstance();
        Camera camera = client.gameRenderer.getCamera();
        StringRenderer.drawBox(countr, camera, matrixStack,pos, min, max, red, green, blue, alpha);
    }

}
