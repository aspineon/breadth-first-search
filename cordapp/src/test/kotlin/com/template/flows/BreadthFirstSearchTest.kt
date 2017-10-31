package com.template.flows

import com.template.MockNetworkTest
import com.template.structures.AgreedText
import net.corda.core.utilities.getOrThrow
import net.corda.node.internal.StartedNode
import net.corda.testing.node.MockNetwork
import org.junit.Before
import org.junit.Test

class BreadthFirstSearchTest : MockNetworkTest(numberOfNodes = 6) {

    lateinit private var A: StartedNode<MockNetwork.MockNode>
    lateinit private var B: StartedNode<MockNetwork.MockNode>
    lateinit private var C: StartedNode<MockNetwork.MockNode>
    lateinit private var D: StartedNode<MockNetwork.MockNode>
    lateinit private var E: StartedNode<MockNetwork.MockNode>
    lateinit private var F: StartedNode<MockNetwork.MockNode>

    @Before
    override fun initialiseNodes() {
        A = nodes[0]
        B = nodes[1]
        C = nodes[2]
        D = nodes[3]
        E = nodes[4]
        F = nodes[5]
    }

    @Test
    fun `search a network of six nodes with seven agreements`() {
        agreedText(A, B, "Test1")
        agreedText(B, C, "Test2")
        agreedText(A, C, "Test3")
        agreedText(B, D, "Test4")
        agreedText(B, E, "Test5")
        agreedText(C, F, "Test6")
        agreedText(E, E, "Test7")

        net.waitQuiescent()

        val searchResult = A.start(BreadthFirstSearch(AgreedText::class.java))

        A.smm.changes.subscribe { println("PartyA $it") }
        B.smm.changes.subscribe { println("PartyB $it") }
        C.smm.changes.subscribe { println("PartyC $it") }
        D.smm.changes.subscribe { println("PartyD $it") }
        E.smm.changes.subscribe { println("PartyE $it") }
        F.smm.changes.subscribe { println("PartyF $it") }

        net.waitQuiescent()

        println(searchResult.getOrThrow())
    }

}