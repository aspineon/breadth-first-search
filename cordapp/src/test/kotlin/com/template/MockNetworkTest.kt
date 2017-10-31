package com.template

import com.template.flows.IssueAgreement
import com.template.services.PersistentState
import com.template.structures.AgreedNumber
import com.template.structures.AgreedText
import net.corda.core.flows.FlowLogic
import net.corda.core.identity.CordaX500Name
import net.corda.core.transactions.SignedTransaction
import net.corda.core.utilities.getOrThrow
import net.corda.node.internal.StartedNode
import net.corda.testing.node.MockNetwork
import net.corda.testing.setCordappPackages
import net.corda.testing.unsetCordappPackages
import org.junit.After
import org.junit.Before

abstract class MockNetworkTest(val numberOfNodes: Int) {

    lateinit protected var net: MockNetwork
    lateinit protected var nodes: List<StartedNode<MockNetwork.MockNode>>

    @Before
    abstract fun initialiseNodes()

    @Before
    fun setupNetwork() {
        setCordappPackages(
                "com.template",
                "net.corda.finance"
        )
        net = MockNetwork(threadPerNode = true)
        nodes = createSomeNodes(numberOfNodes)
        nodes.forEach { node -> registerFlowsAndServices(node) }
    }

    @After
    fun tearDownNetwork() {
        net.stopNodes()
        unsetCordappPackages()
    }

    private fun registerFlowsAndServices(node: StartedNode<MockNetwork.MockNode>) {
        val mockNode = node.internals
        mockNode.registerInitiatedFlow(IssueAgreement.Responder::class.java)
        node.database.transaction {
            mockNode.installCordaService(PersistentState::class.java)
        }
    }

    protected fun createSomeNodes(numberOfNodes: Int = 2): List<StartedNode<MockNetwork.MockNode>> {
        net.createNotaryNode(legalName = CordaX500Name("Notary", "London", "GB"))
        return (1..numberOfNodes).map { current ->
            val char = current.toChar() + 64
            val name = CordaX500Name("Party$char", "London", "GB")
            net.createPartyNode(name)
        }
    }

    protected fun <T : Any> StartedNode<MockNetwork.MockNode>.start(logic: FlowLogic<T>) = this.services.startFlow(logic).resultFuture
    private fun StartedNode<MockNetwork.MockNode>.legalIdentity() = this.services.myInfo.legalIdentities.first()

    protected fun agreedText(a: StartedNode<MockNetwork.MockNode>,
                             b: StartedNode<MockNetwork.MockNode>,
                             text: String
    ): SignedTransaction {
        val agreement = AgreedText(a.legalIdentity(), b.legalIdentity(), text)
        val flow = IssueAgreement.Initiator(agreement)
        return a.services.startFlow(flow).resultFuture.getOrThrow()
    }

    protected fun agreedNumber(a: StartedNode<MockNetwork.MockNode>,
                               b: StartedNode<MockNetwork.MockNode>,
                               number: Int
    ): SignedTransaction {
        val agreement = AgreedNumber(a.legalIdentity(), b.legalIdentity(), number)
        val flow = IssueAgreement.Initiator(agreement)
        return a.services.startFlow(flow).resultFuture.getOrThrow()
    }

}
