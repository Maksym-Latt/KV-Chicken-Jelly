package com.chicken.jelly.model

data class GameItem(
        val id: Int,
        val lane: Int,
        val isReward: Boolean,
        val speed: Float,
        val isHit: Boolean = false,
        val isCollisionChecked: Boolean = false,
)
