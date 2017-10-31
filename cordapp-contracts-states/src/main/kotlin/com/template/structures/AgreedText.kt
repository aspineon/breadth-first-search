package com.template.structures

import net.corda.core.identity.Party

/** Allows two parties to agree on a number. */
data class AgreedText(private val a: Party, private val b: Party, val text: String) : BilateralAgreement(a, b)