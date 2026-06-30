package com.aledparry.player

/**
 * Hands a freshly parsed playlist from [NetworkStreamActivity] to
 * [ChannelListActivity] without serialising a large list through an Intent
 * (which can trip TransactionTooLargeException).
 */
object PlaylistHolder {
    @Volatile
    var channels: List<Channel> = emptyList()

    @Volatile
    var sourceTitle: String = "Channels"
}
