package com.pedrorok.hypertube.client;

import com.pedrorok.hypertube.blocks.blockentities.HyperAcceleratorBlockEntity;
import com.pedrorok.hypertube.blocks.blockentities.HyperEntranceBlockEntity;
import com.pedrorok.hypertube.blocks.blockentities.HyperJunctionBlockEntity;
import com.pedrorok.hypertube.blocks.blockentities.parent.ActionTubeBlockEntity;
import com.pedrorok.hypertube.core.travel.TravelConstants;
import com.zurrtum.create.client.foundation.blockEntity.behaviour.tooltip.KineticTooltipBehaviour;
import com.zurrtum.create.client.foundation.blockEntity.behaviour.tooltip.SpeedGaugeTooltipBehaviour;
import net.minecraft.network.chat.Component;

import java.util.List;

/**
 * Create Fly moved goggle tooltips from the block entity to client behaviours. This reproduces Hypertube's 1.21.1
 * addToGoggleTooltip overrides:
 * <ul>
 *     <li>entrance / accelerator: Create's kinetic stats, the speed line, then the block's own status line;</li>
 *     <li>junction: only its own preview (it never showed kinetic stats).</li>
 * </ul>
 */
public class HypertubeTooltipBehaviour extends KineticTooltipBehaviour<ActionTubeBlockEntity> {

    public HypertubeTooltipBehaviour(ActionTubeBlockEntity be) {
        super(be);
    }

    @Override
    public boolean addToTooltip(List<Component> tooltip, boolean isPlayerSneaking) {
        // The entrance replaced Create's kinetic hover warnings with its own line
        if (blockEntity instanceof HyperEntranceBlockEntity entrance) {
            return entrance.addHypertubeHoverInfo(tooltip);
        }
        return super.addToTooltip(tooltip, isPlayerSneaking);
    }

    @Override
    public boolean addToGoggleTooltip(List<Component> tooltip, boolean isPlayerSneaking) {
        ActionTubeBlockEntity be = blockEntity;
        if (be instanceof HyperJunctionBlockEntity junction) {
            return junction.addHypertubeGoggleInfo(tooltip);
        }
        super.addToGoggleTooltip(tooltip, isPlayerSneaking);
        boolean belowNeededSpeed = Math.abs(be.getSpeed()) < TravelConstants.NEEDED_SPEED;
        SpeedGaugeTooltipBehaviour.getFormattedSpeedText(be.getSpeed(), belowNeededSpeed).forGoggles(tooltip);
        if (be instanceof HyperAcceleratorBlockEntity accelerator) {
            accelerator.addHypertubeGoggleInfo(tooltip);
        } else if (be instanceof HyperEntranceBlockEntity entrance) {
            entrance.addHypertubeGoggleInfo(tooltip);
        }
        return true;
    }
}
