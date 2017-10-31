package com.template.structures

import net.corda.core.identity.Party

/** Allows two parties to agree on a number. */
data class AgreedNumber(private val a: Party, private val b: Party, val number: Int) : BilateralAgreement(a, b)