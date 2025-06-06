package com.jelly.mightyminerv2.failsafe.impl;

import com.jelly.mightyminerv2.event.PacketEvent;
import com.jelly.mightyminerv2.failsafe.AbstractFailsafe;
import com.jelly.mightyminerv2.macro.MacroManager;
import com.jelly.mightyminerv2.util.Logger;
import com.jelly.mightyminerv2.util.helper.Clock;
import lombok.Getter;
import net.minecraft.init.Blocks;
import net.minecraft.network.play.server.S23PacketBlockChange;
import net.minecraft.util.BlockPos;
import net.minecraftforge.fml.common.gameevent.TickEvent.ClientTickEvent;

import java.util.ArrayList;
import java.util.List;


/*Hours wasted on this: None, this was easy as fuck*/
public class BedrockBlockChangeFailsafe extends AbstractFailsafe {

    @Getter
    private static final BedrockBlockChangeFailsafe instance = new BedrockBlockChangeFailsafe();
    private static final int THRESHOLD = 20;
    private static final long TIME_WINDOW = 100;
    private static final int RADIUS = 10;
    private final Clock timer = new Clock();
    private final List<Long> bedrockChangeTimestamps = new ArrayList<>();

    @Override
    public String getName() {
        return "BedrockBlockChangeFailsafe";
    }

    @Override
    public Failsafe getFailsafeType() {
        return Failsafe.BLOCK_CHANGE;
    }

    @Override
    public int getPriority() {
        return 7;
    }

    @Override
    public boolean onTick(ClientTickEvent event) {
        long currentTime = System.currentTimeMillis();

        List<Long> validTimestamps = new ArrayList<>();

        for (Long timestamp : bedrockChangeTimestamps) {
            if (currentTime - timestamp <= TIME_WINDOW) {
                validTimestamps.add(timestamp);
            }
        }

        bedrockChangeTimestamps.clear();
        bedrockChangeTimestamps.addAll(validTimestamps);

        if (bedrockChangeTimestamps.size() >= THRESHOLD) {
            Logger.sendWarning("Too many Bedrock block changes in the last " + TIME_WINDOW / 1000.0 + " seconds. Triggering failsafe.");
            return true;
        }

        return false;
    }

    @Override
    public boolean onPacketReceive(PacketEvent.Received event) {
        if (event.packet instanceof S23PacketBlockChange) {
            S23PacketBlockChange packet = (S23PacketBlockChange) event.packet;
            BlockPos blockPos = packet.getBlockPosition();
            net.minecraft.block.Block block = packet.getBlockState().getBlock();

            if (block == Blocks.bedrock) {
                BlockPos playerPos = mc.thePlayer.getPosition();

                double distance = playerPos.distanceSq(blockPos.getX(), blockPos.getY(), blockPos.getZ());

                double radiusSquared = RADIUS * RADIUS;

                if (distance <= radiusSquared) {
                    long currentTime = System.currentTimeMillis();
                    bedrockChangeTimestamps.add(currentTime);
//                    log("Bedrock block change detected at: " + currentTime + " within radius at position " + blockPos);
                }
            }
        }

        return false;
    }


    @Override
    public boolean react() {
        // Disable macro (iam a lazy mf and haven`t done more here)
        MacroManager.getInstance().disable();
        Logger.sendWarning("Too many Bedrock block changes nearby! Disabling macro.");
        return true;
    }

    @Override
    public void resetStates() {
        this.bedrockChangeTimestamps.clear(); // Clear the recorded timestamps
        this.timer.reset();
    }
}
