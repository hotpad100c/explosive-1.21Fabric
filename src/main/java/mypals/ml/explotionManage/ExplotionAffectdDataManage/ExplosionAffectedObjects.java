package mypals.ml.explotionManage.ExplotionAffectdDataManage;

import mypals.ml.explotionManage.ExplotionAffectdDataManage.DamagedEntityData.EntityToDamage;
import mypals.ml.explotionManage.ExplotionAffectdDataManage.DamagedEntityData.SamplePointsData.SamplePointData;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Vec3d;

import java.util.ArrayList;
import java.util.List;

public class ExplosionAffectedObjects {
    public final List<BlockPos> blocksToDestriy;
    public final List<BlockPos> blocksShouldBeFine;
    public final List<Vec3d> explotionCenters;

    public final List<EntityToDamage> entityToDamage;
    public final List<SamplePointData> samplePointData;

    public ExplosionAffectedObjects(List<BlockPos> blocksToDestriy, List<BlockPos> blocksShouldBeFine, List<EntityToDamage> entityToDamage, List<Vec3d> explotionCenters) {
        this.explotionCenters = new ArrayList<Vec3d>();;
        this.blocksToDestriy = new ArrayList<BlockPos>();
        this.blocksShouldBeFine = new ArrayList<BlockPos>();
        this.entityToDamage = new ArrayList<EntityToDamage>();
        this.samplePointData = new ArrayList<SamplePointData>();
    }
    public List<BlockPos> getBlocksToDestriy() {return blocksToDestriy;}
    public List<BlockPos> getBlocksShouldBeFine() {return blocksToDestriy;}
    public List<EntityToDamage> getEntitysToDamage() {
        return entityToDamage;
    }
    public List<SamplePointData> getSamplePointData()
    {
        return samplePointData;
    }
    public List<Vec3d> getExplotionCenters() {
        return explotionCenters;
    }

}
