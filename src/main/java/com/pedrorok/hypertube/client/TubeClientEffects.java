package com.pedrorok.hypertube.client;

import com.pedrorok.hypertube.blocks.HyperEntranceBlock;
import com.pedrorok.hypertube.blocks.HyperJunctionBlock;
import com.pedrorok.hypertube.blocks.blockentities.HyperJunctionBlockEntity;
import com.pedrorok.hypertube.blocks.blockentities.parent.TravelInteractTubeBlockEntity;
import com.pedrorok.hypertube.core.connection.BezierConnection;
import com.pedrorok.hypertube.core.connection.interfaces.IConnection;
import com.pedrorok.hypertube.core.data.JunctionMode;
import com.pedrorok.hypertube.core.sound.TubeSoundManager;
import com.pedrorok.hypertube.core.travel.TravelConstants;
import com.pedrorok.hypertube.utils.JunctionDirectionUtils;
import com.pedrorok.hypertube.utils.ModColors;
import com.pedrorok.hypertube.utils.TubePulseRenderer;
import net.minecraft.client.Minecraft;
import net.minecraft.client.player.LocalPlayer;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.Vec3;

import java.util.List;

/**
 * Client-only parts of Hypertube's block entities (26.2 port). Upstream marked these methods @OnlyIn(Dist.CLIENT) so they
 * were stripped from servers; NeoForge 26.2 no longer strips them, and a dedicated server crashed loading the block
 * entity classes because they referenced LocalPlayer. Only ever called from client-side code paths.
 */
public final class TubeClientEffects {
    private TubeClientEffects() {
    }

    public static void playTubeEffects(TravelInteractTubeBlockEntity tube, TubeSoundManager.TubeAmbientSound sound) {
        BlockState state = tube.getBlockState();
        BlockPos pos = tube.getBlockPos();

        boolean isOpen = state.getValue(HyperEntranceBlock.OPEN);

        LocalPlayer player = Minecraft.getInstance().player;
        Vec3 source = Vec3.atCenterOf(pos);
        Vec3 listener = player.position();

        Vec3 worldDirection = source.subtract(listener).normalize();

        Vec3 forward = player.getLookAngle().normalize();
        Vec3 up = player.getUpVector(1.0F).normalize();
        Vec3 right = forward.cross(up).normalize();

        double x = worldDirection.dot(right);
        double y = worldDirection.dot(up);
        double z = worldDirection.dot(forward);

        Vec3 rotatedDirection = new Vec3(x, y, z).normalize();

        double distance = player.distanceToSqr(source);

        if (isOpen) {
            tube.spawnSuctionParticle(tube.getLevel(), pos, state.getValue(HyperEntranceBlock.FACING));
        }

        sound.enableClientPlayerSound(
                player,
                rotatedDirection,
                distance,
                isOpen
        );
    }

    public static boolean junctionGoggleInfo(HyperJunctionBlockEntity junction) {
        Minecraft mc = Minecraft.getInstance();
        if (mc.player == null) return false;
        if (mc.player.tickCount % 10 != 0) return false;
        if (mc.player.getPersistentData().getBooleanOr(TravelConstants.TRAVEL_TAG, false)) return false;

        List<Direction> connectedFaces = JunctionDirectionUtils.getConnectedFaces(junction.getBlockState(), null, (HyperJunctionBlock) junction.getBlockState().getBlock());
        renderFromDirections(junction, connectedFaces, 0.3f, ModColors.GREEN, 0.72f);
        if (!junction.getBlockState().getValue(HyperJunctionBlock.JUNCTION_MODE).equals(JunctionMode.AUTOMATIC)) {
            List<Direction> fromCenterDirection = JunctionDirectionUtils.getConnectedFaces(junction.getBlockState(), junction.getBlockState().getValue(HyperJunctionBlock.FACING), (HyperJunctionBlock) junction.getBlockState().getBlock());
            IConnection connectionInDirection = junction.getConnectionInDirection(junction.getBlockState().getValue(HyperJunctionBlock.FACING));
            if (connectionInDirection != null) {
                BezierConnection thisEntranceConnection = connectionInDirection.getThisEntranceConnection(mc.level);
                if (thisEntranceConnection != null) {
                    boolean inverted = thisEntranceConnection.isInverted(junction.getBlockPos());
                    BlockPos pos = thisEntranceConnection.getFromPos().pos();
                    TubePulseRenderer.start(pos, thisEntranceConnection, !inverted, 2, 0.1f, 0.2f, ModColors.ORANGE, 2, 8, true, 0.6f);
                }
            }
            renderFromDirections(junction, fromCenterDirection, 0.2f, ModColors.ORANGE, 0.6f);
        }

        return false;
    }

    private static void renderFromDirections(HyperJunctionBlockEntity junction, List<Direction> directions, float speed, int color, float radius) {
        Minecraft mc = Minecraft.getInstance();
        for (Direction direction : directions) {
            IConnection iConnection = junction.getConnectionInDirection(direction);
            if (iConnection == null) continue;
            BezierConnection connection = iConnection.getThisEntranceConnection(mc.level);
            if (connection == null) continue;
            boolean inverted = connection.isInverted(junction.getBlockPos());
            BlockPos pos = connection.getFromPos().pos();
            TubePulseRenderer.start(pos, connection, inverted, 2, 0.1f, speed, color, 0.2f, 2, false, radius);
        }
    }
}
