/*
 * Copyright (c) 2013, 2014, Oracle and/or its affiliates. All rights reserved.
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS FILE HEADER.
 *
 * This code is free software; you can redistribute it and/or modify it
 * under the terms of the GNU General Public License version 2 only, as
 * published by the Free Software Foundation.
 *
 * This code is distributed in the hope that it will be useful, but WITHOUT
 * ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or
 * FITNESS FOR A PARTICULAR PURPOSE.  See the GNU General Public License
 * version 2 for more details (a copy is included in the LICENSE file that
 * accompanied this code).
 *
 * You should have received a copy of the GNU General Public License version
 * 2 along with this work; if not, write to the Free Software Foundation,
 * Inc., 51 Franklin St, Fifth Floor, Boston, MA 02110-1301 USA.
 *
 * Please contact Oracle, 500 Oracle Parkway, Redwood Shores, CA 94065 USA
 * or visit www.oracle.com if you need additional information or have any
 * questions.
 */
package com.oracle.graal.nodes.test;

import org.junit.Assert;
import org.junit.BeforeClass;
import org.junit.Test;

import com.oracle.graal.compiler.test.GraalCompilerTest;
import com.oracle.graal.graph.iterators.NodePredicate;
import com.oracle.graal.nodes.LoopBeginNode;
import com.oracle.graal.nodes.PhiNode;
import com.oracle.graal.nodes.StructuredGraph;
import com.oracle.graal.nodes.StructuredGraph.AllowAssumptions;
import com.oracle.graal.phases.common.CanonicalizerPhase;
import com.oracle.graal.phases.tiers.PhaseContext;

public class LoopPhiCanonicalizerTest extends GraalCompilerTest {

    private static int[] array = new int[1000];

    @BeforeClass
    public static void before() {
        for (int i = 0; i < array.length; i++) {
            array[i] = i;
        }
    }

    public static long loopSnippet() {
        int a = 0;
        int b = 0;
        int c = 0;
        int d = 0;

        long sum = 0;
        while (d < 1000) {
            sum += array[a++] + array[b++] + array[c++] + array[d++];
        }
        return sum;
    }

    @Test
    public void test() {
        StructuredGraph graph = parseEager("loopSnippet", AllowAssumptions.YES);
        NodePredicate loopPhis = node -> node instanceof PhiNode && ((PhiNode) node).merge() instanceof LoopBeginNode;

        PhaseContext context = new PhaseContext(getProviders());
        Assert.assertEquals(5, graph.getNodes().filter(loopPhis).count());
        new CanonicalizerPhase().apply(graph, context);
        Assert.assertEquals(2, graph.getNodes().filter(loopPhis).count());

        test("loopSnippet");
    }
}