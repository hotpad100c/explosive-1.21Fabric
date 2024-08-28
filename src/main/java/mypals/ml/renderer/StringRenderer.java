package mypals.ml.renderer;

import com.mojang.blaze3d.systems.RenderSystem;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.font.TextRenderer;
import net.minecraft.client.model.ModelPart;
import net.minecraft.client.render.*;
import net.minecraft.client.render.debug.DebugRenderer;
import net.minecraft.client.render.entity.EntityRenderDispatcher;
import net.minecraft.client.util.math.MatrixStack;
import net.minecraft.entity.Entity;
import net.minecraft.util.Formatting;
import net.minecraft.util.math.*;
import org.joml.Matrix4f;
import org.joml.Matrix4fStack;
import com.mojang.blaze3d.systems.RenderSystem;
import net.minecraft.client.render.Tessellator;
import net.minecraft.client.render.VertexConsumerProvider;
import net.minecraft.client.render.VertexFormats;
import net.minecraft.client.util.math.MatrixStack;
import net.minecraft.util.math.Vec3d;

import java.awt.*;
import java.util.Set;


public class StringRenderer {
    public static double lastTickPosX = 0;
    public static double lastTickPosY = 0;
    public static double lastTickPosZ = 0;
    public static void renderText(MatrixStack matrixStack,RenderTickCounter counter,BlockPos pos, String text, int color, float SIZE)
    {
        MinecraftClient client = MinecraftClient.getInstance();
        Camera camera = client.gameRenderer.getCamera();
        //Vec3d textPos = new Vec3d(0, 0, 0);
        Vec3d textPos = new Vec3d(pos.toCenterPos().toVector3f());
        drawString(counter, camera, textPos, text, color, SIZE);
    }
    public static void renderText(MatrixStack matrixStack,RenderTickCounter counter,Vec3d pos, String text, int color, float SIZE)
    {
        MinecraftClient client = MinecraftClient.getInstance();
        Camera camera = client.gameRenderer.getCamera();
        //Vec3d textPos = new Vec3d(0, 0, 0);
        Vec3d textPos = pos;
        drawString(counter, camera, textPos, text, color, SIZE);
    }
    public static void drawString(RenderTickCounter tickCounter, Camera camera, Vec3d textPos, String text, int color, float SIZE) {
        float f = MinecraftClient.getInstance().options.getTextBackgroundOpacity(0.25F);
        int j = (int)(f * 255.0F) << 24;

        Matrix4fStack modelViewMatrix = new Matrix4fStack(1);
        modelViewMatrix.identity();

        float tickDelta = tickCounter.getTickDelta(false);
        float x = (float) (textPos.x - MathHelper.lerp(tickDelta, lastTickPosX, camera.getPos().getX()));
        float y = (float) (textPos.y - MathHelper.lerp(tickDelta, lastTickPosY, camera.getPos().getY()));
        float z = (float) (textPos.z - MathHelper.lerp(tickDelta, lastTickPosZ, camera.getPos().getZ()));
        lastTickPosX = camera.getPos().getX();
        lastTickPosY = camera.getPos().getY();
        lastTickPosZ = camera.getPos().getZ();
        modelViewMatrix.translate(x, y, z);
        modelViewMatrix.rotate(MinecraftClient.getInstance().gameRenderer.getCamera().getRotation());
        modelViewMatrix.scale(SIZE, -SIZE, SIZE);

        TextRenderer textRenderer = MinecraftClient.getInstance().textRenderer;
        DebugRenderer debugRenderer = MinecraftClient.getInstance().debugRenderer;
        float totalWidth = textRenderer.getWidth(text);
        float writtenWidth = 1;
        float renderX = -totalWidth * 0.5F + writtenWidth;
        textRenderer.draw(text, renderX, 0, color, false, modelViewMatrix
                , MinecraftClient.getInstance().getBufferBuilders().getEntityVertexConsumers(), TextRenderer.TextLayerType.SEE_THROUGH, 0, 15);
    }

    public static void drawBox(RenderTickCounter tickCounter, Camera camera, MatrixStack matrices,Vec3d pos,  Vec3d min, Vec3d max, float red, float green, float blue, float alpha) {
        // 开始渲染方盒
        // 获取Tessellator和BufferBuilder实例
        Tessellator tessellator = Tessellator.getInstance();
        RenderSystem.setShaderColor(red, green, blue, alpha);
        RenderSystem.setShader(GameRenderer::getPositionColorProgram);
        BufferBuilder buffer = tessellator.begin(VertexFormat.DrawMode.QUADS, VertexFormats.POSITION_COLOR);
        // 设置矩阵
        Matrix4fStack modelViewMatrix = new Matrix4fStack(1);
        modelViewMatrix.identity();
        lastTickPosX = camera.getPos().getX();
        lastTickPosY = camera.getPos().getY();
        lastTickPosZ = camera.getPos().getZ();
        float tickDelta = tickCounter.getTickDelta(false);
        float x = (float) (pos.x - MathHelper.lerp(tickDelta, lastTickPosX, camera.getPos().getX()));
        float y = (float) (pos.y - MathHelper.lerp(tickDelta, lastTickPosY, camera.getPos().getY()));
        float z = (float) (pos.z - MathHelper.lerp(tickDelta, lastTickPosZ, camera.getPos().getZ()));

        modelViewMatrix.translate(x, y, z);
        modelViewMatrix.scale(1f, 1f, 1f);

        float centerX = (float) ((min.x + max.x) / 2.0f);
        float centerY = (float) ((min.y + max.y) / 2.0f);
        float centerZ = (float) ((min.z + max.z) / 2.0f);

        // 前面
        buffer.vertex(modelViewMatrix, (float)min.x - centerX, (float)min.y - centerY, (float)max.z - centerZ).color(red, green, blue, alpha);
        buffer.vertex(modelViewMatrix, (float)max.x - centerX, (float)min.y - centerY, (float)max.z - centerZ).color(red, green, blue, alpha);
        buffer.vertex(modelViewMatrix, (float)max.x - centerX, (float)max.y - centerY, (float)max.z - centerZ).color(red, green, blue, alpha);
        buffer.vertex(modelViewMatrix, (float)min.x - centerX, (float)max.y - centerY, (float)max.z - centerZ).color(red, green, blue, alpha);

// 后面
        buffer.vertex(modelViewMatrix, (float)min.x - centerX, (float)max.y - centerY, (float)min.z - centerZ).color(red, green, blue, alpha);
        buffer.vertex(modelViewMatrix, (float)max.x - centerX, (float)max.y - centerY, (float)min.z - centerZ).color(red, green, blue, alpha);
        buffer.vertex(modelViewMatrix, (float)max.x - centerX, (float)min.y - centerY, (float)min.z - centerZ).color(red, green, blue, alpha);
        buffer.vertex(modelViewMatrix, (float)min.x - centerX, (float)min.y - centerY, (float)min.z - centerZ).color(red, green, blue, alpha);

// 左面
        buffer.vertex(modelViewMatrix, (float)min.x - centerX, (float)min.y - centerY, (float)min.z - centerZ).color(red, green, blue, alpha);
        buffer.vertex(modelViewMatrix, (float)min.x - centerX, (float)min.y - centerY, (float)max.z - centerZ).color(red, green, blue, alpha);
        buffer.vertex(modelViewMatrix, (float)min.x - centerX, (float)max.y - centerY, (float)max.z - centerZ).color(red, green, blue, alpha);
        buffer.vertex(modelViewMatrix, (float)min.x - centerX, (float)max.y - centerY, (float)min.z - centerZ).color(red, green, blue, alpha);

// 右面
        buffer.vertex(modelViewMatrix, (float)max.x - centerX, (float)max.y - centerY, (float)min.z - centerZ).color(red, green, blue, alpha);
        buffer.vertex(modelViewMatrix, (float)max.x - centerX, (float)max.y - centerY, (float)max.z - centerZ).color(red, green, blue, alpha);
        buffer.vertex(modelViewMatrix, (float)max.x - centerX, (float)min.y - centerY, (float)max.z - centerZ).color(red, green, blue, alpha);
        buffer.vertex(modelViewMatrix, (float)max.x - centerX, (float)min.y - centerY, (float)min.z - centerZ).color(red, green, blue, alpha);

// 上面
        buffer.vertex(modelViewMatrix, (float)min.x - centerX, (float)max.y - centerY, (float)min.z - centerZ).color(red, green, blue, alpha);
        buffer.vertex(modelViewMatrix, (float)min.x - centerX, (float)max.y - centerY, (float)max.z - centerZ).color(red, green, blue, alpha);
        buffer.vertex(modelViewMatrix, (float)max.x - centerX, (float)max.y - centerY, (float)max.z - centerZ).color(red, green, blue, alpha);
        buffer.vertex(modelViewMatrix, (float)max.x - centerX, (float)max.y - centerY, (float)min.z - centerZ).color(red, green, blue, alpha);

// 下面
        buffer.vertex(modelViewMatrix, (float)min.x - centerX, (float)min.y - centerY, (float)max.z - centerZ).color(red, green, blue, alpha);
        buffer.vertex(modelViewMatrix, (float)min.x - centerX, (float)min.y - centerY, (float)min.z - centerZ).color(red, green, blue, alpha);
        buffer.vertex(modelViewMatrix, (float)max.x - centerX, (float)min.y - centerY, (float)min.z - centerZ).color(red, green, blue, alpha);
        buffer.vertex(modelViewMatrix, (float)max.x - centerX, (float)min.y - centerY, (float)max.z - centerZ).color(red, green, blue, alpha);

        // 结束并提交数据
        BufferRenderer.drawWithGlobalProgram(buffer.end());
        /*ModelPart.Cuboid cuboid = new ModelPart.Cuboid(
                0,                   // u - The x-coordinate of the texture
                0,                   // v - The y-coordinate of the texture
                0.0F,                // x - The x position of the cuboid
                0.0F,                // y - The y position of the cuboid
                0.0F,                // z - The z position of the cuboid
                1.0F,                // sizeX - The width of the cuboid
                1.0F,                // sizeY - The height of the cuboid
                1.0F,                // sizeZ - The depth of the cuboid
                0.0F,                // extraX - Extra padding on the x-axis
                0.0F,                // extraY - Extra padding on the y-axis
                0.0F,                // extraZ - Extra padding on the z-axis
                false,               // mirror - Whether to mirror the cuboid
                16.0F,               // textureWidth - The width of the texture
                16.0F,               // textureHeight - The height of the texture
                Set.of(Direction.NORTH, Direction.SOUTH, Direction.EAST, Direction.WEST, Direction.UP, Direction.DOWN) // The set of sides to render
        );
        cuboid.renderCuboid(matrices.peek(), MinecraftClient.getInstance().getBufferBuilders().getEntityVertexConsumers().getBuffer(RenderLayer.getSolid()), 0, 0, Formatting.RED.getCode());*/
    }

}
