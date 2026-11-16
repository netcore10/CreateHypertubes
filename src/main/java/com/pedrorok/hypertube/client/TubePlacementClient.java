package com.pedrorok.hypertube.client;

import com.mojang.blaze3d.vertex.PoseStack;
import com.pedrorok.hypertube.blocks.HypertubeBlock;
import com.pedrorok.hypertube.core.connection.BezierConnection;
import com.pedrorok.hypertube.core.connection.SimpleConnection;
import com.pedrorok.hypertube.core.connection.interfaces.ITubeConnection;
import com.pedrorok.hypertube.core.placement.ResponseDTO;
import com.pedrorok.hypertube.registry.ModBlocks;
import com.pedrorok.hypertube.registry.ModDataComponent;
import com.pedrorok.hypertube.utils.MessageUtils;
import com.pedrorok.hypertube.utils.RayCastUtils;
import com.pedrorok.hypertube.utils.TubeUtils;
import com.zurrtum.create.catnip.animation.LerpedFloat;
import com.zurrtum.create.client.content.trains.track.TrackBlockOutline;
import net.minecraft.client.Minecraft;
import net.minecraft.client.player.LocalPlayer;
import net.minecraft.client.renderer.SubmitNodeCollector;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.HitResult;
import net.minecraft.world.phys.Vec3;

/**
 * Client half of TubePlacement (placement preview + outline). Upstream marked these @OnlyIn(Dist.CLIENT); NeoForge 26.2
 * no longer strips that, and a dedicated server crashed loading TubePlacement because these reference LocalPlayer.
 */
public final class TubePlacementClient {
    private static BlockPos hoveringPos;
    private static boolean canPlace = false;
    private static LerpedFloat animation = LerpedFloat.linear()
            .startWithValue(0);

    private TubePlacementClient() {
    }

    @SuppressWarnings("D")
    public static void clientTick() {
        LocalPlayer player = Minecraft.getInstance().player;
        ItemStack stack = player.getMainHandItem();
        HitResult hitResult = Minecraft.getInstance().hitResult;

        if (hitResult == null)
            return;
        if (hitResult.getType() != HitResult.Type.BLOCK)
            return;

        Item tubeItem = ModBlocks.HYPERTUBE.asItem();
        if (!stack.getItem().equals(tubeItem)) {
            stack = player.getOffhandItem();
            if (!stack.getItem().equals(tubeItem))
                return;
        }

        if (!stack.hasFoil())
            return;

        Level level = player.level();
        BlockHitResult bhr = (BlockHitResult) hitResult;
        BlockPos pos = bhr.getBlockPos();
        BlockState hitState = level.getBlockState(pos);
        boolean hypertubeHitResult = hitState.getBlock() instanceof ITubeConnection;
        if (hitState.isAir() || hypertubeHitResult) {
            hoveringPos = pos;
        } else {
            pos = pos.relative(bhr.getDirection());
        }

        SimpleConnection connectionFrom = stack.get(ModDataComponent.TUBE_CONNECTING_FROM);

        animation.setValue(0.8);
        if (connectionFrom == null) {
            animation.setValue(0);
            return;
        }

        Direction finalDirection = RayCastUtils.getDirectionFromHitResult(player, () -> hypertubeHitResult);

        SimpleConnection connectionTo = new SimpleConnection(pos, finalDirection, 0);
        BezierConnection bezierConnection = BezierConnection.of(connectionFrom, connectionTo);

        boolean isAngleInverted = false;
        if (bezierConnection.isAngleTooHigh()) {
            BezierConnection newBezierConnection = BezierConnection.of(connectionFrom, new SimpleConnection(pos, finalDirection.getOpposite(), 0));
            if (!newBezierConnection.isAngleTooHigh()) {
                bezierConnection = newBezierConnection;
                isAngleInverted = true;
            }
        }

        // Exception & visual
        ResponseDTO response = bezierConnection.getValidation();

        if (response.valid()) {
            response = TubeUtils.checkSurvivalItems(player, (int) bezierConnection.distance(), true);
        }
        if (response.valid()) {
            response = TubeUtils.checkBlockCollision(level, bezierConnection);
        }
        if (response.valid() && hypertubeHitResult) {
            response = TubeUtils.checkClickedHypertube(level, pos, isAngleInverted ? finalDirection : finalDirection.getOpposite());
        }

        animation.setValue(!response.valid() ? 0.2 : 0.8);

        canPlace = response.valid();
        bezierConnection.drawPath(animation, canPlace);

        if (!response.valid()) {
            MessageUtils.sendActionMessage(player, response.getMessageComponent());
            return;
        }

        MessageUtils.sendActionMessage(player, "");
    }

    public static void drawCustomBlockSelection(PoseStack ms, SubmitNodeCollector collector, Vec3 camera) {
        ItemStack mainHandItem = Minecraft.getInstance().player.getMainHandItem();
        if (!mainHandItem.is(ModBlocks.HYPERTUBE.asItem())) return;
        if (!mainHandItem.hasFoil()) return;
        SimpleConnection connection = mainHandItem.get(ModDataComponent.TUBE_CONNECTING_FROM);
        if (connection == null) return;

        Minecraft mc = Minecraft.getInstance();
        BlockState blockState = mc.level.getBlockState(connection.pos());
        if (!(blockState.getBlock() instanceof HypertubeBlock)) return;
        HypertubeBlock block = (HypertubeBlock) blockState.getBlock();

        // Create's renderShape colours: white (can place), red (can't)
        int color = canPlace ? TrackBlockOutline.WHITE_COLOR : TrackBlockOutline.RED_COLOR;
        ms.pushPose();
        ms.translate(connection.pos().getX() - camera.x, connection.pos().getY() - camera.y, connection.pos().getZ() - camera.z);
        TrackBlockOutline.submitShape(block.getShape(blockState), ms, collector, color, mc.getWindow().getAppropriateLineWidth());
        ms.popPose();
    }
}
