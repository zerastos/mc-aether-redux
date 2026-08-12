package net.zepalesque.redux.capability.arrow;

import net.minecraft.nbt.CompoundTag;
import net.minecraft.world.entity.projectile.AbstractArrow;

public class SubzeroArrowCapability implements SubzeroArrow {
    private static final String SUBZERO_ARROW_TAG = "SubzeroArrow";
    private static final String SLOWNESS_TIME_TAG = "SlownessTime";

    private final AbstractArrow arrow;

    private boolean subzeroArrow;
    private int slownessTime;

    public SubzeroArrowCapability(AbstractArrow arrow) {
        this.arrow = arrow;
    }

    @Override
    public AbstractArrow getArrow() {
        return this.arrow;
    }

    @Override
    public CompoundTag serializeNBT() {
        CompoundTag tag = new CompoundTag();

        // Avoid storing entries for ordinary arrows, which make up the
        // overwhelming majority of AbstractArrow instances.
        if (this.subzeroArrow) {
            tag.putBoolean(SUBZERO_ARROW_TAG, true);
        }

        if (this.slownessTime != 0) {
            tag.putInt(SLOWNESS_TIME_TAG, this.slownessTime);
        }

        return tag;
    }

    @Override
    public void deserializeNBT(CompoundTag tag) {
        // CompoundTag returns false/0 when these keys are absent.
        this.subzeroArrow = tag.getBoolean(SUBZERO_ARROW_TAG);
        this.slownessTime = tag.getInt(SLOWNESS_TIME_TAG);
    }

    @Override
    public void setSubzeroArrow(boolean subzeroArrow) {
        this.subzeroArrow = subzeroArrow;
    }

    @Override
    public boolean isSubzeroArrow() {
        return this.subzeroArrow;
    }

    @Override
    public void setSlownessTime(int slownessTime) {
        this.slownessTime = slownessTime;
    }

    @Override
    public int getSlownessTime() {
        return this.slownessTime;
    }
}