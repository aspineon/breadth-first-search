package com.template

import com.template.structures.BilateralAgreement
import net.corda.core.identity.Party
import net.corda.core.node.ServiceHub

fun <T : BilateralAgreement> myCounterparties(services: ServiceHub, type: Class<T>): Set<Party> {
    val me = services.myInfo.legalIdentities.first()
    // Query for all states of sub-type 'BilateralAgreement'.
    val queryResults = services.vaultService.queryBy(type).states

    // Return a list of all counterparties for the queried BilateralAgreements.
    val counterparties: List<Party> = queryResults.map { (state) ->
        val participants = state.data.participants
        if (participants[0] == me) participants[1] else participants[0]
    }

    // De-dupe.
    return counterparties.toSet()
}