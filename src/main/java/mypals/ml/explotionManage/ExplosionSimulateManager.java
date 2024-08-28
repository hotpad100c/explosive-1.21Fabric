package mypals.ml.explotionManage;

import mypals.ml.explotionManage.ExplotionAffectdDataManage.ExplosionAffectedObjects;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Vec3d;
import net.minecraft.world.World;


public class ExplosionSimulateManager {

    public static ExplosionAffectedObjects simulateExplosiveBlocks(World world, BlockPos pos, float explosionPower) {
        Vec3d explosionPos = pos.toCenterPos();
        // 使用自定义的模拟器
        ExplosionSimulator simulator = new ExplosionSimulator(true, world, (float) explosionPos.getX(), (float) explosionPos.getY(), (float) explosionPos.getZ(), explosionPower);
        simulator.simulate();
        return simulator.getAffected();


    }
    public static ExplosionAffectedObjects simulateExplosiveEntitys(World world, Vec3d pos, float explosionPower) {
        Vec3d explosionPos = pos;
        // 使用自定义的模拟器
        ExplosionSimulator simulator = new ExplosionSimulator(false, world, (float) explosionPos.getX(), (float) explosionPos.getY(), (float) explosionPos.getZ(), explosionPower);
        simulator.simulate();
        return simulator.getAffected();
    }

}
