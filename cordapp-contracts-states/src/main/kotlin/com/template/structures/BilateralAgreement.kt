package com.template.structures

import net.corda.core.contracts.ContractState
import net.corda.core.identity.Party

/** Abstract Bilateral Agreement class that only supports two participants. */
abstract class BilateralAgreement(override val participants: List<Party>) : ContractState {
    constructor(vararg partiesToAgreement: Party) : this(partiesToAgreement.toList()) {
        require(partiesToAgreement.size == 2) { "Bilateral Agreements must have only two parties." }
    }
}