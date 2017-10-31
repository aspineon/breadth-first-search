package com.template.contracts

import net.corda.core.contracts.Contract
import net.corda.core.contracts.TypeOnlyCommandData
import net.corda.core.transactions.LedgerTransaction

/** Dummy contract for the purposes of this tutorial. */
class AgreementContract : Contract {
    companion object {
        @JvmStatic
        val ID = "com.template.contracts.AgreementContract"
    }

    override fun verify(tx: LedgerTransaction) = Unit
    class Agree : TypeOnlyCommandData()
}