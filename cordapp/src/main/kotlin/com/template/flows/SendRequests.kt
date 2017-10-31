package com.template.flows

import co.paralleluniverse.fibers.Suspendable
import com.template.structures.BilateralAgreement
import com.template.structures.SearchRequest
import com.template.structures.SearchResult
import net.corda.core.flows.FlowLogic
import net.corda.core.flows.FlowSession
import net.corda.core.flows.InitiatingFlow
import net.corda.core.identity.Party
import net.corda.core.utilities.unwrap

@InitiatingFlow
class SendRequests<T : BilateralAgreement>(
        private val recipients: Set<Party>,
        private val request: SearchRequest<T>
) : FlowLogic<SearchResult>() {

    @Suspendable
    override fun call(): SearchResult {
        // Create a flow session for each recipient and send them a SearchRequest.
        val sessions: List<FlowSession> = recipients.map { party -> initiateFlow(party) }
        sessions.forEach { session -> session.send(request) }

        // Suspend this flow and wait for all the SearchResults to be returned.
        val responses: List<SearchResult> = receiveAll(SearchResult::class.java, sessions).map { response ->
            response.unwrap { it }
        }

        // Aggregate all the results together and form them into a new SearchResult to return.
        val aggregatedSearchResults: Set<BilateralAgreement> = responses.flatMap { (agreements) -> agreements }.toSet()
        return SearchResult(aggregatedSearchResults)
    }

}