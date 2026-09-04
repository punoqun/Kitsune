package io.github.drumber.kitsune.data.source.network.group

import io.github.drumber.kitsune.data.common.Filter
import io.github.drumber.kitsune.data.source.network.BasePagingDataSource
import io.github.drumber.kitsune.data.source.network.PageData
import io.github.drumber.kitsune.data.source.network.group.model.NetworkGroupMember

class FollowedGroupsPagingDataSource(
    private val dataSource: GroupsNetworkDataSource,
    private val filter: Filter,
    private val query: String?
) : BasePagingDataSource<NetworkGroupMember>() {

    override suspend fun requestPage(pageOffset: Int): PageData<NetworkGroupMember> {
        var nextOffset: Int? = pageOffset
        var requestedPrev: Int? = null
        var isFirstRequest = true
        while (nextOffset != null) {
            val page = dataSource.getGroupMembers(filter.pageOffset(nextOffset))
            if (isFirstRequest) {
                requestedPrev = page.prev
                isFirstRequest = false
            }
            val matching = page.data.orEmpty().filter { member ->
                query.isNullOrBlank() ||
                    member.group?.name?.contains(query, ignoreCase = true) == true
            }
            if (matching.isNotEmpty() || page.next == null) {
                return page.copy(data = matching, prev = requestedPrev)
            }
            nextOffset = page.next
        }
        return PageData(emptyList(), null, null, null, null)
    }
}
