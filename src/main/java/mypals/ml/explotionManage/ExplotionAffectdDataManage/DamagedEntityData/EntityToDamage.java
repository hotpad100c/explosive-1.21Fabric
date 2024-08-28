package mypals.ml.explotionManage.ExplotionAffectdDataManage.DamagedEntityData;

import mypals.ml.explotionManage.ExplotionAffectdDataManage.DamagedEntityData.SamplePointsData.SamplePointData;
import net.minecraft.entity.Entity;


public class EntityToDamage {
    public final Entity entityToDamage;
    public final float Damage;
    public final SamplePointData samplePointData;
    public EntityToDamage(Entity entityToDamage, float damage, SamplePointData samplePointData) {
        this.entityToDamage = entityToDamage;
        Damage = damage;
        this.samplePointData = samplePointData;
    }
    public Entity getEntity() {
        return entityToDamage;
    }

    public float getDamage() {
        return Damage;
    }
    public SamplePointData getPointData() {
        return samplePointData;
    }
}
