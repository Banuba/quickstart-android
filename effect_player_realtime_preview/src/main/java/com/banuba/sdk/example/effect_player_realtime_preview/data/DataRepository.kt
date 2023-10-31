package com.banuba.sdk.example.effect_player_realtime_preview.data

object DataRepository {
    enum class Action {
        NOTHING,
        APPLY_MASK,
        HIDE_MASK,
        START_RECORDING,
        STOP_RECORDING,
        PLAY_LAST_VIDEO,
        START_PROCESSING,
    }

    data class Option(
        val name: String,
        val action: Action,
        val nextOptionIndex: Int,
        val canSwitchPreview: Boolean
    )

    data class Preview(
        val name: String,
        val options: List<Option>
    )

    val listOfPreview = listOf(
        Preview("Camera preview",
            listOf(
                Option("", Action.NOTHING, 0, true)
            ),
        ),
        Preview("Mask",
            listOf(
                Option("Show mask", Action.APPLY_MASK, 1, true),
                Option("Hide mask", Action.HIDE_MASK, 0, true),
            ),
        ),
        Preview("Video recording",
            listOf(
                Option("Start recording", Action.START_RECORDING, 1, true),
                Option("Stop recording", Action.STOP_RECORDING, 2, false),
                Option("Play video", Action.PLAY_LAST_VIDEO, 0, true),
            ),
        ),
        Preview("Video processing",
            listOf(
                Option("Start processing", Action.START_PROCESSING, 1, true),
                Option("Wait for finish...", Action.NOTHING, 1, false),
                Option("Play video", Action.PLAY_LAST_VIDEO, 0, true),
            ),
        ),
    )
}
