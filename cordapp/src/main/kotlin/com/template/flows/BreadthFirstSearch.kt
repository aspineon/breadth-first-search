package com.template.flows

import co.paralleluniverse.fibers.Suspendable
import com.template.myCounterparties
import com.template.structures.BilateralAgreement
import com.template.structures.SearchRequest
import com.template.structures.SearchResult
import net.corda.core.crypto.SecureHash
import net.corda.core.flows.FlowException
import net.corda.core.flows.FlowLogic
import net.corda.core.flows.StartableByRPC
import net.corda.core.identity.Party

@StartableByRPC
class BreadthFirstSearch<T : BilateralAgreement>(private val agreementType: Class<T>) : FlowLogic<SearchResult>() {

    @Suspendable
    override fun call(): SearchResult {
        // Get a set of my counterparties.
        val counterparties: Set<Party> = myCounterparties(serviceHub, agreementType)
        check(counterparties.isNotEmpty()) { throw FlowException("No agreements. Aborting search.") }

        // Create a new Request.
        val randomHash = SecureHash.randomSHA256()
        val newRequest = SearchRequest(id = randomHash, type = agreementType)

        // Send the request to my counterparties.
        val result = subFlow(SendRequests(counterparties, newRequest))
        return result
    }

}