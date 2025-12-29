package com.chicken.jelly.config

import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp

/**
 * Configuration object for game perspective and lane positioning. Adjust these values to match your
 * road background and desired perspective effect.
 */
object GameConfig {

    // === Lane Configuration ===

    /** Number of lanes in the game */
    const val LANE_COUNT = 3

    /** Horizontal offset for each lane when items are NEAR (at bottom, fully visible) */
    val laneOffsetsNear =
            listOf(
                    (-100).dp, // Left lane
                    0.dp, // Center lane
                    100.dp // Right lane
            )

    /** Horizontal offset for each lane when items are FAR (at top, just spawned) */
    val laneOffsetsFar =
            listOf(
                    (10).dp, // Left lane (closer to center)
                    0.dp, // Center lane
                    -10.dp // Right lane (closer to center)
            )

    // === Item Scaling Configuration ===

    /** Item size when spawned (far away, at top) */
    val itemSizeFar: Dp = 24.dp

    /** Item size when near player (at bottom) */
    val itemSizeNear: Dp = 64.dp

    // === Vertical Position Configuration ===

    /** Vertical offset where items spawn (top of screen, in dp from top) */
    val spawnVerticalOffset: Dp = 130.dp // Even lower, just above car level for better perspective

    /** Vertical offset where collision occurs (near bottom, in dp from top) */
    val collisionVerticalOffset: Dp = 500.dp

    /** Speed threshold for collision detection (0.0 to 1.0) */
    const val COLLISION_THRESHOLD = 1.0f

    // === Level Configuration ===

    /** Duration of each level in seconds */
    const val LEVEL_DURATION_SECONDS = 30

    /** Stop spawning items this many seconds before level ends */
    const val STOP_SPAWN_BEFORE_END_SECONDS = 3

    // === Player Car Configuration ===

    /** Horizontal offset for player car in each lane */
    val playerCarLaneOffsets =
            listOf(
                    (-100).dp, // Left lane
                    0.dp, // Center lane
                    100.dp // Right lane
            )

    /** Height of the player car */
    val playerCarHeight: Dp = 150.dp

    /** Bottom padding for player car */
    val playerCarBottomPadding: Dp = 0.dp

    // === Lane Indicator Configuration ===

    /** Bottom padding for lane indicator line */
    val laneIndicatorBottomPadding: Dp = 140.dp

    /** Height of lane indicator line */
    val laneIndicatorHeight: Dp = 6.dp

    // === Helper Functions ===

    /** Interpolate between far and near values based on progress (0.0 to 1.0) */
    fun lerp(start: Float, end: Float, progress: Float): Float {
        return start + (end - start) * progress
    }

    /** Get horizontal offset for a lane at a given progress */
    fun getLaneOffset(lane: Int, progress: Float): Dp {
        if (lane !in 0 until LANE_COUNT) return 0.dp

        val farOffset = laneOffsetsFar[lane].value
        val nearOffset = laneOffsetsNear[lane].value

        return lerp(farOffset, nearOffset, progress).dp
    }

    /** Get item size at a given progress */
    fun getItemSize(progress: Float): Dp {
        return lerp(itemSizeFar.value, itemSizeNear.value, progress).dp
    }

    /** Get vertical offset for an item at a given progress */
    fun getVerticalOffset(progress: Float): Dp {
        return lerp(spawnVerticalOffset.value, collisionVerticalOffset.value, progress).dp
    }
}
