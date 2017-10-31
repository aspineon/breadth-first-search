package com.template.flows

import co.paralleluniverse.fibers.Suspendable
import com.template.contracts.AgreementContract
import com.template.structures.BilateralAgreement
import net.corda.core.contracts.Command
import net.corda.core.contracts.StateAndContract
import net.corda.core.flows.*
import net.corda.core.transactions.SignedTransaction
import net.corda.core.transactions.TransactionBuilder

object IssueAgreement {

    @InitiatingFlow
    @StartableByRPC
    class Initiator(private val agreement: BilateralAgreement) : FlowLogic<SignedTransaction>() {

        @Suspendable
        override fun call(): SignedTransaction {
            // Notary reference.
            val notary = serviceHub.networkMapCache.notaryIdentities.firstOrNull()
                    ?: throw FlowException("No notary available.")

            // Build state and contract.
            val stateAndContract = StateAndContract(state = agreement, contract = AgreementContract.ID)

            // Build command.
            val participantKeys = agreement.participants.map { it.owningKey }
            val command = Command(value = AgreementContract.Agree(), signers = participantKeys)

            // Build transaction.
            val utx = TransactionBuilder(notary = notary).withItems(stateAndContract, command)

            // Sign transaction.
            val ptx = serviceHub.signInitialTransaction(builder = utx)

            // Get counterparty signature.
            val counterparty = if (agreement.participants[0] == ourIdentity) agreement.participants[1] else agreement.participants[0]
            val lenderFlow = initiateFlow(counterparty)
            val stx = subFlow(CollectSignaturesFlow(partiallySignedTx = ptx, sessionsToCollectFrom = setOf(lenderFlow)))

            // Finalise and broadcast transaction.
            return subFlow(FinalityFlow(transaction = stx))
        }
    }

    @InitiatedBy(Initiator::class)
    class Responder(val otherFlow: FlowSession) : FlowLogic<SignedTransaction>() {

        @Suspendable
        override fun call(): SignedTransaction {
            val flow = object : SignTransactionFlow(otherFlow) {

                @Suspendable
                override fun checkTransaction(stx: SignedTransaction) = Unit

            }

            val stx = subFlow(flow)
            return waitForLedgerCommit(stx.id)
        }

    }
}
