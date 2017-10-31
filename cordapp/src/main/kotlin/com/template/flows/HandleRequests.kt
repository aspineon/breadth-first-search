package com.template.flows

import co.paralleluniverse.fibers.Suspendable
import com.template.myCounterparties
import com.template.services.PersistentState
import com.template.structures.BilateralAgreement
import com.template.structures.SearchRequest
import com.template.structures.SearchResult
import net.corda.core.flows.FlowLogic
import net.corda.core.flows.FlowSession
import net.corda.core.flows.InitiatedBy
import net.corda.core.utilities.unwrap

@InitiatedBy(SendRequests::class)
class HandleRequests<T : BilateralAgreement>(private val otherSession: FlowSession) : FlowLogic<Unit>() {

    private fun gatherMyAgreements(agreementType: Class<T>): Set<BilateralAgreement> {
        val queryResult = serviceHub.vaultService.queryBy(agreementType).states
        return queryResult.map { it.state.data }.toSet()
    }

    @Suspendable
    override fun call() {
        // Get a reference to the persistent state that tracks which requests have been seen.
        val seenRequests = serviceHub.cordaService(PersistentState::class.java).seenRequests

        // Receive the request, add it to the seen list and check whether we've seen it before or not.
        val (request, hasBeenSeen) = otherSession.receive<SearchRequest<T>>().unwrap {
            it to (seenRequests.add(it.id)).not() // 'add()' returns true if the item is not a dupe.
        }

        // We don't want to propagate the Search request to the node which just sent it to us.
        val counterparties = myCounterparties(serviceHub, request.type)
        val counterPartiesMinusRequester = counterparties - otherSession.counterparty

        val response = when {
        // There's no-one else to propagate the request to, so send back our edges.
            counterPartiesMinusRequester.isEmpty() -> {
                SearchResult(gatherMyAgreements(request.type))
            }
        // We've seen this request before, so will send back our edges in response to another Request.
            hasBeenSeen -> {
                SearchResult(emptySet())
            }
        // Propagate the Request, wait for all responses, add our edges, then send back to the Requester.
            else -> {
                val response = subFlow(SendRequests(counterPartiesMinusRequester, request.propagate()))
                SearchResult(response.agreements + gatherMyAgreements(request.type))
            }
        }

        otherSession.send(response)
    }

}