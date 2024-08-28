package mypals.ml.explotionManage.ExplotionAffectdDataManage.DamagedEntityData.SamplePointsData;

import mypals.ml.explotionManage.ExplotionAffectdDataManage.DamagedEntityData.SamplePointsData.RayCastPointInfo.RayCastData;
import net.minecraft.util.math.Vec3d;

import java.util.ArrayList;
import java.util.List;

public class SamplePointData {
    public final List<RayCastData> castPointData;

    public SamplePointData(List<RayCastData> castPointData) {
        this.castPointData = castPointData;
    }
    public List<RayCastData> getCastPointData()
    {
        return castPointData;
    }

}
